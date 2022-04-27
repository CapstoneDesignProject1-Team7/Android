package com.example.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        Button btn_driver = findViewById(R.id.gotoMap_driver);
        btn_driver.setOnClickListener(
                view -> {
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    intent.putExtra("type",1);
                    startActivity(intent);
                }
        );

        Button btn_walker = findViewById(R.id.gotoMap_walker);
        btn_walker.setOnClickListener(
                view -> {
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    intent.putExtra("type",0);
                    startActivity(intent);
                }
        );
    }
    public void checkPermission(){
        //현재 안드로이드 버전이 6.0미만이면 메서드를 종료한다.
        //안드로이드6.0 (마시멜로) 이후 버전부터 유저 권한설정 필요
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;

        for(String permission : Constants.PERMISSIONS){
            //권한 허용 여부를 확인한다.
            if(checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_DENIED){
                //권한 허용을여부를 확인하는 창을 띄운다
                requestPermissions(Constants.PERMISSIONS,0);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==0)
        {
            for (int grantResult : grantResults) {
                // 허용함
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    // 권한을 하나라도 허용하지 않는다면 앱 종료
                    Toast.makeText(getApplicationContext(), "앱 실행을 위해 권한을 설정해주세요", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

}