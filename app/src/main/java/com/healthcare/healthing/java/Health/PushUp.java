package com.healthcare.healthing.java.Health;

import android.graphics.PointF;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PushUp implements HealthKind {
    private int numAnglesInRange = 0;
    private int numPushUp = 0;
    private double maxAngle = 0;
    private double temp = 130.0;
    private double leftAngle = 0;
    private double rightAngle = 0;
    private double waistAngle = 0;
    private double contract;
    private TextToSpeech tts;
    private int MnumAnglesInRange;
    private boolean isPushUp = false;
    private boolean waist_banding = false;
    private boolean goodPose = false;
    private boolean Tension = false;
    private double timeAsDoubleTemp;

    public TextToSpeech getTts() { return tts; }

    public int getNumAnglesInRange() { return numAnglesInRange; }

    public void setNumAnglesInRange(int numAnglesInRange) { this.numAnglesInRange = numAnglesInRange; }

    public int getNum() {
        return numPushUp;
    }

    public void setNum(int numPushUp) {
        this.numPushUp = numPushUp;
    }

    public double getMaxAngle() { return maxAngle; }

    public void setMaxAngle(double maxAngle) {
        this.maxAngle = maxAngle;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getLeftAngle() {
        return leftAngle;
    }

    public void setLeftAngle(double leftAngle) {
        this.leftAngle = leftAngle;
    }

    public double getRightAngle() {
        return rightAngle;
    }

    public void setRightAngle(double rightAngle) {
        this.rightAngle = rightAngle;
    }

    public double getWaistAngle() {
        return waistAngle;
    }

    public void setWaistAngle(double waistAngle) {
        this.waistAngle = waistAngle;
    }

    public double getContract() {
        return contract;
    }

    public void setContract(double contract) {
        this.contract = contract;
    }

    public int getMnumAnglesInRange() {
        return MnumAnglesInRange;
    }

    public void setMnumAnglesInRange(int mnumAnglesInRange) { MnumAnglesInRange = mnumAnglesInRange; }

    public boolean isState() { return isPushUp; }

    public void setState(boolean pushUp) { isPushUp = pushUp; }

    public boolean isWaist_banding() {
        return waist_banding;
    }

    public void setWaist_banding(boolean waist_banding) {
        this.waist_banding = waist_banding;
    }

    public boolean isGoodPose() {
        return goodPose;
    }

    public void setGoodPose(boolean goodPose) {
        this.goodPose = goodPose;
    }

    public boolean isTension() {
        return Tension;
    }

    public void setTension(boolean tension) {
        Tension = tension;
    }

    public void setTts(TextToSpeech tts) {
        this.tts = tts;
    }

    public void waistBending(Pose pose){
        PointF leftShoulder = null;
        PointF rightShoulder = null;
        PointF leftHip = null;
        PointF rightHip = null;
        PointF leftAnkle= null;
        PointF rightAnkle = null;

        // 어깨, 엉덩이, 무릎 중심점을 계산합니다.
        if (pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER) != null) {
            leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER) != null) {
            rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.LEFT_HIP) != null) {
            leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.RIGHT_HIP) != null) {
            rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.LEFT_KNEE) != null) {
            leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE) != null) {
            rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE).getPosition();
        }

        if (leftShoulder != null && rightShoulder != null
                && leftHip != null && rightHip != null
                && leftAnkle != null && rightAnkle != null) {
            // 어깨 중심점과 엉덩이 중심점을 연결한 선과
            // 엉덩이 중심점과 무릎 중심점을 연결한 선 사이의 각도를 계산합니다.
            double leftAngle = calculateWaistAngle(leftShoulder, leftHip, leftAnkle);
            double rightAngle = calculateWaistAngle(leftShoulder, leftHip, leftAnkle);

            double dataAngle = Math.min(leftAngle,rightAngle);

            // 두 선이 벌어진 각도는 두 각도의 합이 됩니다.
            waistAngle = dataAngle;
            Log.d("PushUp", "waistAngle: " + waistAngle);
        }
    }

    public void onHealthAngle(Pose pose) {
        PointF leftShoulder = null;
        PointF leftElbow = null;
        PointF leftWrist = null;

        PointF rightShoulder = null;
        PointF rightElbow = null;
        PointF rightWrist = null;

        if (pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER) != null) {
            leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW) != null) {
            leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.LEFT_WRIST) != null) {
            leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER) != null) {
            rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW) != null) {
            rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST) != null) {
            rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST).getPosition();
        }

        leftAngle = calculateAngle(leftShoulder, leftElbow, leftWrist);
        rightAngle = calculateAngle(rightShoulder, rightElbow, rightWrist);
        boolean sm = false;
        double allAngle = Math.min(leftAngle, rightAngle);

        // 새로운 푸쉬업마다 상태 초기화
        if (allAngle == 0) {
            isPushUp = false;
            waist_banding = false;
            numAnglesInRange = 0;
        }

        if (leftShoulder != null && leftElbow != null && leftWrist != null
                && rightShoulder != null && rightElbow != null && rightWrist != null) {

            waistBending(pose);

            if (allAngle != 0 && allAngle <= 110) {
                numAnglesInRange++;
                if (numAnglesInRange >= 12 && !isPushUp) {  // 푸쉬업 체크
                    tts.speak("Up!!", TextToSpeech.QUEUE_FLUSH, null, null);
                    //허리 각도 확인
                    if (waistAngle < 155 || waistAngle > 170){
                        waist_banding = true;
                    }else{
                        waist_banding = false;
                    }
                    if((int)temp < (int)allAngle){
                        isPushUp = true;
                    }else{
                        temp = allAngle;
                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        SimpleDateFormat dataFormat = new SimpleDateFormat("ss.SSS");
                        String getTime = dataFormat.format(date);
                        timeAsDoubleTemp = Double.parseDouble(getTime);
                    }
                }
            }

            if (allAngle > 145) {
                if(isPushUp){
                    numPushUp++;
                    maxAngle = temp;
                    temp = 130;

                    long now = System.currentTimeMillis();
                    Date date = new Date(now);
                    SimpleDateFormat dataFormat = new SimpleDateFormat("ss.SSS");
                    String getTime = dataFormat.format(date);
                    double timeAsDouble = Double.parseDouble(getTime);

                    if(timeAsDouble < timeAsDoubleTemp){
                        contract = (timeAsDouble + 60) - timeAsDoubleTemp;
                    }
                    else{
                        contract = timeAsDouble - timeAsDoubleTemp;
                    }

                    if (waistAngle < 155 || waistAngle > 170){
                        waist_banding = true;
                    }else{
                        waist_banding = false;
                    }

                    if(waistAngle == 0){
                        //허리가 내려갔을 때
                        tts.speak("허리를 올려주세요.", TextToSpeech.QUEUE_FLUSH, null, null);
                        goodPose = false;
                    }else if(waist_banding == true){
                        //허리가 내려갔을 때
                        tts.speak("허리를 내려주세요.", TextToSpeech.QUEUE_FLUSH, null, null);
                        goodPose = false;
                    }
                    else if(maxAngle >= 100){
                        //조금 구부렸을 때
                        tts.speak("조금 더 내려가세요.", TextToSpeech.QUEUE_FLUSH, null, null);
                        goodPose = false;
                    }
                    else if(maxAngle >= 40 && maxAngle <= 90 && waist_banding == false){
                        //좋은 자세 일 때
                        tts.speak("좋은 자세입니다.", TextToSpeech.QUEUE_FLUSH, null, null);
                        goodPose = true;
                    }
                }
                isPushUp = false;
                sm = false;
                numAnglesInRange = 0;
            }

            if (allAngle <= 90){
                Tension = true;
            }else{
                Tension = false;
            }
        }
    }

    public static double calculateAngle(PointF shoulder, PointF elbow, PointF wrist) {
        if (shoulder == null || elbow == null || wrist == null) {
            // 필요한 점이 없는 경우 처리
            return 0.0;
        }

        // 어깨에서 팔꿈치로 향하는 벡터와 손목에서 팔꿈치로 향하는 벡터를 계산합니다.
        float shoulderToElbowX = elbow.x - shoulder.x;
        float shoulderToElbowY = elbow.y - shoulder.y;
        float wristToElbowX = elbow.x - wrist.x;
        float wristToElbowY = elbow.y - wrist.y;

        // 두 벡터의 크기를 계산합니다.
        double shoulderToElbowMagnitude = Math.sqrt(shoulderToElbowX * shoulderToElbowX + shoulderToElbowY * shoulderToElbowY);
        double wristToElbowMagnitude = Math.sqrt(wristToElbowX * wristToElbowX + wristToElbowY * wristToElbowY);

        // 두 벡터 사이의 각도의 코사인 값을 계산합니다.
        double cosAngle = (shoulderToElbowX * wristToElbowX + shoulderToElbowY * wristToElbowY) /
                (shoulderToElbowMagnitude * wristToElbowMagnitude);

        // 라디안 단위의 각을 계산합니다.
        double angle = Math.acos(cosAngle);

        // 각도를 도 단위로 변환합니다.
        angle = Math.toDegrees(angle);

        return angle;
    }

    public static double calculateWaistAngle(PointF point1, PointF point2, PointF point3) {
        if (point1 == null || point2 == null || point3 == null) {
            // 필요한 점이 없는 경우 처리
            return 0.0;
        }

        // 벡터 1: point1에서 point2로 향하는 벡터
        float vector1X = point2.x - point1.x;
        float vector1Y = point2.y - point1.y;

        // 벡터 2: point3에서 point2로 향하는 벡터
        float vector2X = point2.x - point3.x;
        float vector2Y = point2.y - point3.y;

        // 벡터 1과 벡터 2의 내적을 계산
        double crossProduct = vector1X * vector2Y - vector1Y * vector2X;

        // 크로스 제품을 이용하여 각도의 회전 방향을 결정
        double angle = Math.toDegrees(Math.atan2(crossProduct, vector1X * vector2X + vector1Y * vector2Y));

        // 범위를 0도에서 250도로 조정
        if (angle > 250.0) {
            angle = 250.0;
        } else if (angle < 0.0) {
            angle = 0.0;
        }
        return angle;
    }
}
