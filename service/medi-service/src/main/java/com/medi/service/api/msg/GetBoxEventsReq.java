package com.medi.service.api.msg;

import lombok.Data;

import java.time.LocalDate;

/**
 * Created by nanxiao on 5/13/17.
 */
@Data
public class GetBoxEventsReq {
    String boxId;
    String slotId;
    LocalDate startDate;
    LocalDate endDate;
}
