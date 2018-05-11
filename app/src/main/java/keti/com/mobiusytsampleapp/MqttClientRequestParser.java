package keti.com.mobiusytsampleapp;

import android.util.Log;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
/**
 * Created by araha on 2016-09-13.
 */
public class MqttClientRequestParser {
    private static String TAG = "MqttClientRequestParser";
    // xml parser

    // json parser
    public static String notificationJsonParse(String message) throws Exception {
        JSONObject json = new JSONObject(message);
        String responserqi = json.getString("rqi");

        return responserqi;
    }

}
