package org.springmvc.ebanking.dtos.auth;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@NoArgsConstructor
public class JwtAuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String[] roles;
    public JwtAuthResponse(String token) {
        this.token = token;
    }
}