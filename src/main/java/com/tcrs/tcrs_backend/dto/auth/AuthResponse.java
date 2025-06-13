package com.tcrs.tcrs_backend.dto.auth;

import com.tcrs.tcrs_backend.entity.Role;

import java.util.Set;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserInfo user;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, Long expiresIn, UserInfo user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    // Getters and Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }

    // Inner class for user info
    public static class UserInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private Set<Role> roles;
        private Boolean emailVerified;
        private Boolean phoneVerified;

        // Constructors
        public UserInfo() {}

        public UserInfo(Long id, String firstName, String lastName, String email, String phone,
                        Set<Role> roles, Boolean emailVerified, Boolean phoneVerified) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
            this.roles = roles;
            this.emailVerified = emailVerified;
            this.phoneVerified = phoneVerified;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public Set<Role> getRoles() { return roles; }
        public void setRoles(Set<Role> roles) { this.roles = roles; }

        public Boolean getEmailVerified() { return emailVerified; }
        public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

        public Boolean getPhoneVerified() { return phoneVerified; }
        public void setPhoneVerified(Boolean phoneVerified) { this.phoneVerified = phoneVerified; }

        public String getFullName() {
            return firstName + " " + lastName;
        }
    }
}
