package com.healthcare.healthing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class RecordListActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private CustomAdapter adapter;
    private TextView noRecordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        noRecordTextView = findViewById(R.id.noRecordTextView);
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
