package com.medi.service.api.data;

import lombok.Data;

import java.time.LocalDate;

/**
 * Created by nanxiao on 5/13/17.
 */
@Data
public class BoxEvent {
    LocalDate date;
    boolean isTaken = false;
}
