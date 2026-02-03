package com.orionhotel.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class HousekeepingTask implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Startus {
        PENDING, IN_PROGRESS, COMPLETED
    }

    public enum TaskType {
        CLEANING, INSPECTION
    }

    private int taskId;
    private int roomNumber;
    private TaskType type;
    private Integer assignedStaffId;
    private String assignedStaffName;
    private Startus status;
    private LocalDateTime timeAssigned;
    private LocalDateTime completionTime;

    public HousekeepingTask(int taskId, int roomNumber, TaskType type, Integer assignedStaffId,
            String assignedStaffName) {
        this.taskId = taskId;
        this.roomNumber = roomNumber;
        this.type = type;
        this.assignedStaffId = assignedStaffId;
        this.assignedStaffName = assignedStaffName;
        this.status = Startus.PENDING;
        this.timeAssigned = LocalDateTime.now();
    }

    public int getTaskId() {
        return taskId;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public TaskType getType() {
        return type;
    }

    public Integer getAssignedStaffId() {
        return assignedStaffId;
    }

    public String getAssignedStaffName() {
        return assignedStaffName;
    }

    public Startus getStatus() {
        return status;
    }

    public LocalDateTime getTimeAssigned() {
        return timeAssigned;
    }

    public LocalDateTime getCompletionTime() {
        return completionTime;
    }

    public void setStatus(Startus status) {
        this.status = status;
        if (status == Startus.COMPLETED) {
            this.completionTime = LocalDateTime.now();
        }
    }
}
