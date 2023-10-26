package com.healthcare.healthing.java.Health;

import android.graphics.PointF;
import android.speech.tts.TextToSpeech;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.text.SimpleDateFormat;
import java.util.Date;
public class Squat implements HealthKind{
    private int numAnglesInRange = 0;
    private int numSquats = 0;
    private double maxAngle = 0;
    private double temp = 120.0;
    private double leftAngle = 0;
    private double rightAngle = 0;
    private double waistAngle = 0;
    private double contract;
    private TextToSpeech tts;
    private int MnumAnglesInRange;
    private boolean isSquat = false;
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

    public void setNumAnglesInRange(int numAnglesInRange) { this.numAnglesInRange = numAnglesInRange; }

    public int getNum() {
        return numSquats;
    }

    public void setNum(int numSquats) {
        this.numSquats = numSquats;
    }

    public double getMaxAngle() { return maxAngle; }

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

    public boolean isState() {
        return isSquat;
    }

    public void setState(boolean squat) {
        isSquat = squat;
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
        PointF leftHip = null;
        PointF leftKnee = null;
        PointF leftAnkle = null;

        PointF rightHip = null;
        PointF rightKnee = null;
        PointF rightAnkle = null;

        if (pose.getPoseLandmark(PoseLandmark.LEFT_HIP) != null) {
            leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.LEFT_KNEE) != null) {
            leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE) != null) {
            leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.RIGHT_HIP) != null) {
            rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE) != null) {
            rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE).getPosition();
        }
        if (pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE) != null) {
            rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE).getPosition();
        }
        leftAngle = calculateAngle(leftHip, leftKnee, leftAnkle);
        rightAngle = calculateAngle(rightHip, rightKnee, rightAnkle);
        boolean sm = false;
        double allAngle = Math.min(leftAngle, rightAngle);
        // 새로운 스쿼트마다 상태 초기화
        if (allAngle == 0) {
            isSquat = false;
            waist_banding = false;
            numAnglesInRange = 0;
        }

        if (leftHip != null && leftKnee != null && leftAnkle != null
                && rightHip != null && rightKnee != null && rightAnkle != null) {

            waistBending(pose);

            if (allAngle != 0 && allAngle <= 130) {
                numAnglesInRange++;
                if (numAnglesInRange >= 24 && !isSquat) {  // 스쿼트 체크
                    tts.speak("Up!!", TextToSpeech.QUEUE_FLUSH, null, null);
                    if(waistAngle >= 170 && waistAngle <= 225){
                        waist_banding = true;
                    }else{
                        waist_banding = false;
                    }
                    if((int)temp < (int)allAngle){
                        isSquat = true;
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

            if (allAngle > 150) {
                if(isSquat){
                    numSquats++;
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

                    if(waistAngle >= 170 && waistAngle <= 200){
                        waist_banding = true;
                    }else{
                        waist_banding = false;
                    }
                    if(waist_banding == false){
                        //허리가 굽었을 때
                        tts.speak("허리를 펴주세요.", TextToSpeech.QUEUE_FLUSH, null, null);
                        goodPose = false;

                    }
                    else if(maxAngle > 85){
                        //조금 구부렸을 때
                        tts.speak("조금 더 구부려주세요.", TextToSpeech.QUEUE_FLUSH, null, null);
                        goodPose = false;

                    }
                    else if(maxAngle < 56){
                        //너무 많이 구부렸을 때
                        tts.speak("너무 많이 구부리셨어요.", TextToSpeech.QUEUE_FLUSH, null, null);
                        goodPose = false;
                    }
                    else if(maxAngle >= 56 && maxAngle <= 85 && waist_banding == true){
                        //좋은 자세 일 때
                        tts.speak("좋은 자세 입니다.", TextToSpeech.QUEUE_FLUSH, null, null);
                        goodPose = true;
                    }

                }
                isSquat = false;
                sm = false;
                numAnglesInRange = 0;
            }

            if (allAngle <= 160){
                Tension = true;
            }else{
                Tension = false;
            }
        }
    }

    private static double calculateAngle(PointF hip, PointF knee, PointF ankle) {
        if (hip == null || knee == null || ankle == null) {
            return 0;
        }
        double angle = Math.toDegrees(Math.atan2(hip.y - knee.y, hip.x - knee.x)
                - Math.atan2(ankle.y - knee.y, ankle.x - knee.x));

        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }
}
