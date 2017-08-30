package keti.com.mobiusytsampleapp;

/**
 * Created by araha on 2016-09-20.
 */
public class ApplicationEntityObject {
    private String resource_name="";

    public void setResourceName(String resourceName){
        this.resource_name = resourceName;
    }

    public String makeXML(){

        String xml ="";
        xml +="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        xml +="<m2m:ae ";
        xml +="xmlns:m2m=\"http://www.onem2m.org/xml/protocols\" ";
        xml +="xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" rn=\""+ resource_name +"\">";
        xml +="<api>0.2.481.2.0001.001.0001114</api>";
        xml +="<rr>true</rr>";
        xml +="</m2m:ae>";

        return xml;
    }
}
