package com.intqeasd007.SyncIn.repository;

import com.intqeasd007.SyncIn.entity.LeaveRequest;
import com.intqeasd007.SyncIn.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
	List<LeaveRequest> findByInternEmpIdOrderByIdDesc(String internEmpId);
	List<LeaveRequest> findByBatchCodeAndStatusOrderByIdDesc(String batchCode, LeaveStatus status);
}

