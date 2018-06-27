package com.square.renov.swipevoicechat.Util;

import android.location.Location;

import com.square.renov.swipevoicechat.Model.User;

public class DistanceUtil {
    public static int getDistanceFromLatLng(User other, User me){

        Location startLocation = new Location("");
        startLocation.setLatitude(Double.valueOf(other.getLat()));
        startLocation.setLongitude(Double.valueOf(other.getLng()));

        Location endLocation = new Location("");
        endLocation.setLatitude(Double.valueOf(me.getLat()));
        endLocation.setLongitude(Double.valueOf(me.getLng()));
        float result = startLocation.distanceTo(endLocation);

        return (int) result / 1000;
    }
}
