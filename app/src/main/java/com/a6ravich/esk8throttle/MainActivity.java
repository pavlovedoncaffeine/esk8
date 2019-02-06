package com.a6ravich.esk8throttle;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.bluetooth.*;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int ENABLE_BT_REQUEST_CODE = 1337;
    private static final String TAG = "MainActivity";

    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice esk8 = null;
    Set<BluetoothDevice> pairedDev;
    BluetoothSocket btsocket;

    //You can use a random UUID generator from the internet, or use the UUID.randomUUID() method
    //private final static UUID uuid = UUID.fromString("350320f0-b302-4f69-b827-53f3a203c3b2");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //checking if bluetooth exists on device
        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "This device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (!btAdapter.isEnabled())
            { //if bt not enabled
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, ENABLE_BT_REQUEST_CODE);
                Toast.makeText(getApplicationContext(), "Enabling Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Check what request we’re responding to//
        if (requestCode == ENABLE_BT_REQUEST_CODE) {

            //If the request was successful…//
            if (resultCode == Activity.RESULT_OK) {
                //...then display the following toast.//
                Toast.makeText(getApplicationContext(), "Bluetooth has been enabled",
                        Toast.LENGTH_SHORT).show();
                if (btAdapter.getState() == BluetoothAdapter.STATE_ON)
                    pairedDev = btAdapter.getBondedDevices();
                else {
                    //boolean once = true;
                    if (pairedToEsk8()) {
                        if (connectToEsk8()) {
                            //--------------------------------------
                            //
                            // The specifics will vary depending on how you want your app to use
                            // its newly-forged Bluetooth connection, but as a rough guideline,
                            // you transfer data between two remote devices by completing the following steps:
                            //
                            // 1) Call getInputStream and getOutputStream on the BluetoothSocket.
                            // 2) Use the read() method to start listening for incoming data.
                            // 3) Send data to a remote device by calling the thread’s write() method and passing it the bytes you want to send.
                            // 4)  Note that both the read() and write() methods are blocking calls, so you should always run them from a separate thread.
                            //
                            //--------------------------------------
                        }
                    }
                }
            }

            //If the request was unsuccessful...//
            if(resultCode == RESULT_CANCELED){
                Toast.makeText(getApplicationContext(), "An error occurred while enabling Bluetooth",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean pairedToEsk8() {
        if (pairedDev.size() <= 0) {
            return false;
            //EXIT APPLICATION? TOAST- "Could not find e-Skateboard"
        }
        else {
            for (BluetoothDevice device : pairedDev) {
                if (device.getName().equalsIgnoreCase("HC-06")) { //Change esk8 adapter name here
                    esk8 = device;
                    return true;
                }
            }
            if (esk8 == null) {
                Toast.makeText(getApplicationContext(),
                        "Pair with HC-06 using device's Bluetooth settings",
                        Toast.LENGTH_LONG).show();

                //Open BT settings for device if es8 HC-06 is not paired with device
                Intent intentBluetooth = new Intent();
                intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentBluetooth);
                return false;
            }
        }
        return false;

    }

    public boolean connectToEsk8() {
        try {
            BluetoothServerSocket bluetoothServerSocket = btAdapter.listenUsingRfcommWithServiceRecord("esk8", UUID.randomUUID());
            btsocket = bluetoothServerSocket.accept(300);
            bluetoothServerSocket.close();
            if (btsocket.isConnected()) {
                // btsocket.getInputStream() returns an inputstream
                // btsocket.getOutputStream() returns an outputstream

                // both must be run on separate threads to prevent blocking
                // CAN ADD TEXT FIELD TO TELL APP STATE OF ESK8? LED ON OR OFF?
                // Onclick button implementations to send specific data to arduino necessary
                return true;
            } else {
                return false;
            }
        } catch (IOException ex) {
            Throwable cause = ex.getCause();
            Log.v(TAG, ex.getMessage());
            ex.printStackTrace();
            System.out.println("\n");
            cause.printStackTrace();
            return false;
        }
    }

    public void sendBTMsg(View view) {

    }
}
