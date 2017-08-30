package keti.com.mobiusytsampleapp;

/**
 * Created by araha on 2016-09-20.
 */
public class ContentInstanceObject {
    private String content = "";

    public void setContent(String contentValue) {
        this.content = contentValue;
    }

    public String makeXML() {
        String xml = "";

        xml += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        xml += "<m2m:cin ";
        xml += "xmlns:m2m=\"http://www.onem2m.org/xml/protocols\" ";
        xml += "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
        xml += "<cnf>text</cnf>";
        xml += "<con>" + content + "</con>";
        xml += "</m2m:cin>";

        return xml;
    }
}
