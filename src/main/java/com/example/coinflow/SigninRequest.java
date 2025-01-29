package com.example.coinflow;

import lombok.Data;
import lombok.Getter;

@Data
public class SigninRequest {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
