package org.springmvc.ebanking.dtos.auth;

import lombok.Data;

@Data
public class JwtAuthResponse {
    private String token;

    public JwtAuthResponse(String token) {
        this.token = token;
    }
}