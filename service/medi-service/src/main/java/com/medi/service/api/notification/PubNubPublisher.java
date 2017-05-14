package com.medi.service.api.notification;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

/**
 * Created by nanxiao on 5/14/17.
 */
@Slf4j
public class PubNubPublisher {
    private final String PUB_KEY = "pub-c-8f2eb9b1-1023-4426-945b-c65d6fb4c932";
    private final String SUB_KEY = "sub-c-b229d046-3805-11e7-a58b-02ee2ddab7fe";
    private PubNub pubnub;

    public PubNubPublisher(){
        init();
    }

    public void init(){
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setPublishKey(PUB_KEY);
        pnConfiguration.setSubscribeKey(SUB_KEY);
        pnConfiguration.setSecure(true);

        pubnub = new PubNub(pnConfiguration);
    }

    public void publish(String channel, String msg) throws Exception{
        log.info("Sending to pubnub: channel={}, msg={}", channel, msg);
        JSONObject meta = new JSONObject();
        this.pubnub.publish().message(msg).channel(channel).shouldStore(true)
                .usePOST(true)
                /*.async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        if (status.isError()) {
                            // something bad happened.
                            System.out.println("error happened while publishing: " + status.toString());
                        } else {
                            System.out.println("publish worked! timetoken: " + result.getTimetoken());
                        }
                    }
                });*/
                .sync();
        log.trace("published");
    }
}
