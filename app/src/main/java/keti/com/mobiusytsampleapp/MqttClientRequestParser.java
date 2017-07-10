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
    public static ArrayList<String> notificationParse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource xmlSource = new InputSource();
        xmlSource.setCharacterStream(new StringReader(xml));
        Document document = builder.parse(xmlSource);

        String rqi = "";
        String content = "";
        String subr = "";

        NodeList rqiNodeList = document.getElementsByTagName("rqi");
        if (rqiNodeList.getLength() > 0 && rqiNodeList.item(0).getChildNodes().getLength() > 0) {
            Node rqiNode = rqiNodeList.item(0).getChildNodes().item(0);
            rqi = rqiNode.getNodeValue();
        }

        NodeList conNodeList = document.getElementsByTagName("con");
        if (conNodeList.getLength() > 0 && conNodeList.item(0).getChildNodes().getLength() > 0) {
            Node conNode = conNodeList.item(0).getChildNodes().item(0);
            content = conNode.getNodeValue();
        }
        NodeList surNodeList = document.getElementsByTagName("sur");
        if (surNodeList.getLength() > 0 && surNodeList.item(0).getChildNodes().getLength() > 0) {
            Node surNode = surNodeList.item(0).getChildNodes().item(0);
            subr = surNode.getNodeValue();
        }

        ArrayList<String> returnArray = new ArrayList<String>();
        returnArray.add(rqi);
        returnArray.add(content);
        returnArray.add(subr);

        return returnArray;
    }
    // json parser
    public static String notificationJsonParse(String message) throws Exception {
        JSONObject json = new JSONObject(message.toString());
        JSONObject jsorqp = new JSONObject(json.getString("m2m:rqp"));
        String responserqi = jsorqp.getString("rqi");
        return responserqi;
    }

}
