package com.heritage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heritage.dto.CartItemAddRequest;
import com.heritage.dto.CartItemSelectedUpdateRequest;
import com.heritage.dto.CartItemQuantityUpdateRequest;
import com.heritage.dto.CartItemVO;
import com.heritage.dto.CartSummaryVO;
import com.heritage.entity.CartItem;

import java.util.List;

public interface CartService extends IService<CartItem> {

    Long addItem(Long userId, CartItemAddRequest request);

    List<CartItemVO> listItems(Long userId);

    void updateQuantity(Long userId, Long cartItemId, CartItemQuantityUpdateRequest request);

    void updateSelected(Long userId, Long cartItemId, CartItemSelectedUpdateRequest request);

    void deleteItem(Long userId, Long cartItemId);

    CartSummaryVO getSummary(Long userId);
}
