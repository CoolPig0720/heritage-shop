package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heritage.common.BusinessException;
import com.heritage.dto.PageQuery;
import com.heritage.dto.ProductCreateRequest;
import com.heritage.dto.ProductImageAddRequest;
import com.heritage.dto.ProductImageBatchSortRequest;
import com.heritage.dto.ProductImageUpdateRequest;
import com.heritage.dto.ProductImageVO;
import com.heritage.dto.ProductUpdateRequest;
import com.heritage.dto.ProductVO;
import com.heritage.entity.Product;
import com.heritage.entity.ProductImage;
import com.heritage.entity.User;
import com.heritage.mapper.ProductImageMapper;
import com.heritage.mapper.ProductMapper;
import com.heritage.mapper.UserMapper;
import com.heritage.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductImageMapper productImageMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Long createProduct(Long operatorUserId, boolean isAdmin, ProductCreateRequest request) {
        if (!isAdmin && operatorUserId == null) {
            throw new BusinessException("无效用户");
        }

        Product product = new Product();
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice() == null ? new BigDecimal("999") : request.getPrice());
        product.setMerchantId(operatorUserId);
        product.setStatus(1);

        this.save(product);
        return product.getId();
    }

    @Override
    @Transactional
    public void updateProduct(Long operatorUserId, boolean isAdmin, Long productId, ProductUpdateRequest request) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        assertManagePermission(operatorUserId, isAdmin, product);

        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) {
                throw new BusinessException("商品名称不能为空");
            }
            if (name.length() > 100) {
                throw new BusinessException("商品名称长度不能超过100");
            }
            product.setName(name);
        }
        if (request.getDescription() != null) {
            String description = request.getDescription().trim();
            if (description.isEmpty()) {
                throw new BusinessException("商品描述不能为空");
            }
            product.setDescription(description);
        }
        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("价格不能为负数");
            }
            product.setPrice(request.getPrice());
        }
        if (request.getTraceCode() != null) {
            String traceCode = request.getTraceCode().trim();
            product.setTraceCode(traceCode.isEmpty() ? null : traceCode);
        }
        if (request.getTraceQrUrl() != null) {
            String traceQrUrl = request.getTraceQrUrl().trim();
            product.setTraceQrUrl(traceQrUrl.isEmpty() ? null : traceQrUrl);
        }
        if (request.getModel3dUrl() != null) {
            String model3dUrl = request.getModel3dUrl().trim();
            product.setModel3dUrl(model3dUrl.isEmpty() ? null : model3dUrl);
        }
        if (request.getStatus() != null) {
            validateStatus(request.getStatus());
            product.setStatus(request.getStatus());
        }

        this.updateById(product);
    }

    @Override
    @Transactional
    public void updateProductStatus(Long operatorUserId, boolean isAdmin, Long productId, Integer status) {
        validateStatus(status);
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        assertManagePermission(operatorUserId, isAdmin, product);
        product.setStatus(status);
        this.updateById(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long operatorUserId, boolean isAdmin, Long productId) {
        Product product = this.getById(productId);
        if (product == null) {
            return;
        }
        assertManagePermission(operatorUserId, isAdmin, product);

        LambdaQueryWrapper<ProductImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ProductImage::getProductId, productId);
        productImageMapper.delete(imageWrapper);

        this.removeById(productId);
    }

    @Override
    public Page<ProductVO> pageManageProducts(Long operatorUserId, boolean isAdmin, PageQuery query, Integer status) {
        Page<Product> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (!isAdmin) {
            wrapper.eq(Product::getMerchantId, operatorUserId);
        }
        if (status != null) {
            validateStatus(status);
            wrapper.eq(Product::getStatus, status);
        }
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            wrapper.like(Product::getName, query.getKeyword().trim());
        } else if (query.getName() != null && !query.getName().trim().isEmpty()) {
            wrapper.like(Product::getName, query.getName().trim());
        }

        wrapper.orderByDesc(Product::getCreateTime);
        Page<Product> productPage = this.page(page, wrapper);

        List<Product> records = productPage.getRecords();
        List<ProductVO> voRecords = toProductVOList(records);
        fillImages(voRecords);
        fillMerchantNames(voRecords);

        Page<ProductVO> voPage = new Page<>(productPage.getCurrent(), productPage.getSize(), productPage.getTotal());
        voPage.setRecords(voRecords);
        return voPage;
    }

    @Override
    public ProductVO getProductDetail(Long productId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        ProductVO vo = toProductVO(product);
        fillImages(Collections.singletonList(vo));
        fillMerchantNames(Collections.singletonList(vo));
        return vo;
    }

    @Override
    public List<ProductVO> getRandomOnSaleProducts(Integer count) {
        int limit = normalizeCount(count);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        wrapper.last("ORDER BY RAND() LIMIT " + limit);
        List<Product> products = this.list(wrapper);

        List<ProductVO> vos = toProductVOList(products);
        fillImages(vos);
        fillMerchantNames(vos);
        return vos;
    }

    @Override
    public List<ProductVO> getHotProducts(Integer count) {
        int limit = normalizeCount(count);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        wrapper.last("ORDER BY avg_rating DESC, rating_count DESC, create_time DESC LIMIT " + limit);
        List<Product> products = this.list(wrapper);

        List<ProductVO> vos = toProductVOList(products);
        fillImages(vos);
        fillMerchantNames(vos);
        return vos;
    }

    @Override
    public List<ProductImageVO> listManageProductImages(Long operatorUserId, boolean isAdmin, Long productId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        assertManagePermission(operatorUserId, isAdmin, product);
        return toProductImageVOList(selectProductImages(productId));
    }

    @Override
    @Transactional
    public List<ProductImageVO> addManageProductImages(Long operatorUserId, boolean isAdmin, Long productId,
            ProductImageAddRequest request) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        assertManagePermission(operatorUserId, isAdmin, product);
        if (request == null || request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
            throw new BusinessException("图片列表不能为空");
        }

        List<String> imageUrls = request.getImageUrls().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
        if (imageUrls.isEmpty()) {
            throw new BusinessException("图片列表不能为空");
        }

        Integer maxSortOrder = getMaxSortOrder(productId);
        int nextSortOrder = maxSortOrder == null ? 0 : maxSortOrder + 1;

        boolean setFirstAsCover = request.getSetFirstAsCover() == null || request.getSetFirstAsCover();
        boolean hasCover = hasCoverImage(productId);

        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setImageUrl(imageUrls.get(i));
            image.setSortOrder(nextSortOrder++);
            if (setFirstAsCover && !hasCover && i == 0) {
                image.setIsCover(1);
                hasCover = true;
            } else {
                image.setIsCover(0);
            }
            productImageMapper.insert(image);
        }

        ensureCoverImage(productId);
        return toProductImageVOList(selectProductImages(productId));
    }

    @Override
    @Transactional
    public void updateManageProductImage(Long operatorUserId, boolean isAdmin, Long imageId,
            ProductImageUpdateRequest request) {
        ProductImage existing = productImageMapper.selectById(imageId);
        if (existing == null) {
            throw new BusinessException("图片不存在");
        }
        Product product = this.getById(existing.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        assertManagePermission(operatorUserId, isAdmin, product);

        if (request == null) {
            throw new BusinessException("参数不能为空");
        }

        Integer isCover = request.getIsCover();
        Integer sortOrder = request.getSortOrder();

        // 设为封面时：将原封面的sortOrder与当前图片互换
        if (isCover != null && isCover == 1) {
            // 查找当前封面
            LambdaQueryWrapper<ProductImage> coverWrapper = new LambdaQueryWrapper<>();
            coverWrapper.eq(ProductImage::getProductId, existing.getProductId())
                    .eq(ProductImage::getIsCover, 1)
                    .last("LIMIT 1");
            List<ProductImage> currentCovers = productImageMapper.selectList(coverWrapper);
            if (!currentCovers.isEmpty()) {
                ProductImage currentCover = currentCovers.get(0);
                if (!currentCover.getId().equals(imageId)) {
                    // 将原封面的sortOrder设为当前图片的sortOrder
                    ProductImage swapCover = new ProductImage();
                    swapCover.setId(currentCover.getId());
                    swapCover.setSortOrder(existing.getSortOrder());
                    swapCover.setIsCover(0);
                    productImageMapper.updateById(swapCover);
                }
            }
            // 当前图片设为封面，sortOrder=0
            ProductImage update = new ProductImage();
            update.setId(imageId);
            update.setSortOrder(0);
            update.setIsCover(1);
            if (request.getImageUrl() != null) {
                String url = request.getImageUrl().trim();
                if (url.isEmpty()) {
                    throw new BusinessException("图片地址不能为空");
                }
                update.setImageUrl(url);
            }
            productImageMapper.updateById(update);
            // 重新整理sortOrder使其连续
            reorderSortOrders(existing.getProductId());
            return;
        }

        // 普通更新（sortOrder变更等）
        ProductImage update = new ProductImage();
        update.setId(imageId);
        if (sortOrder != null) {
            update.setSortOrder(sortOrder);
            // sortOrder=0 自动设为封面
            if (sortOrder == 0) {
                // 取消原封面
                ProductImage reset = new ProductImage();
                reset.setIsCover(0);
                LambdaQueryWrapper<ProductImage> resetWrapper = new LambdaQueryWrapper<>();
                resetWrapper.eq(ProductImage::getProductId, existing.getProductId())
                        .eq(ProductImage::getIsCover, 1);
                productImageMapper.update(reset, resetWrapper);
                update.setIsCover(1);
            } else if (existing.getIsCover() != null && existing.getIsCover() == 1 && sortOrder > 0) {
                // 封面图被移到非0位置，取消封面
                update.setIsCover(0);
            }
        }
        if (request.getImageUrl() != null) {
            String url = request.getImageUrl().trim();
            if (url.isEmpty()) {
                throw new BusinessException("图片地址不能为空");
            }
            update.setImageUrl(url);
        }
        if (isCover != null) {
            update.setIsCover(isCover);
        }
        productImageMapper.updateById(update);

        ensureCoverImage(existing.getProductId());
    }

    @Override
    @Transactional
    public void batchUpdateImageSort(Long operatorUserId, boolean isAdmin, ProductImageBatchSortRequest request) {
        Long productId = request.getProductId();
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        assertManagePermission(operatorUserId, isAdmin, product);

        // 验证所有imageId都属于该商品
        List<Long> requestIds = request.getItems().stream()
                .map(ProductImageBatchSortRequest.SortItem::getImageId)
                .toList();
        List<ProductImage> existingImages = productImageMapper.selectBatchIds(requestIds);
        for (ProductImage img : existingImages) {
            if (!img.getProductId().equals(productId)) {
                throw new BusinessException("图片不属于该商品");
            }
        }

        // 批量更新sortOrder和isCover
        for (ProductImageBatchSortRequest.SortItem item : request.getItems()) {
            ProductImage update = new ProductImage();
            update.setId(item.getImageId());
            update.setSortOrder(item.getSortOrder());
            // sortOrder=0 的图自动设为封面
            update.setIsCover(item.getSortOrder() == 0 ? 1 : 0);
            productImageMapper.updateById(update);
        }

        // 重新整理sortOrder使其连续
        reorderSortOrders(productId);
        ensureCoverImage(productId);
    }

    @Override
    @Transactional
    public void deleteManageProductImage(Long operatorUserId, boolean isAdmin, Long imageId) {
        ProductImage existing = productImageMapper.selectById(imageId);
        if (existing == null) {
            return;
        }
        Product product = this.getById(existing.getProductId());
        if (product == null) {
            return;
        }
        assertManagePermission(operatorUserId, isAdmin, product);

        productImageMapper.deleteById(imageId);
        ensureCoverImage(existing.getProductId());
    }

    private void assertManagePermission(Long operatorUserId, boolean isAdmin, Product product) {
        if (isAdmin) {
            return;
        }
        if (operatorUserId == null || !Objects.equals(product.getMerchantId(), operatorUserId)) {
            throw new BusinessException("无权限操作该商品");
        }
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("商品状态不合法");
        }
    }

    private int normalizeCount(Integer count) {
        if (count == null) {
            return 20;
        }
        if (count < 1) {
            return 1;
        }
        return Math.min(count, 50);
    }

    private ProductVO toProductVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        return vo;
    }

    private List<ProductVO> toProductVOList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return new ArrayList<>();
        }
        return products.stream().map(this::toProductVO).collect(Collectors.toList());
    }

    private void fillImages(List<ProductVO> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        List<Long> productIds = products.stream()
                .map(ProductVO::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (productIds.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ProductImage::getProductId, productIds);
        wrapper.orderByAsc(ProductImage::getSortOrder)
                .orderByAsc(ProductImage::getId);
        List<ProductImage> images = productImageMapper.selectList(wrapper);

        Map<Long, List<ProductImage>> imageMap = images.stream()
                .collect(Collectors.groupingBy(ProductImage::getProductId, LinkedHashMap::new, Collectors.toList()));

        for (ProductVO product : products) {
            List<ProductImage> list = imageMap.getOrDefault(product.getId(), Collections.emptyList());
            if (list.isEmpty()) {
                product.setCoverImageUrl(null);
                product.setImageUrls(Collections.emptyList());
                continue;
            }

            String coverUrl = list.stream()
                    .filter(img -> img.getIsCover() != null && img.getIsCover() == 1)
                    .map(ProductImage::getImageUrl)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseGet(() -> list.get(0).getImageUrl());

            List<String> urls = list.stream()
                    .map(ProductImage::getImageUrl)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            product.setCoverImageUrl(coverUrl);
            product.setImageUrls(urls);
        }
    }

    private void fillMerchantNames(List<ProductVO> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        Set<Long> merchantIds = products.stream()
                .map(ProductVO::getMerchantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (merchantIds.isEmpty()) {
            return;
        }

        List<User> users = userMapper.selectBatchIds(merchantIds);
        if (users == null || users.isEmpty()) {
            try {
                users = userMapper.selectBatchIdsFromUserTable(merchantIds);
            } catch (Exception ignored) {
            }
        }
        if (users == null || users.isEmpty()) {
            for (ProductVO product : products) {
                product.setMerchantName(null);
            }
            return;
        }

        Map<Long, String> nameMap = users.stream()
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(User::getId, User::getName, (a, b) -> a));

        for (ProductVO product : products) {
            Long merchantId = product.getMerchantId();
            if (merchantId == null) {
                product.setMerchantName(null);
                continue;
            }
            product.setMerchantName(nameMap.get(merchantId));
        }
    }

    private List<ProductImage> selectProductImages(Long productId) {
        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductImage::getProductId, productId);
        wrapper.orderByAsc(ProductImage::getSortOrder)
                .orderByAsc(ProductImage::getId);
        return productImageMapper.selectList(wrapper);
    }

    private List<ProductImageVO> toProductImageVOList(List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        return images.stream().map(img -> {
            ProductImageVO vo = new ProductImageVO();
            BeanUtils.copyProperties(img, vo);
            return vo;
        }).toList();
    }

    private Integer getMaxSortOrder(Long productId) {
        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductImage::getProductId, productId);
        wrapper.orderByDesc(ProductImage::getSortOrder)
                .orderByDesc(ProductImage::getId)
                .last("LIMIT 1");
        List<ProductImage> list = productImageMapper.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0).getSortOrder();
    }

    private boolean hasCoverImage(Long productId) {
        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductImage::getProductId, productId)
                .eq(ProductImage::getIsCover, 1)
                .last("LIMIT 1");
        List<ProductImage> list = productImageMapper.selectList(wrapper);
        return list != null && !list.isEmpty();
    }

    private void ensureCoverImage(Long productId) {
        if (productId == null) {
            return;
        }
        if (hasCoverImage(productId)) {
            return;
        }
        LambdaQueryWrapper<ProductImage> firstWrapper = new LambdaQueryWrapper<>();
        firstWrapper.eq(ProductImage::getProductId, productId);
        firstWrapper.orderByAsc(ProductImage::getSortOrder)
                .orderByAsc(ProductImage::getId)
                .last("LIMIT 1");
        List<ProductImage> list = productImageMapper.selectList(firstWrapper);
        if (list == null || list.isEmpty()) {
            return;
        }
        ProductImage first = new ProductImage();
        first.setId(list.get(0).getId());
        first.setIsCover(1);
        first.setSortOrder(0);
        productImageMapper.updateById(first);
    }

    private void reorderSortOrders(Long productId) {
        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductImage::getProductId, productId);
        wrapper.orderByAsc(ProductImage::getSortOrder)
                .orderByAsc(ProductImage::getId);
        List<ProductImage> images = productImageMapper.selectList(wrapper);
        for (int i = 0; i < images.size(); i++) {
            ProductImage img = images.get(i);
            if (img.getSortOrder() != null && img.getSortOrder() == i) {
                continue;
            }
            ProductImage update = new ProductImage();
            update.setId(img.getId());
            update.setSortOrder(i);
            productImageMapper.updateById(update);
        }
    }
}
