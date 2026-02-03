package com.orionhotel.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MaintenanceRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum IssueType {
        ELECTRICAL, PLUMBING, FURNITURE, OTHER
    }

    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    public enum Status {
        OPEN, IN_PROGRESS, FIXED
    }

    private int requestId;
    private int roomNumber;
    private IssueType issueType;
    private String description;
    private Priority priority;
    private Integer assignedTechnicianId;
    private String assignedTechnicianName;
    private Status status;
    private LocalDateTime timestamp;

    public MaintenanceRequest(int requestId, int roomNumber, IssueType issueType, String description,
            Priority priority) {
        this.requestId = requestId;
        this.roomNumber = roomNumber;
        this.issueType = issueType;
        this.description = description;
        this.priority = priority;
        this.status = Status.OPEN;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public int getRequestId() {
        return requestId;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public IssueType getIssueType() {
        return issueType;
    }

    public String getDescription() {
        return description;
    }

    public Priority getPriority() {
        return priority;
    }

    public Integer getAssignedTechnicianId() {
        return assignedTechnicianId;
    }

    public String getAssignedTechnicianName() {
        return assignedTechnicianName;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setAssignedTechnician(Integer id, String name) {
        this.assignedTechnicianId = id;
        this.assignedTechnicianName = name;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
