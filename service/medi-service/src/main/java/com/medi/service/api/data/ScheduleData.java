package com.medi.service.api.data;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by nanxiao on 5/13/17.
 */
@Data
public class ScheduleData {
    String boxId;
    String slotId;
    Integer frequency = 0;
    LocalDateTime scheduleTime;
}
