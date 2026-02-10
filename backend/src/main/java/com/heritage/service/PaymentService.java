package com.heritage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heritage.dto.PaymentActionRequest;
import com.heritage.dto.PaymentCreateRequest;
import com.heritage.dto.PaymentVO;
import com.heritage.entity.Payment;

public interface PaymentService extends IService<Payment> {

    PaymentVO createPayment(Long userId, PaymentCreateRequest request);

    PaymentVO getMyPayment(Long userId, Long paymentId);

    PaymentVO mockPay(Long userId, Long paymentId, PaymentActionRequest request);
}
