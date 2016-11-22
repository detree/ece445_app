package ece445.BTtoAdruino;

import android.os.Handler;

import junit.framework.TestCase;

/**
 * Created by SC.
 *
 * @Description:
 */
public class MsgFormatterTest extends TestCase {
    Handler handler = new Handler();
    MsgFormatter formatter = new MsgFormatter(handler);

    public void testDecodeAndAct() throws Exception {
        formatter.decodeAndAct("V:10\nW:10\n");
    }

    public void testEncodeRecalcWeight() throws Exception {

    }

}