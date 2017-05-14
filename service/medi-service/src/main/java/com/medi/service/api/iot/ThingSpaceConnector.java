package com.medi.service.api.iot;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.medi.service.api.data.TsDataPoint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Created by nanxiao on 5/13/17.
 */
public class ThingSpaceConnector {
    private final String url = "https://thingspace.io/get/dweets/for";
    private final int proxThreshold = 200;
    private final int timeThreshold = 10;
    private DateTimeFormatter dtFmt;

    public ThingSpaceConnector(){
        dtFmt = DateTimeFormatter.ISO_INSTANT;
    }

    private TreeMap<Long, TsDataPoint> pullAll(String boxId){
        Client client = ClientBuilder.newClient();
        WebTarget mgRoot = client.target(url);

        String resp = mgRoot
                .path("/" + boxId)
                .request()
                .buildGet()
                .invoke(String.class);

        System.out.println(resp);

        JSONObject jsonObj = new JSONObject(resp);

        TreeMap<Long, TsDataPoint> dataPoints = Maps.newTreeMap();

        JSONArray events = jsonObj.getJSONArray("with");
        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.getJSONObject(i);
            String bId = event.getString("thing");
            String strTimestamp = event.getString("created");
            LocalDateTime timestamp = LocalDateTime.ofInstant(Instant.from(dtFmt.parse(strTimestamp)), ZoneId.of("-05:00"));
            JSONObject content = event.getJSONObject("content");
            Integer generation = content.getJSONObject("globals").getInt("dweet-generation");
            Integer proximity;
            try{
                proximity = content.getJSONObject("PmodB:TMD3782").getInt("proximity");
            }catch (JSONException e){
                proximity = 0;
            }

            TsDataPoint tdp = new TsDataPoint();
            tdp.setBoxId(bId);
            tdp.setRev(generation);
            tdp.setProximity(proximity);
            tdp.setTimestamp(timestamp);

            dataPoints.put(tdp.getTimestamp().toEpochSecond(ZoneOffset.UTC), tdp);
        }

        return dataPoints;
    }

    public List<TsDataPoint> getValidDataPoints(String boxId){
        List<TsDataPoint> dps = pullAll(boxId).values().stream().filter(dp -> dp.getProximity() < proxThreshold).collect(Collectors.toList());
        List<TsDataPoint> validDps = removeNoises(dps);
        return validDps;
    }

    public List<TsDataPoint> removeNoises(List<TsDataPoint> orgList){
        LocalDateTime prevTime = LocalDateTime.MIN;
        List<TsDataPoint> dps = Lists.newArrayList();
        for (TsDataPoint dp : orgList) {
            long sec = prevTime.until( dp.getTimestamp(), ChronoUnit.SECONDS);
            if(sec > timeThreshold){
                dps.add(dp);
            }
            prevTime = dp.getTimestamp();
        }
        return dps;
    }

    public static void main(String[] args){
        ThingSpaceConnector s  = new ThingSpaceConnector();
        List<TsDataPoint> tsDataPoints = s.getValidDataPoints("SKS7-a4c1");

        tsDataPoints.forEach(d -> {
            System.out.println(d);
        });
    }
}
