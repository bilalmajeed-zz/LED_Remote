package bilalmajeed.com.ledremote;

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

    //declare the button variables
    Button onButton;
    Button offButton;
    Button connectButton;

    //declare a few constant error messages
    private final String deviceName = "HC-06";
    private final String CONNECTION_ERROR= "The phone must be connected to the device";
    private final String PAIRING_ERROR = "Device: " + deviceName + " not paired";
    private String CONNECT_BUTTON_ERROR = "Unidentified ERROR";

    //declare the objects needed for the bluetooth
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private BluetoothDevice btRemoteDevice;
    private OutputStream output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize the buttons
        onButton = (Button) findViewById(R.id.onButton);
        offButton = (Button) findViewById(R.id.offButton);
        connectButton = (Button) findViewById(R.id.connectButton);

        //initialize the btAdapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter btConnectedIntent = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(broadcastReceiver, btConnectedIntent);

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

    final private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            //if bluetooth is not connected then enable connect button
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED == action){
                connectButton.setClickable(true);
                connectButton.setEnabled(true);
                connectButton.setText(getResources().getString(R.string.default_connectButton_txt));
            }
        }
    };

    //IF CONNECT BUTTON CLICKED
    public void connect(View view){
        //find the paired device
        findRemoteDevice();

        try {
            //connect to the remote device
            connectRemoteDevice();
        } catch (IOException e) {
            //handle any error
            if(!btAdapter.isEnabled())
                CONNECT_BUTTON_ERROR = "Bluetooth is not enabled on the phone";
            else if(btSocket.isConnected())
                CONNECT_BUTTON_ERROR = "Already connected to " + deviceName;
            else if(!btSocket.isConnected())
                CONNECT_BUTTON_ERROR = "Device: " + btRemoteDevice.getName() + " not found";
            else
                CONNECT_BUTTON_ERROR = "Unidentified ERROR";

            //show a message to the user with the error
            messageBox("CONNECT Button", CONNECT_BUTTON_ERROR);
        }
    }

    //IF ON BUTTON CLICKED
    public void turnON(View view){
        try{
            output.write("1\n".getBytes());
        }catch(Exception e){
            messageBox("ON Button", CONNECTION_ERROR);
        }
    }

    //IF OFF BUTTON CLICKED
    public void turnOFF(View view){
        try{
            output.write("0\n".getBytes());
        }catch(Exception e){
            messageBox("OFF Button", CONNECTION_ERROR);
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
        //set uuid
        UUID btUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        //create a rfcomm socket and connect tot device
        btSocket = btRemoteDevice.createRfcommSocketToServiceRecord(btUUID);
        btSocket.connect();
        output = btSocket.getOutputStream();

        Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();

        //disable the connect button, so you can not click to it once connected
        connectButton.setClickable(false);
        connectButton.setEnabled(false);
        connectButton.setText(getResources().getString(R.string.connectedText));
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

    //disables a message box to the user with the inputed message with one button
    private void messageBox(String method, String message) {
        Log.d("EXCEPTION: " + method, message);

        AlertDialog.Builder messageBox = new AlertDialog.Builder(this);
        messageBox.setTitle(method);
        messageBox.setMessage(message);
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        messageBox.show();
    }
}
