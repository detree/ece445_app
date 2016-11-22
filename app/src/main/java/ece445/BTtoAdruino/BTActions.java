package ece445.BTtoAdruino;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by SC.
 *
 * @Description:
 */

public class BTActions {
    private String TAG = "BTActions";
    private Handler myHandler;
    private BTRecvTh btrecv = null;
    MsgFormatter msgFormatter = null;

    BluetoothSocket btSocket = null;

    BTActions(BluetoothSocket skt, Handler handler){
        myHandler = handler;
        btSocket = skt;
        msgFormatter = new MsgFormatter(myHandler);
    }

    private class BTSendOnceTh implements Runnable{
        String msgToSend = null;
        BTSendOnceTh(String msgToSend){
            this.msgToSend = msgToSend;
        }
        @Override
        public void run() {
            try {
                if(btSocket==null)
                    Log.e(TAG, "btSocket==null");
                btSocket.getOutputStream().write(msgToSend.getBytes());
                myHandler.obtainMessage(Parameter.BTMSG_SEND_SUCCESS, msgToSend).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "bluetooth send message error");
                myHandler.obtainMessage(Parameter.BT_CONNECT_FAIL, msgToSend).sendToTarget();
            }
        }
        void start(){
            Thread t = new Thread(this);
            t.start();
        }
    }

    private class BTRecvTh implements Runnable{
        private InputStream inStream = null;
        private StringBuilder msgRecv = new StringBuilder();
        private volatile boolean stopReq = false;
        BTRecvTh(){
            try {
                inStream = btSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "bluetooth recv message error");
                myHandler.sendMessage(Message.obtain(myHandler, Parameter.BT_CONNECT_FAIL, "Receive message error"));
            }
        }
        @Override
        public void run() {
            Log.i(TAG, "Begin BTRecvThread");
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            while(!stopReq) {
                try {
                    bytes = inStream.read(buffer);
                    String read = new String(buffer, 0, bytes);
                    msgRecv.append(read);

                    if (read.contains("\n")) {
                        Log.d(TAG, read);
                        myHandler.obtainMessage(Parameter.BTMSG_RECV_SUCCESS, msgRecv.toString()).sendToTarget();
                        msgFormatter.decodeAndAct(msgRecv.toString());
                        msgRecv.setLength(0);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "bluetooth recv message error");
                    myHandler.sendMessage(Message.obtain(myHandler, Parameter.BT_CONNECT_FAIL, "Receive message error"));
                    SystemClock.sleep(5000);
                }
            }
        }

        void start(){
            Thread t = new Thread(this);
            t.start();
        }
        void stop(){
            stopReq = true;
        }
    }

    public void sendMsgOnce(String toSend){
        BTSendOnceTh btsend = new BTSendOnceTh(toSend);
        btsend.start();
    }

    public void startRecv(){
        btrecv = new BTRecvTh();
        btrecv.start();
    }

    public void cancelRecv(){
        btrecv.stop();
        btrecv = null;
    }
}

