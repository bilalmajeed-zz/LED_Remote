package com.bilalmajeed.ledremote;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity {

    Button connectButton;

    //declare a few constant error messages
    private final String deviceName = "HC-06";
    private final String CONNECTION_ERROR = "Not connected to device";

    //declare the objects needed for the bluetooth
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private BluetoothDevice btRemoteDevice;
    private OutputStream output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = (Button) findViewById(R.id.connectButton);

        //initialize the btAdapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter btDisconnected = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        IntentFilter btConnected = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        this.registerReceiver(bt_broadcastReceiver, btDisconnected);
        this.registerReceiver(bt_broadcastReceiver, btConnected);

        //if there not btAdapter, meaning no Bluetooth support on the phone. Then notify user
        if(btAdapter == null){
            messageBox("DEVICE NOT SUPPORTED", "Your device does not have bluetooth capabilities");
        }

        //if the bluetooth is not enabled then ask for the user to enable it
        if(!btAdapter.isEnabled()){
            Intent turnBTOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTOn, 1);
        }
    }

    final private BroadcastReceiver bt_broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //if bluetooth is not connected then enable connect button
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED == action){
                connectButton.setClickable(true);
                connectButton.setEnabled(true);
                connectButton.setText(getResources().getString(R.string.default_connectButton_txt));
            }else if(BluetoothDevice.ACTION_ACL_CONNECTED == action){
                connectButton.setClickable(false);
                connectButton.setEnabled(false);
                connectButton.setText(getResources().getString(R.string.connectedText));
            }
        }
    };

    //IF CONNECT BUTTON CLICKED
    public void connect(View view){

        findRemoteDevice();

        try {
            connectRemoteDevice();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    //IF ON BUTTON CLICKED
    public void turnON(View view){
        try{
            output.write("1\n".getBytes());
        }catch(Exception e){
            Toast.makeText(this, CONNECTION_ERROR, Toast.LENGTH_LONG).show();
        }
    }

    //IF OFF BUTTON CLICKED
    public void turnOFF(View view){
        try{
            output.write("0\n".getBytes());
        }catch(Exception e){
            Toast.makeText(this, CONNECTION_ERROR, Toast.LENGTH_LONG).show();
        }
    }

    public void findRemoteDevice(){
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        //if there any paired devices
        if(pairedDevices.size() > 0) {
            //loop through paired devices
            for (BluetoothDevice deviceFound : pairedDevices) {
                if (deviceFound.getName().equals(deviceName))
                    btRemoteDevice = deviceFound;
            }
        }
    }

    public void connectRemoteDevice() throws IOException{
        UUID btUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        btSocket = btRemoteDevice.createRfcommSocketToServiceRecord(btUUID);
        btSocket.connect();
        output = btSocket.getOutputStream();
    }

    //displays a message box to the user with the inputed message with one button
    private void messageBox(String method, String message) {
        Log.d("EXCEPTION: " + method, message);

        AlertDialog.Builder messageBox = new AlertDialog.Builder(this);
        messageBox.setTitle(method);
        messageBox.setMessage(message);
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        messageBox.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Setting tapped", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            btSocket.close();
        }catch(IOException e){
            Toast.makeText(this, "ERROR - Could not close socket", Toast.LENGTH_LONG).show();
        }
    }
}