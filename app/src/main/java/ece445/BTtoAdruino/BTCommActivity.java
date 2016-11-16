package ece445.BTtoAdruino;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;


public class BTCommActivity extends AppCompatActivity {
    private String TAG = "BTCommActivity";
    Button btnSend, btnSync, btnDisconnect;
    TextView textView_send, textView_recv;
    String address = null;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    BTActions btActions = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message inputMsg) {
            switch (inputMsg.what){
                case Parameter.BTMSG_SEND_SUCCESS:
                    textView_send.setText("SUCC:"+(String)inputMsg.obj);
                    break;
                case Parameter.BT_CONNECT_FAIL:
                    msg("Connection FAILED. Please disconnect then reconnect");
                    break;
                case Parameter.BTMSG_RECV_SUCCESS:
                    textView_recv.setText("SUCC:"+(String)inputMsg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the BTCommActivity
        setContentView(R.layout.activity_led_control);

        //set up the GUI components
        btnSend = (Button)findViewById(R.id.button2);
        btnSync = (Button)findViewById(R.id.button3);
        btnDisconnect = (Button)findViewById(R.id.button4);
        textView_send = (TextView)findViewById(R.id.textView2);
        textView_recv = (TextView)findViewById(R.id.textView4);

        new InitBT().execute(); //Call the class to connect
    }

    @Override
    protected void onStart(){
        super.onStart();
        //commands to be sent to bluetooth
        btnSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                btActions.sendMsgOnce("TO");
            }
        });

        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                btActions.sendMsgOnce("TF");
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
                finish(); //return to the first layout
            }
        });
//                EditText editText = (EditText) findViewById(R.id.edit_message);
//                String message = editText.getText().toString();
//                textView_send = (TextView)findViewById(R.id.textView2);
//                textView_send.setText(message);
    }

    private void Disconnect() {
        if (btSocket!=null){ //If the btSocket is busy
            try {
                btSocket.close(); //close connection
                btActions.cancelRecv();
                btActions = null;
            }
            catch (IOException e) {
                msg("Error");
            }
        }
    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
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


    /**
     * @Description: The class to initiate the bluetooth connection.
     * Specifically, it will handle the very first contact with the bluetooth device
     * and prepare the bluetooth socket(btSocket) for further use in the remaining
     * code.
     */
    private class InitBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(BTCommActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices){ //while the progress dialog is shown, the connection is done in background
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                    btActions = new BTActions(btSocket, myHandler);
                    btActions.startRecv();
                }
            }
            catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result){ //after the doInBackground, it checks if everything went fine
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}