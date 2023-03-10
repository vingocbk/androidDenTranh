package com.example.androiddentranh;

import static android.R.layout.simple_list_item_1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ImageView imgMenuListDevice, imgBluetoothConnection;
    TextView txtNameBluetoothConnection, txtBackListDevice;
    View layoutListDevice;
    ProgressBar pgbRefreshListDevice;
    ListView lvListDevice;

    SeekBar[] sbControlLight = new SeekBar[12];
    SeekBar sbControlLightAll;

    ArrayAdapter<String> arrayAdapterListDevice;


    public static int REQUEST_BLUETOOTH = 1;
    public static int REQUEST_DISCOVERABLE_BT = 1;
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    boolean isSwitchStep = false;

    String TAG = "MainActivity";
    String deviceName;
    private BluetoothDevice mmDevice;
    //private UUID deviceUUID;

    ParcelUuid[] mDeviceUUIDs;
    ConnectedThread mConnectedThread;
    //    private Handler handler;
    Object[] ObjectBluetooth;

    //variable for save name motor
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide(); // hide the title bar
        setContentView(R.layout.activity_main);
        checkPermissions();
        initLayout();
        initBluetooth();
        for (int i = 0; i < 12; i++) {
            int finalI = i;
            sbControlLight[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (mmDevice != null && isConnected(mmDevice)) {
                        String data = "{\"channel\":\"";
                        data += String.valueOf(finalI + 1);
                        data += "\",\"data\":\"";
                        data += String.valueOf(seekBar.getProgress());
                        data += "\"}";
                        byte[] bytes = data.getBytes(Charset.defaultCharset());
                        mConnectedThread.write(bytes);
                        Log.d("onProgressChanged", data);
                        Toast.makeText(MainActivity.this, "DONE", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Connect Device First!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        sbControlLightAll.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mmDevice != null && isConnected(mmDevice)) {
                    String data = "{\"channel\":\"all\",\"data\":\"";
                    data += String.valueOf(seekBar.getProgress());
                    data += "\"}";
                    byte[] bytes = data.getBytes(Charset.defaultCharset());
                    mConnectedThread.write(bytes);
                    Log.d("onProgressChanged", data);
                    Toast.makeText(MainActivity.this, "DONE", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Connect Device First!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        imgMenuListDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                layoutListDevice.setVisibility(View.VISIBLE);
//                imgRefreshListDevice.setVisibility(View.VISIBLE);
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    return;
                }
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                List<String> s = new ArrayList<String>();
//                s.add("---Thiết bị đã ghép đôi---");
                for (BluetoothDevice bt : pairedDevices) {
                    s.add(bt.getName() + "\n" + bt.getAddress());
                }
                ObjectBluetooth = pairedDevices.toArray();
//                s.add("---Thiết bị hiện có---");
                arrayAdapterListDevice = new ArrayAdapter<String>(
                        MainActivity.this,
                        simple_list_item_1,
                        s);
                lvListDevice.setAdapter(arrayAdapterListDevice);
            }
        });

        txtBackListDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                layoutListDevice.setVisibility(View.INVISIBLE);
                pgbRefreshListDevice.setVisibility(View.INVISIBLE);
            }
        });

        lvListDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Log.i(TAG, arrayAdapterListdevice.getItem(i));
                pgbRefreshListDevice.setVisibility(View.VISIBLE);
                BluetoothDevice bluetoothDeviceconnect = (BluetoothDevice) ObjectBluetooth[i];

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    return;
                }
                deviceName = bluetoothDeviceconnect.getName();
                String deviceAdress = bluetoothDeviceconnect.getAddress();

                Log.d(TAG, "onItemClick: deviceName = " + deviceName);
                Log.d(TAG, "onItemClick: deviceAdress = " + deviceAdress);
                Log.d(TAG, "Trying to Pair with " + deviceName);
                bluetoothDeviceconnect.createBond();

                mDeviceUUIDs = bluetoothDeviceconnect.getUuids();


                Log.d(TAG, "Trying to create UUID: " + deviceName);

                for (ParcelUuid uuid : mDeviceUUIDs) {
                    Log.d(TAG, "UUID: " + uuid.getUuid().toString());
                }
