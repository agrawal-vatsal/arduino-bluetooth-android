package drdo.project;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    //    TextView myLabel;
//    EditText myTextbox;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    //    int counter;
    volatile boolean stopWorker;
    static final String DEVICE_NAME = "";
    String finalData;
    Button graphPlot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button openButton = findViewById(R.id.open);
//        Button sendButton = findViewById(R.id.send);
        Button closeButton = findViewById(R.id.close);
        graphPlot = findViewById(R.id.plot);
//        myLabel = findViewById(R.id.label);
//        myTextbox = findViewById(R.id.entry);

        //Open Button
        openButton.setOnClickListener((View v) -> {
            try {
                findBT();
                openBT();
                openButton.setVisibility(View.GONE);
                closeButton.setVisibility(View.VISIBLE);
                openButton.setClickable(false);
                closeButton.setClickable(true);
            } catch (IOException ignored) {
            }
        });

//        sendButton.setOnClickListener((View v) -> {
//            try {
//                sendData();
//            } catch (IOException ignored) {
//            }
//        });

        closeButton.setOnClickListener((View v) -> {
            try {
                closeBT();
                openButton.setVisibility(View.VISIBLE);
                closeButton.setVisibility(View.GONE);
                openButton.setClickable(true);
                closeButton.setClickable(false);
            } catch (IOException ignored) {
            }
        });

        graphPlot.setOnClickListener((View v) -> {
            Intent i = new Intent(MainActivity.this, GraphPlotActivity.class);
            i.putExtra("data", finalData);
            startActivity(i);
        });
    }

    void findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "No Bluetooth adapter available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(DEVICE_NAME)) {
                    mmDevice = device;
                    break;
                }
            }
        }
//        myLabel.setText("Bluetooth Device Found");
        Toast.makeText(getApplicationContext(), "Bluetooth Device Found", Toast.LENGTH_SHORT).show();
    }

    void openBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

//        myLabel.setText("Bluetooth Opened");
        Toast.makeText(getApplicationContext(), "Bluetooth Openede", Toast.LENGTH_SHORT).show();
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                try {
                    int bytesAvailable = mmInputStream.available();
                    if (bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        mmInputStream.read(packetBytes);
                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = packetBytes[i];
                            if (b == delimiter) {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                final String data = new String(encodedBytes, StandardCharsets.US_ASCII);
                                readBufferPosition = 0;

                                handler.post(() -> finalData = data);
                            } else {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                    Toast.makeText(getApplicationContext(), "All data stored", Toast.LENGTH_SHORT).show();
                } catch (IOException ex) {
                    stopWorker = true;
                }
            }
        });

        workerThread.start();
    }

//    void sendData() throws IOException {
//        String msg = myTextbox.getText().toString();
//        msg += "\n";
//        mmOutputStream.write(msg.getBytes());
//        myLabel.setText("Data Sent");
//    }

    void closeBT() throws IOException {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
//        myLabel.setText("Bluetooth Closed");
        Toast.makeText(getApplicationContext(), "Bluetooth Closed", Toast.LENGTH_SHORT).show();
    }
}
