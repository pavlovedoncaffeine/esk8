package com.a6ravich.esk8throttle;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.util.Log;
import android.view.*;
import android.bluetooth.*;
//import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Global variables
    public static Handler mHandler;

    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice esk8 = null;
    BluetoothSocket btsocket;

    private String btStatus = null;
    Set<BluetoothDevice> pairedDev;

    // Layout views
    private Button LEDon;
    private Button LEDoff;
    private TextView mBTStatus;
    private TextView mReadBuffer;

    //You can use a random UUID generator from the internet, or use the UUID.randomUUID() method
    //private final static UUID uuid = UUID.fromString("350320f0-b302-4f69-b827-53f3a203c3b2");

    //Request codes
    public static final int ENABLE_BT_REQUEST_CODE = 1337;
    public final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    public final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LEDon = findViewById(R.id.ledON);
        LEDoff = findViewById(R.id.ledOFF);
        mBTStatus = findViewById(R.id.btStatusView);
        mReadBuffer = findViewById(R.id.readBuffer);


        LEDon.setClickable(true);
        LEDoff.setClickable(true);
        mBTStatus.setClickable(false);
        mReadBuffer.setClickable(false);

        //checking if bluetooth exists on device
        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "This device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (!btAdapter.isEnabled()) { //if bt not enabled
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, ENABLE_BT_REQUEST_CODE);
                // flow goes to onActivityResult()
                Toast.makeText(getApplicationContext(), "Enabling Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }

        Boolean foundDevice = false;
        if (btAdapter.getState() == BluetoothAdapter.STATE_ON) {
            pairedDev = btAdapter.getBondedDevices();
            for (BluetoothDevice dev : pairedDev) {
                if (dev.getName().equalsIgnoreCase("HC-06")) {
                    esk8 = dev;
                    foundDevice = true;
                    break;
                }
            }
            if (!foundDevice) {
                Toast.makeText(getApplicationContext(),
                        "Pair with HC-06 using device's Bluetooth settings",
                        Toast.LENGTH_LONG).show();

                //Open BT settings for device if esk8 HC-06 is not paired with device
                Intent intentBluetooth = new Intent();
                intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentBluetooth);
            }
        }


        mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
                    mReadBuffer.setText(readMessage);
                }

                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1) {
                        btStatus = "Connected to Device: " + (String) (msg.obj);
                        mBTStatus.setText(btStatus);
                    } else {
                        btStatus = "Connection Failed";
                        mBTStatus.setText(btStatus);
                    }

                }
            }
        };

        new Thread() {
            public void run() {
                boolean fail = false;

                //BluetoothDevice device = btAdapter.getRemoteDevice(address);
                btsocket = createBluetoothSocket(esk8);
                if (btsocket == null) {
                    fail = true;
                    Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                }
                // Establish the Bluetooth socket connection.
                try {
                    btsocket.connect();
                } catch (IOException e) {
                    try {
                        fail = true;
                        btsocket.close();
                        mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                .sendToTarget();
                    } catch (IOException e2) {
                        //insert code to deal with this
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                }
                if(!fail) {
                    mConnectedThread = new ConnectedThread(btsocket);
                    mConnectedThread.start();

                    mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, esk8.getName())
                            .sendToTarget();
                }
            }
        }.start();
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
            }

            //If the request was unsuccessful...//
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "An error occurred while enabling Bluetooth",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

//    public boolean pairedToEsk8() {
//        if (pairedDev.size() <= 0) {
//            return false;
//            //EXIT APPLICATION? TOAST- "Could not find e-Skateboard"
//        } else {
//            for (BluetoothDevice device : pairedDev) {
//                if (device.getName().equalsIgnoreCase("HC-06")) { //Change esk8 adapter name here
//                    esk8 = device;
//                    return true;
//                }
//            }
//            if (esk8 == null) {
//                Toast.makeText(getApplicationContext(),
//                        "Pair with HC-06 using device's Bluetooth settings",
//                        Toast.LENGTH_LONG).show();
//
//                //Open BT settings for device if esk8 HC-06 is not paired with device
//                Intent intentBluetooth = new Intent();
//                intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
//                startActivity(intentBluetooth);
//                return false;
//            }
//        }
//        return false;
//
//    }

    public void sendOnMsg(View view) {
        Toast.makeText(getApplicationContext(), "On LED ON clicked", Toast.LENGTH_SHORT).show();
        //write to connected thread
        mConnectedThread.write("1");
    }

    public void sendOffMsg(View view) {
        Toast.makeText(getApplicationContext(), "On LED OFF clicked", Toast.LENGTH_SHORT).show();
        //write to connected thread
        mConnectedThread.write("0");

    }

//    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
//        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
//
//            if(!btAdapter.isEnabled()) {
//                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//
//            // Get the device MAC address, which is the last 17 chars in the View
//            String info = ((TextView) v).getText().toString();
//            final String address = info.substring(info.length() - 17);
//            final String name = info.substring(0,info.length() - 17);
//
//            // Spawn a new thread to avoid blocking the GUI one
//
//        }
//    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) {
        try {
            return device.createRfcommSocketToServiceRecord(UUID.randomUUID());
            //creates secure outgoing connection with BT device using a randomly generated UUID
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private static final String TAG2 = "ConnectedThread";


        private ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}