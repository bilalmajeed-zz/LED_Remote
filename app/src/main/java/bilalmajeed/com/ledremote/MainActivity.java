package bilalmajeed.com.ledremote;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.Resources;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity {

    Button onButton;
    Button offButton;
    Button connectButton;

    private final String remoteDeviceName = "HC-06";
    private final String BUTTON_PRESS_ERROR = "The phone must be connected to the device";
    private String CONNECT_BUTTON_ERROR;

    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private BluetoothDevice btRemoteDevice;
    private OutputStream output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onButton = (Button) findViewById(R.id.onButton);
        offButton = (Button) findViewById(R.id.offButton);
        connectButton = (Button) findViewById(R.id.connectButton);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(btAdapter == null){
            messageBox("DEVICE NOT SUPPORTED", "Your device does not have bluetooth capabilities");
        }
        if(!btAdapter.isEnabled()){
            Intent turnBTOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTOn, 1);
        }

        buttonClickListener();
    }

    private void buttonClickListener() {

        /* CONNECT BUTTON LISTENER */
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View connectClick) {
                findRemoteDevice();
                try{
                    connectRemoteDevice();
                }catch(Exception e){
                    if(!btAdapter.isEnabled())
                        CONNECT_BUTTON_ERROR = "Bluetooth is not enabled on the phone";
                    else if(btSocket.isConnected())
                        CONNECT_BUTTON_ERROR = "Already connected to " + remoteDeviceName;
                    else if(!btSocket.isConnected())
                        CONNECT_BUTTON_ERROR = "Device: " + btRemoteDevice.getName() + " not found";
                    else
                        CONNECT_BUTTON_ERROR = "Unidentified ERROR";

                    messageBox("CONNECT Button", CONNECT_BUTTON_ERROR);
                }
            }
        });

        /* ON BUTTON LISTENER */
        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View onClick) {
                try{
                    turnON();
                }catch(Exception e){
                    messageBox("ON Button", BUTTON_PRESS_ERROR);
                }
            }
        });

        /* OFF BUTTON LISTENER */
        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View offClick) {
                try{
                    turnOFF();
                }catch(Exception e){
                    messageBox("OFF Button", BUTTON_PRESS_ERROR);
                }
            }
        });
    }

    public void findRemoteDevice(){
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        //if there any paired devices
        if(pairedDevices.size() > 0) {
            //loop through paired devices
            for (BluetoothDevice deviceFound : pairedDevices) {
                if (deviceFound.getName().equals(remoteDeviceName))
                    btRemoteDevice = deviceFound;
            }
        }
    }

    public void connectRemoteDevice() throws IOException{
        UUID btUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        btSocket = btRemoteDevice.createRfcommSocketToServiceRecord(btUUID);
        btSocket.connect();
        output = btSocket.getOutputStream();
        Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
        connectButton.setClickable(false);
        connectButton.setEnabled(false);
        connectButton.setText(getResources().getString(R.string.connectedText));
    }

    public void turnON() throws IOException{
        String msg = "1";
        msg += "\n";
        output.write(msg.getBytes());
    }

    public void turnOFF() throws IOException{
        String msg = "0";
        msg += "\n";
        output.write(msg.getBytes());
    }

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
