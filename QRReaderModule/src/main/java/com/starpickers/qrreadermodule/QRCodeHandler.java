package com.starpickers.qrreadermodule;

import android.app.Activity;
import android.content.Intent;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class QRCodeHandler {
    private static QRCodeHandler qrCodeHandlerInstance = null;
    private QRCodeHandler() {}
    public static QRCodeHandler getQRCodeHandlerInstance(){
        if(qrCodeHandlerInstance == null){
            qrCodeHandlerInstance = new QRCodeHandler();
        }
        return qrCodeHandlerInstance;
    }

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private ImageAnalysis imageAnalysis;

    public void Initialize(Activity activity, PreviewView previewView){
        cameraProviderFuture = ProcessCameraProvider.getInstance(activity);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(activity, previewView, cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(activity));
    }

    private void bindCameraPreview(Activity activity, PreviewView previewView, @NonNull ProcessCameraProvider cameraProvider) {
        previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(activity), new QRCodeImageAnalyzer(new QRCodeFoundListener() {
            @Override
            public void onQRCodeFound(String qrCode) {
                clearCameraService();
                Intent intent = new Intent("QRCODE_FOUND");
                intent.putExtra("data", qrCode);
                LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
            }

            @Override
            public void qrCodeNotFound() {
            }
        }));
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) activity, cameraSelector, imageAnalysis, preview);
    }

    public void clearCameraService(){
        if(imageAnalysis != null){
            imageAnalysis.clearAnalyzer();
            imageAnalysis = null;
        }
        if(cameraProvider != null){
            cameraProvider.unbindAll();
            cameraProvider = null;
        }
    }
}
