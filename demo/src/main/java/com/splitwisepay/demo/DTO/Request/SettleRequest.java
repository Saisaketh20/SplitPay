package com.splitwisepay.demo.DTO.Request;

import jakarta.validation.constraints.DecimalMin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// dto/request/SettleRequest.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettleRequest {

    @NotNull(message = "User to settle with is required")
    private Long paidToUserId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
}
