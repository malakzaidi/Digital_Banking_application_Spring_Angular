package org.springmvc.ebanking.dtos;

import java.util.Date;

public class UserProfileDTO {
    private Long userId; // Maps to User.id
    private String username; // Maps to User.username
    private String email; // Maps to User.email
    private String name; // Concatenated from firstName + lastName
    private String phone; // Optional, not in User entity yet
    private String address; // Optional, not in User entity yet
    private Date createdAt; // Maps to User.createdAt
    private Date updatedAt; // Maps to User.updatedAt

    // Default constructor
    public UserProfileDTO() {}

    // Parameterized constructor
    public UserProfileDTO(Long userId, String username, String email, String name, String phone, String address, Date createdAt, Date updatedAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}