package com.craterzone.demo.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.craterzone.demo.model.PushNotificationRequest;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.Notification.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Service
public class FCMService{
	
	private Logger logger = LoggerFactory.getLogger(FCMService.class);
	
	//convert data into json(coming from request) and send message with data
    public void sendMessage(Map<String,String> data,PushNotificationRequest request) throws InterruptedException,ExecutionException{
    	Message message = getPreconfiguredMessageWithData(data,request);
    	Gson gson = new GsonBuilder().setPrettyPrinting().create();
    	String jsonOutput = gson.toJson(message);
    	String response = sendAndGetResponse(message);
    	logger.info("Sent message with data. Topic:"+request.getTopic()+" , "+response+" msg "+jsonOutput);
    }
    
    //send message without data
    public void sendMessageWithoutData(PushNotificationRequest request) throws InterruptedException,ExecutionException{
    	Message message = getPreconfiguredMessageWithoutData(request);
    	String response = sendAndGetResponse(message);
    	logger.info("Sent message without data. Topics:"+request.getTopic()+" , "+response);
    }
    
    //send mesage to token
    public void sendMessageToToken(PushNotificationRequest request) throws InterruptedException,ExecutionException {
    	Message message = getPreconfiguredMessageToToken(request);
    	Gson gson = new GsonBuilder().setPrettyPrinting().create();
    	String jsonOutput = gson.toJson(message);
    	String response = sendAndGetResponse(message);
    	logger.info("Sent Message to Token. Device Token:"+request.getToken()+","+response+" msg "+jsonOutput);;
    }
    
    //send message and get response
    private String sendAndGetResponse(Message message) throws InterruptedException,ExecutionException{
    	return FirebaseMessaging.getInstance().sendAsync(message).get();
    }
    
    //get android configuration
    private AndroidConfig getAndroidConfig(String topic) {
        return AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(2).toMillis()).setCollapseKey(topic)
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder().setSound(NotificationParameter.SOUND.getValue())
                        .setColor(NotificationParameter.COLOR.getValue()).setTag(topic).build()).build();
    }
    //get APNs configuration
    private ApnsConfig getApnsConfig(String topic) {
        return ApnsConfig.builder()
                .setAps(Aps.builder().setCategory(topic).setThreadId(topic).build()).build();
    }
    
    private Message getPreconfiguredMessageToToken(PushNotificationRequest request) {
    	return getPreconfiguredMessageBuilder(request).setToken(request.getToken()).build();
    }
    
    private Message getPreconfiguredMessageWithoutData(PushNotificationRequest request) {
    	return getPreconfiguredMessageBuilder(request).setTopic(request.getTopic()).build();
    }
    
    private Message getPreconfiguredMessageWithData(Map<String,String> data,PushNotificationRequest request) {
    	return getPreconfiguredMessageBuilder(request).putAllData(data).setToken(request.getToken()).build();
    }
    
    private Message.Builder getPreconfiguredMessageBuilder(PushNotificationRequest request){
    	AndroidConfig androidConfig = getAndroidConfig(request.getTopic());
    	ApnsConfig apnsConfig = getApnsConfig(request.getTopic());
    	Builder builder = Notification.builder();
    	builder.setTitle(request.getTitle());
    	builder.setBody(request.getMessage());
    	builder.setImage(null);
    	return Message.builder()
    			.setAndroidConfig(androidConfig)
    			.setApnsConfig(apnsConfig)
    			.setNotification(builder.build());
    }
}