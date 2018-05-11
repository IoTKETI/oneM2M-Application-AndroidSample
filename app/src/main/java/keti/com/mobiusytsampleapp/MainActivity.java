package keti.com.mobiusytsampleapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener, CompoundButton.OnCheckedChangeListener {
    public Button btnRetrieve;
    public ToggleButton btnControl_Green;
    public ToggleButton btnControl_Blue;
    public Switch Switch_MQTT;
    public TextView textViewData;
    public Handler handler;

    private static CSEBase csebase = new CSEBase();
    private static AE ae = new AE();
    private static String TAG = "MainActivity";
    private String MQTTPort = "1883";
    private String ServiceAEName = "ae-edu1";
    private String MQTT_Req_Topic = "";
    private String MQTT_Resp_Topic = "";
    private MqttAndroidClient mqttClient = null;

    // Main
    public MainActivity() {
        handler = new Handler();
    }
    /* onCreate */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRetrieve = (Button) findViewById(R.id.btnRetrieve);
        Switch_MQTT = (Switch) findViewById(R.id.switch_mqtt);
        btnControl_Green = (ToggleButton) findViewById(R.id.btnControl_Green);
        btnControl_Blue = (ToggleButton) findViewById(R.id.btnControl_Blue);
        textViewData = (TextView) findViewById(R.id.textViewData);

        btnRetrieve.setOnClickListener(this);
        Switch_MQTT.setOnCheckedChangeListener(this);
        btnControl_Green.setOnClickListener(this);
        btnControl_Blue.setOnClickListener(this);
        // Create AE and Get AEID
        GetAEInfo();
    }
    /* AE Create for Androdi AE */
    public void GetAEInfo() {
        csebase.setInfo("192.168.0.135","7579","Mobius","1883");
        //csebase.setInfo("203.253.128.151","7579","Mobius","1883");
        // AE Create for Android AE
        ae.setAppName("ncubeapp");
        aeCreateRequest aeCreate = new aeCreateRequest();
        aeCreate.setReceiver(new IReceived() {
            public void getResponseBody(final String msg) {
                handler.post(new Runnable() {
                    public void run() {
                        Log.d(TAG, "** AE Create ResponseCode[" + msg +"]");
                        if( Integer.parseInt(msg) == 201 ){
                            MQTT_Req_Topic = "/oneM2M/req/Mobius2/"+ae.getAEid()+"_sub"+"/#";
                            MQTT_Resp_Topic = "/oneM2M/resp/Mobius2/"+ae.getAEid()+"_sub"+"/json";
                            Log.d(TAG, "ReqTopic["+ MQTT_Req_Topic+"]");
                            Log.d(TAG, "ResTopic["+ MQTT_Resp_Topic+"]");
                        }
                        else { // If AE is Exist , GET AEID
                            aeRetrieveRequest aeRetrive = new aeRetrieveRequest();
                            aeRetrive.setReceiver(new IReceived() {
                                public void getResponseBody(final String resmsg) {
                                    handler.post(new Runnable() {
                                        public void run() {
                                            Log.d(TAG, "** AE Retrive ResponseCode[" + resmsg +"]");
                                            MQTT_Req_Topic = "/oneM2M/req/Mobius2/"+ae.getAEid()+"_sub"+"/#";
                                            MQTT_Resp_Topic = "/oneM2M/resp/Mobius2/"+ae.getAEid()+"_sub"+"/json";
                                            Log.d(TAG, "ReqTopic["+ MQTT_Req_Topic+"]");
                                            Log.d(TAG, "ResTopic["+ MQTT_Resp_Topic+"]");
                                        }
                                    });
                                }
                            });
                            aeRetrive.start();
                        }
                    }
                });
            }
        });
        aeCreate.start();
    }
    /* Switch - Get Co2 Data With MQTT */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {
            Log.d(TAG, "MQTT Create");
            MQTT_Create(true);
        } else {
            Log.d(TAG, "MQTT Close");
            MQTT_Create(false);
        }
    }
    /* MQTT Subscription */
    public void MQTT_Create(boolean mtqqStart) {
        if (mtqqStart && mqttClient == null) {
            /* Subscription Resource Create to Yellow Turtle */
            SubscribeResource subcribeResource = new SubscribeResource();
            subcribeResource.setReceiver(new IReceived() {
                public void getResponseBody(final String msg) {
                    handler.post(new Runnable() {
                        public void run() {
                            textViewData.setText("**** Subscription Resource Create 요청 결과 ****\r\n\r\n" + msg);
                        }
                    });
                }
            });
            subcribeResource.start();

            /* MQTT Subscribe */
            mqttClient = new MqttAndroidClient(this.getApplicationContext(), "tcp://" + csebase.getHost() + ":" + csebase.getMQTTPort(), MqttClient.generateClientId());
            mqttClient.setCallback(mainMqttCallback);
            try {
                IMqttToken token = mqttClient.connect();
                token.setActionCallback(mainIMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            /* MQTT unSubscribe or Client Close */
            mqttClient.setCallback(null);
            mqttClient.close();
            mqttClient = null;
        }
    }
    /* MQTT Listener */
    private IMqttActionListener mainIMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(TAG, "onSuccess");
            String payload = "";
            int mqttQos = 1; /* 0: NO QoS, 1: No Check , 2: Each Check */

            MqttMessage message = new MqttMessage(payload.getBytes());
            try {
                mqttClient.subscribe(MQTT_Req_Topic, mqttQos);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.d(TAG, "onFailure");
        }
    };
    /* MQTT Broker Message Received */
    private MqttCallback mainMqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.d(TAG, "connectionLost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

            Log.d(TAG, "messageArrived");

            textViewData.setText("");
            textViewData.setText("**** MQTT CO2 실시간 조회 ****\r\n\r\n" + message.toString().replaceAll(",", "\n"));
            Log.d(TAG, "Notify ResMessage:" + message.toString());

            /* Json Type Response Parsing */
            String retrqi = MqttClientRequestParser.notificationJsonParse(message.toString());
            Log.d(TAG, "RQI["+ retrqi +"]");

            String responseMessage = MqttClientRequest.notificationResponse(retrqi);
            Log.d(TAG, "Recv OK ResMessage ["+responseMessage+"]");

            /* Make json for MQTT Response Message */
            MqttMessage res_message = new MqttMessage(responseMessage.getBytes());

            try {
                mqttClient.publish(MQTT_Resp_Topic, res_message);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.d(TAG, "deliveryComplete");
        }

    };
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRetrieve: {
                RetrieveRequest req = new RetrieveRequest();
                textViewData.setText("");
                req.setReceiver(new IReceived() {
                    public void getResponseBody(final String msg) {
                        handler.post(new Runnable() {
                            public void run() {
                                textViewData.setText("************** CO2 조회 *************\r\n\r\n" + msg);
                            }
                        });
                    }
                });
                req.start();
                break;
            }
            case R.id.btnControl_Green: {
                if (((ToggleButton) v).isChecked()) {
                    ControlRequest req = new ControlRequest("1");
                    req.setReceiver(new IReceived() {
                        public void getResponseBody(final String msg) {
                            handler.post(new Runnable() {
                                public void run() {
                                    textViewData.setText("************** LED Green 제어(켜짐) *************\r\n\r\n" + msg);
                                }
                            });
                        }
                    });
                    req.start();
                } else {
                    ControlRequest req = new ControlRequest("2");
                    req.setReceiver(new IReceived() {
                        public void getResponseBody(final String msg) {
                            handler.post(new Runnable() {
                                public void run() {
                                    textViewData.setText("************** LED Green 제어(꺼짐) **************\r\n\r\n" + msg);
                                }
                            });
                        }
                    });
                    req.start();
                }
                break;
            }
            case R.id.btnControl_Blue: {
                if (((ToggleButton) v).isChecked()) {
                    ControlRequest req = new ControlRequest("3");
                    req.setReceiver(new IReceived() {
                        public void getResponseBody(final String msg) {
                            handler.post(new Runnable() {
                                public void run() {
                                    textViewData.setText("************** LED BLUE 제어(켜짐) *************\r\n\r\n" + msg);
                                }
                            });
                        }
                    });
                    req.start();
                } else {
                    ControlRequest req = new ControlRequest("4");
                    req.setReceiver(new IReceived() {
                        public void getResponseBody(final String msg) {
                            handler.post(new Runnable() {
                                public void run() {
                                    textViewData.setText("************** LED BLUE 제어(꺼짐) **************\r\n\r\n" + msg);
                                }
                            });
                        }
                    });
                    req.start();
                }
                break;
            }

        }
    }
    @Override
    public void onStart() {
        super.onStart();

    }
    @Override
    public void onStop() {
        super.onStop();

    }

    /* Response callback Interface */
    public interface IReceived {
        void getResponseBody(String msg);
    }

    /* Retrieve Co2 Sensor */
    class RetrieveRequest extends Thread {
        private final Logger LOG = Logger.getLogger(RetrieveRequest.class.getName());
        private IReceived receiver;
        private String ContainerName = "cnt-co2";

        public RetrieveRequest(String containerName) {
            this.ContainerName = containerName;
        }
        public RetrieveRequest() {}
        public void setReceiver(IReceived hanlder) { this.receiver = hanlder; }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() + "/" + ServiceAEName + "/" + ContainerName + "/" + "latest";

                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid() );
                conn.setRequestProperty("nmtype", "long");
                conn.connect();

                String strResp = "";
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String strLine= "";
                while ((strLine = in.readLine()) != null) {
                    strResp += strLine;
                }

                if ( strResp != "" ) {
                    receiver.getResponseBody(strResp);
                }
                conn.disconnect();

            } catch (Exception exp) {
                LOG.log(Level.WARNING, exp.getMessage());
            }
        }
    }
    /* Request Control LED */
    class ControlRequest extends Thread {
        private final Logger LOG = Logger.getLogger(ControlRequest.class.getName());
        private IReceived receiver;
        private String container_name = "cnt-led";

        public ContentInstanceObject contentinstance;
        public ControlRequest(String comm) {
            contentinstance = new ContentInstanceObject();
            contentinstance.setContent(comm);
        }
        public void setReceiver(IReceived hanlder) { this.receiver = hanlder; }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() +"/" + ServiceAEName + "/" + container_name;

                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml;ty=4");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid() );

                String reqContent = contentinstance.makeXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqContent.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqContent.getBytes());
                dos.flush();
                dos.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String resp = "";
                String strLine="";
                while ((strLine = in.readLine()) != null) {
                    resp += strLine;
                }
                if (resp != "") {
                    receiver.getResponseBody(resp);
                }
                conn.disconnect();

            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }
    /* Request AE Creation */
    class aeCreateRequest extends Thread {
        private final Logger LOG = Logger.getLogger(aeCreateRequest.class.getName());
        String TAG = aeCreateRequest.class.getName();
        private IReceived receiver;
        int responseCode=0;
        public ApplicationEntityObject applicationEntity;
        public void setReceiver(IReceived hanlder) { this.receiver = hanlder; }
        public aeCreateRequest(){
            applicationEntity = new ApplicationEntityObject();
            applicationEntity.setResourceName(ae.getappName());
        }
        @Override
        public void run() {
            try {

                String sb = csebase.getServiceUrl();
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml;ty=2");
                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-Origin", "S"+ae.getappName());
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-NM", ae.getappName() );

                String reqXml = applicationEntity.makeXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqXml.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqXml.getBytes());
                dos.flush();
                dos.close();

                responseCode = conn.getResponseCode();

                BufferedReader in = null;
                String aei = "";
                if (responseCode == 201) {
                    // Get AEID from Response Data
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String resp = "";
                    String strLine;
                    while ((strLine = in.readLine()) != null) {
                        resp += strLine;
                    }

                    ParseElementXml pxml = new ParseElementXml();
                    aei = pxml.GetElementXml(resp, "aei");
                    ae.setAEid( aei );
                    Log.d(TAG, "Create Get AEID[" + aei + "]");
                    in.close();
                }
                if (responseCode != 0) {
                    receiver.getResponseBody( Integer.toString(responseCode) );
                }
                conn.disconnect();
            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }

        }
    }
    /* Retrieve AE-ID */
    class aeRetrieveRequest extends Thread {
        private final Logger LOG = Logger.getLogger(aeCreateRequest.class.getName());
        private IReceived receiver;
        int responseCode=0;

        public aeRetrieveRequest() {
        }
        public void setReceiver(IReceived hanlder) {
            this.receiver = hanlder;
        }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl()+"/"+ ae.getappName();
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", "Sandoroid");
                conn.setRequestProperty("nmtype", "short");
                conn.connect();

                responseCode = conn.getResponseCode();

                BufferedReader in = null;
                String aei = "";
                if (responseCode == 200) {
                    // Get AEID from Response Data
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String resp = "";
                    String strLine;
                    while ((strLine = in.readLine()) != null) {
                        resp += strLine;
                    }

                    ParseElementXml pxml = new ParseElementXml();
                    aei = pxml.GetElementXml(resp, "aei");
                    ae.setAEid( aei );
                    //Log.d(TAG, "Retrieve Get AEID[" + aei + "]");
                    in.close();
                }
                if (responseCode != 0) {
                    receiver.getResponseBody( Integer.toString(responseCode) );
                }
                conn.disconnect();
            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }
    /* Subscribe Co2 Content Resource */
    class SubscribeResource extends Thread {
        private final Logger LOG = Logger.getLogger(SubscribeResource.class.getName());
        private IReceived receiver;
        private String container_name = "cnt-co2"; //change to control container name

        public ContentSubscribeObject subscribeInstance;
        public SubscribeResource() {
            subscribeInstance = new ContentSubscribeObject();
            subscribeInstance.setUrl(csebase.getHost());
            subscribeInstance.setResourceName(ae.getAEid()+"_rn");
            subscribeInstance.setPath(ae.getAEid()+"_sub");
            subscribeInstance.setOrigin_id(ae.getAEid());
        }
        public void setReceiver(IReceived hanlder) { this.receiver = hanlder; }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() + "/" + ServiceAEName + "/" + container_name;

                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml; ty=23");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid());

                String reqmqttContent = subscribeInstance.makeXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqmqttContent.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqmqttContent.getBytes());
                dos.flush();
                dos.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String resp = "";
                String strLine="";
                while ((strLine = in.readLine()) != null) {
                    resp += strLine;
                }

                if (resp != "") {
                    receiver.getResponseBody(resp);
                }
                conn.disconnect();

            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }
}
