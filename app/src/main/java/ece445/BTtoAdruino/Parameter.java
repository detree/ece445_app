package ece445.BTtoAdruino;

/**
 * Created by SC.
 *
 * @Description:
 */

final class Parameter {
    private Parameter(){}
    static final int BTMSG_SEND_SUCCESS = 1;
    static final int BTMSG_RECV_SUCCESS = 2;
    static final int BT_CONNECT_FAIL = 3;
    static final int BT_DISCONNECT = 4;
    static final int GPSMSG_UPDATED = 5;
    static final int GPSMSG_DIST_UPDATED = 6;

    static final int BTMSG_UPDATE_WEIGHT = 10;
    static final int BTMSG_UPDATE_VOLUME = 11;


}
