package com.petcare.system.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequestDTO {
    private String orderNo;
    private BigDecimal amount;
    private String description;
    /** 支付方式: wechat / alipay */
    private String paymentMethod;
    private Long userId;
}
