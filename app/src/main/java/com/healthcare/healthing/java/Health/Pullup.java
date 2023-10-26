package com.healthcare.healthing.java.Health;

import android.graphics.PointF;
import android.speech.tts.TextToSpeech;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Pullup implements HealthKind {

    private int numAnglesInRange = 0;
    private int numPullup = 0;
    private double maxAngle = 0;
    private double temp = 120.0;
    private double rightshoulderAngle = 0;
    private double leftshoulderAngle = 0;
    private double leftAngle = 0;
    private double rightAngle = 0;
    private double waistAngle = 0;
    private double contract;
    private TextToSpeech tts;
    private boolean isPullup = false;
    private boolean waist_banding = false;
    private boolean goodPose = false;
    private boolean Tension = false;
    private double timeAsDoubleTemp;

    public TextToSpeech getTts() {
        return tts;
    }

    public int getNumAnglesInRange() {
        return numAnglesInRange;
    }

    public void setNumAnglesInRange(int numAnglesInRange) {
        this.numAnglesInRange = numAnglesInRange;
    }

    public int getNum() {
        return numPullup;
    }

    public void setNum(int numPullup) {
        this.numPullup = numPullup;
    }

    public double getMaxAngle() {
        return maxAngle;
    }

    public void setMaxAngle(double maxAngle) {
        this.maxAngle = maxAngle;
    }

    public void pelvicBending(Pose pose) {}

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

    public double getLeftShoulderAngle() {
        return leftshoulderAngle;
    }

    public void setLeftShoulderAngle(double leftshoulderAngle) {
        this.leftshoulderAngle = leftshoulderAngle;
    }
    public double getRightShoulderAngle() {
        return rightshoulderAngle;
    }

    public void setRightShoulderAngle(double rightshoulderAngle) {
        this.rightshoulderAngle = rightshoulderAngle;
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

    public boolean isState() {
        return isPullup;
    }

    public void setState(boolean pullup) {
        isPullup = pullup;
    }

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

// 2. 어깨 중심점과 엉덩이 중심점을 계산합니다.
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

        if (leftHip != null && leftShoulder != null
                && rightHip != null && rightShoulder != null) {

            PointF hipCenter = new PointF((leftHip.x + rightHip.x) / 2, (leftHip.y + rightHip.y) / 2);
            PointF shoulderCenter = new PointF((leftShoulder.x + rightShoulder.x) / 2, (leftShoulder.y + rightShoulder.y) / 2);

// 3. 엉덩이 중심점, 골반 중심점 및 어깨 중심점 사이의 각도를 계산합니다.
            waistAngle = calculateAngle(hipCenter, shoulderCenter, new PointF(shoulderCenter.x, shoulderCenter.y - 1));
        }
    }

    public void onHealthAngle(Pose pose){
        PointF leftShoulder = null;
        PointF leftElbow = null;
        PointF leftWrist = null;
        PointF leftHip = null;

        PointF rightShoulder = null;
        PointF rightElbow = null;
        PointF rightWrist = null;
        PointF rightHip = null;


        if (pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER) != null) {
            leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW) != null) {
            leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.LEFT_WRIST) != null) {
            leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.LEFT_HIP) != null) {
            leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP).getPosition();
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
        if (pose.getPoseLandmark(PoseLandmark.RIGHT_HIP) != null) {
            rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).getPosition();
        }
        leftAngle = calculateAngle(leftShoulder, leftElbow, leftWrist);
        rightAngle = calculateAngle(rightShoulder, rightElbow, rightWrist);
        rightshoulderAngle = calculateAngle(rightHip, rightShoulder, rightElbow);
        leftshoulderAngle = calculateAngle(leftHip, leftShoulder, rightElbow);
        boolean sm = false;
        double allAngle = Math.min(leftAngle, rightAngle);
        double tentionAngle = Math.min(rightshoulderAngle, leftshoulderAngle);

        // 새로운 턱걸이마다 상태 초기화
        if (allAngle == 0) {
            isPullup = false;
            waist_banding = false;
            numAnglesInRange = 0;
        }

        if (leftShoulder != null && leftElbow != null && leftWrist != null
                && rightShoulder != null && rightElbow != null && rightWrist != null) {

            waistBending(pose);


            if (allAngle != 0 && allAngle <= 100) {
                numAnglesInRange++;
                if (numAnglesInRange >= 20 && !isPullup) {
                    tts.speak("Down!", TextToSpeech.QUEUE_FLUSH, null, null);
                    if(waistAngle >= 185 && waistAngle <= 200){
                        waist_banding = true;
                    }else{
                        waist_banding = false;
                    }
                    if((int)temp < (int)allAngle){
                        isPullup = true;
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

            if (allAngle > 120) {
                if(isPullup){
                    numPullup++;
                    maxAngle = temp;
                    temp = 120;

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

                    if(waistAngle >= 180 && waistAngle <= 200){
                        waist_banding = true;
                    }else{
                        waist_banding = false;
                    }
                    if(waist_banding == false){
                        //가슴을 안내밀었을 때
                        tts.speak("어깨가 말려 올라갑니다.", TextToSpeech.QUEUE_FLUSH, null, null);
                        goodPose = false;

                    }
                    else if(maxAngle > 85){
                        //덜 당겼을 때
                        tts.speak("조금 더 당겨주세요.", TextToSpeech.QUEUE_FLUSH, null, null);
                        goodPose = false;
                    }
                    else if (maxAngle <= 85 && waist_banding == true){
                        //좋은 자세 일 때
                        tts.speak("좋은 자세 입니다.", TextToSpeech.QUEUE_FLUSH, null, null);
                        goodPose = true;
                    }

                }
                isPullup = false;
                sm = false;
                numAnglesInRange = 0;
            }

            if (tentionAngle <= 180 && allAngle <= 120){
                Tension = true;
            }else{
                Tension = false;
            }
        }
    }

    private static double calculateAngle(PointF shoulder, PointF elbow, PointF wrist) {
        if (shoulder == null || elbow == null || wrist == null) {
            return 0;
        }
        double angle = Math.toDegrees(Math.atan2(shoulder.y - elbow.y, shoulder.x - elbow.x)
                - Math.atan2(wrist.y - elbow.y, wrist.x - elbow.x));

        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }
}