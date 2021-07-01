package com.example.marcello.applam;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class Segnale{
    private Context context;

    public Segnale (Context c){
        this.context = c;
    }

    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    public boolean isConnectedToWifi() {
        NetworkInfo networkInfo = getNetworkInfo(context);
        return (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public boolean isConnectedToMobile() {
        NetworkInfo networkInfo = getNetworkInfo(context);
        return (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    public String networkType() {
        if (isConnectedToWifi()) {//sono in wifi
            return "WIFI";
        }
        else if (isConnectedToMobile()) {//sono in mobile
            TelephonyManager teleMan = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int networkType = teleMan.getNetworkType();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return "1xRTT";
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return "CDMA";
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return "EDGE";
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                    return "eHRPD";
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return "EVDO rev. 0";
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return "EVDO rev. A";
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    return "EVDO rev. B";
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return "GPRS";
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return "HSPA";
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return "iDen";
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return "LTE";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return "UMTS";
                default:
                    return "Unknown1";
            }
        } else {
            return "Unknown";
        }
    }

    public int getSignalStrenght(String signalType) {
        int signalStrenght = 0;
        int colore = 9;
        if(signalType.equals("WIFI")){
            if (isConnectedToWifi()) { //WIFI
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                signalStrenght = wifiInfo.getRssi();
                if(signalStrenght > -66) {
                    Log.d("miotag", "potenza segnale WIFI " + signalStrenght + "dBm: Ottima");
                    colore = 4;
                }
                else if(signalStrenght > -71 && signalStrenght < -67){
                    Log.d("miotag","potenza segnale WIFI "+signalStrenght+"dBm: Buona");
                    colore = 3;
                }
                else if(signalStrenght > -80 && signalStrenght < -70){
                    Log.d("miotag","potenza segnale WIFI "+signalStrenght+"dBm: Bassa");
                    colore = 2;
                }
                else if(signalStrenght < -81) {
                    Log.d("miotag", "potenza segnale WIFI " + signalStrenght + "dBm: Cattiva");
                    colore = 1;
                }

            }
        }
        else if (isConnectedToMobile()) {//mobile
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //se 3g
                if (signalType.equals("UMTS") || signalType.equals("HSPA") || signalType.equals("HSDPA") || signalType.equals("HSUPA") || signalType.equals("HSPA+")) {
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(0);
                    signalStrenght = cellInfoWcdma.getCellSignalStrength().getDbm();
                    if(signalStrenght > -75){
                        Log.d("miotag","potenza segnale 3g "+signalStrenght+"dBm: Ottima");
                        colore = 4;
                    }
                    else if(signalStrenght > -90 && signalStrenght < -76){
                        Log.d("miotag","potenza segnale 3g "+signalStrenght+"dBm: Buona");
                        colore = 3;
                    }
                    else if(signalStrenght > -100 && signalStrenght < -91) {
                        Log.d("miotag", "potenza segnale 3g " + signalStrenght + "dBm: Bassa");
                        colore = 2;
                    }
                    else if(signalStrenght < -101){
                        Log.d("miotag","potenza segnale 3g "+signalStrenght+"dBm: Cattiva");
                        colore = 1;
                    }
                }

                else if (signalType.equals("LTE")) { //4G
                    CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
                    signalStrenght = cellInfoLte.getCellSignalStrength().getDbm();
                    if(-signalStrenght < -112) {
                        Log.d("miotag", "potenza segnale 4G -" + signalStrenght + "dBm: Cattiva");
                        colore = 1;
                    }
                    else if(-signalStrenght > -111 && -signalStrenght < -103) {
                        Log.d("miotag", "potenza segnale 4G -" + signalStrenght + "dBm: Bassa");
                        colore = 2;
                    }
                    else if(-signalStrenght > -102 && -signalStrenght < -85) {
                        Log.d("miotag", "potenza segnale 4G -" + signalStrenght + "dBm: Buona");
                        colore = 3;
                    }
                    else if(-signalStrenght> -84) {
                        Log.d("miotag", "potenza segnale 4G -" + signalStrenght + "dBm: Ottima");
                        colore = 4;
                    }
                }
            }
        } else {
            Toast.makeText(context,"Sei offline, connettiti ad una rete per effettuare rilevazioni", Toast.LENGTH_LONG).show();
            signalStrenght = -1;
            return 8;
        }
        return colore;
    }

}
