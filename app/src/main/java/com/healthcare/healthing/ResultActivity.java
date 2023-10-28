package com.healthcare.healthing;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

public class ResultActivity extends AppCompatActivity {

    private DatabaseReference memoRef1, memoRef2, memoRef3;
    private Together_group_list user, user1;
    private ValueEventListener valueEventListener;
    private int num;
    private ArrayList<Double> maxAngle;
    private ArrayList<Boolean> goodPose, waist_banding;
    private ArrayList<Double> contract;
    private ArrayList<Boolean> Tension;
    private int Health;
    private BarChart barChart, barChart2;
    private String[] label = {"이완", "수축", "긴장", "균형", "종합"}, APPS = new String[5];
    private TextView feedback;
    private TextView TotalFB;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private ArrayList<String> record;
    private Button btn_result;
    private Button btn_share;

    private static final String TAG1 = "AngleTest";
    private ArrayList<Double> scores = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        record = new ArrayList<>();
        record.add("record1");
        record.add("record2");
        record.add("record3");
        record.add("record4");
        record.add("record5");

        Intent intent = getIntent();

        Health = intent.getIntExtra("Health", 0);
        num = intent.getIntExtra("num", 0);
        maxAngle = (ArrayList<Double>) intent.getSerializableExtra("maxAngle");
        goodPose = (ArrayList<Boolean>) intent.getSerializableExtra("goodPose");
        waist_banding = (ArrayList<Boolean>) intent.getSerializableExtra("waist_banding");
        Tension = (ArrayList<Boolean>) intent.getSerializableExtra("Tension");
        contract = (ArrayList<Double>) intent.getSerializableExtra("contract");

        feedback = (TextView) findViewById(R.id.feedback);
        TotalFB = (TextView) findViewById(R.id.TotalFB);

        btn_result = (Button) findViewById(R.id.btn_result);
        btn_share = (Button) findViewById(R.id.btn_share);

        user = new Together_group_list();

        user.setNum(num);

        //수행 개수 점수
        user.setNormalizedNum((float) num / 12 * 20);

        switch (Health){
            case 1:
                SqurtsScore();
                Squrts();
                user.setName("스쿼트");
                break;
            case 2:
                PushUpsScore();
                PushUps();
                user.setName("푸쉬업");
                break;
            case 3:
                PullupScore();
                Pullup();
                user.setName("풀업");
            default:
                break;
        }

        Graph1();
        Graph2();

