package com.unp.mqtt_app02;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    Button btnEnviar, btnConectar;
    EditText edtMsg;
    TextView txtPubRec;
    static String hostMqtt = "tcp://m16.cloudmqtt.com:13348";
    static String usuario = "czfwiubb";
    static String senhaMqtt = "PMrv8IuLCh7D";
    public String topicoMsg="LED";
    public String clienteID;
    MqttAndroidClient clientMqtt;
    MqttConnectOptions options;
    public boolean flagConn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnEnviar = (Button) findViewById(R.id.btnPublicar);
        edtMsg = (EditText) findViewById(R.id.edtMsg);
        btnConectar = (Button) findViewById(R.id.btnConectar);
        txtPubRec = (TextView) findViewById(R.id.txtDadosRecebidos);

        clienteID = MqttClient.generateClientId();//Gerar um ID randômico
        clientMqtt = new MqttAndroidClient(this.getApplicationContext(),hostMqtt, clienteID);
        options = new MqttConnectOptions();
        options.setUserName(usuario);
        options.setPassword(senhaMqtt.toCharArray());

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strMsg = edtMsg.getText().toString();
                publicarMsg(strMsg);
            }
        });
        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(flagConn) {
                        conectar();
                    }else{
                        desconectar();
                    }
                }catch (Exception e){
                    msgToats("Erro na conexao :"+e);
                }

            }
        });

        //implementar o Evento que vai escutar as publicações
        clientMqtt.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                txtPubRec.append("Tópico: "+topic+" - "+"Informação: "+new String(message.getPayload())+"\n");
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

    }
    private void msgToats(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void publicarMsg(String msg){
        try {
            clientMqtt.publish(topicoMsg,msg.getBytes(),0,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private void desconectar() {
        //retirar a inscrição
        endSubscribe();
        try {
            IMqttToken disconToken = clientMqtt.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    msgToats("Desconectado do servidor de MQTT");
                    btnConectar.setText("CONECTAR AO MQTT");
                    btnEnviar.setEnabled(false);
                    edtMsg.setEnabled(false);
                    flagConn = true;
                    txtPubRec.setText("...");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    msgToats("Falha ao desconectar do servidor de MQTT");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private void conectar(){
        //conectar ao servidor MQTT
        try {
            IMqttToken token = clientMqtt.connect(options);

            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    msgToats("Conectado ao servidor de MQTT");
                    btnConectar.setText("DESCONECTAR DO MQTT");
                    btnEnviar.setEnabled(true);
                    edtMsg.setEnabled(true);
                    flagConn = false;
                    txtPubRec.setText("");
                    //inscrição na publicação
                    startSubscribe();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    msgToats("Falha na Conexão com o servidor de MQTT");
                    btnEnviar.setEnabled(false);
                    edtMsg.setEnabled(false);
                    flagConn = true;
                    btnConectar.setText("CONECTAR AO MQTT");
                }
            });

        } catch (MqttException e) {

            e.printStackTrace();
        }
    }
    public void startSubscribe(){
        int qos =1;
        try {
            IMqttToken subtoken = clientMqtt.subscribe(topicoMsg, qos);
        }catch (MqttException e){
            e.printStackTrace();
        }

    }
    public void endSubscribe() {
        try {
            IMqttToken unsubToken = clientMqtt.unsubscribe(topicoMsg);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
