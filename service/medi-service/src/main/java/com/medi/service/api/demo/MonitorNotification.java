package com.medi.service.api.demo;

import com.medi.service.api.notification.PubNubPublisher;

/**
 * Created by nanxiao on 5/14/17.
 */
public class MonitorNotification {
    private PubNubPublisher pub;

    public MonitorNotification(){
        pub = new PubNubPublisher();
    }

    public void send() throws Exception {
        String channel = "mon:SKS7-a4c1";
        String msg = "Patient [Nan Xiao] is taking medicine [Diabetes Med]";
        pub.publish(channel, msg);
    }

    public static void main(String[] args) throws Exception {
        MonitorNotification demo = new MonitorNotification();
        demo.send();
        System.exit(0);
    }
}
