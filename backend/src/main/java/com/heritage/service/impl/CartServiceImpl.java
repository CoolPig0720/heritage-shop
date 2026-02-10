package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heritage.common.BusinessException;
import com.heritage.dto.CartItemAddRequest;
import com.heritage.dto.CartItemQuantityUpdateRequest;
import com.heritage.dto.CartItemSelectedUpdateRequest;
import com.heritage.dto.CartItemVO;
import com.heritage.dto.CartSummaryVO;
import com.heritage.entity.CartItem;
import com.heritage.entity.Product;
import com.heritage.entity.ProductImage;
import com.heritage.mapper.CartItemMapper;
import com.heritage.mapper.ProductImageMapper;
import com.heritage.mapper.ProductMapper;
import com.heritage.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartService {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;

    @Override
    @Transactional
    public Long addItem(Long userId, CartItemAddRequest request) {
        if (userId == null) {
            throw new BusinessException("未登录或登录已过期");
        }
        if (request == null || request.getProductId() == null) {
            throw new BusinessException("商品不能为空");
        }

        Integer qty = request.getQuantity() == null ? 1 : request.getQuantity();
        if (qty <= 0) {
            throw new BusinessException("数量不合法");
        }

        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new BusinessException("商品已下架");
        }

        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, request.getProductId())
                .last("LIMIT 1");
        CartItem existing = this.getOne(wrapper);

        if (existing == null) {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setProductId(request.getProductId());
            item.setQuantity(qty);
            item.setSelected(1);
            item.setStatus(1);
            this.save(item);
            return item.getId();
        }

        CartItem update = new CartItem();
        update.setId(existing.getId());
        if (existing.getStatus() == null || existing.getStatus() != 1) {
            update.setStatus(1);
            update.setSelected(1);
            update.setQuantity(qty);
        } else {
            int oldQty = existing.getQuantity() == null ? 0 : existing.getQuantity();
            update.setQuantity(oldQty + qty);
        }
        this.updateById(update);
        return existing.getId();
    }

    @Override
    public List<CartItemVO> listItems(Long userId) {
        if (userId == null) {
            throw new BusinessException("未登录或登录已过期");
        }

        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId)
                .eq(CartItem::getStatus, 1)
                .orderByDesc(CartItem::getId);
        List<CartItem> items = this.list(wrapper);
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = items.stream()
                .map(CartItem::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Product> productMap = loadProductMap(productIds);
        Map<Long, String> coverMap = loadCoverUrlMap(productIds);

        List<CartItemVO> vos = new ArrayList<>(items.size());
        for (CartItem item : items) {
            CartItemVO vo = new CartItemVO();
            vo.setId(item.getId());
            vo.setProductId(item.getProductId());
            vo.setQuantity(item.getQuantity());
            vo.setSelected(item.getSelected());

            Product product = productMap.get(item.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
                vo.setCoverImageUrl(coverMap.get(item.getProductId()));
                vo.setPrice(product.getPrice());
                vo.setProductStatus(product.getStatus());
            } else {
                vo.setProductStatus(0);
            }
            vos.add(vo);
        }
        return vos;
    }

    @Override
    @Transactional
    public void updateQuantity(Long userId, Long cartItemId, CartItemQuantityUpdateRequest request) {
        CartItem item = getOwnedActiveCartItem(userId, cartItemId);
        if (request == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException("数量不合法");
        }
        CartItem update = new CartItem();
        update.setId(item.getId());
        update.setQuantity(request.getQuantity());
        this.updateById(update);
    }

    @Override
    @Transactional
    public void updateSelected(Long userId, Long cartItemId, CartItemSelectedUpdateRequest request) {
        CartItem item = getOwnedActiveCartItem(userId, cartItemId);
        if (request == null || request.getSelected() == null || (request.getSelected() != 0 && request.getSelected() != 1)) {
            throw new BusinessException("勾选状态不合法");
        }
        CartItem update = new CartItem();
        update.setId(item.getId());
        update.setSelected(request.getSelected());
        this.updateById(update);
    }

    @Override
    @Transactional
    public void deleteItem(Long userId, Long cartItemId) {
        CartItem item = getOwnedActiveCartItem(userId, cartItemId);
        CartItem update = new CartItem();
        update.setId(item.getId());
        update.setStatus(0);
        update.setSelected(0);
        this.updateById(update);
    }

    @Override
    public CartSummaryVO getSummary(Long userId) {
        if (userId == null) {
            throw new BusinessException("未登录或登录已过期");
        }

        List<CartItemVO> items = listItems(userId);
        CartSummaryVO vo = new CartSummaryVO();
        int totalCount = 0;
        int selectedCount = 0;
        BigDecimal selectedAmount = BigDecimal.ZERO;

        for (CartItemVO item : items) {
            int qty = item.getQuantity() == null ? 0 : item.getQuantity();
            totalCount += qty;

            boolean productValid = item.getProductStatus() != null && item.getProductStatus() == 1;
            boolean selected = item.getSelected() != null && item.getSelected() == 1;
            if (productValid && selected) {
                selectedCount += qty;
                BigDecimal price = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
                selectedAmount = selectedAmount.add(price.multiply(BigDecimal.valueOf(qty)));
            }
        }

        vo.setTotalCount(totalCount);
        vo.setSelectedCount(selectedCount);
        vo.setSelectedAmount(selectedAmount);
        return vo;
    }

    private CartItem getOwnedActiveCartItem(Long userId, Long cartItemId) {
        if (userId == null) {
            throw new BusinessException("未登录或登录已过期");
        }
        if (cartItemId == null) {
            throw new BusinessException("条目不存在");
        }
        CartItem item = this.getById(cartItemId);
        if (item == null || item.getStatus() == null || item.getStatus() != 1) {
            throw new BusinessException("条目不存在");
        }
        if (!userId.equals(item.getUserId())) {
            throw new BusinessException("无权限操作");
        }
        return item;
    }

    private Map<Long, Product> loadProductMap(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Product> products = productMapper.selectBatchIds(productIds);
        if (products == null || products.isEmpty()) {
            return Collections.emptyMap();
        }
        return products.stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a, LinkedHashMap::new));
    }

    private Map<Long, String> loadCoverUrlMap(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ProductImage::getProductId, productIds);
        wrapper.orderByDesc(ProductImage::getIsCover)
                .orderByAsc(ProductImage::getSortOrder)
                .orderByAsc(ProductImage::getId);
        List<ProductImage> images = productImageMapper.selectList(wrapper);
        if (images == null || images.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, List<ProductImage>> imageMap = images.stream()
                .filter(img -> img.getProductId() != null)
                .collect(Collectors.groupingBy(ProductImage::getProductId, LinkedHashMap::new, Collectors.toList()));

        Map<Long, String> coverMap = new LinkedHashMap<>();
        for (Map.Entry<Long, List<ProductImage>> e : imageMap.entrySet()) {
            List<ProductImage> list = e.getValue();
            if (list == null || list.isEmpty()) {
                continue;
            }
            String coverUrl = list.stream()
                    .filter(img -> img.getIsCover() != null && img.getIsCover() == 1)
                    .map(ProductImage::getImageUrl)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseGet(() -> list.get(0).getImageUrl());
            coverMap.put(e.getKey(), coverUrl);
        }
        return coverMap;
    }
}

