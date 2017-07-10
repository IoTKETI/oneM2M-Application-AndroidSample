package keti.com.mobiusytsampleapp;
import java.util.ArrayList;
/**
 * Created by araha on 2016-09-13.
 */
public class MqttClientRequest {
    public static String notificationResponse(ArrayList<String> response) {
        String responseMessage =
                "<m2m:rsp\n" +
                        "xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                        "<rsc>2000</rsc>" +
                        "<to>mobius-yt</to>" +
                        "<fr>nCube:AndroidApp</fr>" +
                        "<rqi>" + response.get(0) + "</rqi>" +
                        "<pc></pc>" +
                        "</m2m:rsp>";

        return responseMessage;
    }
    public static String notificationResponse(String  response) {
        String responseMessage =
                "<m2m:rsp\n" +
                        "xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                        "<rsc>2000</rsc>" +
                        "<to>mobius-yt</to>" +
                        "<fr>nCube:AndroidApp</fr>" +
                        "<rqi>" + response + "</rqi>" +
                        "<pc></pc>" +
                        "</m2m:rsp>";

        return responseMessage;
    }
}
