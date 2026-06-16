package com.intqeasd007.SyncIn.controller;

import com.intqeasd007.SyncIn.dto.CreateUserRequest;
import com.intqeasd007.SyncIn.entity.Role;
import com.intqeasd007.SyncIn.entity.User;
import com.intqeasd007.SyncIn.repository.UserRepository;
import com.intqeasd007.SyncIn.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.intqeasd007.SyncIn.entity.Cohort;
import com.intqeasd007.SyncIn.repository.CohortRepository;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final String ADMIN_USERNAME = "sudo";
    private static final String ADMIN_PASSWORD = "racingcar123";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CohortRepository cohortRepository;

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            String token = jwtUtil.generateToken("ADMIN", "ADMIN");
            return ResponseEntity.ok(Map.of("token", token));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid admin credentials"));
    }

    @PostMapping("/create-poc")
    public ResponseEntity<?> createPoc(
            HttpServletRequest httpRequest,
            @RequestBody CreateUserRequest request) {

        String role = (String) httpRequest.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        User user = new User();
        user.setEmpId(request.getEmpId());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setMobileNo(request.getMobileNo());
        user.setPasswordHash(request.getPasswordHash());
        user.setRole(Role.POC);
        user.setFirstLogin(true);

        User saved = userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "empId", saved.getEmpId(),
                "name", saved.getName(),
                "role", saved.getRole().name()
        ));
    }


    @GetMapping("/pocs")
    public List<User> getAllPocs() {
        // This calls the method you already defined in your UserRepository
        // Make sure 'Role.POC' matches exactly how it's defined in your Role enum
        return userRepository.findByRole(Role.POC);
    }
//    @GetMapping("/pocs")
//    public ResponseEntity<?> listPocs(HttpServletRequest httpRequest) {
//        String role = (String) httpRequest.getAttribute("role");
//        if (!"ADMIN".equals(role)) {
//            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
//        }
//
//        List<User> pocs = userRepository.findByRole(com.intqeasd007.SyncIn.entity.Role.POC);
//        List<Object> out = new ArrayList<>();
//        for (User p : pocs) {
//            List<Cohort> cohorts = cohortRepository.findByPocEmpId(p.getEmpId());
//            List<String> codes = new ArrayList<>();
//            for (Cohort c : cohorts) codes.add(c.getBatchCode());
//            out.add(Map.of(
//                    "empId", p.getEmpId(),
//                    "name", p.getName(),
//                    "email", p.getEmail(),
//                    "mobileNo", p.getMobileNo(),
//                    "cohorts", codes
//            ));
//        }
//        return ResponseEntity.ok(out);
//    }

    // Small ping endpoint to verify admin controller mapping during debugging
    @GetMapping("/_ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(Map.of("ok", "admin"));
    }
}
