package com.monetique.PinSenderV0.security.services;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class InfobipSmsService {

    @Value("${infobip.apiKey}")
    private String apiKey;

    @Value("${infobip.baseUrl}")
    private String baseUrl;

    private final OkHttpClient client;

    public InfobipSmsService() {
        this.client = new OkHttpClient().newBuilder().build();
    }

    public void sendSms(String phoneNumber, String message) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, createJsonPayload(phoneNumber, message));
        Request request = new Request.Builder()
                .url(baseUrl + "/sms/2/text/advanced")
                .method("POST", body)
                .addHeader("Authorization", "App " + apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        System.out.println(response.body().string());
    }

    private String createJsonPayload(String phoneNumber, String message) {
        return "{\"messages\":[{\"destinations\":[{\"to\":\"" + phoneNumber + "\"}],\"from\":\"ServiceSMS\",\"text\":\"" + message + "\"}]}";
    }
}