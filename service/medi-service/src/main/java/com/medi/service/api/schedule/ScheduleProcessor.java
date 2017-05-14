package com.medi.service.api.schedule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.medi.service.api.data.ScheduleData;
import com.medi.service.api.util.RedisConnector;
import com.medi.service.api.util.RedisException;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Tuple;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Created by nanxiao on 5/13/17.
 */
@Slf4j
public class ScheduleProcessor {
    private final static int SCHEDULE_INFO_DB_INDEX = 2;
    private final static int SCHEDULE_QUEUE_DB_INDEX = 3;
    private final static String QUEUE_KEY_SPACE = "queue";

    private static final long CHECK_INTERVAL = 5000; //5 secs
    public static final int MILLI_SECS_PER_DAY = 86400000;

    private final RedisConnector redis;
    private final Gson gson;

    @Inject
    public ScheduleProcessor(final RedisConnector redis) throws Exception {
        this.redis = redis;
        this.gson = new GsonBuilder().create();
        try {
            redis.connect();
        } catch (RedisException e) {
            throw new Exception("Unable to connect to redis. Reason: " + e.getMessage(), e);
        }
    }

    public void updateSchedule(String boxId, String slotId, LocalDateTime scheduledTime, Integer freq){
        ScheduleData data = new ScheduleData();
        data.setBoxId(boxId);
        data.setSlotId(slotId);
        data.setFrequency(freq);

        long timestamp = scheduledTime.toEpochSecond(ZoneOffset.UTC);
        redis.setDbIndex(SCHEDULE_QUEUE_DB_INDEX);
        try {
            redis.zadd(QUEUE_KEY_SPACE, gson.toJson(data), timestamp);
        } catch (RedisException e) {
            log.error("Failed adding schedule: {}", e.getMessage(), e);
        }
    }


}
