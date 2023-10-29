package com.healthcare.healthing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecordListActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private CustomAdapter adapter;
    private TextView noRecordTextView;
    private CalendarView calendarView;
    private String[] label = {"풀업", "푸쉬업", "스쿼트"}, APPS = new String[3];
    private String selectedDate;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);

        calendarView = (CalendarView) findViewById(R.id.calendarView);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        noRecordTextView = findViewById(R.id.noRecordTextView);
        DateFormat formatter = new SimpleDateFormat("yyyy년 MM월 dd일");
        Date date = new Date(System.currentTimeMillis());
        selectedDate = formatter.format(date);
        Graph1();
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                month++; // 혹은 month = month + 1;

                selectedDate = year + "년 " + (month) + "월 " + dayOfMonth + "일";
                Graph1();
            }
        });

        adapter = new CustomAdapter(this, new ArrayList<ExerciseItem>());

        for(int i = 1; i <= 10; i++){
            mFirebaseDatabase.getReference("memos/" + mFirebaseAuth.getUid()+"/record"+i)
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            Together_group_list tgList = dataSnapshot.getValue(Together_group_list.class);
                            if (tgList != null) {
                                String date = tgList.getDate();
                                String name = tgList.getName();

                                adapter.add(new ExerciseItem(name, date));
                                adapter.notifyDataSetChanged();
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

        ListView list = findViewById(R.id.listView1);

        list.setAdapter(adapter);

        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                // 데이터가 변경될 때마다 호출되며, 데이터 유무에 따라 TextView를 보이거나 숨깁니다.
                if (adapter.getCount() == 0) {
                    noRecordTextView.setVisibility(View.VISIBLE);
                } else {
                    noRecordTextView.setVisibility(View.GONE);
                }
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExerciseItem selectedItem = adapter.getItem(position);
                Intent intent = new Intent(RecordListActivity.this, RecordActivity.class);
                intent.putExtra("Name", adapter.getItem(position).getName());
                intent.putExtra("list", position+1);
                startActivity(intent);
                finish();
            }
        });
        adapter.clear();


    }

    public void Graph1(){
        DatabaseReference userRef = mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid()).child("/Calendar/" + selectedDate);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    mFirebaseDatabase.getReference("memos/" + mFirebaseAuth.getUid()+"/Calendar/" + selectedDate)
                            .addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                    ArrayList<BarEntry> entry_chart = new ArrayList<>(); // 데이터를 담을 Arraylist
                                    ArrayList<BarEntry> entry_chart1 = new ArrayList<>(); // 데이터를 담을 Arraylist
                                    List<BarEntry> entries = new ArrayList<>();

                                    barChart = (BarChart) findViewById(R.id.chart);
                                    barChart.setVisibility(View.VISIBLE);
                                    barChart.getDescription().setEnabled(false); // chart 밑에 description 표시 유무
                                    barChart.setTouchEnabled(false); // 터치 유무
                                    barChart.getLegend().setEnabled(false); // Legend는 차트의 범례
                                    barChart.setExtraOffsets(10f, 0f, 40f, 0f);

                                    // XAxis (수평 막대 기준 왼쪽) - 선 유무, 사이즈, 색상, 축 위치 설정
                                    XAxis xAxis = barChart.getXAxis();
                                    xAxis.setDrawAxisLine(false);
                                    xAxis.setGranularity(1f);
                                    xAxis.setTextSize(15f);
                                    xAxis.setTextColor(Color.rgb(182, 182, 182));
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

                                    entry_chart.add(new BarEntry(3, dataSnapshot.child("스쿼트").getValue(int.class))); //entry_chart1에 좌표 데이터를 담는다.
                                    entry_chart.add(new BarEntry(2, dataSnapshot.child("푸쉬업").getValue(int.class)));
                                    entry_chart.add(new BarEntry(1, dataSnapshot.child("풀업").getValue(int.class)));

                                    if (dataSnapshot.child("스쿼트").getValue(int.class) != 0) {
                                        entry_chart1.add(new BarEntry(3, dataSnapshot.child("스쿼트").getValue(int.class)));
                                    }
                                    if (dataSnapshot.child("푸쉬업").getValue(int.class) != 0) {
                                        entry_chart1.add(new BarEntry(2, dataSnapshot.child("푸쉬업").getValue(int.class)));
                                    }
                                    if (dataSnapshot.child("풀업").getValue(int.class) != 0) {
                                        entry_chart1.add(new BarEntry(1, dataSnapshot.child("풀업").getValue(int.class)));
                                    }

                                    entries.add(entry_chart.get(0));
                                    entries.add(entry_chart.get(1));
                                    entries.add(entry_chart.get(2));

                                    BarDataSet barDataSet = new BarDataSet(entries, label.toString());

                                    barData = new BarData(barDataSet);

                                    // 새로운 IntegerValueFormatter 생성
                                    IntegerValueFormatter integerValueFormatter = new IntegerValueFormatter();

                                    // BarDataSet에 IntegerValueFormatter를 적용
                                    barDataSet.setValueFormatter(integerValueFormatter);

                                    barDataSet.setDrawIcons(false);
                                    barDataSet.setDrawValues(true);

                                    if (24 < dataSnapshot.child("스쿼트").getValue(int.class) || 24 < dataSnapshot.child("푸쉬업").getValue(int.class) || 24 < dataSnapshot.child("풀업").getValue(int.class)) {
                                        axisLeft.setAxisMaximum(50); // 최댓값
                                    } else if (50 < dataSnapshot.child("스쿼트").getValue(int.class) || 50 < dataSnapshot.child("푸쉬업").getValue(int.class) || 50 < dataSnapshot.child("풀업").getValue(int.class)) {
                                        axisLeft.setAxisMaximum(100);
                                    } else if (100 < dataSnapshot.child("스쿼트").getValue(int.class) || 100 < dataSnapshot.child("푸쉬업").getValue(int.class) || 100 < dataSnapshot.child("풀업").getValue(int.class)) {
                                        axisLeft.setAxisMaximum(200);
                                    }else if (200 < dataSnapshot.child("스쿼트").getValue(int.class) || 200 < dataSnapshot.child("푸쉬업").getValue(int.class) || 200 < dataSnapshot.child("풀업").getValue(int.class)){
                                        axisLeft.setAxisMaximum(1000);
                                    }

                                    barDataSet.setColors(Color.BLUE);
                                    barDataSet.setValueTextSize(15f);
                                    barData.addDataSet(barDataSet); // 해당 BarDataSet 을 적용될 차트에 들어갈 DataSet 에 넣는다.
                                    barData.setBarWidth(0.5f);
                                    barDataSet.setValueTextColor(Color.rgb(182, 182, 182));
                                    barChart.setData(barData); // 차트에 위의 DataSet 을 넣는다.
                                    barChart.invalidate(); // 차트 업데이트
                                    barChart.setTouchEnabled(false); // 차트 터치 불가능하게
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
                    barChart = (BarChart) findViewById(R.id.chart);
                    barChart.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 데이터 읽기가 실패한 경우

            }
        });


    }


}


class ExerciseItem {
    private String name;
    private String date;

    public ExerciseItem(String name, String date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

class CustomAdapter extends ArrayAdapter<ExerciseItem> {

    public CustomAdapter(@NonNull Context context, List<ExerciseItem> exerciseItems) {
        super(context, 0, exerciseItems);
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_list_item, parent, false);
        }

        ExerciseItem currentItem = getItem(position);

        TextView exerciseName = convertView.findViewById(R.id.exercise_name);
        TextView exerciseDate = convertView.findViewById(R.id.exercise_date);

        exerciseName.setText("운동 이름: " + currentItem.getName());
        exerciseDate.setText("날짜: " + currentItem.getDate());

        return convertView;
    }
}