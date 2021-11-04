package com.starpickers.qrreaderformodule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.starpickers.qrreadermodule.QRCodeHandler;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CAMERA = 8009;
    private PreviewView previewView;
    private TextView qrDataText;
    private Button retryButton;

    private static QRCodeHandler qrCodeHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        previewView = findViewById(R.id.cameraView);
        qrDataText = findViewById(R.id.qrData);
        retryButton = findViewById(R.id.retryButton);
        retryButton.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Entry point for Camera Operations
        checkPermission(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("QRCODE_FOUND");
        LocalBroadcastManager.getInstance(this).registerReceiver(qrBroadcastReceiver, intentFilter);
    }

    public void startCamera(Activity activity, PreviewView previewView){
        qrCodeHandler = QRCodeHandler.getQRCodeHandlerInstance();
        qrCodeHandler.Initialize(activity, previewView);
    }

    private void checkPermission(Activity activity) {
        if(ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.INTERNET}, 0);
        }
        if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera(this, previewView);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera(this, previewView);
            } else {
                Toast.makeText(getApplicationContext(), "카메라 권한이 없어 이 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    public void onRetryClick(View v){
        retryButton.setVisibility(View.INVISIBLE);
        qrDataText.setText("");
        startCamera(this, previewView);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(qrBroadcastReceiver);
        if(qrCodeHandler != null){
            qrCodeHandler.clearCameraService();
            qrCodeHandler = null;
        }
    }

    private final BroadcastReceiver qrBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case "QRCODE_FOUND":
                    String qrData = intent.getExtras().getString("data");
                    Log.e("TEST", "QR Code Found : " + qrData);
                    qrDataText.setText(qrData);
                    retryButton.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };
}