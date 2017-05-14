package com.medi.service.api.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.medi.service.api.util.RedisConnector;
import com.medi.service.api.util.RedisException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Created by nanxiao on 5/14/17.
 */
@Slf4j
public class DataAccess {
    private final static int OBJECT_DATA_DB_INDEX = 1;
    private final static String KEY_BOX_DATA = "box_data";
    private final static String KEY_SLOT_DATA = "slot_data";

    private final RedisConnector redis;
    private final Gson gson;

    @Inject
    public DataAccess(final RedisConnector redis) throws Exception {
        this.redis = redis;
        this.gson = new GsonBuilder().create();
        try {
            redis.connect();
        } catch (RedisException e) {
            throw new Exception("Unable to connect to redis. Reason: " + e.getMessage(), e);
        }
    }

    public SlotData getSlotData(String slotId){
        redis.setDbIndex(OBJECT_DATA_DB_INDEX);
        try {
            String strData = redis.hget(KEY_SLOT_DATA, slotId);
            return gson.fromJson(strData, SlotData.class);
        } catch (RedisException e) {
            log.error("failed getting slot data: {}", e.getMessage(), e);
            return null;
        }
    }

    public void setSlotData(String slotId, SlotData data) throws RedisException {
        redis.setDbIndex(OBJECT_DATA_DB_INDEX);
        redis.hset(KEY_SLOT_DATA, slotId, gson.toJson(data));
    }

    public static void main(String[] args) throws Exception {
        RedisConnector redis = new RedisConnector("localhost", 6379, 1);
        DataAccess d = new DataAccess(redis);
        /*
        SlotData data = new SlotData();
        data.setSlotId("1");
        data.setMedicineName("Diabetes Med");
        data.setDescription("100 mg tablet. Once daily.");
        d.setSlotData(data.getSlotId(), data);
        */
        System.out.println(d.getSlotData("1"));
    }
}
