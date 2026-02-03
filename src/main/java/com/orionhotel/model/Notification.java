package com.orionhotel.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum TargetRole {
        ALL, ADMIN, MANAGER, RECEPTIONIST, HOUSEKEEPING, ACCOUNTANT, SYSTEM
    }

    private int id;
    private String message;
    private TargetRole targetRole;
    private boolean isRead;
    private LocalDateTime timestamp;

    public Notification(int id, String message, TargetRole targetRole) {
        this.id = id;
        this.message = message;
        this.targetRole = targetRole;
        this.isRead = false;
        this.timestamp = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public TargetRole getTargetRole() {
        return targetRole;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
