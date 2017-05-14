package com.medi.service.api.schedule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.medi.service.api.data.ScheduleData;
import com.medi.service.api.util.RedisConnector;
import com.medi.service.api.util.RedisException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Created by nanxiao on 5/13/17.
 */
@Slf4j
public class ScheduleProcessor {
    private final static int SCHEDULE_DB_INDEX = 1;
    private final RedisConnector redis;
    private final Gson gson;

    @Inject
    public ScheduleProcessor(final RedisConnector redis) throws Exception {
        this.redis = redis;
        this.gson = new GsonBuilder().create();
        try {
            redis.connect();
            redis.setDbIndex(SCHEDULE_DB_INDEX);
        } catch (RedisException e) {
            throw new Exception("Unable to connect to redis. Reason: " + e.getMessage(), e);
        }
    }

    public void updateSchedule(String boxId, String slotId, LocalDateTime scheduledTime, Integer freq){
        ScheduleData data = new ScheduleData();
        data.setBoxId(boxId);
        data.setSlotId(slotId);
        data.setFrequency(freq);
    }
}
