package keti.com.mobiusytsampleapp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 * Created by araha on 2016-09-20.
 */
public class ParseElementXml {
    private String getstr="";

    public String GetElementXml(String xmlParam, String tagName) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xmlParam.getBytes());
            Document doc = docBuilder.parse(is);
            NodeList resultNodes = doc.getElementsByTagName(tagName);

            if (resultNodes.getLength() > 0 && resultNodes.item(0).getChildNodes().getLength() > 0) {
                Element subElement = (Element) resultNodes.item(0);
                Node aeIdNode = subElement.getChildNodes().item(0);
                getstr = aeIdNode.getNodeValue();
            }
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return getstr;
    }
}
