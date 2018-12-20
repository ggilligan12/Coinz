package com.example.george.coinz;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener {


    private String tag = "MainActivity";
    private MapView mapView;
    private MapboxMap map;
    public String mapData;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;
    private String todaysDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
    private final String PreferencesFile = "MyPrefsFile"; // for storing preferences

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // -------Lifecycle Functions-------- //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Mapbox related stuff.
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        ///// BUTTONS!! /////

        // Transferring to the settings activity.
        FloatingActionButton settingsButton = findViewById(R.id.btn_settings);
        settingsButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        // Transferring to the items activity.
        FloatingActionButton itemsButton = findViewById(R.id.btn_items);
        itemsButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ItemsActivity.class)));

        // Transferring to the mail activity.
        FloatingActionButton mailButton = findViewById(R.id.btn_mail);
        mailButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MailActivity.class)));

        // Transferring to the bank activity.
        FloatingActionButton bankButton = findViewById(R.id.btn_bank);
        bankButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BankActivity.class)));

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (locationEngine != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) { return; }
            locationEngine.requestLocationUpdates();}
        if (locationLayerPlugin != null) {locationLayerPlugin.onStart();}
        mapView.onStart();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PreferencesFile, Context.MODE_PRIVATE);
        // Using ”” as the default value as this might be the first time the app is run.
        todaysDate = settings.getString("lastDownloadDate", "");
        Log.d(tag, "[onStart] Recalled lastDownloadDate is ’" + todaysDate + "’");
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    // Made public to fix, review.
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    /*@Override
    protected void onSaveInstanceState() {
        super.onSaveInstanceState();
        mapView.onSaveInstanceState();
    }*/
    @Override
    public void onStop() {
        super.onStop();
        Log.d(tag, "[onStop] Storing lastDownloadDate of " + todaysDate);
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PreferencesFile, Context.MODE_PRIVATE);
        // We need an Editor object to make preference changes.
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", todaysDate);
        // Apply the edits!
        editor.apply();
    }


    // onMapReady, takes mapbox instance, downloads and drops the GeoJson markers of the day,
    // and implements an onClickListener to pick them up and add them to a users Firestore account.
    @Override
    public void onMapReady(MapboxMap mapboxMap) {

        String todaysDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
        SharedPreferences FromFile = getSharedPreferences(PreferencesFile, Context.MODE_PRIVATE);
        if (FromFile.contains(todaysDate)){
            mapData = FromFile.getString(todaysDate, "");
            Log.d(tag, "msg  onReady map data has been taken from file");
        }
        else {
            Log.d(tag, "msg  onReady map data has been taken from file");

            // Creating an instance of our AsyncTask class to pull the geoJson data down.
            DownloadFileTask download = new DownloadFileTask();
            download.execute("http://homepages.inf.ed.ac.uk/stg/coinz/"+todaysDate+"/coinzmap.geojson");

            // Force program to wait until the download has completed before attempting to drop markers.
            try { mapData = download.get(); }
            catch (ExecutionException e) { e.printStackTrace(); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }

        if (mapboxMap == null) { Log.d(tag, "[onMapReady] mapBox is null"); }
        else {
            // Creating the map with location enabled and interface options set.
            map = mapboxMap;
            enableLocation();
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.setOnMarkerClickListener(this::onMarkerClick);

            List<Feature> features = FeatureCollection.fromJson(mapData).features();
            ArrayList<String> markerCurrencies = new ArrayList<>();
            ArrayList<Float> markerValues = new ArrayList<>();
            ArrayList<String> markerIDs = new ArrayList<>();



            for (int i=0; i<features.size(); i++) {
                try {
                    JSONObject jsonObject = new JSONObject(features.get(i).toJson());

                    // Extract coordinates, assign to objects lng and lat.
                    JSONArray coords = jsonObject.getJSONObject("geometry").getJSONArray("coordinates");
                    double lng = Double.parseDouble(coords.get(0).toString());
                    double lat = Double.parseDouble(coords.get(1).toString());

                    // Get the Coin's unique ID.
                    String id = jsonObject.getJSONObject("properties").getString("id");

                    // Get the value associated with this particular Coin.
                    double value = jsonObject.getJSONObject("properties").getDouble("value");
                    String strValue = Double.toString(value);

                    // Get the currency of this particular Coin.
                    String currency = jsonObject.getJSONObject("properties").getString("currency");

                    // Add the marker on the map.
                    mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat,lng))
                            .title(currency)
                            .setSnippet("Value: "+strValue));
                    markerCurrencies.add(currency);
                    markerValues.add(Float.parseFloat(strValue));
                    markerIDs.add(id);
                    Log.d(tag, "[onMapReady] Adding marking "+i+" to map");
                }
                catch (JSONException e) { e.printStackTrace(); }
            }
            setMarkerCur(markerCurrencies);
            setMarkerVal(markerValues);
            setMarkerID(markerIDs);

        }
    }








    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Log.d(tag, "Permissions are granted");
            initializeLocationEngine();
            initializeLocationLayer();
        }
        else {
            Log.d(tag, "Permissions are not granted");
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setInterval(5000); // preferably every 5 seconds
        locationEngine.setFastestInterval(1000); // at most every second
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) { originLocation = lastLocation; setCameraPosition(lastLocation); }
        else { locationEngine.addLocationEngineListener(this); }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer() {
        if (mapView == null) { Log.d(tag, "mapView is null"); }
        else {
            if (map == null) { Log.d(tag, "map is null"); }
            else {
                locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
                locationLayerPlugin.setLocationLayerEnabled(true);
                locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
                locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
            }
        }
    }

    private void setCameraPosition(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        Log.d(tag, "[onConnected] requesting location updates");
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) { Log.d(tag, "[onLocationChanged] location is null"); }
        else {
            Log.d(tag, "[onLocationChanged] location is not null");
            originLocation = location;
            setCameraPosition(location);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Log.d(tag, "Permissions: " + permissionsToExplain.toString());
    }

    @Override
    public void onPermissionResult(boolean granted) {
        Log.d(tag, "[onPermissionResult] granted == " + granted);
        if (granted) { enableLocation(); }
        else {
        // Open a dialogue with the user
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /// Getters, Setters, and ArrayLists to keep track of currency ///
    private static ArrayList<String> markerCur = new ArrayList<>();
    public static void setMarkerCur(ArrayList<String> markerCur) { MainActivity.markerCur = markerCur; }
    public static ArrayList<String> getMarkerCur() { return markerCur; }

    private static ArrayList<Float> markerVal = new ArrayList<>();
    public static void setMarkerVal(ArrayList<Float> markerVal) { MainActivity.markerVal = markerVal; }
    public static ArrayList<Float> getMarkerVal() { return markerVal; }

    private static ArrayList<String> markerID = new ArrayList<>();
    //public static ArrayList<String> getMarkerID() { return markerID; }
    public static void setMarkerID(ArrayList<String> markerID) { MainActivity.markerID = markerID; }


    public boolean onMarkerClick (Marker mr) {

        mAuth = FirebaseAuth.getInstance();
        String email = mAuth.getCurrentUser().getEmail();
        CollectionReference users = db.collection("users");
        List<Marker> mrkrs = map.getMarkers();

        // Getting our location, and the value and currency of the marker.
        String cur = getMarkerCur().get(mrkrs.indexOf(mr));
        Log.d(tag, "curr is " + cur);
        Float val = getMarkerVal().get(mrkrs.indexOf(mr));
        Log.d(tag, mr.getTitle());
        LatLng loc = new LatLng(originLocation.getLatitude(), originLocation.getLongitude());

        // Proximity check.
        if (mr.getPosition().distanceTo(loc) <= 25 && !(markerID.get(mrkrs.indexOf(mr)).isEmpty())) {

            switch (cur) {
                case ("SHIL"):
                    Map<String, Object> doc0 = new HashMap<>();
                    doc0.put("SHIL", val);
                    users.document(email).set(doc0, SetOptions.merge()); //Preventing overwriting.
                    Toast.makeText(MainActivity.this, "\n You found " + val + " SHILs!", Toast.LENGTH_LONG).show();
                    break;

                case ("DOLR"):
                    Map<String, Object> doc1 = new HashMap<>();
                    doc1.put("DOLR", val);
                    users.document(email).set(doc1, SetOptions.merge()); //Preventing overwriting.
                    Toast.makeText(MainActivity.this, "\n You found " + val + " DOLRs!", Toast.LENGTH_LONG).show();
                    break;

                case ("QUID"):
                    Map<String, Object> doc2 = new HashMap<>();
                    doc2.put("QUID", val);
                    users.document(email).set(doc2, SetOptions.merge()); //Preventing overwriting.
                    Toast.makeText(MainActivity.this, "\n You found " + val + " QUID!", Toast.LENGTH_LONG).show();
                    break;

                case ("PENY"):
                    Map<String, Object> doc3 = new HashMap<>();
                    doc3.put("PENY", val);
                    users.document(email).set(doc3, SetOptions.merge()); //Preventing overwriting.
                    Toast.makeText(MainActivity.this, "\n You found " + val + " PENYs!", Toast.LENGTH_LONG).show();
                    break;

            }

            markerID.set(mrkrs.indexOf(mr), " ");
            map.removeMarker(mr);
            Log.d(tag, "click click hurrah!");

            return true;

        }

        else {
            Log.d(tag, "the click failed... why click why");
            return false;
        }

    }


}
