package ru.practicum;

public class H2DistanceFunction {
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0;
        }

        double radLat1 = Math.PI * lat1 / 180;
        double radLat2 = Math.PI * lat2 / 180;
        double theta = lon1 - lon2;
        double radTheta = Math.PI * theta / 180;
        double dist = Math.sin(radLat1) * Math.sin(radLat2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radTheta);

        if (dist > 1) dist = 1;

        dist = Math.acos(dist);
        dist = dist * 180 / Math.PI;
        dist = dist * 60 * 1.8524;

        return dist;
    }
}
