package com.splitwisepay.demo.DTO.Response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatsResponse {

    private Long totalUsers;
    private Long totalGroups;
    private Long totalExpenses;
}