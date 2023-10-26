package com.healthcare.healthing.java.Health;

import android.speech.tts.TextToSpeech;

import com.google.mlkit.vision.pose.Pose;

public interface HealthKind {

    //tts 기능 함수
    TextToSpeech getTts();

    //각도 계산 반복 수량 함수 get형
    int getNumAnglesInRange();
    //각도 계산 반복 수량 함수 set형
    void setNumAnglesInRange(int numAnglesInRange);

    //운동 개수 계산 함수 get형
    int getNum();
    //운동 개수 계산 함수 set형
    void setNum(int numSquats);

    //운동 최대 각도 함수 get형
    double getMaxAngle();
    //운동 최대 각도 함수 set형
    void setMaxAngle(double maxAngle);

    //운동 왼쪽 각도 함수 get형
    double getLeftAngle();
    //운동 왼쪽 각도 함수 set형
    void setLeftAngle(double leftAngle);

    //운동 오른쪽 각도 함수 get형
    double getRightAngle();
    //운동 오른쪽 각도 함수 set형
    void setRightAngle(double rightAngle);

    //운동 허리 각도 함수 get형
    double getWaistAngle();
    //운동 허리 각도 함수 set형
    void setWaistAngle(double waistAngle);

    //운동 수축 시간 함수 get형
    double getContract();
    //운동 수축 시간 함수 set형
    void setContract(double contract);

    //허리 굽음 판단 함수 get형
    boolean isWaist_banding();
    //허리 굽음 판단 함수 set형
    void setWaist_banding(boolean waist_banding);

    //좋은 자세 판단 함수 get형
    boolean isGoodPose();
    //좋은 자세 판단 함수 set형
    void setGoodPose(boolean goodPose);

    //운동 긴장 판단 함수 get형
    boolean isTension();
    //운동 긴장 판단 함수 set형
    void setTension(boolean tension);

    //tts 기능 함수 set형
    void setTts(TextToSpeech tts);

    //운동 허리 각도 함수
    void waistBending(Pose pose);

    //운동 각도 총 계산 함수
    void onHealthAngle(Pose pose);
}
