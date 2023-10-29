package com.healthcare.healthing.java;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;

import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.healthcare.healthing.ExerciseFragment;
import com.healthcare.healthing.Graphic.CameraXViewModel;
import com.healthcare.healthing.Graphic.GraphicOverlay;
import com.healthcare.healthing.Graphic.VisionImageProcessor;
import com.healthcare.healthing.R;
import com.healthcare.healthing.ResultActivity;
import com.healthcare.healthing.java.posedetector.PoseDetectorProcessor;
import com.healthcare.healthing.preference.PreferenceUtils;

import java.util.ArrayList;

@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
public final class CameraXLivePreviewActivity extends AppCompatActivity {
    private static final String TAG = "CameraXLivePreview";
    private static final String TAG1 = "AngleTest";

    private Intent intent;
    private int Health;
    private int num, temp = 1;
    private ArrayList<Double> maxAngle = new ArrayList<>();
    private ArrayList<Boolean> goodPose = new ArrayList<>();
    private ArrayList<Boolean> waist_banding = new ArrayList<>();
    private ArrayList<Double> contract = new ArrayList<>();
    private ArrayList<Boolean> Tension = new ArrayList<>();
    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;

    @Nullable private ProcessCameraProvider cameraProvider;
    @Nullable private Preview previewUseCase;
    @Nullable private ImageAnalysis analysisUseCase;
    @Nullable private VisionImageProcessor imageProcessor;
    private boolean needUpdateGraphicOverlayImageSourceInfo;

    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private CameraSelector cameraSelector;

    private PoseDetectorProcessor poseDetectorProcessor;
    private Button btn_pass;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        intent = getIntent();
        Health = intent.getIntExtra("Health", 0);

        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        setContentView(R.layout.activity_camera_xlive_preview);
        previewView = findViewById(R.id.preview_view);
        if (previewView == null) {
            Log.d(TAG, "previewView is null");
        }
        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        btn_pass = (Button) findViewById(R.id.btn_pass);
        btn_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (num > 0) {
                    new AlertDialog.Builder(view.getContext())
                        .setMessage("지금 바로 그만두시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cameraProvider.unbind(analysisUseCase);
                                imageProcessor.stop();
                                Intent intent = new Intent(CameraXLivePreviewActivity.this, ResultActivity.class);

                                intent.putExtra("waist_banding", waist_banding);
                                intent.putExtra("Health", Health);
                                intent.putExtra("num", num);
                                intent.putExtra("maxAngle", maxAngle);
                                intent.putExtra("goodPose", goodPose);
                                intent.putExtra("contract", contract);
                                intent.putExtra("Tension", Tension);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("아니오", null)
                        .show();
                }
                else{
                    onBackPressed();
                }
            }
        });


        new ViewModelProvider(this, (ViewModelProvider.Factory) AndroidViewModelFactory.getInstance(getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(
                        this,
                        provider -> {
                            cameraProvider = provider;
                            bindAllCameraUseCases();
                        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindAllCameraUseCases();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    private void bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider.unbindAll();
            bindPreviewUseCase();
            bindAnalysisUseCase();
        }
    }

    private void bindPreviewUseCase() {

        if (cameraProvider == null) {
            return;
        }
        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }

        Preview.Builder builder = new Preview.Builder();

        previewUseCase = builder.build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, previewUseCase);
    }

    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
        try {
            PoseDetectorOptionsBase poseDetectorOptions =
                    PreferenceUtils.getPoseDetectorOptionsForLivePreview(this);
            boolean shouldShowInFrameLikelihood =
                    PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this);
            boolean visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this);
            boolean rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this);
            boolean runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this);
            imageProcessor =
                    (VisionImageProcessor) new PoseDetectorProcessor(
                            this,
                            this,
                            Health,
                            poseDetectorOptions,
                            shouldShowInFrameLikelihood,
                            visualizeZ,
                            rescaleZ,
                            runClassification,
                            /* isStreamMode = */ true);
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + e);
            Toast.makeText(
                            getApplicationContext(),
                            "Can not create image processor: " + e.getLocalizedMessage(),
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }

        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();

        analysisUseCase = builder.build();

        needUpdateGraphicOverlayImageSourceInfo = true;
        analysisUseCase.setAnalyzer(
                // imageProcessor.processImageProxy will use another thread to run the detection underneath,
                // thus we can just runs the analyzer itself on main thread.
                ContextCompat.getMainExecutor(this),
                imageProxy -> {
                    if (needUpdateGraphicOverlayImageSourceInfo) {
                        boolean isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT;
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        if (rotationDegrees == 0 || rotationDegrees == 180) {
                            graphicOverlay.setImageSourceInfo(
                                    imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
                        } else {
                            graphicOverlay.setImageSourceInfo(
                                    imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
                        }
                        needUpdateGraphicOverlayImageSourceInfo = false;
                    }
                    try {
                        imageProcessor.processImageProxy(imageProxy, graphicOverlay);

                        // Add this block after processing the imageProxy
                        if (imageProcessor instanceof PoseDetectorProcessor) {
                            PoseDetectorProcessor poseProcessor = (PoseDetectorProcessor) imageProcessor;
                            num = poseProcessor.getNum();
                            if(num == temp) {
                                maxAngle.add(poseProcessor.getMaxAngle());
                                goodPose.add(poseProcessor.isGoodPose());
                                contract.add(poseProcessor.getContract());
                                Tension.add(poseProcessor.isTension());
                                waist_banding.add(poseProcessor.isWaist_banding());

                                temp++;
                            }
                            if(num == 12){
                                cameraProvider.unbind(analysisUseCase);
                                imageProcessor.stop();
                                Intent intent = new Intent(CameraXLivePreviewActivity.this, ResultActivity.class);

                                intent.putExtra("waist_banding", waist_banding);
                                intent.putExtra("Health", Health);
                                intent.putExtra("num", num);
                                intent.putExtra("maxAngle", maxAngle);
                                intent.putExtra("goodPose", goodPose);
                                intent.putExtra("contract", contract);
                                intent.putExtra("Tension", Tension);
                                startActivity(intent);
                                finish();
                            }

                        }
                    } catch (MlKitException e) {
                        Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });

        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
    }



    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("운동 목록으로 돌아가시겠습니까?")
                    .setCancelable(false)
                    .setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("아니오", null)
                    .show();
        }
    }
}


