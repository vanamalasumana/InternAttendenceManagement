package com.intqeasd007.SyncIn.controller;

import com.intqeasd007.SyncIn.dto.CreateCohortRequest;
import com.intqeasd007.SyncIn.dto.OnboardInternRequest;
import com.intqeasd007.SyncIn.entity.Attendance;
import com.intqeasd007.SyncIn.entity.Cohort;
import com.intqeasd007.SyncIn.entity.Role;
import com.intqeasd007.SyncIn.entity.User;
import com.intqeasd007.SyncIn.repository.AttendanceRepository;
import com.intqeasd007.SyncIn.repository.CohortRepository;
import com.intqeasd007.SyncIn.repository.UserRepository;
import com.intqeasd007.SyncIn.service.LeaveManagementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/poc")
public class POCController {

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveManagementService leaveManagementService;

    // ── helper: assert POC role ──
    private String requirePoc(HttpServletRequest req) {
        String role = (String) req.getAttribute("role");
        if (!"POC".equals(role)) return null;
        return (String) req.getAttribute("empId");
    }

    // ───────────── COHORT CRUD ─────────────

    @PostMapping("/cohorts")
    public ResponseEntity<?> createCohort(HttpServletRequest req,
                                          @RequestBody CreateCohortRequest body) {
        String pocEmpId = requirePoc(req);
        if (pocEmpId == null) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        if (cohortRepository.existsById(body.getBatchCode())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Batch code already exists"));
        }

        Cohort cohort = new Cohort();
        cohort.setBatchCode(body.getBatchCode());
        cohort.setTrackName(body.getTrackName());
        cohort.setPocEmpId(pocEmpId);

        Cohort saved = cohortRepository.save(cohort);
        return ResponseEntity.ok(Map.of(
                "batchCode", saved.getBatchCode(),
                "trackName", saved.getTrackName()
        ));
    }

    @GetMapping("/cohorts")
    public ResponseEntity<?> getMyCohorts(HttpServletRequest req) {
        String pocEmpId = requirePoc(req);
        if (pocEmpId == null) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        List<Cohort> cohorts = cohortRepository.findByPocEmpId(pocEmpId);
        return ResponseEntity.ok(cohorts);
    }

    // ───────────── ONBOARD INTERN ─────────────

