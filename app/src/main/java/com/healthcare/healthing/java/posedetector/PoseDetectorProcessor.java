/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.healthcare.healthing.java.posedetector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.odml.image.MlImage;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.healthcare.healthing.Graphic.GraphicOverlay;
import com.healthcare.healthing.java.Health.HealthKind;
import com.healthcare.healthing.java.Health.Pullup;
import com.healthcare.healthing.java.Health.PushUp;
import com.healthcare.healthing.java.Health.Squat;
import com.healthcare.healthing.java.VisionProcessorBase;
import com.healthcare.healthing.java.posedetector.classification.PoseClassifierProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/** A processor to run pose detector. */
public class PoseDetectorProcessor
        extends VisionProcessorBase<PoseDetectorProcessor.PoseWithClassification> {
    private static final String TAG = "PoseDetectorProcessor";

    private final PoseDetector detector;
    private final boolean showInFrameLikelihood;
    private final boolean visualizeZ;
    private final boolean rescaleZForVisualization;
    private final boolean runClassification;
    private final boolean isStreamMode;
    private boolean isTtsInitialized = false;
    private final Context context;
    private final Executor classificationExecutor;
    private int Health;
    private TextToSpeech tts;
    //운동 변수

    private int numAnglesInRange = 0;
    private int num = 0;
    private double maxAngle = 0;
    private boolean goodPose = false;
    private boolean waist_banding = false;
    private boolean Tension = false;
    private String ExerAngle;
    private String PelvicAngle;

    private double TotalAngle;
    private double contract;

    public boolean isWaist_banding(){
        return waist_banding;
    }
    public int getNum() {
        return num;
    }
    public double getContract() {
        return contract;
    }
    public double getMaxAngle() {
        return maxAngle;
    }
    public boolean isGoodPose() {
        return goodPose;
    }
    public boolean isTension() {
        return Tension;
    }

    private PoseClassifierProcessor poseClassifierProcessor;

    /** Internal class to hold Pose and classification results. */
    protected static class PoseWithClassification {
        private final Pose pose;
        private final List<String> classificationResult;

        public PoseWithClassification(Pose pose, List<String> classificationResult) {
            this.pose = pose;
            this.classificationResult = classificationResult;
        }

        public Pose getPose() {
            return pose;
        }

        public List<String> getClassificationResult() {
            return classificationResult;
        }
    }

    public PoseDetectorProcessor(
            Context context1,
            Context context2,
            int Health,
            PoseDetectorOptionsBase options,
            boolean showInFrameLikelihood,
            boolean visualizeZ,
            boolean rescaleZForVisualization,
            boolean runClassification,
            boolean isStreamMode) {
        super(context1);
        initializeTextToSpeech(context2);
        this.showInFrameLikelihood = showInFrameLikelihood;
        this.visualizeZ = visualizeZ;
        this.rescaleZForVisualization = rescaleZForVisualization;
        detector = PoseDetection.getClient(options);
        this.runClassification = runClassification;
        this.isStreamMode = isStreamMode;
        this.context = context1;
        this.Health = Health;
        classificationExecutor = Executors.newSingleThreadExecutor();
    }


    @Override
    public void stop() {
        super.stop();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        detector.close();
    }

    @Override
    protected Task<PoseWithClassification> detectInImage(InputImage image) {
        return detector
                .process(image)
                .continueWith(
                        classificationExecutor,
                        task -> {
                            Pose pose = task.getResult();
                            List<String> classificationResult = new ArrayList<>();
                            if (runClassification) {
                                if (poseClassifierProcessor == null) {
                                    poseClassifierProcessor = new PoseClassifierProcessor(context, isStreamMode);
                                }
                                classificationResult = poseClassifierProcessor.getPoseResult(pose);
                            }
                            return new PoseWithClassification(pose, classificationResult);
                        });
    }

    @Override
    protected Task<PoseWithClassification> detectInImage(MlImage image) {
        Task<PoseWithClassification> poseWithClassificationTask = detector
                .process(image)
                .continueWith(
                        classificationExecutor,
                        task -> {
                            Pose pose = task.getResult();
                            List<String> classificationResult = new ArrayList<>();
                            if (runClassification) {
                                if (poseClassifierProcessor == null) {
                                    poseClassifierProcessor = new PoseClassifierProcessor(context, isStreamMode);
                                }
                                classificationResult = poseClassifierProcessor.getPoseResult(pose);
                            }
                            return new PoseWithClassification(pose, classificationResult);
                        });
        return poseWithClassificationTask;
    }

    private void initializeTextToSpeech(Context context) {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    // 언어를 선택합니다.
                    tts.setLanguage(Locale.KOREAN);
                    isTtsInitialized = true;
                }
            }
        });
    }
    private HealthKind Kind;
    @Override
    protected void onSuccess(
            @NonNull PoseWithClassification poseWithClassification,
            @NonNull GraphicOverlay graphicOverlay) {
        Pose pose = poseWithClassification.getPose();
        List<String> classificationResult = poseWithClassification.getClassificationResult();

        graphicOverlay.add(
                new PoseGraphic(
                        graphicOverlay,
                        poseWithClassification.pose,
                        showInFrameLikelihood,
                        visualizeZ,
                        rescaleZForVisualization,
                        poseWithClassification.classificationResult){

                    public void draw(Canvas canvas) {
                        super.draw(canvas);
                        if (pose != null) {
                            if(isTtsInitialized){
                                tts.speak("인식되었습니다. 준비 되시면 시작해주세요.", TextToSpeech.QUEUE_FLUSH, null, null);
                                isTtsInitialized = false;
                                switch (Health){
                                    case 1:
                                        Kind = new Squat();
                                        Kind.setTts(tts);
                                        ExerAngle = "무릎";
                                        break;
                                    case 2:
                                        Kind = new PushUp();
                                        Kind.setTts(tts);
                                        ExerAngle = "팔꿈치";
                                        break;
                                    case 3:
                                        Kind = new Pullup();
                                        Kind.setTts(tts);
                                        ExerAngle = "팔꿈치";
                                        break;
                                    default:
                                        Kind = null;
                                        break;
                                }
                            }

                            Paint whitePaint = new Paint();
                            whitePaint.setColor(Color.WHITE);
                            whitePaint.setStyle(Paint.Style.STROKE);
                            whitePaint.setStrokeWidth(4.0f);
                            whitePaint.setTextSize(50f);

                            Kind.onHealthAngle(pose);

                            waist_banding = Kind.isWaist_banding();
                            num = Kind.getNum();
                            maxAngle = Kind.getMaxAngle();
                            goodPose = Kind.isGoodPose();
                            Tension = Kind.isTension();

                            TotalAngle = Math.min(Kind.getLeftAngle(),Kind.getRightAngle());

                            if(waist_banding){
                                PelvicAngle = "좋음";
                            } else{
                                PelvicAngle = "나쁨";
                            }

                            drawCircularProgressBar(canvas, num, 12, 850, 200, 100);

                            canvas.drawText(ExerAngle + "각도 : " + (int) TotalAngle, 40, 150, whitePaint);
                            canvas.drawText("최대 운동 각도: " + (int) maxAngle, 40, 225, whitePaint);
                            canvas.drawText("허리 각도 : " + PelvicAngle, 40, 300, whitePaint);
                            canvas.drawText("운동 개수 : " + num, 725, 375, whitePaint);
                        }
                    }
                });
    }

    private void drawCircularProgressBar(Canvas canvas, int progress, int maxProgress, int x, int y, int radius) {
        Paint graphPaint = new Paint();
        graphPaint.setColor(Color.GRAY); // 기본 그래프 색상을 회색으로 설정
        graphPaint.setStyle(Paint.Style.STROKE);
        graphPaint.setStrokeWidth(20); // 그래프의 두께 설정

        Paint progressPaint = new Paint();
        progressPaint.setColor(Color.GREEN); // progress 증가시 그래프 색상을 초록색으로 유지
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(20); // 그래프의 두께 설정

        // 진행 퍼센트를 표시합니다.
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(80); // 글자 굵기 설정

        RectF oval = new RectF(x - radius, y - radius, x + radius, y + radius);

        // 그래프의 외곽 테두리를 그립니다.
        canvas.drawArc(oval, 0, 360, false, graphPaint);

        // 그래프의 진행 부분을 그립니다.
        float sweepAngle = ((float) progress / maxProgress) * 360;
        canvas.drawArc(oval, -90, sweepAngle, false, progressPaint);

        String text = progress + "";
        float textWidth = textPaint.measureText(text);
        canvas.drawText(text, x - textWidth / 2, y + 25, textPaint);
    }
    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Pose detection failed!", e);
    }

    @Override
    protected boolean isMlImageEnabled(Context context) {
        // Use MlImage in Pose Detection by default, change it to OFF to switch to InputImage.
        return true;
    }
}

