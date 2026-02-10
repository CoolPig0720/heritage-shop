package com.heritage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.heritage.dto.OrderCreateRequest;
import com.heritage.dto.OrderVO;
import com.heritage.dto.PageQuery;
import com.heritage.entity.Orders;

public interface OrderService extends IService<Orders> {

    Long createOrderFromCart(Long userId, OrderCreateRequest request);

    Page<OrderVO> pageMyOrders(Long userId, PageQuery query, String status);

    OrderVO getMyOrderDetail(Long userId, Long orderId);

    void cancelMyOrder(Long userId, Long orderId);
}
