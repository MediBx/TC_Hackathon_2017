package com.medi.service.api.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nanxiao on 5/13/17.
 */
public class DateRange {
    public static List<LocalDate> between(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            dates.add(d);
        }
        return dates;
    }
}
