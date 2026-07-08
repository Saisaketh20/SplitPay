package com.splitwisepay.demo.DTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCredRequest {

    // Both optional — user may update name only, password only, or both
    @Size(min = 2, message = "Name must be at least 2 characters")
    private String name;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}