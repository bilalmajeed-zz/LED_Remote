package bilalmajeed.com.ledremote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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


public class MainActivity extends ActionBarActivity {

    Button onButton;
    Button offButton;
    Button connectButton;

    boolean error = false;
    String remoteDeviceName = "HC-06";

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
            Toast.makeText(this, "Your device is not supported", Toast.LENGTH_LONG).show();
        }
        if(!btAdapter.isEnabled()){
            Toast.makeText(this, "Bluetooth must be enabled", Toast.LENGTH_LONG).show();
            Intent turnBTOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTOn, 1);
        }
        buttonClickListener();
    }

    private void buttonClickListener() {
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View connectClick) {
                findRemoteDevice();
                try{
                    connectRemoteDevice();
                }catch(IOException e){
                    error = true;
                }
            }
        });

        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View onClick) {
                try{
                    turnON();
                }catch(IOException e){
                    error = true;
                }
            }
        });

        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View offClick) {
                try{
                    turnOFF();
                }catch(IOException e){
                    e.getMessage();
                }
            }
        });

        if (error) {
            Toast.makeText(this, "error", Toast.LENGTH_LONG).show();
        }
    }

    public void findRemoteDevice(){
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        //if there any paired devices
        if(pairedDevices.size() > 0){
            //loop through paired devices
            for (BluetoothDevice deviceFound : pairedDevices){
                if(deviceFound.getName().equals(remoteDeviceName)){
                    btRemoteDevice = deviceFound;
                }
            }
        }
    }

    public void connectRemoteDevice() throws IOException{
        UUID btUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        btSocket = btRemoteDevice.createRfcommSocketToServiceRecord(btUUID);
        btSocket.connect();
        output = btSocket.getOutputStream();
        Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
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
