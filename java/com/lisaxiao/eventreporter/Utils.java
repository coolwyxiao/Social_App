package com.lisaxiao.eventreporter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;

import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static String username= null;
    // this class is designed to use md5 to encrypt password
    public static String md5Encryption(final String input){
        String result = "";
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(input.getBytes(Charset.forName("UTF8")));
            byte[] resultByte = messageDigest.digest();
            result = new String(Hex.encodeHex(resultByte));
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return result;
    }

    //transform time unit to different time format
    public static String timeTransformer(long mills){
        long currenttime = System.currentTimeMillis();
        long diff = currenttime-mills;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if(seconds < 60){
            return seconds + "seconds ago";
        }else if(minutes < 60){
            return minutes + "minutes ago";
        }else if(hours < 24){
            return hours + "hours ago";
        }else{
            return days + "days ago";
        }

    }

    //Download an Image from the given URL, then decodes and returns a Bitmap object
    public static Bitmap getBitmapFromURL(String imageUrl){
        Bitmap bitmap = null;

        if(bitmap == null){
            try{
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Error:",e.getMessage().toString());
            }
        }
        return bitmap;
    }

    public static int distanceBetweenTwoLocations(double currentLatitute, double currentLongtitute, double destLatitute, double destLongtitute){
        Location currentLocation = new Location("CurrentLocation");
        currentLocation.setLatitude(currentLatitute);
        currentLocation.setLongitude(currentLongtitute);
        Location destLocation = new Location("DestLocation");
        destLocation.setLatitude(destLatitute);
        destLocation.setLongitude(destLongtitute);
        double distance = currentLocation.distanceTo(destLocation);

        double inches = (39.370078 * distance);
        int miles = (int)(inches/63360);
        return miles;
    }
}
