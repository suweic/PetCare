package com.petcare.system.service.impl;

import com.petcare.system.dto.PaymentRequestDTO;
import com.petcare.system.dto.PaymentResultDTO;
import com.petcare.system.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

/**
 * Mock 支付服务实现。
 * <p>
 * 当没有配置真实支付 SDK 时使用此默认实现（开发/演示环境）。
 * 接入真实支付时，创建 WechatPaymentService / AlipayService 实现 PaymentService 接口，
 * 并标注 @Primary 或使用 @ConditionalOnProperty 切换。
 *
 * @see PaymentService
 */
@Slf4j
@Service
@ConditionalOnMissingBean(value = PaymentService.class, ignored = MockPaymentServiceImpl.class)
public class MockPaymentServiceImpl implements PaymentService {

    @Override
    public PaymentResultDTO createPayment(PaymentRequestDTO request) {
        log.info("[Mock支付] 创建支付: orderNo={}, amount={}, method={}",
                request.getOrderNo(), request.getAmount(), request.getPaymentMethod());

        // 模拟支付处理延迟
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        PaymentResultDTO result = new PaymentResultDTO();
        result.setSuccess(true);
        result.setOrderNo(request.getOrderNo());
        result.setTransactionId("MOCK_" + System.currentTimeMillis());
        result.setMessage("Mock支付成功（开发环境）");
        return result;
    }

    @Override
    public PaymentResultDTO queryPayment(String orderNo) {
        PaymentResultDTO result = new PaymentResultDTO();
        result.setSuccess(true);
        result.setOrderNo(orderNo);
        result.setMessage("Mock支付查询 — 支付成功");
        return result;
    }

    @Override
    public boolean handleCallback(String rawData) {
        log.info("[Mock支付] 收到回调: {}", rawData);
        return true;
    }
}