    @PostMapping("/interns")
    public ResponseEntity<?> onboardIntern(HttpServletRequest req,
                                           @RequestBody OnboardInternRequest body) {
        String pocEmpId = requirePoc(req);
        if (pocEmpId == null) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        // Verify cohort belongs to this POC
        Cohort cohort = cohortRepository.findById(body.getBatchCode()).orElse(null);
        if (cohort == null || !cohort.getPocEmpId().equals(pocEmpId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid cohort"));
        }

        // Generate random 8-char temp password
        String plainTextPassword = generateTempPassword(8);

        // *** CRITICAL: Log the unhashed password for developer testing ***
        log.info("GENERATED TEMP PASSWORD FOR {}: {}", body.getEmail(), plainTextPassword);

        User user = new User();
        user.setEmpId(body.getEmpId());
        user.setName(body.getName());
        user.setEmail(body.getEmail());
        user.setMobileNo(body.getMobileNo());
        user.setPasswordHash(plainTextPassword); // stored as plain text (Sprint 1 style)
        user.setRole(Role.INTERN);
        user.setFirstLogin(true);
        user.setCohort(cohort);

        User saved = userRepository.save(user);
        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("empId", saved.getEmpId());
        resp.put("name", saved.getName());
        resp.put("role", saved.getRole().name());
        resp.put("batchCode", cohort.getBatchCode());
        resp.put("tempPassword", plainTextPassword);
        return ResponseEntity.ok(resp);
    }

    // ───────────── GET INTERNS BY COHORT ─────────────

    @GetMapping("/cohorts/{batchCode}/interns")
    public ResponseEntity<?> getInternsByCohort(HttpServletRequest req,
                                                @PathVariable String batchCode) {
        String pocEmpId = requirePoc(req);
        if (pocEmpId == null) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        Cohort cohort = cohortRepository.findById(batchCode).orElse(null);
        if (cohort == null || !cohort.getPocEmpId().equals(pocEmpId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid cohort"));
        }

        List<User> interns = userRepository.findByCohort_BatchCode(batchCode);
        java.util.List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (User u : interns) {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("empId", u.getEmpId());
            m.put("name", u.getName());
            m.put("email", u.getEmail());
            m.put("mobileNo", u.getMobileNo());
            m.put("role", u.getRole().name());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    // ───────────── PROMOTE INTERN → CR ─────────────

    @PutMapping("/interns/{empId}/promote")
    public ResponseEntity<?> promoteInternToCR(HttpServletRequest req,
                                               @PathVariable String empId) {
        String pocEmpId = requirePoc(req);
        if (pocEmpId == null) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        User intern = userRepository.findById(empId).orElse(null);
        if (intern == null || intern.getCohort() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        Cohort cohort = intern.getCohort();
        if (!cohort.getPocEmpId().equals(pocEmpId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        if (intern.getRole() == Role.CR) {
            return ResponseEntity.badRequest().body(Map.of("error", "User is already a CR"));
        }

        long crCount = userRepository.countByCohort_BatchCodeAndRole(cohort.getBatchCode(), Role.CR);
        if (crCount >= 2) {
            return ResponseEntity.badRequest().body(Map.of("error", "Maximum 2 CRs allowed per cohort"));
        }

        intern.setRole(Role.CR);
        userRepository.save(intern);

        return ResponseEntity.ok(Map.of(
                "empId", intern.getEmpId(),
                "name", intern.getName(),
                "role", "CR"
        ));
    }

    // ───────────── DEMOTE CR → INTERN ─────────────

    @PutMapping("/interns/{empId}/demote")
    public ResponseEntity<?> demoteCRToIntern(HttpServletRequest req,
                                              @PathVariable String empId) {
        String pocEmpId = requirePoc(req);
        if (pocEmpId == null) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        User user = userRepository.findById(empId).orElse(null);
        if (user == null || user.getCohort() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        Cohort cohort = user.getCohort();
        if (!cohort.getPocEmpId().equals(pocEmpId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        if (user.getRole() != Role.CR) {
            return ResponseEntity.badRequest().body(Map.of("error", "User is not a CR"));
        }

        user.setRole(Role.INTERN);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "empId", user.getEmpId(),
                "name", user.getName(),
                "role", "INTERN"
        ));
    }

    @GetMapping("/leaves/pending")
    public ResponseEntity<?> pendingLeaves(HttpServletRequest req) {
        String pocEmpId = requirePoc(req);
        if (pocEmpId == null) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        return ResponseEntity.ok(leaveManagementService.getPendingLeavesForPoc(pocEmpId));
    }

    @PutMapping("/leaves/{leaveId}/approve")
    public ResponseEntity<?> approveLeave(HttpServletRequest req,
                                          @PathVariable Long leaveId) {
        return handleLeaveAction(req, leaveId, "APPROVE");
    }

    @PutMapping("/leaves/{leaveId}/reject")
    public ResponseEntity<?> rejectLeave(HttpServletRequest req,
                                         @PathVariable Long leaveId) {
        return handleLeaveAction(req, leaveId, "REJECT");
    }

    @PutMapping("/leaves/{leaveId}/mark-ul")
    public ResponseEntity<?> markUl(HttpServletRequest req,
                                    @PathVariable Long leaveId) {
        return handleLeaveAction(req, leaveId, "MARK_UL");
    }

    private ResponseEntity<?> handleLeaveAction(HttpServletRequest req, Long leaveId, String action) {
        String pocEmpId = requirePoc(req);
        if (pocEmpId == null) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        Map<String, Object> out = leaveManagementService.handlePocAction(pocEmpId, leaveId, action);
        if (out.containsKey("error")) {
            if ("Forbidden".equals(out.get("error"))) {
                return ResponseEntity.status(403).body(out);
            }
            return ResponseEntity.badRequest().body(out);
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/cohorts/{batchCode}/summary")
    public ResponseEntity<?> cohortSummary(HttpServletRequest req,
                                           @PathVariable String batchCode,
                                           @RequestParam(defaultValue = "daily") String range,
                                           @RequestParam(required = false) String date) {
        String pocEmpId = requirePoc(req);
        if (pocEmpId == null) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        Cohort cohort = cohortRepository.findById(batchCode).orElse(null);
        if (cohort == null || !pocEmpId.equals(cohort.getPocEmpId())) {
            return ResponseEntity.status(404).body(Map.of("error", "Cohort not found"));
        }

        LocalDate today = LocalDate.now();
        LocalDate anchor = parseDateOrToday(date);
        LocalDate from = anchor;
        LocalDate to = anchor;
        if ("weekly".equalsIgnoreCase(range)) {
            from = anchor.with(java.time.DayOfWeek.MONDAY);
            to = from.plusDays(6);
        } else if ("monthly".equalsIgnoreCase(range)) {
            from = anchor.with(TemporalAdjusters.firstDayOfMonth());
            to = anchor.with(TemporalAdjusters.lastDayOfMonth());
        }
        // Never calculate beyond today
        if (to.isAfter(today)) {
            to = today;
        }

        List<User> members = userRepository.findByCohort_BatchCode(batchCode);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (User member : members) {
            Map<String, Object> health = leaveManagementService.calculateHealth(member.getEmpId(), from, to);
            Map<String, Object> row = new HashMap<>();
            row.put("empId", member.getEmpId());
            row.put("name", member.getName());
            row.put("role", member.getRole().name());
            row.put("attendancePercentage", health.get("attendancePercentage"));
            row.put("bucket", health.get("bucket"));
            rows.add(row);
        }

        return ResponseEntity.ok(Map.of(
                "batchCode", batchCode,
                "range", range.toLowerCase(),
                "from", from.toString(),
                "to", to.toString(),
                "rows", rows
        ));
    }

    @GetMapping("/cohorts/{batchCode}/attendance-sheet")
    public ResponseEntity<?> attendanceSheet(HttpServletRequest req,
                                             @PathVariable String batchCode,
                                             @RequestParam(required = false) String from,
                                             @RequestParam(required = false) String to) {
        String pocEmpId = requirePoc(req);
        if (pocEmpId == null) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        Cohort cohort = cohortRepository.findById(batchCode).orElse(null);
        if (cohort == null || !pocEmpId.equals(cohort.getPocEmpId())) {
            return ResponseEntity.status(404).body(Map.of("error", "Cohort not found"));
        }

        LocalDate fromDate = from == null || from.isBlank() ? LocalDate.now().minusDays(6) : LocalDate.parse(from);
        LocalDate toDate = to == null || to.isBlank() ? LocalDate.now() : LocalDate.parse(to);

        List<Attendance> records = attendanceRepository.findByUser_Cohort_BatchCodeAndDateBetween(batchCode, fromDate, toDate);
        Map<String, Map<String, String>> matrix = new HashMap<>();
        for (Attendance record : records) {
            matrix.computeIfAbsent(record.getUser().getEmpId(), k -> new HashMap<>())
                    .put(record.getDate().toString(), record.getStatus().name());
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (User user : userRepository.findByCohort_BatchCode(batchCode)) {
            rows.add(Map.of(
                    "empId", user.getEmpId(),
                    "name", user.getName(),
                    "role", user.getRole().name(),
                    "days", matrix.getOrDefault(user.getEmpId(), Map.of())
            ));
        }

        Set<String> daySet = new HashSet<>();
        for (Map<String, String> map : matrix.values()) {
            daySet.addAll(map.keySet());
        }
        List<String> dates = new ArrayList<>(daySet);
        dates.sort(String::compareTo);

        return ResponseEntity.ok(Map.of(
                "batchCode", batchCode,
                "from", fromDate.toString(),
                "to", toDate.toString(),
                "dates", dates,
                "rows", rows
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

    // ── password generator ──
    private String generateTempPassword(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}

