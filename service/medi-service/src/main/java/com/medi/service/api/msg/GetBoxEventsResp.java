package com.medi.service.api.msg;

import com.medi.service.api.data.BoxEvent;
import lombok.Data;

import java.util.List;

/**
 * Created by nanxiao on 5/13/17.
 */
@Data
public class GetBoxEventsResp {
    List<BoxEvent> events;
}
