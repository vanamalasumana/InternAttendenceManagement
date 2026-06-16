package com.intqeasd007.SyncIn.service;

import com.intqeasd007.SyncIn.dto.SubmitLeaveRequest;
import com.intqeasd007.SyncIn.entity.*;
import com.intqeasd007.SyncIn.repository.AttendanceRepository;
import com.intqeasd007.SyncIn.repository.CohortRepository;
import com.intqeasd007.SyncIn.repository.LeaveRequestRepository;
import com.intqeasd007.SyncIn.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class LeaveManagementService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CohortRepository cohortRepository;

    public Map<String, Object> submitLeave(String internEmpId, SubmitLeaveRequest body) {
        User intern = userRepository.findById(internEmpId).orElse(null);
        if (intern == null || intern.getCohort() == null) {
            return Map.of("error", "Intern not found or not assigned to cohort");
        }

        String reason = body.getReason() == null ? "" : body.getReason().trim();
        String description = body.getDescription() == null ? "" : body.getDescription().trim();
        if (reason.isEmpty()) {
            return Map.of("error", "Reason is required");
        }

        LocalDate requestDate = parseDateOrToday(body.getDate());

        LeaveRequest request = new LeaveRequest();
        request.setInternEmpId(internEmpId);
        request.setBatchCode(intern.getCohort().getBatchCode());
        request.setReason(reason);
        request.setDescription(description);
        request.setRequestDate(requestDate);
        request.setStatus(LeaveStatus.PENDING);

        LeaveRequest saved = leaveRequestRepository.save(request);

        Map<String, Object> out = new HashMap<>();
        out.put("id", saved.getId());
        out.put("internEmpId", saved.getInternEmpId());
        out.put("batchCode", saved.getBatchCode());
        out.put("reason", saved.getReason());
        out.put("description", saved.getDescription());
        out.put("requestDate", saved.getRequestDate().toString());
        out.put("status", saved.getStatus().name());
        return out;
    }

    public List<LeaveRequest> getInternLeaves(String internEmpId) {
        return leaveRequestRepository.findByInternEmpIdOrderByIdDesc(internEmpId);
    }

    public List<Map<String, Object>> getPendingLeavesForPoc(String pocEmpId) {
        List<Cohort> cohorts = cohortRepository.findByPocEmpId(pocEmpId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Cohort cohort : cohorts) {
            List<LeaveRequest> pending = leaveRequestRepository.findByBatchCodeAndStatusOrderByIdDesc(
                    cohort.getBatchCode(), LeaveStatus.PENDING
            );
            for (LeaveRequest leave : pending) {
                User intern = userRepository.findById(leave.getInternEmpId()).orElse(null);
                Map<String, Object> row = new HashMap<>();
                row.put("id", leave.getId());
                row.put("internEmpId", leave.getInternEmpId());
                row.put("internName", intern != null ? intern.getName() : "");
                row.put("batchCode", leave.getBatchCode());
                row.put("reason", leave.getReason());
                row.put("description", leave.getDescription());
                row.put("requestDate", leave.getRequestDate() != null ? leave.getRequestDate().toString() : "");
                row.put("status", leave.getStatus().name());
                result.add(row);
            }
        }

        return result;
    }

    public Map<String, Object> handlePocAction(String pocEmpId, Long leaveId, String action) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId).orElse(null);
        if (leave == null) {
            return Map.of("error", "Leave request not found");
        }

        Cohort cohort = cohortRepository.findById(leave.getBatchCode()).orElse(null);
        if (cohort == null || !pocEmpId.equals(cohort.getPocEmpId())) {
            return Map.of("error", "Forbidden");
        }

        LeaveStatus target;
        AttendanceStatus attendanceStatus = null;
        switch (action) {
            case "APPROVE" -> {
                target = LeaveStatus.APPROVED;
                attendanceStatus = AttendanceStatus.AL;
            }
            case "REJECT" -> target = LeaveStatus.REJECTED;
            case "MARK_UL" -> {
                target = LeaveStatus.UNAPPROVED;
                attendanceStatus = AttendanceStatus.UL;
            }
            default -> {
                return Map.of("error", "Invalid action");
            }
        }

        leave.setStatus(target);
        leaveRequestRepository.save(leave);

        if (attendanceStatus != null) {
            User intern = userRepository.findById(leave.getInternEmpId()).orElse(null);
            if (intern == null) {
                return Map.of("error", "Intern not found");
            }
            upsertAttendance(intern, leave.getRequestDate(), attendanceStatus);
        }

        Map<String, Object> out = new HashMap<>();
        out.put("id", leave.getId());
        out.put("internEmpId", leave.getInternEmpId());
        out.put("status", leave.getStatus().name());
        out.put("requestDate", leave.getRequestDate() != null ? leave.getRequestDate().toString() : "");
        out.put("message", "Leave request updated");
        return out;
    }

    public Map<String, Object> getInternHealth(String empId, String month) {
        User user = userRepository.findById(empId).orElse(null);
        if (user == null) {
            return Map.of("error", "User not found");
        }

        YearMonth ym = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        return calculateHealth(empId, from, to);
    }

    public Map<String, Object> calculateHealth(String empId, LocalDate from, LocalDate to) {
        List<Attendance> rows = attendanceRepository.findByUser_EmpIdAndDateBetween(empId, from, to);

        long totalDays = countWorkingDays(from, to);
        long effectiveDays = rows.stream()
                .filter(a -> isWorkingDay(a.getDate()))
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.AL)
                .count();

        double percentage = totalDays <= 0 ? 0.0 : (effectiveDays * 100.0) / totalDays;
        String bucket = percentage < 85.0 ? "CRITICAL" : (percentage <= 87.0 ? "AT_RISK" : "HEALTHY");

        Map<String, Object> out = new HashMap<>();
        out.put("empId", empId);
        out.put("from", from.toString());
        out.put("to", to.toString());
        out.put("totalDays", totalDays);
        out.put("effectiveDays", effectiveDays);
        out.put("attendancePercentage", Math.round(percentage * 100.0) / 100.0);
        out.put("bucket", bucket);
        return out;
    }

    private long countWorkingDays(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            return 0;
        }

        long count = 0;
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            if (isWorkingDay(d)) {
                count++;
            }
        }
        return count;
    }

    private boolean isWorkingDay(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }

    public List<Map<String, Object>> getCalendar(String empId, String month) {
        YearMonth ym = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        List<Attendance> rows = attendanceRepository.findByUser_EmpIdAndDateBetween(empId, from, to);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Attendance a : rows) {
            out.add(Map.of(
                    "date", a.getDate().toString(),
                    "status", a.getStatus().name()
            ));
        }
        return out;
    }

    private void upsertAttendance(User user, LocalDate date, AttendanceStatus status) {
        if (date == null) {
            return;
        }
        Attendance attendance = attendanceRepository.findByUser_EmpIdAndDate(user.getEmpId(), date).orElse(null);
        if (attendance == null) {
            attendance = new Attendance();
            attendance.setUser(user);
            attendance.setDate(date);
        }
        attendance.setStatus(status);
        attendanceRepository.save(attendance);
    }

    private LocalDate parseDateOrToday(String dateText) {
        if (dateText == null || dateText.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(dateText.trim());
        } catch (Exception ignored) {
            return LocalDate.now();
        }
    }
}
