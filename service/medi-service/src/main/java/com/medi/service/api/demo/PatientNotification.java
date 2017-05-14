package com.medi.service.api.demo;

import com.medi.service.api.notification.PubNubPublisher;

/**
 * Created by nanxiao on 5/14/17.
 */
public class PatientNotification {
    private PubNubPublisher pub;

    public PatientNotification(){
        pub = new PubNubPublisher();
    }

    public void send() throws Exception {
        String channel = "notf:SKS7-a4c1";
        String msg = "It's time to take your medicine [Diabetes Med]";
        pub.publish(channel, msg);
    }

    public static void main(String[] args) throws Exception {
        PatientNotification demo = new PatientNotification();
        demo.send();
        System.exit(0);
    }
}
