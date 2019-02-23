package com.example.v.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    Button btPairedBlue;
    Button btConnectdBlue;
    Button btSendBlue;
    TextView tv;
    BluetoothSocket mSocket = null;
    BluetoothDevice mDevice;
    BluetoothAdapter mBluetoothAdapter;
    int REQUEST_ENABLE_BT = 1;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btConnectdBlue = (Button) findViewById(R.id.btConnectBlue);
        btPairedBlue = (Button) findViewById(R.id.btPairBlue);
        btSendBlue = (Button) findViewById(R.id.btSendBlue);
        tv = (TextView) findViewById(R.id.tv);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        context = this;
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        btPairedBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    // There are paired devices. Get the name and address of each paired device.
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // MAC address
                        if (deviceName.equals("HC-05")){
                            mDevice = device;
                            Toast.makeText(context, "Paired with HC-05", Toast.LENGTH_SHORT).show();
                            tv.setText("PAired with HC-05");
                        }
                    }
                }
            }
        });
        btConnectdBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText("Connecting..");
                if (mSocket == null || !mSocket.isConnected())
                    new ConnectAsync().execute();
            }
        });
        btSendBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte b = 1;
                if (mSocket != null && mSocket.isConnected())
                    new WriteAsync().execute(b);
                else
                    Toast.makeText(context, "Not connected Cant write", Toast.LENGTH_SHORT).show();
            }
        });
    }
    class ConnectAsync extends  AsyncTask<Void, Void, Void>{
        UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        @Override
        protected Void doInBackground(Void...voids) {
            try {
                mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                mBluetoothAdapter.cancelDiscovery();
                if (mSocket == null || !mSocket.isConnected()) {
                    mSocket.connect();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mSocket != null && mSocket.isConnected()) {
                tv.setText("Connected");
            }
            else
                tv.setText("Connection failed");
        }
    }
    class WriteAsync extends AsyncTask<Byte, Void, Void>{
        boolean bool = false;

        @Override
        protected Void doInBackground(Byte...bytes) {
                try {
                    OutputStream out = mSocket.getOutputStream();
                    out.write(bytes[0]);
                    out.flush();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    bool = true;
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!bool) {
                tv.setText("Sendt data");
            }
            else
                tv.setText("Data sending failure");
        }
    }
}
