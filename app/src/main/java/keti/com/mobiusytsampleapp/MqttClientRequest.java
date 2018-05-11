package keti.com.mobiusytsampleapp;
import java.util.ArrayList;
/**
 * Created by araha on 2016-09-13.
 */
public class MqttClientRequest {

    public static String notificationResponse(String  response) {
        String responseMessage =
                "{\"rsc\":\"2000\",\n" +
                 "\"rqi\":\""+ response + "\",\n" +
                 "\"pc\":\"\"}";

        return responseMessage;
    }
}
