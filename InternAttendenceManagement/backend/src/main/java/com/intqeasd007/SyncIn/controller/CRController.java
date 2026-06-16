package com.intqeasd007.SyncIn.controller;

import com.intqeasd007.SyncIn.entity.*;
import com.intqeasd007.SyncIn.repository.AttendanceRepository;
import com.intqeasd007.SyncIn.repository.DailyTokenRepository;
import com.intqeasd007.SyncIn.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/cr")
public class CRController {

    @Autowired
    private DailyTokenRepository dailyTokenRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    // ── helper: require CR role ──
    private User requireCR(HttpServletRequest req) {
        String role = (String) req.getAttribute("role");
        if (!"CR".equals(role)) return null;
        String empId = (String) req.getAttribute("empId");
        return userRepository.findById(empId).orElse(null);
    }

    // ───────────── GENERATE DAILY QR TOKEN ─────────────

    @PostMapping("/generate-token")
    public ResponseEntity<?> generateDailyToken(HttpServletRequest req) {
        User cr = requireCR(req);
        if (cr == null || cr.getCohort() == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        String batchCode = cr.getCohort().getBatchCode();
        LocalDate today = LocalDate.now();

        // Check if token already exists for today
        var existing = dailyTokenRepository.findByBatchCodeAndDate(batchCode, today);
        if (existing.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "token", existing.get().getToken(),
                    "batchCode", batchCode,
                    "date", today.toString(),
                    "message", "Token already generated for today"
            ));
        }

