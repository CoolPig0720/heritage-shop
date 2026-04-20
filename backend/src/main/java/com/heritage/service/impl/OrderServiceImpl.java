package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heritage.common.BusinessException;
import com.heritage.dto.OrderCreateRequest;
import com.heritage.dto.OrderItemVO;
import com.heritage.dto.OrderVO;
import com.heritage.dto.PageQuery;
import com.heritage.entity.CartItem;
import com.heritage.entity.OrderItem;
import com.heritage.entity.Orders;
import com.heritage.entity.Product;
import com.heritage.entity.ProductImage;
import com.heritage.entity.UserAddress;
import com.heritage.mapper.CartItemMapper;
import com.heritage.mapper.OrderItemMapper;
import com.heritage.mapper.OrdersMapper;
import com.heritage.mapper.ProductImageMapper;
import com.heritage.mapper.ProductMapper;
import com.heritage.mapper.UserAddressMapper;
import com.heritage.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final UserAddressMapper userAddressMapper;

    @Override
    @Transactional
    public Long createOrderFromCart(Long userId, OrderCreateRequest request) {
        if (userId == null) {
            throw new BusinessException("未登录或登录已过期");
        }
        if (request == null || request.getAddressId() == null) {
            throw new BusinessException("收货地址不能为空");
        }

        UserAddress address = userAddressMapper.selectById(request.getAddressId());
        if (address == null || address.getStatus() == null || address.getStatus() != 1) {
            throw new BusinessException("收货地址不存在");
        }
        if (!userId.equals(address.getUserId())) {
            throw new BusinessException("无权限操作");
        }

        LambdaQueryWrapper<CartItem> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(CartItem::getUserId, userId)
                .eq(CartItem::getStatus, 1)
                .eq(CartItem::getSelected, 1)
                .orderByAsc(CartItem::getId);
        List<CartItem> cartItems = cartItemMapper.selectList(cartWrapper);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new BusinessException("请先勾选需要结算的商品");
        }

        List<Long> productIds = cartItems.stream()
                .map(CartItem::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Product> productMap = loadProductMap(productIds);
        Map<Long, String> coverMap = loadCoverUrlMap(productIds);

        List<OrderItem> orderItems = new ArrayList<>(cartItems.size());
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = productMap.get(cartItem.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在");
            }
            if (product.getStatus() == null || product.getStatus() != 1) {
                throw new BusinessException("存在已下架商品，请重新选择");
            }

            int qty = cartItem.getQuantity() == null ? 0 : cartItem.getQuantity();
            if (qty <= 0) {
                throw new BusinessException("购物车数量不合法");
            }

            BigDecimal unitPrice = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));
            totalAmount = totalAmount.add(subtotal);

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setCoverImageUrl(coverMap.get(product.getId()));
            item.setUnitPrice(unitPrice);
            item.setQuantity(qty);
            item.setSubtotal(subtotal);
            orderItems.add(item);
        }

        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setAddressId(address.getId());
        order.setTotalAmount(totalAmount);
        order.setStatus("UNPAID");
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setRegionNamePath(address.getRegionNamePath());
        order.setDetailAddress(address.getDetailAddress());
        ordersMapper.insert(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        CartItem cartUpdate = new CartItem();
        cartUpdate.setStatus(0);
        cartUpdate.setSelected(0);
        LambdaQueryWrapper<CartItem> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(CartItem::getUserId, userId)
                .eq(CartItem::getStatus, 1)
                .eq(CartItem::getSelected, 1);
        cartItemMapper.update(cartUpdate, updateWrapper);

        return order.getId();
    }

    @Override
    public Page<OrderVO> pageMyOrders(Long userId, PageQuery query, String status, String keyword) {
        if (userId == null) {
            throw new BusinessException("未登录或登录已过期");
        }

        Page<Orders> page = new Page<>(query == null ? 1 : query.getPage(), query == null ? 10 : query.getSize());
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, userId);
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Orders::getStatus, status.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Orders::getOrderNo, keyword.trim());
        }
        wrapper.orderByDesc(Orders::getCreateTime).orderByDesc(Orders::getId);

        Page<Orders> orderPage = this.page(page, wrapper);
        List<Orders> orders = orderPage.getRecords();
        if (orders == null || orders.isEmpty()) {
            Page<OrderVO> empty = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        List<Long> orderIds = orders.stream()
                .map(Orders::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, List<OrderItem>> itemMap = loadOrderItemMap(orderIds);

        List<OrderVO> voList = orders.stream()
                .map(o -> toOrderVO(o, itemMap.getOrDefault(o.getId(), Collections.emptyList()))).toList();
        Page<OrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public OrderVO getMyOrderDetail(Long userId, Long orderId) {
        Orders order = getOwnedOrder(userId, orderId);

        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, order.getId()).orderByAsc(OrderItem::getId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        return toOrderVO(order, items);
    }

    @Override
    @Transactional
    public void cancelMyOrder(Long userId, Long orderId) {
        Orders order = getOwnedOrder(userId, orderId);
        if (!"UNPAID".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许取消");
        }
        Orders update = new Orders();
        update.setId(order.getId());
        update.setStatus("CANCELLED");
        update.setCancelTime(LocalDateTime.now());
        ordersMapper.updateById(update);
    }

    private Orders getOwnedOrder(Long userId, Long orderId) {
        if (userId == null) {
            throw new BusinessException("未登录或登录已过期");
        }
        if (orderId == null) {
            throw new BusinessException("订单不存在");
        }
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException("无权限操作");
        }
        return order;
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

    private Map<Long, List<OrderItem>> loadOrderItemMap(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(OrderItem::getOrderId, orderIds).orderByAsc(OrderItem::getId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }
        return items.stream()
                .filter(i -> i.getOrderId() != null)
                .collect(Collectors.groupingBy(OrderItem::getOrderId, LinkedHashMap::new, Collectors.toList()));
    }

    private OrderVO toOrderVO(Orders order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        if (items == null || items.isEmpty()) {
            vo.setItems(Collections.emptyList());
            return vo;
        }
        List<OrderItemVO> itemVOs = items.stream().map(this::toOrderItemVO).toList();
        vo.setItems(itemVOs);
        return vo;
    }

    private OrderItemVO toOrderItemVO(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    private String generateOrderNo() {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int rnd = ThreadLocalRandom.current().nextInt(100, 1000);
        return "O" + timePart + rnd;
    }
}
