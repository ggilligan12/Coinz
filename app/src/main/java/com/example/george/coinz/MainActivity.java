package com.example.george.coinz;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
    private String downloadDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
    private final String PreferencesFile = "MyPrefsFile"; // for storing preferences

    // -------Lifecycle Functions-------- //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Mapbox Access token
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                SharedPreferences FromFile = getSharedPreferences(PreferencesFile, Context.MODE_PRIVATE);
                if (FromFile.contains(downloadDate)){
                    mapData = FromFile.getString(downloadDate, "");
                    Log.d(tag, "msg  onReady map data has been taken from file");
                }
                else {
                    Log.d(tag, "msg  onReady map data has been taken from file");
                    DownloadFileTask download = new DownloadFileTask();
                    download.execute("http://homepages.inf.ed.ac.uk/stg/coinz/2019/01/01/coinzmap.geojson");

                    try { mapData = download.get(); }
                    catch (ExecutionException e) { e.printStackTrace(); }
                    catch (InterruptedException e) { e.printStackTrace(); }

                    if (mapData == null) { Log.e("debuggage", "MapData is null"); }
                    else { Log.e(tag, "Its not null, ehhhh");}
                }

                if (mapboxMap == null) { Log.d(tag, "[onMapReady] mapBox is null"); }
                else {
                    //new TestDialogue().show(getSupportFragmentManager(),"tag"); //Testing
                    //new TestDialogue().show(getSupportFragmentManager(),"tag"); //Testing
                    map = mapboxMap;
                    // Make location information available
                    enableLocation();
                    // Set user interface options
                    map.getUiSettings().setCompassEnabled(true);
                    map.getUiSettings().setZoomControlsEnabled(true);
                    //new TestDialogue().show(getSupportFragmentManager(),"tag"); //Testing
                    Log.e("debuggage", "yet another error message");

                    List<Feature> features = FeatureCollection.fromJson(mapData).features(); //Doesn't get past this line!!!!!!

                    new TestDialogue().show(getSupportFragmentManager(),"tag"); //Testing

                    for (int i = 0; i<features.size(); i++){
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
                                    .setSnippet("Value -" +strValue));
                            Log.d(tag, "[onMapReady] Adding marking "+i+" to map");
                        }
                        catch (JSONException e){ e.printStackTrace(); }
                    }

                }
            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();
        if (locationEngine != null) {locationEngine.requestLocationUpdates();}
        if (locationLayerPlugin != null) {locationLayerPlugin.onStart();}
        mapView.onStart();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PreferencesFile, Context.MODE_PRIVATE);
        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "");
        Log.d(tag, "[onStart] Recalled lastDownloadDate is ’" + downloadDate + "’");
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
        Log.d(tag, "[onStop] Storing lastDownloadDate of " + downloadDate);
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PreferencesFile, Context.MODE_PRIVATE);
        // We need an Editor object to make preference changes.
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", downloadDate);
        // Apply the edits!
        editor.apply();
    }

    // Auxiliary methods
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        if (mapboxMap == null) { Log.d(tag, "[onMapReady] mapBox is null"); }
        else {
            map = mapboxMap;
            // Set user interface options
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            // Make location information available
            enableLocation();
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


}
