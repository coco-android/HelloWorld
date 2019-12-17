package com.example.helloworld;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class HelloWorldActivity extends AppCompatActivity {

    private static final String TAG = "HelloWorldActivity";

    private TextView textView = null;

    @Override
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"HardwareIds", "MissingPermission", "WifiManagerLeak"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);

        textView = findViewById(R.id.textview);
        loadInfo(textView);

        Button btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadInfo(textView);
                Toast.makeText(HelloWorldActivity.this, "信息加载完成", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * 加载手机信息
     * @param textView
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"HardwareIds", "MissingPermission", "WifiManagerLeak"})
    private void loadInfo(TextView textView) {
        String[] permissions = {
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.READ_CONTACTS
        };
        if (!isHavePermission(permissions)) {
            ActivityCompat.requestPermissions(HelloWorldActivity.this, permissions, 1);
        } else {
            List<String> infos = new ArrayList<>();
            PhoneInfo phoneInfo = new PhoneInfo(HelloWorldActivity.this);

            // 0. 运用反射得到build类里的字段
            Field[] fields = Build.class.getDeclaredFields();
            // 遍历字段名数组
            for (Field field : fields) {
                try {
                    //将字段都设为public可获取
                    field.setAccessible(true);
                    //filed.get(null)得到的即是设备信息
                    if (field.getName().equals("BRAND")) {
                        infos.add(field.getName() + "：" + field.get(null));
                    }
                } catch (Exception e) {
                }
            }
            String model = Build.MODEL;
            infos.add("型号: " + model);
            int sdkInt = Build.VERSION.SDK_INT;
            infos.add("SdkVersion: " + sdkInt);

            // 1. 获取手机信息
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String meid = telephonyManager.getMeid(0);
            String imei1 = telephonyManager.getImei(0);
            String imei2 = telephonyManager.getImei(1);
            String phoneNumber = telephonyManager.getLine1Number();
            infos.add("MEID：" + meid);
            infos.add("IMEI-1：" + imei1);
            infos.add("IMEI-2：" + imei2);
            if (!TextUtils.isEmpty(phoneNumber)) {
                infos.add("手机号：" + phoneNumber);
            }

            // 2. wifi下获取本地网络IP地址（局域网地址）
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ip = PhoneInfo.int2Ip(wifiInfo.getIpAddress());
                infos.add("IP：" + ip);
            }

            // 3. 当前使用2G/3G/4G网络
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (null != connectivityManager) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (null != networkInfo && networkInfo.isConnected()) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                        infos.add("当前使用网络：" + networkInfo.getTypeName());
                    }
                }
            }

            // 4. 获取wlan mac地址
            String macAddress = phoneInfo.getMacAddress();
            infos.add("WLAN MAC地址：" + macAddress);

            // 5. 获取手机SN码
            String serial = Build.getSerial();
            infos.add("SN：" + serial);

            // 信息在页面显示
            textView.setText(String.join("\n", infos));
        }
    }

    /**
     * 是否有权限
     * @return
     */
    public boolean isHavePermission(String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(HelloWorldActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    public void call() {
        try {
            //Intent intent = new Intent(Intent.ACTION_DIAL); // 显示拨打页面
            Intent intent = new Intent(Intent.ACTION_CALL); // 直接拨打电话
            intent.setData(Uri.parse("tel:10086"));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "call: 异常了", e);
            e.printStackTrace();
        }
    }

    /**
     * 授权
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    @SuppressLint("NewApi")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadInfo(textView);
                    Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
}
