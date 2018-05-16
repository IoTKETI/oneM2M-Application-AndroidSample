package kr.re.keti.mobiussampleapp_v25;

/**
 * Created by araha on 2016-09-20.
 */
public class ContentSubscribeObject {

    private String resource_name="";
    private String content_url = "";
    private String subscribe_path ="";
    private String origin_id="";

    public void setUrl(String url) {
        this.content_url = url;
    }
    public void setResourceName(String resourceName){
        this.resource_name = resourceName;
    }
    public void setPath(String subscribePath ){
        this.subscribe_path = subscribePath;
    }

    public void setOrigin_id(String originid){
        this.origin_id = originid;
    }

    public String makeXML(){

        String xml ="";
        xml +="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        xml +="<m2m:sub ";
        xml +="xmlns:m2m=\"http://www.onem2m.org/xml/protocols\" ";
        xml +="xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" rn=\""+ resource_name +"\">";
        xml +="<enc>";
        xml +="<net>3 4</net>";
        xml +="</enc>";
        xml +="<nu>mqtt://"+ content_url +"/"+ subscribe_path +"</nu>";
        xml +="<pn>1</pn>";
        xml +="<nct>2</nct>";
        xml +="<cr>"+origin_id+"</cr>";
        xml +="</m2m:sub>";

        return xml;
    }
}