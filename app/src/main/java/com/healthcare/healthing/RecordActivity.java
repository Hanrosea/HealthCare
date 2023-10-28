package com.healthcare.healthing;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class RecordActivity extends AppCompatActivity {

    private String Health = "";
    private BarChart barChart, barChart2;
    private String[] label = {"이완", "수축", "긴장", "균형", "종합"}, APPS = new String[5];

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private ArrayList<String> record;
    private Button btn_result;
    private Button btn_share;

    private ArrayList<Double> scores = new ArrayList<>();
    private DatabaseReference memoRef;
    private Together_group_list user;
    private ValueEventListener valueEventListener;
    private TextView feedback;
    private TextView TotalFB;
    private int list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Intent intent = getIntent();

        btn_result = (Button) findViewById(R.id.btn_result);
        btn_share = (Button) findViewById(R.id.btn_share);

        feedback = (TextView) findViewById(R.id.feedback);
        TotalFB = (TextView) findViewById(R.id.TotalFB);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        Health = intent.getStringExtra("Name");
        list = intent.getIntExtra("list", 0);

        if(Health.contains("스쿼트")){
            APPS[0] = "과한 동작";
            APPS[1] = "작은 동작";
            APPS[2] = "허리 말림";
            APPS[3] = "긴장 풀림";
            APPS[4] = "좋은 자세";
        }
        else if(Health.contains("푸쉬업")){
            APPS[0] = "과한 동작";
            APPS[1] = "작은 동작";
            APPS[2] = "허리 굽힘";
            APPS[3] = "긴장 풀림";
            APPS[4] = "좋은 자세";
        }
        else if(Health.contains("풀업")){
            APPS[0] = "과한 동작";
            APPS[1] = "작은 동작";
            APPS[2] = "허리 젖힘";
            APPS[3] = "긴장 풀림";
            APPS[4] = "좋은 자세";
        }
        else if(Health.contains("푸쉬업")){
            APPS[0] = "과한 동작";
            APPS[1] = "작은 동작";
            APPS[2] = "허리 굽힘";
            APPS[3] = "긴장 풀림";
            APPS[4] = "좋은 자세";
        }
        else if(Health.contains("풀업")){
            APPS[0] = "과한 동작";
            APPS[1] = "작은 동작";
            APPS[2] = "허리 젖힘";
            APPS[3] = "긴장 풀림";
            APPS[4] = "좋은 자세";
        }


        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot profileSnapshot = dataSnapshot.child("profile");
                    if (profileSnapshot != null) {
                        user = profileSnapshot.getValue(Together_group_list.class);
                        if (user != null) {
                            Graph1();
                            Graph2();
                            feedback.setText(user.getFb());
                            TotalFB.setText(user.getTotalFB());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("AngleTest", "Failed to get data from Firebase.");
            }
        };

        memoRef = mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid()).child("/record" + list);
        memoRef.addValueEventListener(valueEventListener);



        btn_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RecordActivity.this, RecordListActivity.class));
                finish();
            }
        });
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        memoRef.removeEventListener(valueEventListener);
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

    public void onBackPressed() {
        startActivity(new Intent(RecordActivity.this, RecordListActivity.class));
        finish();
    }
}