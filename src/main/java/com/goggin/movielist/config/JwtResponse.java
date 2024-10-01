package com.goggin.movielist.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Integer id;
    private String username;
    private String email;

    public JwtResponse(String accessToken, Integer id, String username, String email) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
    }
}
