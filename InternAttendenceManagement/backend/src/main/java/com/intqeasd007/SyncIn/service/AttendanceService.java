package com.intqeasd007.SyncIn.service;

import com.intqeasd007.SyncIn.entity.*;
import com.intqeasd007.SyncIn.repository.AttendanceRepository;
import com.intqeasd007.SyncIn.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

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

        long totalDays = DateUtil.countWorkingDays(from, to);
        long effectiveDays = rows.stream()
                .filter(a -> DateUtil.isWorkingDay(a.getDate()))
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

    public void upsertAttendance(User user, LocalDate date, AttendanceStatus status) {
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
}

