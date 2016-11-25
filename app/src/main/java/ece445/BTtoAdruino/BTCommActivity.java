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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.UUID;


public class BTCommActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private String TAG = "BTCommActivity";
    //UI components=========================
    Button btnSend, btnSync, btnDisconnect;
    TextView txtV_send, txtV_recv, txtV_weight, txtV_vol, txtV_gps, txtV_est;
    //Bluetooth & GPS from System=========================
    String address = null;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //SPP UUID. Look for it
    GoogleApiClient mPlayApi = null;
    //My custom Bluetooth & GPS protocol=========================
    BTActions btActions = null;
    private boolean isBtConnected = false;
    private GPSComm gpsComm = null;

    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message inputMsg) {
            switch (inputMsg.what){
                case Parameter.BTMSG_SEND_SUCCESS:
                    txtV_send.setText("SUCC:"+ inputMsg.obj);
                    break;
                case Parameter.BT_CONNECT_FAIL:
                    msg("Connection FAILED. Please disconnect then reconnect");
                    break;
                case Parameter.BTMSG_RECV_SUCCESS:
                    txtV_recv.setText("SUCC:"+ inputMsg.obj);
                    break;
                case Parameter.BTMSG_UPDATE_VOLUME:
                    txtV_vol.setText(Integer.toString((int)inputMsg.obj));
                    break;
                case Parameter.BTMSG_UPDATE_WEIGHT:
                    txtV_weight.setText(Integer.toString((int)inputMsg.obj));
                    break;
                case Parameter.GPSMSG_UPDATED:
                    txtV_gps.setText((String)inputMsg.obj);
                    break;
                case Parameter.GPSMSG_DIST_UPDATED:
                    txtV_est.setText(Double.toString((Double) inputMsg.obj));
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
        txtV_send = (TextView)findViewById(R.id.textView2);
        txtV_recv = (TextView)findViewById(R.id.textView4);
        txtV_weight = (TextView)findViewById(R.id.textView6_1);
        txtV_vol = (TextView)findViewById(R.id.textView6_2);
        txtV_gps = (TextView)findViewById(R.id.textView8_1);
        txtV_est = (TextView)findViewById(R.id.textView8_2);

        if (mPlayApi == null) {
            mPlayApi = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        new InitBT().execute(); //Call the class to connect
    }

    @Override
    protected void onStart(){
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
                btActions.sendMsgOnce(MsgFormatter.encodeRecalcWeight()+"\n");
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

        mPlayApi.connect();
        super.onStart();
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

    @Override
    protected void onStop() {
        mPlayApi.disconnect();
        super.onStop();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        msg("Google Play client connection SUCCESS!");
        gpsComm = new GPSComm(mPlayApi, myHandler, this);
        gpsComm.firstComm();
        gpsComm.startUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        msg("Google Play client connection FAILED!");
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
//                finish();
            }
            else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