        btn_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMemo();
                finish();
            }
        });
    }

    public void Graph1(){
        ArrayList<BarEntry> entry_chart = new ArrayList<>(); // 데이터를 담을 Arraylist
        ArrayList<BarEntry> entry_chart1 = new ArrayList<>(); // 데이터를 담을 Arraylist
        List<BarEntry> entries = new ArrayList<>();

        barChart = (BarChart) findViewById(R.id.chart);

        barChart.getDescription().setEnabled(false); // chart 밑에 description 표시 유무
        barChart.setTouchEnabled(false); // 터치 유무
        barChart.getLegend().setEnabled(false); // Legend는 차트의 범례
        barChart.setExtraOffsets(10f, 0f, 40f, 0f);

        // XAxis (수평 막대 기준 왼쪽) - 선 유무, 사이즈, 색상, 축 위치 설정
        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(15f);
        xAxis.setGridLineWidth(25f);
        xAxis.setGridColor(Color.parseColor("#80E5E5E5"));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X 축 데이터 표시 위치

        // YAxis(Left) (수평 막대 기준 아래쪽) - 선 유무, 데이터 최솟값/최댓값, label 유무
        YAxis axisLeft = barChart.getAxisLeft();
        axisLeft.setDrawGridLines(false);
        axisLeft.setDrawAxisLine(false);
        axisLeft.setAxisMinimum(0); // 최솟값
        axisLeft.setAxisMaximum(20); // 최댓값
        axisLeft.setGranularity(1f); // 값만큼 라인선 설정
        axisLeft.setDrawLabels(false); // label 삭제

        // YAxis(Right) (수평 막대 기준 위쪽) - 사이즈, 선 유무
        YAxis axisRight = barChart.getAxisRight();
        axisRight.setTextSize(15f);
        axisRight.setDrawLabels(false); // label 삭제
        axisRight.setDrawGridLines(false);
        axisRight.setDrawAxisLine(false);

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return label[(int)value - 1];
            }
        });

        barChart.animateY(1000);
        barChart.animateX(1000);

        BarData barData = new BarData(); // 차트에 담길 데이터

        if((user.getMaxAnglePercentage() + user.getContractPercentage() + user.getTensionPercentage() + user.getNormalizedNum() + user.getGoodPosePercentage()) > 0){
            user.setResult((user.getMaxAnglePercentage() + user.getContractPercentage() + user.getTensionPercentage() + user.getNormalizedNum() + user.getGoodPosePercentage()) / 5);
        }

        entry_chart.add(new BarEntry(1, (int)user.getMaxAnglePercentage())); //entry_chart1에 좌표 데이터를 담는다.
        entry_chart.add(new BarEntry(2, (int)user.getContractPercentage()));
        entry_chart.add(new BarEntry(3, (int)user.getTensionPercentage()));
        entry_chart.add(new BarEntry(4, (int)user.getGoodPosePercentage()));
        entry_chart.add(new BarEntry(5, (int)user.getResult()));

        if (user.getMaxAnglePercentage() != 0) {
            entry_chart1.add(new BarEntry(1, (int) user.getMaxAnglePercentage()));
        }
        if (user.getContractPercentage() != 0) {
            entry_chart1.add(new BarEntry(2, (int) user.getContractPercentage()));
        }
        if (user.getTensionPercentage() != 0) {
            entry_chart1.add(new BarEntry(3, (int) user.getTensionPercentage()));
        }
        if (user.getGoodPosePercentage() != 0) {
            entry_chart1.add(new BarEntry(4, (int) user.getGoodPosePercentage()));
        }
        if (user.getResult() != 0) {
            entry_chart1.add(new BarEntry(5, (int) user.getResult()));
        }
        entries.add(entry_chart.get(0));
        entries.add(entry_chart.get(1));
        entries.add(entry_chart.get(2));
        entries.add(entry_chart.get(3));
        entries.add(entry_chart.get(4));

        BarDataSet barDataSet = new BarDataSet(entries, label.toString());
        BarDataSet barDataSet1 = new BarDataSet(entry_chart1, "두 번째 데이터"); // 두 번째 데이터 세트

        barData = new BarData(barDataSet, barDataSet1);

        // 새로운 IntegerValueFormatter 생성
        IntegerValueFormatter integerValueFormatter = new IntegerValueFormatter();

        // BarDataSet에 IntegerValueFormatter를 적용
        barDataSet.setValueFormatter(integerValueFormatter);

        barDataSet.setDrawIcons(false);
        barDataSet.setDrawValues(true);
        List<Integer> colors = new ArrayList<>();
        for (BarEntry entry : entry_chart) {
            if (entry.getY() >= 15 && entry.getY() <= 20) {
                colors.add(Color.GREEN);
            } else if (entry.getY() >= 10 && entry.getY() <= 14) {
                colors.add(Color.parseColor("#FFA500")); // orange 색깔 추가
            } else if (entry.getY() >= 5 && entry.getY() <= 9) {
                colors.add(Color.YELLOW);
            }else {
                colors.add(Color.RED);
            }
        }
        barDataSet.setColors(colors);
        barDataSet.setValueTextSize(15f);
        barData.addDataSet(barDataSet); // 해당 BarDataSet 을 적용될 차트에 들어갈 DataSet 에 넣는다.
        barData.setBarWidth(0.65f);

        barDataSet1.setColor(Color.DKGRAY); // 어두운 회색
        barDataSet1.setDrawValues(false); // 값 표시
        barDataSet1.setBarBorderWidth(0.2f); // 막대 간 간격
        barDataSet1.setDrawIcons(false);

        float shiftValue1 = -0.03f; // 이동할 값 (0.2f는 예시)
        for (BarEntry entry : entry_chart1) {
            entry.setX(entry.getX() + shiftValue1);
        }
        float shiftValue2 = 0.04f; // 이동할 값 (0.2f는 예시)
        for (BarEntry entry : entry_chart1) {
            entry.setY(entry.getY() + shiftValue2);
        }

        barChart.setData(barData); // 차트에 위의 DataSet 을 넣는다.
        barChart.invalidate(); // 차트 업데이트
        barChart.setTouchEnabled(false); // 차트 터치 불가능하게
    }

    public void Graph2(){
        ArrayList<BarEntry> entry_chart = new ArrayList<>(); // 데이터를 담을 Arraylist
        ArrayList<BarEntry> entry_chart1 = new ArrayList<>(); // 데이터를 담을 Arraylist
        List<BarEntry> entries = new ArrayList<>();

        barChart2 = (BarChart) findViewById(R.id.chart2);

        barChart2.getDescription().setEnabled(false); // chart 밑에 description 표시 유무
        barChart2.setTouchEnabled(false); // 터치 유무
        barChart2.getLegend().setEnabled(false); // Legend는 차트의 범례
        barChart2.setExtraOffsets(10f, 0f, 40f, 0f);

        // XAxis (수평 막대 기준 왼쪽) - 선 유무, 사이즈, 색상, 축 위치 설정
        XAxis xAxis = barChart2.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(15f);
        xAxis.setGridLineWidth(25f);
        xAxis.setGridColor(Color.parseColor("#80E5E5E5"));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X 축 데이터 표시 위치

        // YAxis(Left) (수평 막대 기준 아래쪽) - 선 유무, 데이터 최솟값/최댓값, label 유무
        YAxis axisLeft = barChart2.getAxisLeft();
        axisLeft.setDrawGridLines(false);
        axisLeft.setDrawAxisLine(false);
        axisLeft.setAxisMinimum(0); // 최솟값
        axisLeft.setAxisMaximum(12); // 최댓값
        axisLeft.setGranularity(1f); // 값만큼 라인선 설정
        axisLeft.setDrawLabels(false); // label 삭제

        // YAxis(Right) (수평 막대 기준 위쪽) - 사이즈, 선 유무
        YAxis axisRight = barChart2.getAxisRight();
        axisRight.setTextSize(15f);
        axisRight.setDrawLabels(false); // label 삭제
        axisRight.setDrawGridLines(false);
        axisRight.setDrawAxisLine(false);

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return APPS[(int)value - 1];
            }
        });

        barChart2.animateY(1000);
        barChart2.animateX(1000);

        BarData barData = new BarData(); // 차트에 담길 데이터

        entry_chart.add(new BarEntry(1, (int)user.getBig())); //entry_chart1에 좌표 데이터를 담는다.
        entry_chart.add(new BarEntry(2, (int)user.getSmall()));
        entry_chart.add(new BarEntry(3, (int)user.getWaist()));
        entry_chart.add(new BarEntry(4, (int)user.getTension()));
        entry_chart.add(new BarEntry(5, (int)user.getGood()));

        if (user.getBig() != 0) {
            entry_chart1.add(new BarEntry(1, (int) user.getBig()));
        }
        if (user.getSmall() != 0) {
            entry_chart1.add(new BarEntry(2, (int) user.getSmall()));
        }
        if (user.getWaist() != 0) {
            entry_chart1.add(new BarEntry(3, (int) user.getWaist()));
        }
        if (user.getTension() != 0) {
            entry_chart1.add(new BarEntry(4, (int) user.getTension()));
        }
        if (user.getGood() != 0) {
            entry_chart1.add(new BarEntry(5, (int) user.getGood()));
        }


        entries.add(entry_chart.get(0));
        entries.add(entry_chart.get(1));
        entries.add(entry_chart.get(2));
        entries.add(entry_chart.get(3));
        entries.add(entry_chart.get(4));

        BarDataSet barDataSet = new BarDataSet(entries, APPS.toString());
        BarDataSet barDataSet1 = new BarDataSet(entry_chart1, "두 번째 데이터"); // 두 번째 데이터 세트

        barData = new BarData(barDataSet, barDataSet1);
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (String.valueOf((int) value)) + "회";
            }
        });

        // 새로운 IntegerValueFormatter 생성
        IntegerValueFormatter integerValueFormatter = new IntegerValueFormatter();

        // BarDataSet에 IntegerValueFormatter를 적용
        barDataSet.setValueFormatter(integerValueFormatter);

        barDataSet.setDrawIcons(false);
        barDataSet.setDrawValues(true);
        List<Integer> colors = new ArrayList<>();
        for (BarEntry entry : entry_chart) {
            if (entry.getY() >= 15 && entry.getY() <= 20) {
                colors.add(Color.GREEN);
            } else if (entry.getY() >= 10 && entry.getY() <= 14) {
                colors.add(Color.parseColor("#FFA500")); // orange 색깔 추가
            } else if (entry.getY() >= 5 && entry.getY() <= 9) {
                colors.add(Color.YELLOW);
            }else {
                colors.add(Color.RED);
            }
        }
        barDataSet.setValueTextSize(15f);
        barData.addDataSet(barDataSet); // 해당 BarDataSet 을 적용될 차트에 들어갈 DataSet 에 넣는다.
        barData.setBarWidth(0.65f);

        barDataSet1.setColor(Color.DKGRAY); // 어두운 회색
        barDataSet1.setDrawValues(false); // 값 표시
        barDataSet1.setBarBorderWidth(0.2f); // 막대 간 간격
        barDataSet1.setDrawIcons(false);

        float shiftValue1 = -0.03f; // 이동할 값 (0.2f는 예시)
        for (BarEntry entry : entry_chart1) {
            entry.setX(entry.getX() + shiftValue1);
        }
        float shiftValue2 = 0.04f; // 이동할 값 (0.2f는 예시)
        for (BarEntry entry : entry_chart1) {
            entry.setY(entry.getY() + shiftValue2);
        }

        barChart2.setData(barData); // 차트에 위의 DataSet 을 넣는다.
        barChart2.invalidate(); // 차트 업데이트
        barChart2.setTouchEnabled(false); // 차트 터치 불가능하게
    }

    public void SqurtsScore(){
        //이완 점수
        double sum = 0;
        if(maxAngle != null && maxAngle.size() > 0){
            // maxAngle 값들에 대한 점수 계산
            for (Double angle : maxAngle) {
                double score;
                if (angle <= 60) {
                    score = 60 - angle;
                } else {
                    score = angle - 60;
                }
                scores.add(score);
            }

            // 점수 평균 계산
            for (double score : scores) {
                sum += score;
            }
            double average = sum / num;

            // 0일 수록 평균이 20, 90일 수록 평균이 0 (10%에서 0% 사이)
            user.setMaxAnglePercentage(20 - (average / 90 * 20));
        }

        //균형 점수
        if(goodPose != null && goodPose.size() > 0){
            int countTrue = 0;
            for (boolean isWaistBanding : goodPose) {
                if (isWaistBanding) {
                    countTrue++;
                }
            }
            double percentageTrue = (double) countTrue / num;

            // 0~10% 범위로 변환
            user.setGoodPosePercentage(percentageTrue * 20);
        }

        //긴장 점수
        if(Tension != null && Tension.size() > 0){
            int countTrue = 0;
            for (boolean isWaistBanding : Tension) {
                if (isWaistBanding) {
                    countTrue++;
                }
            }
            double percentageTrue = (double) countTrue / num;

            // 0~10% 범위로 변환
            user.setTensionPercentage(percentageTrue * 20);
        }

        //수축 점수
        if(contract != null && contract.size() > 0) {
            ArrayList<Double> percentages = new ArrayList<>();

            for (double value : contract) {
                double percentage;
                if (value >= 0 && value <= 1.5) {
                    percentage = 20;
                } else if (value > 5) {
                    percentage = 0;
                } else {
                    // 원하는 비율에 맞게 값 사이의 비율을 조정하십시오.
                    // 예: 선형 비례를 사용하여 6~30 사이의 값에 대해 계산하려면 다음을 사용하십시오.
                    percentage = 20 - (value - 1.5) * (20.0 / (5 - 1.5));
                }
                percentages.add(percentage);
            }
            sum = 0;
            for (double value : percentages) {
                sum += value;
            }
            // contract 값을 0~10% 범위로 변환
            user.setContractPercentage(sum / num);
        }

        if(user.getMaxAnglePercentage() == 0 && user.getContractPercentage() == 0 && user.getTensionPercentage() == 0 && user.getGoodPosePercentage() == 0) {
            user.setTotalFB("운동을 하지 않았습니다!");
        }
        else if(user.getMaxAnglePercentage() <= 5 && user.getContractPercentage() <= 5 && user.getTensionPercentage() <= 5 && user.getGoodPosePercentage() <= 5){
            user.setTotalFB("체력이 너무 부족합니다..!\n꾸준한 운동이 필요해요!");
        }else if(user.getMaxAnglePercentage() <= 15 && user.getContractPercentage() <= 15 && user.getTensionPercentage() <= 15 && user.getGoodPosePercentage() <= 15){
            user.setTotalFB("안정적인 자세입니다...!\n하지만 조금 더 노력해봐요!");
        }else{
            user.setTotalFB("자세가 완벽합니다...!\n앞으로도 꾸준히 운동하세요!");
        }

        TotalFB.setText(user.getTotalFB());
    }

    public void Squrts(){
        APPS[0] = "과한 동작";
        APPS[1] = "작은 동작";
        APPS[2] = "허리 굽힘";
        APPS[3] = "긴장 풀림";
        APPS[4] = "좋은 자세";

        double caloriesBurnedPerRep = 0.325;
        double countSquat = 0;

        for(int i = 0; i < num; i++){
            if(maxAngle.get(i) < 56){
                user.setBig(user.getBig()+1);
            }
            else if (maxAngle.get(i) > 85){
                user.setSmall(user.getSmall() + 1);
            }
            if (waist_banding.get(i) == false){
                user.setWaist(user.getWaist() + 1);
            }
            if (Tension.get(i) == false){
                user.setTension(user.getTension() + 1);
            }
            if(goodPose.get(i) == true){
                user.setGood(user.getGood() + 1);
            }
            countSquat++;
        }

        double totalCaloriesBurned = countSquat * caloriesBurnedPerRep;
        String formattedTotalCaloriesBurned = String.format("%.2f", totalCaloriesBurned);

        user.setFb("예상 칼로리 소모량: " + formattedTotalCaloriesBurned + " 칼로리");
        feedback.setText(user.getFb());
    }
    public void PushUpsScore(){
        //이완 점수
        double sum = 0;
        if(maxAngle != null && maxAngle.size() > 0){
            // maxAngle 값들에 대한 점수 계산
            for (Double angle : maxAngle) {
                double score;
                if (angle <= 60) {
                    score = 60 - angle;
                } else {
                    score = angle - 60;
                }
                scores.add(score);
            }

            // 점수 평균 계산
            for (double score : scores) {
                sum += score;
            }
            double average = sum / num;

            // 0일 수록 평균이 20, 90일 수록 평균이 0 (10%에서 0% 사이)
            user.setMaxAnglePercentage(20 - (average / 90 * 20));
        }

        //균형 점수
        if(goodPose != null && goodPose.size() > 0){
            int countTrue = 0;
            for (boolean isWaistBanding : goodPose) {
                if (isWaistBanding) {
                    countTrue++;
                }
            }
            double percentageTrue = (double) countTrue / num;

            // 0~10% 범위로 변환
            user.setGoodPosePercentage(percentageTrue * 20);
        }

        //긴장 점수
        if(Tension != null && Tension.size() > 0){
            int countTrue = 0;
            for (boolean isWaistBanding : Tension) {
                if (isWaistBanding) {
                    countTrue++;
                }
            }
            double percentageTrue = (double) countTrue / num;

            // 0~10% 범위로 변환
            user.setTensionPercentage(percentageTrue * 20);
        }

        //수축 점수
        if(contract != null && contract.size() > 0){
            ArrayList<Double> percentages = new ArrayList<>();

            for (double value : contract) {
                double percentage;
                if (value >= 0 && value <= 1.5) {
                    percentage = 20;
                } else if (value > 5) {
                    percentage = 0;
                } else {
                    // 원하는 비율에 맞게 값 사이의 비율을 조정하십시오.
                    // 예: 선형 비례를 사용하여 6~30 사이의 값에 대해 계산하려면 다음을 사용하십시오.
                    percentage = 20 - (value - 1.5) * (20.0 / (5 - 1.5));
                }
                percentages.add(percentage);
            }
            sum = 0;
            for(double value : percentages){
                sum += value;
            }
            // contract 값을 0~10% 범위로 변환
            user.setContractPercentage(sum / num);
        }

        if(user.getMaxAnglePercentage() == 0 && user.getContractPercentage() == 0 && user.getTensionPercentage() == 0 && user.getGoodPosePercentage() == 0) {
            user.setTotalFB("운동을 하지 않았습니다!");
        }
        else if(user.getMaxAnglePercentage() <= 5 && user.getContractPercentage() <= 5 && user.getTensionPercentage() <= 5 && user.getGoodPosePercentage() <= 5){
            user.setTotalFB("체력이 너무 부족합니다..!\n꾸준한 운동이 필요해요!");
        }else if(user.getMaxAnglePercentage() <= 15 && user.getContractPercentage() <= 15 && user.getTensionPercentage() <= 15 && user.getGoodPosePercentage() <= 15){
            user.setTotalFB("안정적인 자세입니다...!\n하지만 조금 더 노력해봐요!");
        }else{
            user.setTotalFB("자세가 완벽합니다...!\n앞으로도 꾸준히 운동하세요!");
        }

        TotalFB.setText(user.getTotalFB());
    }

    public void PushUps(){
        APPS[0] = "과한 동작";
        APPS[1] = "작은 동작";
        APPS[2] = "허리 굽힘";
        APPS[3] = "긴장 풀림";
        APPS[4] = "좋은 자세";

        double caloriesBurnedPerRep = 0.75;
        double countPushUp = 0;

        for(int i = 0; i < num; i++){
            if(maxAngle.get(i) > 100){
                user.setBig(user.getBig()+1);
            }
            else if (maxAngle.get(i) < 90){
                user.setSmall(user.getSmall() + 1);
            }
            if (waist_banding.get(i)){
                user.setWaist(user.getWaist() + 1);
            }
            if (Tension.get(i) == false){
                user.setTension(user.getTension() + 1);
            }
            if(goodPose.get(i) == true){
                user.setGood(user.getGood() + 1);
            }
            countPushUp++;
        }

        double totalCaloriesBurned = countPushUp * caloriesBurnedPerRep;
        String formattedTotalCaloriesBurned = String.format("%.2f", totalCaloriesBurned);

        user.setFb("예상 칼로리 소모량: " + formattedTotalCaloriesBurned + " 칼로리");
        feedback.setText(user.getFb());
    }

    public void PullupScore(){
        //이완 점수
        double sum = 0;
        if(maxAngle != null && maxAngle.size() > 0){
            // maxAngle 값들에 대한 점수 계산
            for (Double angle : maxAngle) {
                double score;
                if (angle <= 60) {
                    score = 60 - angle;
                } else {
                    score = angle - 60;
                }
                scores.add(score);
            }

            // 점수 평균 계산
            for (double score : scores) {
                sum += score;
            }
            double average = sum / num;

            // 0일 수록 평균이 20, 90일 수록 평균이 0 (10%에서 0% 사이)
            user.setMaxAnglePercentage(20 - (average / 90 * 20));
        }

        //균형 점수
        if(goodPose != null && goodPose.size() > 0){
            int countTrue = 0;
            for (boolean isWaistBanding : goodPose) {
                if (isWaistBanding) {
                    countTrue++;
                }
            }
            double percentageTrue = (double) countTrue / num;

            // 0~10% 범위로 변환
            user.setGoodPosePercentage(percentageTrue * 20);
        }

        //긴장 점수
        if(Tension != null && Tension.size() > 0){
            int countTrue = 0;
            for (boolean isWaistBanding : Tension) {
                if (isWaistBanding) {
                    countTrue++;
                }
            }
            double percentageTrue = (double) countTrue / num;

            // 0~10% 범위로 변환
            user.setTensionPercentage(percentageTrue * 20);
        }

        //수축 점수
        if(contract != null && contract.size() > 0){
            ArrayList<Double> percentages = new ArrayList<>();

            for (double value : contract) {
                double percentage;
                if (value >= 0 && value <= 1.5) {
                    percentage = 20;
                } else if (value > 5) {
                    percentage = 0;
                } else {
                    // 원하는 비율에 맞게 값 사이의 비율을 조정하십시오.
                    // 예: 선형 비례를 사용하여 6~30 사이의 값에 대해 계산하려면 다음을 사용하십시오.
                    percentage = 20 - (value - 1.5) * (20.0 / (5 - 1.5));
                }
                percentages.add(percentage);
            }
            sum = 0;
            for(double value : percentages){
                sum += value;
            }
            // contract 값을 0~10% 범위로 변환
            user.setContractPercentage(sum / num);
        }

        if(user.getMaxAnglePercentage() == 0 && user.getContractPercentage() == 0 && user.getTensionPercentage() == 0 && user.getGoodPosePercentage() == 0) {
            user.setTotalFB("운동을 하지 않았습니다!");
        }
        else if(user.getMaxAnglePercentage() <= 5 && user.getContractPercentage() <= 5 && user.getTensionPercentage() <= 5 && user.getGoodPosePercentage() <= 5){
            user.setTotalFB("체력이 너무 부족합니다..!\n꾸준한 운동이 필요해요!");
        }else if(user.getMaxAnglePercentage() <= 15 && user.getContractPercentage() <= 15 && user.getTensionPercentage() <= 15 && user.getGoodPosePercentage() <= 15){
            user.setTotalFB("안정적인 자세입니다...!\n하지만 조금 더 노력해봐요!");
        }else{
            user.setTotalFB("자세가 완벽합니다...!\n앞으로도 꾸준히 운동하세요!");
        }

        TotalFB.setText(user.getTotalFB());
    }

    public void Pullup(){
        APPS[0] = "과한 동작";
        APPS[1] = "작은 동작";
        APPS[2] = "허리 젖힘";
        APPS[3] = "긴장 풀림";
        APPS[4] = "좋은 자세";

        double caloriesBurnedPerRep = 1.5;
        double countPullUp = 0;

        for(int i = 0; i < num; i++){
            if(maxAngle.get(i) < 0){
                user.setBig(user.getBig()+1);
            }
            else if (maxAngle.get(i) > 85){
                user.setSmall(user.getSmall() + 1);
            }
            if (waist_banding.get(i) == false){
                user.setWaist(user.getWaist() + 1);
            }
            if (Tension.get(i) == false){
                user.setTension(user.getTension() + 1);
            }
            if(goodPose.get(i) == true){
                user.setGood(user.getGood() + 1);
            }
            countPullUp++;
        }


        double totalCaloriesBurned = countPullUp * caloriesBurnedPerRep;
        String formattedTotalCaloriesBurned = String.format("%.2f", totalCaloriesBurned);

        user.setFb("예상 칼로리 소모량: " + formattedTotalCaloriesBurned + " 칼로리");
        feedback.setText(user.getFb());
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
    @Override
    public void onBackPressed() {
        saveMemo();
        finish();
    }

    private void findMinimumEmptyRecord(int i, IntConsumer onMinFound) {
        if (i > 10) {
            onMinFound.accept(-1);
            return;
        }

        DatabaseReference userRef = mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid()).child("/record" + i);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    onMinFound.accept(i);
                } else {
                    findMinimumEmptyRecord(i + 1, onMinFound);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onMinFound.accept(-1);
            }
        });
    }

    private void saveMemo(){
        AtomicBoolean state = new AtomicBoolean(true);

        findMinimumEmptyRecord(1, minEmptyRecord -> {
            if (minEmptyRecord > 1) {
                state.set(false);
                AtomicInteger counter = new AtomicInteger(minEmptyRecord - 1);
                for(int i = minEmptyRecord - 1; i >= 1; i--){
                    int finalI = i;
                    mFirebaseDatabase.getReference("memos/" + mFirebaseAuth.getUid()+"/record"+i)
                            .addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                    Together_group_list tgList = dataSnapshot.getValue(Together_group_list.class);
                                    if (tgList != null) {
                                        memoRef2 = mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid()).child("/record" + (finalI+1));

                                        // 수정할 데이터의 참조 경로와 고유 ID를 결합하여 해당 데이터의 참조 경로를 가져옵니다.
                                        DatabaseReference memoToUpdateRef = memoRef2.child("profile");

                                        // 수정할 데이터의 값을 Map 객체로 만듭니다.
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("MaxAnglePercentage", tgList.getMaxAnglePercentage());
                                        updates.put("goodPosePercentage", tgList.getGoodPosePercentage());
                                        updates.put("TensionPercentage", tgList.getTensionPercentage());
                                        updates.put("contractPercentage", tgList.getContractPercentage());
                                        updates.put("normalizedNum", tgList.getNormalizedNum());
                                        updates.put("result", tgList.getResult());
                                        updates.put("num", user.getNum());

                                        updates.put("big", tgList.getBig());
                                        updates.put("small", tgList.getSmall());
                                        updates.put("waist", tgList.getWaist());
                                        updates.put("tension", tgList.getTension());
                                        updates.put("good", tgList.getGood());

                                        updates.put("fb", tgList.getFb());
                                        updates.put("TotalFB", tgList.getTotalFB());
                                        updates.put("date", tgList.getDate());
                                        updates.put("name", tgList.getName());


                                        // 해당 데이터의 참조 경로에 updateChildren() 메소드를 호출하여 값을 수정합니다.
                                        memoToUpdateRef.updateChildren(updates);

                                        int currentCount = counter.decrementAndGet();

                                        // 모든 작업이 완료되었는지 확인
                                        if (currentCount == 0) {
                                            updateRecord1();
                                        }
                                    }
                                }

                                @Override
                                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                    // 변경된 데이터 처리
                                }

                                @Override
                                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                                    // 삭제된 데이터 처리
                                }

                                @Override
                                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                    // 이동된 데이터 처리
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // 처리할 오류가 있으면 여기에 작성
                                }
                            });
                }
                Log.d(TAG1, "Minimum empty record: " + minEmptyRecord);
            }
            else if(minEmptyRecord == 1){
                if(state.get()){
                    Log.d(TAG1, "Minimum empty record: " + minEmptyRecord);
                    updateRecord1();
                }
            }
            else {
                // 모든 record에 값이 있습니다.
                deleteMemo();
                saveMemo();
                Log.d(TAG1, "All records are full.");
            }
        });

    }
    private void updateRecord1() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String getTime = dataFormat.format(date);
        SimpleDateFormat dataFormat1 = new SimpleDateFormat("yyyy년 MM월 dd일");
        String memoryDate = dataFormat1.format(date);

        user.setDate(getTime);
        // 수정할 데이터의 참조 경로 가져오기
        // 수정할 데이터의 참조 경로 가져오기
        memoRef1 = mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid()).child("/record1");

        // 수정할 데이터의 참조 경로와 고유 ID를 결합하여 해당 데이터의 참조 경로를 가져옵니다.
        DatabaseReference memoToUpdateRef = memoRef1.child("profile");

        // 수정할 데이터의 값을 Map 객체로 만듭니다.
        Map<String, Object> updates = new HashMap<>();
        updates.put("MaxAnglePercentage", user.getMaxAnglePercentage());
        updates.put("goodPosePercentage", user.getGoodPosePercentage());
        updates.put("TensionPercentage", user.getTensionPercentage());
        updates.put("contractPercentage", user.getContractPercentage());
        updates.put("normalizedNum", user.getNormalizedNum());
        updates.put("result", user.getResult());

        updates.put("big", user.getBig());
        updates.put("small", user.getSmall());
        updates.put("waist", user.getWaist());
        updates.put("tension", user.getTension());
        updates.put("good", user.getGood());
        updates.put("num", user.getNum());

        updates.put("fb", user.getFb());
        updates.put("TotalFB", user.getTotalFB());
        updates.put("date", user.getDate());
        updates.put("name", user.getName());


        // 해당 데이터의 참조 경로에 updateChildren() 메소드를 호출하여 값을 수정합니다.
        memoToUpdateRef.updateChildren(updates);



        DatabaseReference memoRef3 = mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid()).child("/Calendar/" + memoryDate);

        DatabaseReference userRef = mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid()).child("/Calendar/" + memoryDate + "/profile/" + user.getName());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 데이터가 존재하는 경우
                    mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid()).child("/Calendar/" + memoryDate)
                            .addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                    Log.d("TAG2323", Integer.toString(user.getNum()));
                                    DatabaseReference memoToUpdateRef = memoRef3.child("profile");

                                    // 수정할 데이터의 값을 Map 객체로 만듭니다.
                                    Map<String, Object> updates = new HashMap<>();
                                    if(user.getName() == "스쿼트"){
                                        updates.put("스쿼트", dataSnapshot.child("스쿼트").getValue(int.class) + num);
                                    }else if(user.getName() == "푸쉬업"){
                                        updates.put("푸쉬업", dataSnapshot.child("푸쉬업").getValue(int.class) + num);
                                    }else if(user.getName() == "풀업"){
                                        updates.put("풀업", dataSnapshot.child("풀업").getValue(int.class) + num);
                                    }



                                    // 해당 데이터의 참조 경로에 updateChildren() 메소드를 호출하여 값을 수정합니다.
                                    memoToUpdateRef.updateChildren(updates);
                                    // user 데이터를 사용하여 출력
                                }

                                @Override
                                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                    // 변경된 데이터 처리
                                }

                                @Override
                                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                                    // 삭제된 데이터 처리
                                }

                                @Override
                                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                    // 이동된 데이터 처리
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // 처리할 오류가 있으면 여기에 작성
                                }
                            });
                } else {
                    DatabaseReference memoToUpdateRef = memoRef3.child("profile");

                    // 수정할 데이터의 값을 Map 객체로 만듭니다.
                    Map<String, Object> updates = new HashMap<>();
                    if(user.getName() == "스쿼트"){
                        updates.put("스쿼트", num);
                    }else if(user.getName() == "푸쉬업"){
                        updates.put("푸쉬업", num);
                    }else if(user.getName() == "풀업"){
                        updates.put("풀업", num);
                    }



                    // 해당 데이터의 참조 경로에 updateChildren() 메소드를 호출하여 값을 수정합니다.
                    memoToUpdateRef.updateChildren(updates);
                    // user 데이터를 사용하여 출력
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 데이터 읽기가 실패한 경우

            }
        });

    }

    private void deleteMemo() {
        DatabaseReference memoRef = FirebaseDatabase.getInstance().getReference("memos/" + FirebaseAuth.getInstance().getUid() + "/record10");

        memoRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG1, "Memo deleted successfully");
            } else {
                Log.e(TAG1, "Error deleting memo", task.getException());
            }
        });
    }

}

class IntegerValueFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        return String.valueOf((int) value);
    }
}

class Together_group_list{
    private double MaxAnglePercentage = 0, goodPosePercentage = 0, TensionPercentage = 0, contractPercentage = 0, result = 0, normalizedNum = 0;
    private int big = 0, small = 0, waist = 0, tension = 0, good = 0, num = 0;
    private String fb = "", TotalFB = "", date = "", name = "";

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public double getMaxAnglePercentage() {
        return MaxAnglePercentage;
    }

    public void setMaxAnglePercentage(double maxAnglePercentage) {
        MaxAnglePercentage = maxAnglePercentage;
    }

    public double getGoodPosePercentage() {
        return goodPosePercentage;
    }

    public void setGoodPosePercentage(double goodPosePercentage) {
        this.goodPosePercentage = goodPosePercentage;
    }

    public double getTensionPercentage() {
        return TensionPercentage;
    }

    public void setTensionPercentage(double tensionPercentage) {
        TensionPercentage = tensionPercentage;
    }

    public double getContractPercentage() {
        return contractPercentage;
    }

    public void setContractPercentage(double contractPercentage) {
        this.contractPercentage = contractPercentage;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }

    public double getNormalizedNum() {
        return normalizedNum;
    }

    public void setNormalizedNum(double normalizedNum) {
        this.normalizedNum = normalizedNum;
    }

    public int getBig() {
        return big;
    }

    public void setBig(int big) {
        this.big = big;
    }

    public int getSmall() {
        return small;
    }

    public void setSmall(int small) {
        this.small = small;
    }

    public int getWaist() {
        return waist;
    }

    public void setWaist(int waist) {
        this.waist = waist;
    }

    public int getTension() {
        return tension;
    }

    public void setTension(int tension) {
        this.tension = tension;
    }

    public int getGood() {
        return good;
    }

    public void setGood(int good) {
        this.good = good;
    }

    public String getFb() {
        return fb;
    }

    public void setFb(String fb) {
        this.fb = fb;
    }

    public String getTotalFB() { return TotalFB; }

    public void setTotalFB(String TotalFB) {
        this.TotalFB = TotalFB;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}