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
        // RESUMED 상태에서 실행 불가능
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
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (locationReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
        locationReceiver = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationService();

        // 내 데이터 삭제
        httpConn.deleteUserData(userDTO);
        // 타이머 종료
        if (timer != null)
            timer.cancel();
        // TextToSpeech 종료
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
                Toast.makeText(this, "내 위치를 알기 위해 위치 접근을 허용해주세요", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    public void showMap() {
        // 지도 객체 생성
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        locationSource = new FusedLocationSource(this, Constants.LOCATION_PERMISSION_REQUEST_CODE);
        // getMapAsync를 호출하여 비동기로 onMapReady 콜백 메서드 호출
        // onMapReady에서 NaverMap 객체를 받음
        mapFragment.getMapAsync(this);
    }

    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        //NaverMap 객체 받아서 위치 소스 지정
        this.mNaverMap = naverMap;
        mNaverMap.setLocationSource(locationSource); //현재위치 표시
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

        TimerTask getUserDataTask = new TimerTask(){
            @Override
            public void run() {
                //Log.i("내 위치: ",userDTO.getLatitude()+" "+userDTO.getLongitude());
                UserDataThread getThread = new UserDataThread(userDTO);
                Thread putThread = httpConn.putUserData(userDTO);
                try {
                    if(completePost) {
                        // get 끝나면 put 실행
                        getThread.start();
                        getThread.join();
                        putThread.start();
                        putThread.join();
                        Log.i("내 위치: ", userDTO.getLatitude() + " " + userDTO.getLongitude());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                nearByUserList = getThread.getUserData();
                if (nearByUserList!=null) {
                    if (nearByUserList.size()>0){
                        if(numberOfPeople!=nearByUserList.size()) {
                            // (3초 전에 50m 주변에 있던 사람 수) != (지금 50m 주변에 있는 사람 수)
                            numberOfPeople = nearByUserList.size();
                            // 사용자가 운전자(1)면 50m 이내 보행자 수를 알림
                            String otherType = userDTO.getType() == 1 ? "보행자가" : "운전자가";
                            if (ttsFlag) {
                                talk.speak("오십미터 내에 " + otherType + numberOfPeople + "명 있습니다", QUEUE_ADD, null);
                            }
                        }
                        for (int i = 0; i < numberOfPeople; i++) {
                            // 사람 수 만큼 마커 생성
                            String lat = String.valueOf(nearByUserList.get(i).getLatitude());
                            String lo = String.valueOf(nearByUserList.get(i).getLongitude());
                            Log.i("nearByUserList", i+" "+lat + " " + lo);
                            Marker m = new Marker();
                            setMarker(m, nearByUserList.get(i));
                            markerList.add(m); // 마커 리스트에 마커 추가
                        }

                        MapActivity.this.runOnUiThread(new Runnable(){
                            public void run(){
                                // 마커 삭제
                                for (int i=0;i<delMarkerList.size();i++){
                                    Marker marker = delMarkerList.get(i);
                                    marker.setMap(null);
                                    Log.d("마커",i + " 지움");
                                }
                                delMarkerList.clear();
                                delMarkerList = markerList;
                                // 마커 그리기
                                for (int i = 0; i<markerList.size();i++){
                                    Marker marker = markerList.get(i);
                                    marker.setMap(mNaverMap);
                                    Log.d("마커",i + " 그림");
                                }
                                markerList.clear();
                            }
                        });
                    }
                }else{
                    Log.i("nearByUserList ", "NULL");
                }
            }
        };
        // 3초 마다 호출
        timer.schedule(getUserDataTask, 0, 3000);
    }
    private void setMarker(Marker marker, LocationDTO locationDTO){
        // 마커 아이콘, 위도, 경도 등 설정
        //marker.setIconPerspectiveEnabled(true); // 원근효과
        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_location_on_36));
        marker.setAlpha(0.8f); //투명도
        double lat = locationDTO.getLatitude();
        double lng = locationDTO.getLongitude();
        marker.setPosition(new LatLng(lat, lng));
    }
    private void startService(){
        startLocationService();
        Intent intent = getIntent();
        int type = intent.getIntExtra("type", -1); // 운전자 1 보행자 0
        String id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID); // 사용자 id
        // 내 데이터로 변경 - userDTO 생성
        userDTO = new UserDTO(id, type, 0.0, 0.0);

    }
    private void startWifiService(){
        turnOnWifi();
    }

    private void turnOnWifi(){
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null){
            if(!wifiManager.isWifiEnabled()) {
                // 안드로이드 Q 이전 버전에서는 정상 작동
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                    wifiManager.setWifiEnabled(true);
                // 이후 버전은 사용자에게 켜 달라고 요청하는 방법밖에 없음.
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


    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }
    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "서비스를 시작합니다", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "서비스를 종료합니다", Toast.LENGTH_SHORT).show();
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
            //userDTO.setSpeed(speed);
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
        // TextToSpeechInitListener 인터페이스 구현
        this.talk = tts;
        ttsFlag = true;
    }
    @Override
    public void onFailure(TextToSpeech tts){
        // TextToSpeechInitListener 인터페이스 구현
        ttsFlag = false;
        finish();
    }
}
