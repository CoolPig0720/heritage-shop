package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heritage.common.BusinessException;
import com.heritage.dto.*;
import com.heritage.entity.*;
import com.heritage.mapper.*;
import com.heritage.service.CustomizeMessageService;
import com.heritage.service.CustomizeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomizeRequestServiceImpl extends ServiceImpl<CustomizeRequestMapper, CustomizeRequest>
        implements CustomizeRequestService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final UserAddressMapper userAddressMapper;
    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final CustomizeMessageMapper customizeMessageMapper;

    @Autowired
    @Lazy
    private CustomizeMessageService customizeMessageService;

    @Override
    public List<MerchantCardVO> listMerchants() {
        return pageMerchants(new PageQuery()).getRecords();
    }

    @Override
    public Page<MerchantCardVO> pageMerchants(PageQuery query) {
        // 1. 分页查询 MERCHANT 角色用户
        Page<User> userPage = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getRole, "MERCHANT");
        Page<User> merchantsPage = userMapper.selectPage(userPage, userWrapper);

        List<User> merchants = merchantsPage.getRecords();
        if (merchants == null || merchants.isEmpty()) {
            return new Page<>(query.getPage(), query.getSize(), 0);
        }

        List<Long> merchantIds = merchants.stream().map(User::getId).collect(Collectors.toList());

        // 2. 统计每位商家的在售商品数
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.in(Product::getMerchantId, merchantIds).eq(Product::getStatus, 1);
        List<Product> allProducts = productMapper.selectList(productWrapper);
        Map<Long, Long> productCountMap = allProducts.stream()
                .collect(Collectors.groupingBy(Product::getMerchantId, Collectors.counting()));

        // 3. 每位商家按评分降序取前3个商品
        Map<Long, List<Product>> merchantTopProductsMap = allProducts.stream()
                .collect(Collectors.groupingBy(Product::getMerchantId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        // 4. 批量查所有相关商品的封面图
        Set<Long> allProductIds = allProducts.stream().map(Product::getId).collect(Collectors.toSet());
        Map<Long, String> coverMap = new LinkedHashMap<>();
        if (!allProductIds.isEmpty()) {
            LambdaQueryWrapper<ProductImage> imgWrapper = new LambdaQueryWrapper<>();
            imgWrapper.in(ProductImage::getProductId, allProductIds)
                    .eq(ProductImage::getIsCover, 1);
            List<ProductImage> covers = productImageMapper.selectList(imgWrapper);
            for (ProductImage img : covers) {
                coverMap.putIfAbsent(img.getProductId(), img.getImageUrl());
            }
        }

        // 5. 组装 MerchantCardVO
        List<MerchantCardVO> result = new ArrayList<>();
        for (User merchant : merchants) {
            MerchantCardVO vo = new MerchantCardVO();
            vo.setId(merchant.getId());
            vo.setName(merchant.getName());
            vo.setAvatar(merchant.getAvatar());
            vo.setProductCount(productCountMap.getOrDefault(merchant.getId(), 0L).intValue());

            List<Product> topProducts = merchantTopProductsMap.getOrDefault(merchant.getId(), Collections.emptyList())
                    .stream()
                    .sorted((a, b) -> {
                        double ra = a.getAvgRating() != null ? a.getAvgRating().doubleValue() : 0;
                        double rb = b.getAvgRating() != null ? b.getAvgRating().doubleValue() : 0;
                        return Double.compare(rb, ra);
                    })
                    .limit(3)
                    .collect(Collectors.toList());

            List<MerchantProductBriefVO> topVOList = new ArrayList<>();
            for (Product p : topProducts) {
                MerchantProductBriefVO brief = new MerchantProductBriefVO();
                brief.setId(p.getId());
                brief.setName(p.getName());
                brief.setPrice(p.getPrice());
                brief.setAvgRating(p.getAvgRating());
                brief.setCoverImageUrl(coverMap.getOrDefault(p.getId(), ""));
                topVOList.add(brief);
            }
            vo.setTopProducts(topVOList);

            result.add(vo);
        }
        Page<MerchantCardVO> resultPage = new Page<>(query.getPage(), query.getSize(), merchantsPage.getTotal());
        resultPage.setRecords(result);
        return resultPage;
    }

    @Override
    public Page<ProductVO> pageMerchantProducts(Long merchantId, PageQuery query) {
        Page<Product> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getMerchantId, merchantId).eq(Product::getStatus, 1);
        wrapper.orderByDesc(Product::getAvgRating);
        Page<Product> productPage = this.productMapper.selectPage(page, wrapper);

        List<ProductVO> voList = new ArrayList<>();
        for (Product p : productPage.getRecords()) {
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(p, vo);
            // 填充封面图
            LambdaQueryWrapper<ProductImage> imgWrapper = new LambdaQueryWrapper<>();
            imgWrapper.eq(ProductImage::getProductId, p.getId()).eq(ProductImage::getIsCover, 1);
            List<ProductImage> imgs = productImageMapper.selectList(imgWrapper);
            if (!imgs.isEmpty()) {
                vo.setCoverImageUrl(imgs.get(0).getImageUrl());
            }
            voList.add(vo);
        }

        Page<ProductVO> voPage = new Page<>(productPage.getCurrent(), productPage.getSize(), productPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Long createRequest(Long userId, CustomizeRequestCreateRequest request) {
        // 校验商家角色
        User merchant = userMapper.selectById(request.getMerchantId());
        if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
            throw new BusinessException("商家不存在");
        }

        CustomizeRequest entity = new CustomizeRequest();
        entity.setUserId(userId);
        entity.setMerchantId(request.getMerchantId());
        entity.setTitle(request.getTitle().trim());
        entity.setDescription(request.getDescription().trim());
        entity.setImageUrls(request.getImageUrls());
        entity.setStatus("PENDING");
        this.save(entity);
        return entity.getId();
    }

    @Override
    public Page<CustomizeRequestVO> pageMyRequests(Long userId, PageQuery query, String status, String keyword) {
        Page<CustomizeRequest> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<CustomizeRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomizeRequest::getUserId, userId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(CustomizeRequest::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(CustomizeRequest::getTitle, keyword);
        }
        wrapper.orderByDesc(CustomizeRequest::getCreateTime);
        Page<CustomizeRequest> resultPage = this.page(page, wrapper);

        List<CustomizeRequestVO> voList = toVOList(resultPage.getRecords(), userId);
        Page<CustomizeRequestVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(),
                resultPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Page<CustomizeRequestVO> pageReceivedRequests(Long merchantId, PageQuery query, String status,
            String keyword) {
        Page<CustomizeRequest> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<CustomizeRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomizeRequest::getMerchantId, merchantId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(CustomizeRequest::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(CustomizeRequest::getTitle, keyword);
        }
        wrapper.orderByDesc(CustomizeRequest::getCreateTime);
        Page<CustomizeRequest> resultPage = this.page(page, wrapper);

        List<CustomizeRequestVO> voList = toVOList(resultPage.getRecords(), merchantId);
        Page<CustomizeRequestVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(),
                resultPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public CustomizeRequestVO getDetail(Long userId, Long id) {
        CustomizeRequest entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("工单不存在");
        }
        assertParticipant(userId, entity);
        return toVO(entity, userId);
    }

    @Override
    public void quote(Long merchantId, Long id, CustomizeQuoteRequest request) {
        CustomizeRequest entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("工单不存在");
        }
        if (!entity.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权限操作该工单");
        }
        if (!"PENDING".equals(entity.getStatus())) {
            throw new BusinessException("工单状态不支持报价");
        }
        entity.setQuotedPrice(request.getQuotedPrice());
        entity.setStatus("QUOTED");
        this.updateById(entity);
    }

    @Override
    @Transactional
    public Long confirm(Long userId, Long id, CustomizeConfirmRequest request) {
        CustomizeRequest entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("工单不存在");
        }
        if (!entity.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该工单");
        }
        if (!"QUOTED".equals(entity.getStatus())) {
            throw new BusinessException("工单状态不支持确认");
        }

        // 校验地址
        UserAddress address = userAddressMapper.selectById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收件地址不存在或无效");
        }

        // 创建订单
        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setAddressId(address.getId());
        order.setTotalAmount(entity.getQuotedPrice());
        order.setStatus("UNPAID");
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setRegionNamePath(address.getRegionNamePath());
        order.setDetailAddress(address.getDetailAddress());
        ordersMapper.insert(order);

        // 创建订单明细（定制商品快照）
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(-1L); // 定制商品无关联product
        item.setProductName(entity.getTitle());
        item.setCoverImageUrl(entity.getImageUrls() != null && !entity.getImageUrls().isEmpty()
                ? entity.getImageUrls().get(0)
                : null);
        item.setUnitPrice(entity.getQuotedPrice());
        item.setQuantity(1);
        item.setSubtotal(entity.getQuotedPrice());
        orderItemMapper.insert(item);

        // 更新工单
        entity.setOrderId(order.getId());
        entity.setAddressId(request.getAddressId());
        entity.setStatus("CONFIRMED");
        this.updateById(entity);

        return order.getId();
    }

    @Override
    public void updateStatus(Long userId, String role, Long id, CustomizeStatusUpdateRequest request) {
        CustomizeRequest entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("工单不存在");
        }
        assertParticipant(userId, entity);

        String newStatus = request.getStatus();

        if ("COMPLETED".equals(newStatus)) {
            if (!"CONFIRMED".equals(entity.getStatus())) {
                throw new BusinessException("仅已确认的工单可标记完成");
            }
            if (!entity.getMerchantId().equals(userId)) {
                throw new BusinessException("仅商家可标记完成");
            }
        } else if ("CANCELLED".equals(newStatus)) {
            if ("CONFIRMED".equals(entity.getStatus()) || "COMPLETED".equals(entity.getStatus())) {
                throw new BusinessException("已确认的工单不可取消，如需退款请联系商家");
            }
        } else {
            throw new BusinessException("不支持的状态变更");
        }

        entity.setStatus(newStatus);
        this.updateById(entity);
    }

    // ====== 私有辅助方法 ======

    private void assertParticipant(Long userId, CustomizeRequest entity) {
        if (!entity.getUserId().equals(userId) && !entity.getMerchantId().equals(userId)) {
            throw new BusinessException("无权限访问该工单");
        }
    }

    private List<CustomizeRequestVO> toVOList(List<CustomizeRequest> records, Long viewerId) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream().map(r -> toVO(r, viewerId)).collect(Collectors.toList());
    }

    private CustomizeRequestVO toVO(CustomizeRequest entity, Long viewerId) {
        CustomizeRequestVO vo = new CustomizeRequestVO();
        BeanUtils.copyProperties(entity, vo);

        // 聚合用户名
        User user = userMapper.selectById(entity.getUserId());
        if (user != null) {
            vo.setUserName(user.getName());
        }
        User merchant = userMapper.selectById(entity.getMerchantId());
        if (merchant != null) {
            vo.setMerchantName(merchant.getName());
        }

        // 聚合订单号
        if (entity.getOrderId() != null) {
            Orders order = ordersMapper.selectById(entity.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
            }
        }

        // 聚合未读消息数
        Long unreadCount = customizeMessageMapper.selectCount(
                new LambdaQueryWrapper<CustomizeMessage>()
                        .eq(CustomizeMessage::getRequestId, entity.getId())
                        .eq(CustomizeMessage::getIsRead, 0)
                        .ne(CustomizeMessage::getSenderId, viewerId));
        vo.setUnreadCount(unreadCount);

        return vo;
    }

    @Override
    public CustomizeUnreadVO getUnreadCount(Long userId, String role) {
        CustomizeUnreadVO vo = new CustomizeUnreadVO();
        if ("MERCHANT".equals(role)) {
            vo.setPendingCount(countByMerchantAndStatus(userId, "PENDING"));
            vo.setQuotedCount(countByMerchantAndStatus(userId, "QUOTED"));
            vo.setConfirmedCount(countByMerchantAndStatus(userId, "CONFIRMED"));
        } else {
            vo.setPendingCount(countByUserAndStatus(userId, "PENDING"));
            vo.setQuotedCount(countByUserAndStatus(userId, "QUOTED"));
            vo.setConfirmedCount(countByUserAndStatus(userId, "CONFIRMED"));
        }
        vo.setUnreadMessageCount(customizeMessageService.countUnreadMessages(userId));
        return vo;
    }

    private Long countByUserAndStatus(Long userId, String status) {
        LambdaQueryWrapper<CustomizeRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomizeRequest::getUserId, userId).eq(CustomizeRequest::getStatus, status);
        return this.count(wrapper);
    }

    private Long countByMerchantAndStatus(Long merchantId, String status) {
        LambdaQueryWrapper<CustomizeRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomizeRequest::getMerchantId, merchantId).eq(CustomizeRequest::getStatus, status);
        return this.count(wrapper);
    }

    private String generateOrderNo() {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int rnd = ThreadLocalRandom.current().nextInt(100, 1000);
        return "C" + timePart + rnd;
    }
}
