package com.intqeasd007.SyncIn.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String internEmpId;

    @Column(nullable = false)
    private String batchCode;

    private String reason;
    private String description;

    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.PENDING;
}

