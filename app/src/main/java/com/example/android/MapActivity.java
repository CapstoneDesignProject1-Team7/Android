package com.example.android;

import static android.speech.tts.TextToSpeech.QUEUE_ADD;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, TextToSpeechInitListener {
    private NaverMap mNaverMap;
    private FusedLocationSource locationSource;
    private GoogleApiClient googleApiClient;
    private LocationReceiver locationReceiver;
    BatteryLevelReceiver batteryLevelReceiver;
    private TextView speedTextView;
    private UserDTO userDTO;
    private ArrayList<LocationDTO> nearByUserList;
    private RequestHttpConnection httpConn;
    private Timer timer;
    private int numberOfPeople = -1;
    private TextToSpeechInitializer initTTS;
    private TextToSpeech talk;
    private boolean ttsFlag = false;
    private ArrayList<Marker> markerList;
    private ArrayList<Marker> delMarkerList;
    Boolean completePost = false;
    private int networkFrequency = Constants.NETWORK_FREQUENCY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        googleApiClient = getAPIClientInstance();
        googleApiClient.connect();
        httpConn = new RequestHttpConnection();
        nearByUserList = new ArrayList<LocationDTO>();
        timer = new Timer();
        initTTS = new TextToSpeechInitializer(this, this);
        // RESUMED ???????????? ?????? ?????????
        startWifiService();

        requestGPSSettings();
        showMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationReceiver = new LocationReceiver(new Handler());
        final IntentFilter intentFilter = new IntentFilter("UpdateSpeed");
        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver, intentFilter);

        batteryLevelReceiver = new BatteryLevelReceiver(this);
        IntentFilter batteryIntentFilter = new IntentFilter();
        batteryIntentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        batteryIntentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        batteryIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryIntentFilter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (locationReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
        locationReceiver = null;

        unregisterReceiver(batteryLevelReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationService();
        stopWifiConnectionService();
        // ??? ????????? ??????
        httpConn.deleteUserData(userDTO);
        // ????????? ??????
        if (timer != null)
            timer.cancel();
        // TextToSpeech ??????
        if (talk != null){
            talk.stop();
            talk.shutdown();
        }
    }

    private GoogleApiClient getAPIClientInstance() {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).build();
        return mGoogleApiClient;
    }
    private void requestGPSSettings() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(500);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    Log.i("", "All location settings are satisfied.");
                    //Toast.makeText(getApplication(), "GPS is already enable", Toast.LENGTH_SHORT).show();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    Log.i("", "Location settings are not satisfied. Show the user a dialog to" + "upgrade location settings ");
                    try {
                        status.startResolutionForResult(MapActivity.this, Constants.REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e("Applicationset", e.toString());
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.i("", "Location settings are inadequate, and cannot be fixed here. Dialog " + "not created.");
                    //Toast.makeText(getApplication(), "Location settings are inadequate, and cannot be fixed here", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CHECK_SETTINGS) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "??? ????????? ?????? ?????? ?????? ????????? ??????????????????", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    public void showMap() {
        // ?????? ?????? ??????
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        locationSource = new FusedLocationSource(this, Constants.LOCATION_PERMISSION_REQUEST_CODE);
        // getMapAsync??? ???????????? ???????????? onMapReady ?????? ????????? ??????
        // onMapReady?????? NaverMap ????????? ??????
        mapFragment.getMapAsync(this);
    }

    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        //NaverMap ?????? ????????? ?????? ?????? ??????
        this.mNaverMap = naverMap;
        mNaverMap.setLocationSource(locationSource); //???????????? ??????
        mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        mNaverMap.setMinZoom(16.0);
        mNaverMap.setMaxZoom(18.0);
        mNaverMap.setExtent(new LatLngBounds(new LatLng(31.43, 122.37), new LatLng(44.35, 132)));
        UiSettings uiSettings = mNaverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);
        uiSettings.setZoomControlEnabled(false);
        startService();
        markerList = new ArrayList<>();
        delMarkerList = new ArrayList<>();
        runTimerTask();
    }
    public void runTimerTask(){
        TimerTask getUserDataTask = new TimerTask(){
            @Override
            public void run() {
                if(networkFrequency != Constants.NETWORK_FREQUENCY){
                    // ????????? 15% ????????? ?????? TimerTask ???????????? period ?????? ??? reschedule
                    this.cancel();
                    networkFrequency = Constants.NETWORK_FREQUENCY;
                    runTimerTask();
                }
                //Log.i("??? ??????: ",userDTO.getLatitude()+" "+userDTO.getLongitude());
                UserDataThread getThread = new UserDataThread(userDTO);
                Thread putThread = httpConn.putUserData(userDTO);
                try {
                    if(completePost) {
                        // get ????????? put ??????
                        getThread.start();
                        getThread.join();
                        putThread.start();
                        putThread.join();
                        Log.i("??? ??????: ", userDTO.getLatitude() + " " + userDTO.getLongitude());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                nearByUserList = getThread.getUserData();
                if (nearByUserList!=null) {
                    if(numberOfPeople!=nearByUserList.size()) {
                        // (3??? ?????? 50m ????????? ?????? ?????? ???) != (?????? 50m ????????? ?????? ?????? ???)
                        numberOfPeople = nearByUserList.size();
                        // ???????????? ?????????(1)??? 50m ?????? ????????? ?????? ??????
                        String otherType = userDTO.getType() == 1 ? "????????????" : "????????????";
                        if (ttsFlag) {
                            talk.speak("???????????? ?????? " + otherType + numberOfPeople + "??? ????????????", QUEUE_ADD, null);
                        }
                    }
                    for (int i = 0; i < numberOfPeople; i++) {
                        // ?????? ??? ?????? ?????? ??????
                        LocationDTO nearByUser = nearByUserList.get(i);
                        double distance = DistanceCalculate.getDistance(nearByUser.getLatitude(), nearByUser.getLongitude(), userDTO.getLatitude(), userDTO.getLongitude()); // ??????
                        if (distance<=10){
                            // 10m ?????? ?????????/???????????? ????????? ????????? ??????
                            ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
                            tone.startTone(ToneGenerator.TONE_DTMF_S, 500);
                            nearByUser.setWithin10m(true);
                        }else{
                            nearByUser.setWithin10m(false);
                        }
                        Marker m = new Marker();
                        setMarker(m, nearByUser);
                        markerList.add(m); // ?????? ???????????? ?????? ??????
                    }

                    MapActivity.this.runOnUiThread(new Runnable(){
                        public void run(){
                            // ?????? ??????
                            for (int i=0;i<delMarkerList.size();i++){
                                Marker marker = delMarkerList.get(i);
                                marker.setMap(null);
                            }
                            delMarkerList.clear();
                            // ?????? ?????????
                            for (int i = 0; i<markerList.size();i++){
                                Marker marker = markerList.get(i);
                                delMarkerList.add(marker);
                                marker.setMap(mNaverMap);
                            }
                            markerList.clear();
                        }
                    });
                }else{
                    Log.i("nearByUserList ", "NULL");
                }
            }
        };
        // 3??? ?????? ??????
        timer.schedule(getUserDataTask, 0, networkFrequency);
        timer.purge();
    }
    private void setMarker(Marker marker, LocationDTO locationDTO){
        // ?????? ?????????, ??????, ?????? ??? ??????
        //marker.setIconPerspectiveEnabled(true); // ????????????
        marker.setAlpha(0.8f); //?????????
        double lat = locationDTO.getLatitude();
        double lng = locationDTO.getLongitude();
        marker.setPosition(new LatLng(lat, lng));
        int markerSize = locationDTO.isWithin10m() ? 180 : 100;
        int img = R.drawable.ic_baseline_location_on_36_slow;
        if (userDTO.getType()==0){ //???????????? ????????????
            if (locationDTO.getVelocity() >= 30){
                // ????????? ????????? 30km/h ??????
                img = R.drawable.ic_baseline_location_on_36;
            }
        }else{ // ???????????? ????????????
            if (locationDTO.getVelocity() >= 7){
                // ????????? ????????? 7km/h ??????
                img = R.drawable.ic_baseline_location_on_36;
            }
        }
        marker.setIcon(OverlayImage.fromResource(img));
        marker.setHeight(markerSize);
        marker.setWidth(markerSize);
    }
    private void startService(){
        startLocationService();
        Intent intent = getIntent();
        int type = intent.getIntExtra("type", -1); // ????????? 1 ????????? 0
        String id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID); // ????????? id
        // ??? ???????????? ?????? - userDTO ??????
        userDTO = new UserDTO(id, type, 0.0, 0.0);

    }
    private void startWifiService(){
        Intent intent = new Intent(getApplicationContext(), WifiConnectionService.class);
        intent.setAction(Constants.ACTION_START_WIFI_SERVICE);
        startService(intent);
        turnOnWifi();
    }

    private void turnOnWifi(){
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null){
            if(!wifiManager.isWifiEnabled()) {
                // ??????????????? Q ?????? ??????????????? ?????? ??????
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                    wifiManager.setWifiEnabled(true);
                // ?????? ????????? ??????????????? ??? ????????? ???????????? ???????????? ??????.
                else {
                    ActivityResultLauncher<Intent> wifiPanelResult = registerForActivityResult(
                            new ActivityResultContracts.StartActivityForResult(),
                            new ActivityResultCallback<ActivityResult>() {
                                @Override
                                public void onActivityResult(ActivityResult result) {
                                    if(result.getResultCode() == Activity.RESULT_OK){
                                        Log.i("WIFI", "SET WIFI");
                                    }
                                }
                            }
                    );
                    Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                    wifiPanelResult.launch(panelIntent);
                }
            }
        }
    }


    private boolean isServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
                else if(WifiConnectionService.class.getName().equals(service.service.getClassName())){
                    if (service.foreground) return true;
                }
            }
            return false;
        }
        return false;
    }
    private void startLocationService() {
        if (!isServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "???????????? ???????????????", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService() {
        if (isServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "???????????? ???????????????", Toast.LENGTH_SHORT).show();
        }
    }
    public void stopWifiConnectionService(){
        if (isServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), WifiConnectionService.class);
            intent.setAction(Constants.ACTION_STOP_WIFI_SERVICE);
            startService(intent);
        }
    }

    public class LocationReceiver extends BroadcastReceiver {
        private final Handler handler;
        public LocationReceiver(Handler handler){
            this.handler = handler;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            // an Intent broadcast.
            Bundle b = intent.getExtras();
            double latitude = b.getDouble("latitude");
            double longitude = b.getDouble("longitude");
            int speed = b.getInt("speed");
            speedTextView = findViewById(R.id.speed);
            userDTO.setSpeed(speed);
            userDTO.setLatitude(latitude);
            userDTO.setLongitude(longitude);
            if(!completePost){
                httpConn.postUserData(userDTO);
                completePost = true;
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String text = String.valueOf(speed);
                    speedTextView.setText(text);
                }
            });
        }
    }

    @Override
    public void onSuccess(TextToSpeech tts){
        // TextToSpeechInitListener ??????????????? ??????
        this.talk = tts;
        ttsFlag = true;
    }
    @Override
    public void onFailure(TextToSpeech tts){
        // TextToSpeechInitListener ??????????????? ??????
        ttsFlag = false;
        finish();
    }
}
