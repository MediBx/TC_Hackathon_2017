package com.medi.service.api.data;

import lombok.Data;

/**
 * Created by nanxiao on 5/13/17.
 */
@Data
public class ScheduleData {
    String boxId;
    String slotId;
    Integer frequency = 0;
}
