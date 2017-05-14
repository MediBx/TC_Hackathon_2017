package com.medi.service.api.iot;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.medi.service.api.data.TsDataPoint;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nanxiao on 5/13/17.
 */
@Slf4j
public class SensorDataConnector {
    //private final RedisConnector redis;
    private final ThingSpaceConnector dataStreamer;

    private Gson gson;

    @Inject
    public SensorDataConnector(final ThingSpaceConnector dataStreamer) throws Exception {
        //this.redis = redis;
        this.dataStreamer = dataStreamer;
        gson = new GsonBuilder().create();
        /*try {
            redis.connect();
        } catch (RedisException e) {
            throw new Exception("Unable to connect to redis. Reason: " + e.getMessage(), e);
        }*/
    }

    public void storeSensorData(String boxId){
        // pull sensor data from ThingSpace and store to Redis
        List<TsDataPoint> dps = dataStreamer.getValidDataPoints(boxId);
        /*dps.forEach(dp -> {
            try {
                redis.zadd(boxId, gson.toJson(dp), dp.getTimestamp().toEpochSecond(ZoneOffset.UTC));
            } catch (RedisException e) {
                log.warn("Failed storing data points to Redis: {}", e.getMessage(), e);
            }
        });*/
    }

    public HashMap<LocalDate, TsDataPoint> getDataPointsDateMap(String boxId){
        HashMap<LocalDate, TsDataPoint> dmap = Maps.newHashMap();
        dataStreamer.getValidDataPoints(boxId).forEach(dp -> dmap.put(dp.getTimestamp().toLocalDate(), dp));
        return dmap;
    }
}
