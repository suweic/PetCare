package com.petcare.system.dto;

import lombok.Data;

@Data
public class PaymentResultDTO {
    private boolean success;
    private String orderNo;
    private String transactionId;
    /** 支付链接 / 二维码URL */
    private String payUrl;
    private String message;
}
