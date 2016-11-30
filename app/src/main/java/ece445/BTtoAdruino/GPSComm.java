package ece445.BTtoAdruino;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by SC.
 *
 * @Description:
 */

class GPSComm implements LocationListener {
    private String TAG = "GPSComm";
    private GoogleApiClient mPlayApi = null;
    private Handler mHandler = null;
    Location lastLoc = null;
    Context contextIn = null;
    LocationRequest mLocReq = null;
    static int msgcnt = 0;
    double totalDist = 0;

    GPSComm(GoogleApiClient api, Handler handler, Context context) {
        mPlayApi = api;
        mHandler = handler;
        contextIn = context;
    }

    void firstComm() {
        if (ActivityCompat.checkSelfPermission(contextIn, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(contextIn, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(contextIn, "no permission to GPS", Toast.LENGTH_LONG).show();
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lastLoc = LocationServices.FusedLocationApi.getLastLocation(
                mPlayApi);
        locToHandler(lastLoc);
    }

    private void locToHandler(Location loc) {
        StringBuilder sb = new StringBuilder();
        sb.append(msgcnt);
        sb.append("th message\n");
        sb.append("Lattitude=");
        sb.append(loc.getLatitude());
        sb.append("\nLongitude=");
        sb.append(loc.getLongitude());
        mHandler.obtainMessage(Parameter.GPSMSG_UPDATED, sb.toString()).sendToTarget();
        msgcnt++;
    }

    private void precisionToHandler(Location loc) {
        StringBuilder sb = new StringBuilder();
        sb.append(msgcnt);
        sb.append("th message\n");
        sb.append("not accurate enough\n");
        sb.append(loc.getAccuracy());
        mHandler.obtainMessage(Parameter.GPSMSG_UPDATED, sb.toString()).sendToTarget();
        msgcnt++;
    }

    void startUpdate() {
        if (lastLoc == null) {
            Toast.makeText(contextIn, "first time GPS FAIL", Toast.LENGTH_LONG).show();
            return;
        }
        lastLoc = null;
        mLocReq = new LocationRequest();
        mLocReq.setInterval(1000);
        mLocReq.setFastestInterval(500);
        mLocReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(contextIn, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(contextIn, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(contextIn, "no permission to GPS", Toast.LENGTH_LONG).show();
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mPlayApi, mLocReq, this);
        Log.d(TAG, "startUpdate() ended");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "location changed");
        if(location.getAccuracy()<16 && location.getAccuracy()!=0.0) {
            if(lastLoc == null) lastLoc = location;
            double dx = lastLoc.distanceTo(location);
            lastLoc = location;
            totalDist += dx;
            mHandler.obtainMessage(Parameter.GPSMSG_DIST_UPDATED, totalDist).sendToTarget();
            locToHandler(location);
        }else{
            precisionToHandler(location);
        }
    }
}
