package com.example.coinflow;

import lombok.Data;
import lombok.Getter;

@Data
public class SignupRequest {
    private String username;
    private String email;
    private String password;
}