        // Generate new token
        String token = UUID.randomUUID().toString();
        DailyToken dailyToken = new DailyToken();
        dailyToken.setBatchCode(batchCode);
        dailyToken.setDate(today);
        dailyToken.setToken(token);
        dailyTokenRepository.save(dailyToken);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "batchCode", batchCode,
                "date", today.toString()
        ));
    }

    // ───────────── GET TODAY'S TOKEN ─────────────

    @GetMapping("/today-token")
    public ResponseEntity<?> getTodayToken(HttpServletRequest req) {
        User cr = requireCR(req);
        if (cr == null || cr.getCohort() == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        String batchCode = cr.getCohort().getBatchCode();
        LocalDate today = LocalDate.now();

        var existing = dailyTokenRepository.findByBatchCodeAndDate(batchCode, today);
        if (existing.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "token", existing.get().getToken(),
                    "batchCode", batchCode,
                    "date", today.toString()
            ));
        }

        return ResponseEntity.ok(Map.of("token", "", "batchCode", batchCode, "date", today.toString()));
    }

    // ───────────── GET PRESENT INTERNS TODAY (for defaulter marking) ─────────────

    @GetMapping("/present-today")
    public ResponseEntity<?> getPresentToday(HttpServletRequest req) {
        User cr = requireCR(req);
        if (cr == null || cr.getCohort() == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        String batchCode = cr.getCohort().getBatchCode();
        LocalDate today = LocalDate.now();

        List<Attendance> presentList = attendanceRepository
                .findByUser_Cohort_BatchCodeAndDateAndStatus(batchCode, today, AttendanceStatus.PRESENT);

        List<Map<String, Object>> result = presentList.stream().map(a -> Map.<String, Object>of(
                "attendanceId", a.getAttendanceId(),
                "empId", a.getUser().getEmpId(),
                "name", a.getUser().getName(),
                "status", a.getStatus().name()
        )).toList();

        return ResponseEntity.ok(result);
    }

    // ───────────── MARK DEFAULTER (PRESENT → ABSENT) ─────────────

    @PutMapping("/mark-defaulter/{attendanceId}")
    public ResponseEntity<?> markDefaulter(HttpServletRequest req,
                                           @PathVariable Long attendanceId) {
        User cr = requireCR(req);
        if (cr == null || cr.getCohort() == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        Attendance attendance = attendanceRepository.findById(attendanceId).orElse(null);
        if (attendance == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Attendance record not found"));
        }

        // Only allow for current date
        if (!attendance.getDate().equals(LocalDate.now())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Can only modify today's attendance"));
        }

        // Cannot modify if status is AL or UL (locked by POC)
        if (attendance.getStatus() == AttendanceStatus.AL || attendance.getStatus() == AttendanceStatus.UL) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot modify: status is locked (AL/UL)"));
        }

        // Only mark PRESENT as ABSENT
        if (attendance.getStatus() != AttendanceStatus.PRESENT) {
            return ResponseEntity.badRequest().body(Map.of("error", "Can only mark PRESENT as defaulter"));
        }

        // Verify the attendance belongs to the CR's cohort
        if (!attendance.getUser().getCohort().getBatchCode().equals(cr.getCohort().getBatchCode())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        attendance.setStatus(AttendanceStatus.ABSENT);
        attendanceRepository.save(attendance);

        return ResponseEntity.ok(Map.of(
                "attendanceId", attendance.getAttendanceId(),
                "empId", attendance.getUser().getEmpId(),
                "name", attendance.getUser().getName(),
                "status", "ABSENT",
                "message", "Marked as defaulter"
        ));
    }

    @GetMapping("/daily-analytics")
    public ResponseEntity<?> dailyAnalytics(HttpServletRequest req,
                                            @RequestParam(required = false) String date) {
        User cr = requireCR(req);
        if (cr == null || cr.getCohort() == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        String batchCode = cr.getCohort().getBatchCode();
        LocalDate targetDate = parseDateOrToday(date);

        List<User> members = userRepository.findByCohort_BatchCode(batchCode);
        List<Attendance> rows = attendanceRepository.findByUser_Cohort_BatchCodeAndDate(batchCode, targetDate);

        Map<String, String> statusByEmp = new HashMap<>();
        for (Attendance row : rows) {
            statusByEmp.put(row.getUser().getEmpId(), row.getStatus().name());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (User member : members) {
            result.add(Map.of(
                    "empId", member.getEmpId(),
                    "name", member.getName(),
                    "role", member.getRole().name(),
                    "status", statusByEmp.getOrDefault(member.getEmpId(), "ABSENT")
            ));
        }

        return ResponseEntity.ok(Map.of(
                "batchCode", batchCode,
                "date", targetDate.toString(),
                "rows", result
        ));
    }

    @GetMapping("/attendance-sheet")
    public ResponseEntity<?> attendanceSheet(HttpServletRequest req,
                                             @RequestParam(required = false) String from,
                                             @RequestParam(required = false) String to) {
        User cr = requireCR(req);
        if (cr == null || cr.getCohort() == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        String batchCode = cr.getCohort().getBatchCode();
        LocalDate fromDate = from == null || from.isBlank() ? LocalDate.now().minusDays(6) : LocalDate.parse(from);
        LocalDate toDate = to == null || to.isBlank() ? LocalDate.now() : LocalDate.parse(to);

        List<Attendance> records = attendanceRepository.findByUser_Cohort_BatchCodeAndDateBetween(batchCode, fromDate, toDate);
        Map<String, Map<String, String>> matrix = new HashMap<>();
        for (Attendance record : records) {
            matrix.computeIfAbsent(record.getUser().getEmpId(), k -> new HashMap<>())
                    .put(record.getDate().toString(), record.getStatus().name());
        }

        List<Map<String, Object>> users = new ArrayList<>();
        for (User user : userRepository.findByCohort_BatchCode(batchCode)) {
            users.add(Map.of(
                    "empId", user.getEmpId(),
                    "name", user.getName(),
                    "role", user.getRole().name(),
                    "days", matrix.getOrDefault(user.getEmpId(), Map.of())
            ));
        }

        Set<String> daySet = new HashSet<>();
        for (Map<String, String> m : matrix.values()) {
            daySet.addAll(m.keySet());
        }
        List<String> dates = new ArrayList<>(daySet);
        dates.sort(String::compareTo);

        return ResponseEntity.ok(Map.of(
                "batchCode", batchCode,
                "from", fromDate.toString(),
                "to", toDate.toString(),
                "dates", dates,
                "rows", users
        ));
    }

    private LocalDate parseDateOrToday(String date) {
        if (date == null || date.isBlank()) return LocalDate.now();
        try {
            return LocalDate.parse(date);
        } catch (Exception ignored) {
            return LocalDate.now();
        }
    }
}
