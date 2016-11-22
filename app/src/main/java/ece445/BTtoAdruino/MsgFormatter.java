package ece445.BTtoAdruino;

import android.os.Handler;
import android.util.Log;

/**
 * Created by SC.
 *
 * @Description:
 */

public class MsgFormatter {
    //constants involves decoding the messages==========
    public static final String SentSep = ",";
    public static final String PartSep = ":";
    private final char updateW = 'W';
    private final char updateV = 'V';
    private static final char recalcW = 'R';
    private final String TAG = "MsgFormatter";

    //normal variables for this class
    private Handler myHandler;

    public MsgFormatter(Handler handler){
        myHandler = handler;
    }

    public void decodeAndAct(String in){
        String sent[] = in.split(SentSep);
        for(int i=0;i<sent.length;i++) {
            String parts[] = sent[i].split(PartSep);
            if(parts.length<2) continue;
            if(parts[0].charAt(0)==updateW) {
                myHandler.obtainMessage(Parameter.BTMSG_UPDATE_WEIGHT, Integer.parseInt(parts[1])).sendToTarget();
            }
            else if(parts[0].charAt(0)==updateV) {
                myHandler.obtainMessage(Parameter.BTMSG_UPDATE_VOLUME, Integer.parseInt(parts[1])).sendToTarget();
            }else{
                Log.d(TAG, "unregistered message header:" + parts[0]);
            }
        }
    }

    static public String encodeRecalcWeight(){
        return Character.toString(recalcW)+PartSep+Integer.toString(0)+SentSep;
    }
}
