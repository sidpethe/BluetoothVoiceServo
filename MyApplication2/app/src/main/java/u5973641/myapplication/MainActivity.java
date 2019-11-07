package u5973641.myapplication;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE;
import static android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH;

public class MainActivity extends FragmentActivity implements NoticeDialogFragment.NoticeDialogListener{
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_BT_NAME = 3;
    private static final int RESULT_BT_NAME = 3;
    private static final String EXTRA_MESSAGE="com.example.u5973641.myapplication.MESSAGE";
    private BluetoothSocket sock;
    private BluetoothAdapter bluedaat = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice defaultBlue=null;
    private Intent enableBtIntent = new Intent(ACTION_REQUEST_ENABLE);
    private int currentSpeed=0;
    private int currentPos=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!bluedaat.isEnabled()) {
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            btEnabled();
        }
        ImageButton im4=findViewById(R.id.imageButton4);
        im4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btEnabled();
            }
        });
    }

    @Override
    protected void onDestroy() {

        if (sock.isConnected()){
            try {
                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();

    }

    private static final int SPEECH_REQUEST_CODE = 0;

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            spokenText=spokenText.toLowerCase();
            switch (spokenText) {
                case ("position 0 degrees"):
                    currentPos=0;
                    SendPos(0);
                    break;
                case ("position 30 degrees"):
                    currentPos=1;
                    SendPos(1);
                    break;
                case ("position 60 degrees"):
                    SendPos(2);
                    currentPos=2;
                    break;
                case ("position 90 degrees"):
                    SendPos(3);
                    currentPos=3;
                    break;
                case ("position 120 degrees"):
                    SendPos(4);
                    currentPos=4;
                    break;
                case ("position 150 degrees"):
                    SendPos(5);
                    currentPos=5;
                    break;
                case ("position 180 degrees"):
                    SendPos(6);
                    currentPos=6;
                    break;
                case ("valet mode"):
                    SendSpeed(0);
                    currentSpeed=0;
                    break;
                case ("ludicrous mode"):
                    SendSpeed(1);
                    currentSpeed=1;
                    break;
                case ("light speed ahead"):
                    SendSpeed(2);
                    currentSpeed=2;
                    break;
                default:
                    Toast.makeText(this, spokenText, Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode==REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth must be enabled", Toast.LENGTH_LONG).show();
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            if (resultCode == RESULT_OK)
                btEnabled();
        }
        if(requestCode==REQUEST_BT_NAME)
            SetNewBtDevice(data.getStringExtra(EXTRA_MESSAGE));
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void SetNewBtDevice(String newDeviceName){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.defaultBt), newDeviceName);
        editor.apply();

        btEnabled();
    }

    public void showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new NoticeDialogFragment();
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        Intent intentEditBtDeviceActivity=new Intent(this, EditBtDeviceActivity.class);
        startActivityForResult(intentEditBtDeviceActivity,RESULT_BT_NAME);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button

    }

    private void btEnabled(){
        final Set<BluetoothDevice> pairedDevices = bluedaat.getBondedDevices();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultBtName=sharedPref.getString(getString(R.string.defaultBt),getString(R.string.defaultBt));
        // If there are paired devices
        if ((pairedDevices.size()) > 0) {
            // Loop through paired devices
            for (BluetoothDevice HC5 : pairedDevices) {
                if(HC5.getName().equals(defaultBtName)) {
                    defaultBlue = HC5;
                }
            }
            if(defaultBlue==null){
                Toast.makeText(this,defaultBtName+getString(R.string.BtDeviceNotPaired),Toast.LENGTH_LONG).show();
            }
            else ConnectCall(defaultBlue);
        }
        ImageButton imb2=findViewById(R.id.imageButton2);
        imb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoticeDialog();
            }
        });

        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener=new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentSpeed=i;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SendSpeed(currentSpeed);
            }
        };
        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener2=new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentPos=i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SendPos(currentPos);
            }
        };

        SeekBar seekBar=findViewById(R.id.seekBar);
        SeekBar seekBar2=findViewById(R.id.seekBar2);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        seekBar2.setOnSeekBarChangeListener(onSeekBarChangeListener2);
        ImageButton imb=findViewById(R.id.imageButton);
        imb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySpeechRecognizer();
            }
        });
    }
    private void ConnectCall(BluetoothDevice HC5){
        ConnectThread connection = new ConnectThread(HC5);
        connection.run();
        sock = connection.getSocket();
        if (sock.isConnected()) {
            Toast.makeText(this,"Connected",Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this,getString(R.string.BtNotFound),Toast.LENGTH_LONG).show();
        }
    }
    public void SendSpeed(int speed) {
        ConnectedThread blue = new ConnectedThread(sock);
        Toast.makeText(this, "Sending Speed " + (speed),Toast.LENGTH_SHORT).show();
        switch (speed) {
            case (0):
                blue.write('7');//speed 1
                break;
            case (1):
                blue.write('8');//speed 2
                break;
            case (2):
                blue.write('9');//speed 3
                break;
            default:
                blue.write(Character.forDigit(currentSpeed,10));//send same speed
        }
    }
    public void SendPos(int pos) {
        ConnectedThread blue = new ConnectedThread(sock);
        Toast.makeText(this, "Sending Position " + (pos),Toast.LENGTH_SHORT).show();
        switch (pos) {
            case (0):
                blue.write('0');//pos 0
                break;
            case (1):
                blue.write('1');//pos 1
                break;
            case (2):
                blue.write('2');//pos 2
                break;
            case (3):
                blue.write('3');//pos 3
                break;
            case (4):
                blue.write('4');//pos 4
                break;
            case (5):
                blue.write('5');//pos 5
                break;
            case (6):
                blue.write('6');//pos 6
                break;
            default:
                blue.write('0');//reset to position 1
        }
    }
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        BluetoothAdapter bluedaat = BluetoothAdapter.getDefaultAdapter();
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            UUID MY_UUID;
            MY_UUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            bluedaat.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }

            }

        }
        public BluetoothSocket getSocket() {
            return (mmSocket);
        }

    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
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
                    bytes = mmInStream.read(buffer);
                    byte MESSAGE_READ = 0;
                    Handler mHandler = new Handler();
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();

                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(char key) {
            try {
                mmOutStream.write(key);
            } catch (IOException e) {

            }
        }
        /* Call this from the main activity to send data to the remote device */
        public void write(byte key) {
            try {
                mmOutStream.write(key);
            } catch (IOException e) {

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