package com.splitwisepay.demo.DTO.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResponse {
    private Long userId;
    private String userName;
    // positive = they owe you, negative = you owe them
    private BigDecimal netBalance;
}
