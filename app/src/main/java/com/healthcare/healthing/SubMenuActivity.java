package com.healthcare.healthing;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SubMenuActivity extends AppCompatActivity {
    private Button btn_move2, btn_cancel;
    private EditText age_text;
    private EditText height_text;
    private EditText weight_text;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private TextView age_limit, height_limit, weight_limit;
    private User user;
    private RadioGroup sex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_menu);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        user = new User();

        age_text = (EditText) findViewById(R.id.age_text);
        height_text = (EditText) findViewById(R.id.height_text);
        weight_text = (EditText) findViewById(R.id.weight_text);
        age_limit = (TextView) findViewById(R.id.age_limit);
        height_limit = (TextView) findViewById(R.id.height_limit);
        weight_limit = (TextView) findViewById(R.id.weight_limit);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        sex = (RadioGroup) findViewById(R.id.sex_group1);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SubMenuActivity.this, ProfileActivity.class));
                finish();
            }
        });

        sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.male_btn1:
                        user.setSex("남");
                        break;
                    case R.id.female_btn1:
                        user.setSex("여");
                        break;
                }
            }
        });


        if(mFirebaseUser == null){
            startActivity(new Intent(SubMenuActivity.this, AuthActivity.class));
            finish();
            return;
        }

        DatabaseReference userRef = mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid()).child("/AfterData");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 데이터가 존재하는 경우
                    mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid()).child("/AfterData")
                            .addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                    User user = dataSnapshot.getValue(User.class);
                                    age_text.setText(user.getAge());
                                    height_text.setText(user.getHeight());
                                    weight_text.setText(user.getWeight());
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
                    // 데이터가 존재하지 않는 경우
                    mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid()).child("/BeforeData")
                            .addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                    User user = dataSnapshot.getValue(User.class);
                                    age_text.setText(user.getAge());
                                    height_text.setText(user.getHeight());
                                    weight_text.setText(user.getWeight());
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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 데이터 읽기가 실패한 경우

            }
        });


        //나이 글자 제한 수 표시
        age_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = age_text.getText().toString();
                age_limit.setText(input.length()+"/3");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //키 글자 제한 수 표시
        height_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = height_text.getText().toString();
                height_limit.setText(input.length()+"/3");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //몸무게 글자 제한 수 표시
        weight_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = weight_text.getText().toString();
                weight_limit.setText(input.length()+"/3");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_move2 = findViewById(R.id.btn_move2);
        btn_move2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(age_text.getText().toString().length() == 0){
                    Toast.makeText(SubMenuActivity.this, "나이를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(height_text.getText().toString().length() == 0){
                    Toast.makeText(SubMenuActivity.this, "키를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(weight_text.getText().toString().length() == 0){
                    Toast.makeText(SubMenuActivity.this, "몸무게를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 수정할 User 객체 생성
                User updatedUser = new User();
                updatedUser.setSex(user.getSex());
                updatedUser.setAge(age_text.getText().toString());
                updatedUser.setHeight(height_text.getText().toString());
                updatedUser.setWeight(weight_text.getText().toString());

                // 수정할 데이터의 참조 경로 가져오기
                DatabaseReference memoRef = mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid()).child("/AfterData/");

                // 수정할 데이터의 참조 경로와 고유 ID를 결합하여 해당 데이터의 참조 경로를 가져옵니다.
                DatabaseReference memoToUpdateRef = memoRef.child("profile");

                // 수정할 데이터의 값을 Map 객체로 만듭니다.
                Map<String, Object> updates = new HashMap<>();
                updates.put("sex", updatedUser.getSex());
                updates.put("age", updatedUser.getAge());
                updates.put("height", updatedUser.getHeight());
                updates.put("weight", updatedUser.getWeight());

                // 해당 데이터의 참조 경로에 updateChildren() 메소드를 호출하여 값을 수정합니다.
                memoToUpdateRef.updateChildren(updates);
                startActivity(new Intent(SubMenuActivity.this, ProfileActivity.class));
                finish();
            }

        });
    }
    public void onBackPressed() {
        Intent intent = new Intent(SubMenuActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}
