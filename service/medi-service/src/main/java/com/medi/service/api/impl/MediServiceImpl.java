package com.medi.service.api.impl;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.medi.service.api.data.BoxEvent;
import com.medi.service.api.data.TsDataPoint;
import com.medi.service.api.iot.SensorDataConnector;
import com.medi.service.api.msg.GetBoxEventsReq;
import com.medi.service.api.msg.GetBoxEventsResp;
import com.medi.service.api.msg.RequestWrap;
import com.medi.service.api.msg.ResponseWrap;
import com.medi.service.api.util.DateRange;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class MediServiceImpl {
    private final DatabaseConnector db;
    private final SensorDataConnector sensorData;
    private final Gson gson;

    @Inject
    public MediServiceImpl(final DatabaseConnector db, final SensorDataConnector sensorData) {
        this.gson = new GsonBuilder().create();

        this.db = db;
        this.sensorData = sensorData;
    }

    public ResponseWrap<String> userLogin(RequestWrap<String> requestWrap) {
        return process(requestWrap,
                String.class,
                "login failed",
                req -> {
                    // Validate email
                    if (false) {
                        return "Missing email.";
                    }
                    return null;
                },
                req -> {
                    //UserLoginResp ret = loginProcessor.processUserLogin(req);
                    return "test";
                });
    }

    public ResponseWrap<GetBoxEventsResp> getBoxEvents(RequestWrap<String> requestWrap) {
        return process(requestWrap,
                GetBoxEventsReq.class,
                "Failed to get box events",
                req -> {
                    if (req.getBoxId() == null) {
                        return "Missing box ID.";
                    }
                    if(req.getSlotId() == null){
                        return "Missing slot ID.";
                    }
                    return null;
                },
                req -> {
                    GetBoxEventsResp resp = new GetBoxEventsResp();

                    LocalDate startDate = req.getStartDate();
                    LocalDate endDate = req.getEndDate();
                    if(req.getEndDate() == null){
                        endDate = LocalDate.now();
                    }
                    if(req.getStartDate() == null){
                        startDate = endDate.minusMonths(1);
                    }

                    HashMap<LocalDate, TsDataPoint> eventsMap = sensorData.getDataPointsDateMap(req.getBoxId());
                    List<BoxEvent> events = Lists.newArrayList();
                    for (LocalDate d : DateRange.between(startDate, endDate)){
                        BoxEvent event = new BoxEvent();
                        event.setDate(d);
                        if(eventsMap.containsKey(d)){
                            event.setTaken(true);
                        }
                        events.add(event);
                    }

                    resp.setEvents(events);
                    return resp;
                });
    }

    // Helpers

    private <I, O> ResponseWrap<O> process(RequestWrap<String> requestWrap,
                                           Class<I> requestType,
                                           String error,
                                           Function<I, String> validateFn,
                                           Function<I, O> processRequestFn) {
        ResponseWrap<O> resp = new ResponseWrap<>();
        String uri = requestWrap.getUri();
        I req;
        // Parse request
        try {
            req = gson.fromJson(requestWrap.getRequest(), requestType);
        } catch (Exception e) {
            resp.setError(error);
            return resp;
        }
        if (req == null) {
            resp.setError("Invalid empty request.");
            return resp;
        }
        // Validate request
        try {
            String err = validateFn.apply(req);
            if (err != null && !err.isEmpty()) {
                resp.setError(err);
                return resp;
            }
        } catch (Exception e) {
            resp.setError("Request validation error: " + e.getMessage());
            return resp;
        }
        // Process request
        try {
            O ret = processRequestFn.apply(req);
            resp.setResponse(ret);
        } catch (Exception e) {
            String err = "Failed to process: " + e.getMessage();
            log.error(error, e);
            resp.setError(err);
        }
        return resp;
    }

    @FunctionalInterface
    public interface Consumer<T> {
        void accept(T t) throws Exception;
    }

    @FunctionalInterface
    public interface Function<T, R> {
        R apply(T t) throws Exception;
    }
}
