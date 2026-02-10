package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heritage.common.BusinessException;
import com.heritage.dto.PaymentActionRequest;
import com.heritage.dto.PaymentCreateRequest;
import com.heritage.dto.PaymentVO;
import com.heritage.entity.Orders;
import com.heritage.entity.Payment;
import com.heritage.mapper.OrdersMapper;
import com.heritage.mapper.PaymentMapper;
import com.heritage.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrdersMapper ordersMapper;

    private static final Duration CONFIRM_TIMEOUT = Duration.ofMinutes(2);
    private static final double CHANNEL_FAIL_RATE = 0.2d;

    @Override
    @Transactional
    public PaymentVO createPayment(Long userId, PaymentCreateRequest request) {
        if (userId == null) {
            throw new BusinessException("未登录或登录已过期");
        }
        if (request == null || request.getOrderId() == null) {
            throw new BusinessException("订单不能为空");
        }

        Orders order = ordersMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException("无权限操作");
        }
        if (!"UNPAID".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许支付");
        }

        LambdaQueryWrapper<Payment> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(Payment::getOrderId, order.getId())
                .eq(Payment::getStatus, "INIT")
                .orderByDesc(Payment::getId)
                .last("LIMIT 1");
        Payment existing = paymentMapper.selectOne(existingWrapper);
        if (existing != null) {
            return toPaymentVO(existing);
        }

        Payment payment = new Payment();
        payment.setPayNo(generatePayNo());
        payment.setOrderId(order.getId());
        payment.setAmount(order.getTotalAmount());
        payment.setChannel(normalizeChannel(request.getChannel()));
        payment.setStatus("INIT");
        paymentMapper.insert(payment);
        return toPaymentVO(payment);
    }

    @Override
    public PaymentVO getMyPayment(Long userId, Long paymentId) {
        Payment payment = getOwnedPayment(userId, paymentId);
        return toPaymentVO(payment);
    }

    @Override
    @Transactional
    public PaymentVO mockPay(Long userId, Long paymentId, PaymentActionRequest request) {
        Payment payment = getOwnedPayment(userId, paymentId);
        if (request == null || request.getAction() == null || request.getAction().trim().isEmpty()) {
            throw new BusinessException("支付动作不能为空");
        }
        String action = request.getAction().trim().toUpperCase();
        if (!"CONFIRM".equals(action) && !"CANCEL".equals(action)) {
            throw new BusinessException("支付动作不合法");
        }

        if (!"INIT".equals(payment.getStatus())) {
            return toPaymentVO(payment);
        }

        Orders order = ordersMapper.selectById(payment.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException("无权限操作");
        }

        if ("CANCEL".equals(action)) {
            Payment update = new Payment();
            update.setId(payment.getId());
            update.setStatus("FAIL");
            update.setFailReason("USER_CANCELLED");
            paymentMapper.updateById(update);
            payment.setStatus("FAIL");
            payment.setFailReason("USER_CANCELLED");
            return toPaymentVO(payment);
        }

        if (!"UNPAID".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许确认支付");
        }

        boolean timeout = payment.getCreateTime() != null
                && Duration.between(payment.getCreateTime(), LocalDateTime.now()).compareTo(CONFIRM_TIMEOUT) > 0;
        boolean channelFail = ThreadLocalRandom.current().nextDouble() < CHANNEL_FAIL_RATE;

        if (timeout) {
            Payment update = new Payment();
            update.setId(payment.getId());
            update.setStatus("FAIL");
            update.setFailReason("TIMEOUT");
            paymentMapper.updateById(update);
            payment.setStatus("FAIL");
            payment.setFailReason("TIMEOUT");
            return toPaymentVO(payment);
        }

        if (channelFail) {
            Payment update = new Payment();
            update.setId(payment.getId());
            update.setStatus("FAIL");
            update.setFailReason("CHANNEL_FAIL");
            paymentMapper.updateById(update);
            payment.setStatus("FAIL");
            payment.setFailReason("CHANNEL_FAIL");
            return toPaymentVO(payment);
        }

        LocalDateTime now = LocalDateTime.now();
        Payment payUpdate = new Payment();
        payUpdate.setId(payment.getId());
        payUpdate.setStatus("SUCCESS");
        payUpdate.setPaidTime(now);
        paymentMapper.updateById(payUpdate);

        Orders orderUpdate = new Orders();
        orderUpdate.setId(order.getId());
        orderUpdate.setStatus("PAID");
        orderUpdate.setPaidTime(now);
        ordersMapper.updateById(orderUpdate);

        payment.setStatus("SUCCESS");
        payment.setPaidTime(now);
        return toPaymentVO(payment);
    }

    private Payment getOwnedPayment(Long userId, Long paymentId) {
        if (userId == null) {
            throw new BusinessException("未登录或登录已过期");
        }
        if (paymentId == null) {
            throw new BusinessException("支付单不存在");
        }
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付单不存在");
        }
        Orders order = ordersMapper.selectById(payment.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException("无权限操作");
        }
        return payment;
    }

    private PaymentVO toPaymentVO(Payment payment) {
        PaymentVO vo = new PaymentVO();
        BeanUtils.copyProperties(payment, vo);
        vo.setPayUrl(buildPayUrl(payment.getId(), payment.getPayNo()));
        return vo;
    }

    private String buildPayUrl(Long paymentId, String payNo) {
        String idPart = paymentId == null ? "" : paymentId.toString();
        String noPart = payNo == null ? "" : payNo;
        return "mockpay://pay?id=" + idPart + "&no=" + noPart;
    }

    private String generatePayNo() {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int rnd = ThreadLocalRandom.current().nextInt(100, 1000);
        return "P" + timePart + rnd;
    }

    private String normalizeChannel(String channel) {
        if (channel == null || channel.trim().isEmpty()) {
            return "MOCK";
        }
        return channel.trim().toUpperCase();
    }
}

