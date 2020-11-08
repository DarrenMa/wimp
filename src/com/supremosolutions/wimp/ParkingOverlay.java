package com.supremosolutions.wimp;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.supremosolutions.wimp.Utilities;


public class ParkingOverlay extends ItemizedOverlay<OverlayItem> {
    private Context mContext;
    private OverlayItem item = null;
    private static final String TAG_NAME = "name";
    private static final String TAG_LAT = "latitude";
    private static final String TAG_LNG = "longitude";
    private static final String TAG_DIS = "distance";
    private static final String disUnit = "km";

    public ParkingOverlay(Drawable marker, GeoPoint point, String name, Context context, String distance) {
        super(marker);
        boundCenterBottom(marker);
        item = new OverlayItem(point, name, distance);
        this.mContext = context;
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return (item);
    }

    @Override
    protected boolean onTap(int i) {
        Toast.makeText(mContext, item.getSnippet(), Toast.LENGTH_SHORT).show();
        // Starting new intent outside an activity is not a good idea as it will cause a flag new task error
        Intent in = new Intent(mContext, ParkingDetail.class);
        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        GeoPoint p = item.getPoint();
        double gpLat = p.getLatitudeE6() / 1E6;
        double gpLng = p.getLongitudeE6() / 1E6;
        in.putExtra(TAG_NAME, item.getTitle());
        in.putExtra(TAG_LAT, String.valueOf(gpLat));
        in.putExtra(TAG_LNG, String.valueOf(gpLng));
        Double distance = Double.valueOf(item.getSnippet());
        String strDis = String.valueOf(Utilities.round(distance, 2));
        strDis = strDis.concat(disUnit);
        in.putExtra(TAG_DIS, strDis);
        mContext.startActivity(in);
        return (true);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
//        // ---when user lifts his finger---
//        if (event.getAction() == 1) {
//            GeoPoint p = mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
//            Toast.makeText(mContext.getApplicationContext(), p.getLatitudeE6() / 1E6 + "," + p.getLongitudeE6() / 1E6,
//                    Toast.LENGTH_SHORT).show();
//        }
//        return false;
//    }

    @Override
    public int size() {
        return (1);
    }

}