//                ParcelUuid uuidExtra Intent intent = null;
//                intent.getParcelableExtra("android.bluetooth.device.extra.UUID");
//                UUID uuid = mDeviceUUIDs.getUuid();


                ConnectThread connect = new ConnectThread(bluetoothDeviceconnect, MY_UUID_INSECURE);
                connect.start();
            }
        });

    }

    private void checkPermissions(){
        int permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    1
            );
        } else if (permission2 != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_LOCATION,
                    1
            );
        }
    }

    public void initLayout() {
        imgMenuListDevice = findViewById(R.id.imgMenuListDevice);
        imgBluetoothConnection = findViewById(R.id.imgBluetoothConnection);
        txtNameBluetoothConnection = findViewById(R.id.txtNameBluetoothConnection);

        pgbRefreshListDevice = findViewById(R.id.pgbRefreshListDevice);
        lvListDevice = findViewById(R.id.lvListDevice);
        txtBackListDevice = findViewById(R.id.txtBackListDevice);

        sbControlLight[0] = findViewById(R.id.sbControlLight1);
        sbControlLight[1] = findViewById(R.id.sbControlLight2);
        sbControlLight[2] = findViewById(R.id.sbControlLight3);
        sbControlLight[3] = findViewById(R.id.sbControlLight4);
        sbControlLight[4] = findViewById(R.id.sbControlLight5);
        sbControlLight[5] = findViewById(R.id.sbControlLight6);
        sbControlLight[6] = findViewById(R.id.sbControlLight7);
        sbControlLight[7] = findViewById(R.id.sbControlLight8);
        sbControlLight[8] = findViewById(R.id.sbControlLight9);
        sbControlLight[9] = findViewById(R.id.sbControlLight10);
        sbControlLight[10] = findViewById(R.id.sbControlLight11);
        sbControlLight[11] = findViewById(R.id.sbControlLight12);
        sbControlLightAll = findViewById(R.id.sbControlLightAll);

        layoutListDevice = findViewById(R.id.layoutListDevice);


    }

    public void initBluetooth() {
        //--------------------------------------for bluetooth--------------------------------------------------------
        BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
        // Phone does not support Bluetooth so let the user know and exit.
        if (BTAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        if (!BTAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                return;
            }
            startActivityForResult(enableBT, REQUEST_BLUETOOTH);
        }
        //------------------------------------------------------------------------------------------------------
    }

    public static boolean isConnected(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("isConnected", (Class[]) null);
            boolean connected = (boolean) m.invoke(device, (Object[]) null);
            return connected;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            //deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.d(TAG, "RUN mConnectThread ");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        + MY_UUID_INSECURE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    return;
                }
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    return;
                }
                mmSocket.connect();

            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE );
            }

            //will talk about this in the 3rd video
            connected(mmSocket);
        }
        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }
    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");
//        pgbRefeshListdevice.setVisibility(View.INVISIBLE);

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();

//        byte[] bytes = "abcd".getBytes(Charset.defaultCharset());
//        mConnectedThread.write(bytes);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Stuff that updates the UI
                layoutListDevice.setVisibility(View.INVISIBLE);
                pgbRefreshListDevice.setVisibility(View.INVISIBLE);
                imgBluetoothConnection.setBackgroundResource(R.mipmap.ic_bluetooth_connected);
                txtNameBluetoothConnection.setText(deviceName);
            }
        });
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()
            final byte delimiter = 10; //This is the ASCII code for a newline character

            byte[] readBuffer = new byte[1024];;
            int readBufferPosition = 0;
            String incomingMessage = "";
            int data[] = new int[12];
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    incomingMessage += new String(buffer, 0, bytes);
                    if(incomingMessage.contains("}")){
                        Log.d(TAG, "InputStream: " + incomingMessage);
                        JSONObject reader = new JSONObject(incomingMessage);
                        JSONArray jsonArray = reader.getJSONArray("1");
                        for(int i = 0; i < 12; i++){
                            data[i] = jsonArray.getInt(i);
                        }
                        incomingMessage = "";
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for(int i = 0; i < 12; i++){
                                sbControlLight[i].setProgress(data[i]);
                            }
                        }
                    });


                } catch (IOException | JSONException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage());
//                    Toast.makeText(MainActivity.this, "Kết nối thất bại", Toast.LENGTH_SHORT).show();
//                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage());
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}