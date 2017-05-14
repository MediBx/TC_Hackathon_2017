package com.medi.service.api.notification;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.medi.service.api.data.DataAccess;
import com.medi.service.api.data.ScheduleData;
import com.medi.service.api.data.SlotData;
import com.medi.service.api.util.RedisConnector;
import com.medi.service.api.util.RedisException;
import com.sun.xml.internal.ws.api.message.ExceptionHasMessage;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Tuple;

/**
 * Created by nanxiao on 5/14/17.
 */
@Slf4j
public class NotificationService  extends AbstractExecutionThreadService {
    private final static int SCHEDULE_QUEUE_DB_INDEX = 3;
    private final static String KEYSPACE = "queue";

    private static final long CHECK_INTERVAL = 5000; //5 secs
    public static final int MILLI_SECS_PER_DAY = 86400000;

    private final RedisConnector redis;
    private final Gson gson;
    private PubNubPublisher pub;
    private DataAccess dataAccess;

    @Inject
    public NotificationService(final RedisConnector redis) throws Exception {
        this.redis = redis;
        this.gson = new GsonBuilder().create();
        try {
            redis.connect();
        } catch (RedisException e) {
            throw new Exception("Unable to connect to redis. Reason: " + e.getMessage(), e);
        }
        this.pub = new PubNubPublisher();
        this.dataAccess = new DataAccess(redis);
    }

    @Override
    protected void run() throws Exception {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        log.debug(Thread.currentThread().getName() + " starts running");
        while (isRunning()) {
            process();
        }
    }

    private void process() {
        try {
            // If nothing in notification queue, check periodically
            try {
                if (redis.zcard(KEYSPACE) < 1) {
                    log.trace("Checking schedule");  // temp
                    sleepFixedInterval();
                    return;
                }
            } catch (RedisException e) {
                onError("Error connecting to Redis: " + e.getMessage(), e);
                return;
            }

            /*
            * When there are scheduled reminder events in the queue,
            * periodically check the head event, until it's triggered.
            * */
            Tuple reminderObject;
            try {
                reminderObject = redis.zpeekWithScore(KEYSPACE);
            } catch (RedisException e) {
                onError("Error connecting to Redis: " + e.getMessage(), e);
                return;
            }

            String scheduleDataStr = reminderObject.getElement();
            ScheduleData scheduleData;
            try {
                scheduleData = gson.fromJson(scheduleDataStr, ScheduleData.class);
            } catch (Exception e) {
                onError("Malformed schedule: " + scheduleDataStr);
                return;
            }
            long scheduledTime = (long) reminderObject.getScore();
            log.trace("Head schedule: data={}, time={}", scheduleData, scheduledTime);

            // Validate scheduled time
            if (scheduledTime < 1) {
                // Invalid scheduled time. Log error and discard.
                discardHeadReminder();
                onError("Error: Invalid scheduled time: " + scheduledTime);
                return;
            }

            /*
            * Check timestamp of the head event.
            * If time is not up for the next event, determine sleep time till the next check.
            * Otherwise, if time is up, trigger the event.
            * */
            if (scheduledTime > System.currentTimeMillis()) {
                /*
                * Time not up for the current next event yet.
                * Sleep for a smaller period in between the next scheduled event and the check interval,
                * so we can handle the case if there's a new event comes in front of the current head.
                * */
                long nextSleep = Math.min((scheduledTime - System.currentTimeMillis()), CHECK_INTERVAL);
                sleep(nextSleep);
                return;
            } else {
                /*
                * Time is up for the first event. Trigger it.
                * */
                // Pop out this event
                discardHeadReminder();
                // Process it
                processReminder(scheduleData, scheduledTime);
            }
        } catch (Exception e) {
            onError("Error processing: " + e.getMessage(), e);
            return;
        }
    }

    private void processReminder(ScheduleData data, long scheduledTime) throws Exception {
        /*
        * Send reminder.
        * If it is recurring and not ended, schedule the following event.
        * */
        log.info("Processing schedule: {}", data);
        sendNotification(data);

        // Schedule next recurring event of the series
        scheduleNextRecurringEvent(data, scheduledTime);
    }

    public void sendNotification(ScheduleData data) throws Exception {
        // Send reminder (PubNub)
        // get medicine info
        SlotData medicineData = dataAccess.getSlotData(data.getSlotId());
        StringBuilder msg = new StringBuilder("Remember to take your medicine: ").append(medicineData.getMedicineName());
        pub.publish(data.getBoxId(), msg.toString());
    }

    private void scheduleNextRecurringEvent(ScheduleData data, long scheduledTime) {
        /*
        if (schedule.getFrequency() > 0) {
            long nextScheduledTime = scheduledTime + (schedule.getFrequency() * MILLI_SECS_PER_DAY);
            if (nextScheduledTime <= (schedule.getEndDateTime().toEpochSecond(ZoneOffset.UTC) * 1000)) {
                // Next event still in range. Schedule it.
                // Insert event to Redis
                try {
                    redis.zadd(KEYSPACE, String.valueOf(scheduleId), nextScheduledTime);
                } catch (RedisException e) {
                    log.error("Failed to schedule the next event of schedule [{}]. Reason: {}", scheduleId, e.getMessage(), e);
                }
            }
        }
        */
    }

    private void discardHeadReminder() throws RedisException {
        redis.zlpop(KEYSPACE);
    }

    private void onError(String err) {
        onError(err, null);
    }

    private void onError(String err, Exception e) {
        if (e == null) {
            log.error(err);
        } else {
            log.error(err, e);
        }
        sleepFixedInterval();
    }

    private void sleepFixedInterval() {
        sleep(CHECK_INTERVAL);
    }

    private void sleep(long interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
            //
        }
    }

    public static void main(String[] args) throws Exception{
        ScheduleData data = new ScheduleData();
        data.setBoxId("SKS7-a4c1");
        data.setSlotId("1");

        RedisConnector redis = new RedisConnector("localhost", 6379, 1);
        NotificationService s = new NotificationService(redis);
        s.sendNotification(data);
    }
}
