package com.example.marcello.applam;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marcello.applam.data.Rilevazione;
import com.example.marcello.applam.db.RilevazioneDatabase;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.marcello.applam.Segnale;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    Spinner sp ;

    String names[] = {"4G","3G","WIFI"};

    ArrayAdapter<String> adapter;

    String record= "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    makeUseOfNewLocation(location);
                }
            }
        };

        //CODICE PER SPINNER
        sp = (Spinner)findViewById(R.id.spinner);

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,names);

        sp.setAdapter(adapter);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position)

                {

                    case 0:

                        record = "LTE";
                        Log.d("miotag","ho scelto ->"+record);
                        mMap.clear();
                        CaricaDati(record);
                        break;

                    case 1:

                        record = "3G";
                        Log.d("miotag","ho scelto ->"+record);
                        mMap.clear();
                        CaricaDati(record);
                        break;

                    case 2:

                        record = "WIFI";
                        Log.d("miotag","ho scelto ->"+record);
                        mMap.clear();
                        CaricaDati(record);
                        break;

                }

            }

            @Override

            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
    //fine del codice spinner
    }

    public void makeUseOfNewLocation(final Location location) {
        setMapCamera(location);
        final Double latitudine = location.getLatitude();
        final Double longitudine = location.getLongitude();
        final long tempo = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        Segnale segnale = new Segnale(getApplicationContext());
        String tipo = segnale.networkType();
        Segnale segnaleT = new Segnale(getApplicationContext());
        final int s = segnaleT.getSignalStrenght(tipo);
        final int opacita=1;
        boolean okrete=false;

        if(tipo.equals("UMTS") || tipo.equals("HSPA") || tipo.equals("HSDPA") || tipo.equals("HSUPA") || tipo.equals("HSPA+")) {
            tipo = "3G";
        }
        if(tipo.equals(record)) { //se la rete effettiva e la selezione corrispondono
            okrete=true;//le reti corrispondono
        }

        final boolean finalOkrete = okrete;
        final String finalTipo = tipo;
        new AsyncTask<Void, Void, Void>(){
            List<Rilevazione> listacerchi;
            @Override
            protected Void doInBackground(Void... voids) {
                listacerchi = RilevazioneDatabase.getInstance(getApplicationContext()).getRilevazioneDao().trovaRilevazioneByTipo(MainActivity.this.record);

                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(finalOkrete==true) {
                    if (listacerchi.size() > 0) {
                        boolean vicino = false;
                        for (int i = 0; i < listacerchi.size(); i++) {//per ogni voce del database
                            Log.d("miotag", "db-> connessione tipo: " + listacerchi.get(i).getTipoConn() + " potenza: " + listacerchi.get(i).getPotenza() + " latitudine: " + listacerchi.get(i).getLatitude() + " longitudine: " + listacerchi.get(i).getLongitude());
                            Rilevazione r1 = listacerchi.get(i);
                            try {
                                Location locationOld = new Location("");
                                locationOld.setLatitude(listacerchi.get(i).getLatitude());
                                locationOld.setLongitude(listacerchi.get(i).getLongitude());
                                if (location.distanceTo(locationOld) < 5 && finalOkrete == true) {//se il cerchio nuovo è troppo vicino al cerchio vecchio
                                    vicino = true;//la distanza è minore di 5 metri
                                    if(tempo-(listacerchi.get(i).getOrario())>60){//se il cerchio è più vecchio di un minuto
                                        listacerchi.get(i).setPotenza((s+r1.getPotenza())/2);
                                        listacerchi.get(i).setOpacita(r1.getOpacita()+1);//NUOVA OPACITA'
                                        listacerchi.get(i).setOrario(tempo);

                                        try{
                                            Operazione task = new Operazione(listacerchi.get(i));
                                            task.execute();
                                        }
                                        catch (
                                                Exception e){
                                            Log.d("miotag", "eccezione " + e.getMessage());
                                        }
                                        mMap.clear();//cancella la mappa
                                        CaricaDati(finalTipo);//ricarica la mappa
                                    }
                                }
                            }
                            catch (
                                    Exception e) {
                                Log.d("miotag", "eccezione " + e.getMessage());
                            }
                        }
                        if (vicino == false && finalOkrete == true) {
                            DrawCircle(s, latitudine, longitudine, finalTipo, opacita, tempo);
                            AggiungiADB task1 = new AggiungiADB(s,latitudine,longitudine,finalTipo,opacita,tempo);
                            task1.execute();
                        }
                    }
                    else {
                        //la lista è VUOTA
                        DrawCircle(s, latitudine, longitudine, finalTipo, opacita, tempo);
                        try {
                            AggiungiADB task1 = new AggiungiADB(s, latitudine, longitudine, finalTipo, opacita, tempo);
                            task1.execute();
                        }
                        catch (
                                Exception e) {
                            Log.d("miotag", "eccezione " + e.getMessage());
                        }
                    }
                }
            }
        }.execute();

    }
    public class Operazione extends AsyncTask<Void, Void, Void> {
        private Rilevazione ril;
        public  Operazione(Rilevazione r){
            super();
            ril=r;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            RilevazioneDatabase.getInstance(getApplicationContext()).getRilevazioneDao().aggiornaRilevazione(ril);
            return null;
        }
    }
    public class AggiungiADB extends AsyncTask<Void, Void, Void>{
        private int s;
        private Double latitudine;
        private Double longitudine;
        private String tipo;
        private int opacita;
        private long tempo;
        public  AggiungiADB(int s1, Double latitudine1, Double longitudine1, String tipo1, int opacita1, long tempo1){
            super();
            s=s1;
            latitudine=latitudine1;
            longitudine=longitudine1;
            tipo=tipo1;
            opacita=opacita1;
            tempo=tempo1;
        }

            @Override
            protected Void doInBackground(Void... voids) {

                Rilevazione r = new Rilevazione();
                r.setTipoConn(tipo);
                r.setPotenza(s);
                r.setLatitude(latitudine);
                r.setLongitude(longitudine);
                r.setOrario(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
                r.setOpacita(1);

                Log.d("miotag","AGGIUNTO A DB: tipo segnale: "+tipo+" potenza: "+s+" latitudine: "+latitudine+" longitudine: "+longitudine+ " orario: "+r.getOrario()+" opacità: "+r.getOpacita());
                try {
                    Long idRow = RilevazioneDatabase.getInstance(getApplicationContext()).getRilevazioneDao().nuovaRilevazione(r);
                }
                catch(
                        Exception e){
                    Log.d("miotag","eccezione "+e.getMessage());
                }
                return null;
            }
    }

    public void setMapCamera(Location location) {
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
            .target(latlng)
            .zoom(18)
            .tilt(0)
            .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void CaricaDati(String record){
        new AsyncTask<Void, Void, Void>(){
            List<Rilevazione> listacerchi;
            @Override
            protected Void doInBackground(Void... voids) {
                //leggi i dati
                listacerchi = RilevazioneDatabase.getInstance(getApplicationContext()).getRilevazioneDao().trovaRilevazioneByTipo(MainActivity.this.record);

                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                for (int i = 0; i < listacerchi.size(); i++) {
                    Log.d("miotag", "CARICA db-> connessione tipo: " + listacerchi.get(i).getTipoConn() +" potenza: "+listacerchi.get(i).getPotenza()+" OPACITA': "+listacerchi.get(i).getOpacita()+" latitudine: "+listacerchi.get(i).getLatitude()+" longitudine: "+listacerchi.get(i).getLongitude());
                    try {
                        DrawCircle(listacerchi.get(i).getPotenza(), listacerchi.get(i).getLatitude(), listacerchi.get(i).getLongitude(), listacerchi.get(i).getTipoConn(), listacerchi.get(i).getOpacita(), listacerchi.get(i).getOrario());
                    }
                    catch(
                            Exception e){
                        Log.d("miotag","eccezione "+e.getMessage());
                    }
                }
            }
        }.execute();

    }
    public void DrawCircle(final int s, final Double latitudine, final Double longitudine, final String tipo, final int opacita, final long tempo) {
        int color;
        int op=36*opacita;
        if (s == 4) //segnale forte
            color = Color.argb(op, 0, 255, 0); //verde
        else if (s==3) //segnale medio
            color = Color.argb(op, 255, 215, 0); //giallo
        else if (s==2) // segnale basso
            color = Color.argb(op, 255, 140, 0); //arancione
        else if (s==1) // segnale scarso
            color = Color.argb(op, 255, 0, 0); //rosso
        else
            color = Color.argb(0, 0, 0, 0); //trasparente

        final LatLng latlng = new LatLng(latitudine, longitudine);
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(latlng)
                .radius(10)
                .strokeWidth(0)
                .fillColor(color)
        );
    }

    public void createLocationRequest() {
        final LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);//ogni secondo chiede la posizione
        mLocationRequest.setFastestInterval(500);//se c'è un'altra app che chiede la posizione la ottiene da lì
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//utilizza gps, internet e wifi
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {//popup di richiesta
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1020);
            //chiedo l'autorizzazione per il gps
        }
        else{
            mMap.setMyLocationEnabled(true);
            createLocationRequest();
            //ho già l'autorizzazione per il gps
        }


        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setScrollGesturesEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
    }
}