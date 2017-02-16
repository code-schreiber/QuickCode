package com.schreiber.code.seamless.aperol.util.android;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.schreiber.code.seamless.aperol.util.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class NetworkUtils {

    private NetworkUtils() {
        // Hide utility class constructor
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (android.net.ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.isConnected();
    }

    public static <T> byte[] serialize(T o) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
        } catch (IOException e) {
            Logger.logException(e);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    Logger.logException(e);
                }
            }
        }
        return baos.toByteArray();
    }

    public static Object deSerialize(byte[] serObject) {
        ByteArrayInputStream bais = new ByteArrayInputStream(serObject);
        try {
            return new ObjectInputStream(bais).readObject();
        } catch (ClassNotFoundException | IOException e) {
            Logger.logException(e);
        }
        return null;
    }
}
