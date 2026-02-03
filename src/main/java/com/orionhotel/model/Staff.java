package com.orionhotel.model;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Staff implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status {
        ACTIVE, INACTIVE
    }

    private int staffId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private Role role;
    private String username;
    private String passwordHash;
    private Status status;

    public Staff() {
    }

    public Staff(int staffId, String fullName, String phoneNumber, String email, Role role, String username,
            String rawPassword) {
        this.staffId = staffId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.role = role;
        this.username = username;
        this.status = Status.ACTIVE;
        setPassword(rawPassword);
    }

    public void setPasswordHash(String hash) {
        this.passwordHash = hash;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public void setPassword(String rawPassword) {
        this.passwordHash = hashPassword(rawPassword);
    }

    public boolean checkPassword(String rawPassword) {
        return this.passwordHash.equals(hashPassword(rawPassword));
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // Getters and Setters
    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
