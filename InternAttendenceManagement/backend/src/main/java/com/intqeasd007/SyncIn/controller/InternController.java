package com.intqeasd007.SyncIn.controller;

import com.intqeasd007.SyncIn.dto.SubmitLeaveRequest;
import com.intqeasd007.SyncIn.entity.*;
import com.intqeasd007.SyncIn.repository.AttendanceRepository;
import com.intqeasd007.SyncIn.repository.DailyTokenRepository;
import com.intqeasd007.SyncIn.repository.UserRepository;
import com.intqeasd007.SyncIn.service.LeaveManagementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/intern")
public class InternController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private DailyTokenRepository dailyTokenRepository;

    @Autowired
    private LeaveManagementService leaveManagementService;

    // ── helper ──
    private User requireInternOrCR(HttpServletRequest req) {
        String role = (String) req.getAttribute("role");
        if (!"INTERN".equals(role) && !"CR".equals(role)) return null;
        String empId = (String) req.getAttribute("empId");
        return userRepository.findById(empId).orElse(null);
    }

    // ───────────── PUNCH IN (validate QR token) ─────────────

    @PostMapping("/punch-in")
    public ResponseEntity<?> punchIn(HttpServletRequest req,
                                     @RequestBody Map<String, String> body) {
        User user = requireInternOrCR(req);
        if (user == null || user.getCohort() == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token is required"));
        }

        LocalDate today = LocalDate.now();

        // Check if already scanned today
        var existing = attendanceRepository.findByUser_EmpIdAndDate(user.getEmpId(), today);
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Already punched in today",
                    "status", existing.get().getStatus().name()
            ));
        }

        // Validate the token belongs to their cohort and today's date
        var dailyToken = dailyTokenRepository.findByToken(token).orElse(null);
        if (dailyToken == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid QR token"));
        }
        if (!dailyToken.getBatchCode().equals(user.getCohort().getBatchCode())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token does not match your cohort"));
        }
        if (!dailyToken.getDate().equals(today)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token has expired (not today's token)"));
        }

        // Mark attendance
        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setDate(today);
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendanceRepository.save(attendance);

        return ResponseEntity.ok(Map.of(
                "message", "Punch-in successful!",
                "status", "PRESENT",
                "date", today.toString()
        ));
    }

    // ───────────── GET TODAY'S STATUS ─────────────

    @GetMapping("/today-status")
    public ResponseEntity<?> getTodayStatus(HttpServletRequest req) {
        User user = requireInternOrCR(req);
        if (user == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        LocalDate today = LocalDate.now();
        var existing = attendanceRepository.findByUser_EmpIdAndDate(user.getEmpId(), today);

        if (existing.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "status", existing.get().getStatus().name(),
                    "date", today.toString(),
                    "punchedIn", true
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "NOT_MARKED",
                "date", today.toString(),
                "punchedIn", false
        ));
    }

    @PostMapping("/leave")
    public ResponseEntity<?> submitLeave(HttpServletRequest req,
                                         @RequestBody SubmitLeaveRequest body) {
        String role = (String) req.getAttribute("role");
        if (!"INTERN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only intern can submit leave"));
        }

        String empId = (String) req.getAttribute("empId");
        Map<String, Object> out = leaveManagementService.submitLeave(empId, body);
        if (out.containsKey("error")) {
            return ResponseEntity.badRequest().body(out);
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/leave")
    public ResponseEntity<?> myLeaves(HttpServletRequest req) {
        String empId = (String) req.getAttribute("empId");
        if (empId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(leaveManagementService.getInternLeaves(empId));
    }

    @GetMapping("/calendar")
    public ResponseEntity<?> calendar(HttpServletRequest req,
                                      @RequestParam(required = false) String month) {
        String empId = (String) req.getAttribute("empId");
        if (empId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(leaveManagementService.getCalendar(empId, month));
    }

    @GetMapping("/health")
    public ResponseEntity<?> health(HttpServletRequest req,
                                    @RequestParam(required = false) String month) {
        String empId = (String) req.getAttribute("empId");
        if (empId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        Map<String, Object> out = leaveManagementService.getInternHealth(empId, month);
        if (out.containsKey("error")) {
            return ResponseEntity.badRequest().body(out);
        }
        return ResponseEntity.ok(out);
    }
}
