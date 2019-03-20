package com.a6ravich.esk8throttle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.*;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.SeekBar;
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
    private SeekBar mThrottle;
    private TextView mBTStatus;
    private TextView mReadBuffer;

    //You can use a random UUID generator from the internet, or use the UUID.randomUUID() method
    //private final static UUID uuid = UUID.fromString("350320f0-b302-4f69-b827-53f3a203c3b2");

    //Request codes
    public static final int ENABLE_BT_REQUEST_CODE = 1337;
    public final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    public final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mThrottle = findViewById(R.id.throttleBar);
        mThrottle.setProgress(2);
        mBTStatus = findViewById(R.id.btStatusView);
        mReadBuffer = findViewById(R.id.readBuffer);

        final ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        final Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        mThrottle.setMax(5);
        mThrottle.setMin(0);
        mThrottle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressVal, boolean fromUser) {
                progress = progressVal;
                if (progress == 2) {
                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                    vib.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                }
                mConnectedThread.write( String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
            int prog = mThrottle.getProgress();
            if (prog > 0 && prog < 6) {
                mThrottle.setProgress(prog-1, true);
            }  else if (prog == 0) {
                mThrottle.setProgress(0);
            }
            return true;
        }

        else if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            int prog = mThrottle.getProgress();
            if (prog >= 0 && prog < 5) {
                mThrottle.setProgress(prog+1, true);
            }  else if (prog >= 5) {
                mThrottle.setProgress(5);
            }
            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
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
            }

            //If the request was unsuccessful...//
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "An error occurred while enabling Bluetooth",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) {
        UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {
            return device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
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