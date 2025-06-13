package com.tcrs.tcrs_backend.dto.auth;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Email or phone is required")
    private String emailOrPhone;

    @NotBlank(message = "Password is required")
    private String password;

    // Constructors
    public LoginRequest() {}

    public LoginRequest(String emailOrPhone, String password) {
        this.emailOrPhone = emailOrPhone;
        this.password = password;
    }

    // Getters and Setters
    public String getEmailOrPhone() { return emailOrPhone; }
    public void setEmailOrPhone(String emailOrPhone) { this.emailOrPhone = emailOrPhone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
