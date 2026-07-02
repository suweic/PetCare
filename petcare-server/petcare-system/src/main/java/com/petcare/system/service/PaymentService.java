package com.petcare.system.service;

import com.petcare.system.dto.PaymentRequestDTO;
import com.petcare.system.dto.PaymentResultDTO;

/**
 * 支付服务接口。
 * <p>
 * 当前为接口定义 + 配置占位。实际接入微信/支付宝 SDK 时，
 * 创建对应的 {@code PaymentServiceImpl} 实现此接口即可。
 */
public interface PaymentService {

    /**
     * 创建支付订单并返回支付链接/二维码。
     *
     * @param request 支付请求（订单号、金额、支付方式）
     * @return 支付结果（含支付链接/二维码URL）
     */
    PaymentResultDTO createPayment(PaymentRequestDTO request);

    /**
     * 查询支付状态。
     *
     * @param orderNo 订单号
     * @return 支付结果
     */
    PaymentResultDTO queryPayment(String orderNo);

    /**
     * 处理支付回调通知。
     *
     * @param rawData 原始回调数据
     * @return 是否处理成功
     */
    boolean handleCallback(String rawData);
}
