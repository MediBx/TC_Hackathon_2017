package com.medi.service.api.data;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by nanxiao on 5/13/17.
 */
@Data
public class TsDataPoint {
    String boxId;
    Integer rev;
    LocalDateTime timestamp;
    Integer proximity;
}
