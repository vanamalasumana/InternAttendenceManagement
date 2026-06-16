package com.intqeasd007.SyncIn.service;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DateUtil {

    private DateUtil() {}

    public static LocalDate parseDateOrToday(String dateText) {
        if (dateText == null || dateText.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(dateText.trim());
        } catch (Exception ignored) {
            return LocalDate.now();
        }
    }

    public static long countWorkingDays(LocalDate from, LocalDate to) {
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

    public static boolean isWorkingDay(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }
}

