package com.healthcare.healthing;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;

public class UserdataActivity extends AppCompatActivity {

    private Button btn_move2, btn_cancel, btn_move3;
    private EditText age_text;
    private EditText height_text;
    private EditText weight_text;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private TextView txtName2, age_limit, height_limit, weight_limit;
    private RadioGroup sex;
    private User user;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdata);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        user = new User();

        age_text = (EditText) findViewById(R.id.age_text);
        height_text = (EditText) findViewById(R.id.height_text);
        weight_text = (EditText) findViewById(R.id.weight_text);
        txtName2 = (TextView) findViewById(R.id.txtName2);
        age_limit = (TextView) findViewById(R.id.age_limit);
        height_limit = (TextView) findViewById(R.id.height_limit);
        weight_limit = (TextView) findViewById(R.id.weight_limit);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        sex = (RadioGroup) findViewById(R.id.sex_group);
        btn_move3 = (Button) findViewById(R.id.btn_move3);

        txtName2.setText("  " + mFirebaseUser.getDisplayName());

        btn_cancel = (Button) findViewById(R.id.btn_cancel);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseAuth.signOut();
                GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
                startActivity(new Intent(UserdataActivity.this, AuthActivity.class));
                finish();
            }
        });

        if(mFirebaseUser == null){
            startActivity(new Intent(UserdataActivity.this, AuthActivity.class));
            finish();
            return;
        }

        sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.male_btn:
                        user.setSex("남");
                        break;
                    case R.id.female_btn:
                        user.setSex("여");
                        break;
                }
            }
        });




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

                if(user.getSex() == null){
                    Toast.makeText(UserdataActivity.this, "성별를 선택해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(age_text.getText().toString().length() == 0){
                    Toast.makeText(UserdataActivity.this, "나이를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(height_text.getText().toString().length() == 0){
                    Toast.makeText(UserdataActivity.this, "키를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(weight_text.getText().toString().length() == 0){
                    Toast.makeText(UserdataActivity.this, "몸무게를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                user.setAge(age_text.getText().toString());
                user.setHeight(height_text.getText().toString());
                user.setWeight(weight_text.getText().toString());

                saveMemo();

                Intent intent = new Intent(UserdataActivity.this, TutorialActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn_move3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                user.setSex("");

                user.setAge("0");
                user.setHeight("0");
                user.setWeight("0");

                saveMemo();

                Intent intent = new Intent(UserdataActivity.this, TutorialActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    Button.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(UserdataActivity.this, TutorialActivity.class);
            startActivity(intent);
            finish();
        }
    };
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("종료 하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFirebaseAuth.signOut();
                        GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
                        finish();
                    }
                })
                .setNegativeButton("아니오", null)
                .show();
    }
    private void saveMemo(){
        User updatedUser = new User();
        updatedUser.setSex(user.getSex());
        updatedUser.setAge(user.getAge());
        updatedUser.setHeight(user.getHeight());
        updatedUser.setWeight(user.getWeight());
        updatedUser.setName(mFirebaseUser.getDisplayName());
        updatedUser.setEmail(mFirebaseUser.getEmail());


        // 수정할 데이터의 참조 경로 가져오기
        DatabaseReference userRef = mFirebaseDatabase.getReference("memos/users");

        // 수정할 데이터의 참조 경로와 고유 ID를 결합하여 해당 데이터의 참조 경로를 가져옵니다.
        userRef.child("count").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long count = dataSnapshot.getValue(Long.class);
                if (count != null) {
                    updatedUser.setCount(count + 1);
                } else {
                    updatedUser.setCount(1);
                }
                Log.w("testtest", String.valueOf(updatedUser.getCount()));
                DatabaseReference userToUpdateRef = userRef.child("/usersID" + updatedUser.getCount() + "/userData");

                Map<String, Object> updates = new HashMap<>();
                updates.put("count", updatedUser.getCount());
                userRef.updateChildren(updates);

                // 수정할 데이터의 값을 Map 객체로 만듭니다.
                Map<String, Object> updates1 = new HashMap<>();
                updates1.put("name", updatedUser.getName());
                updates1.put("email", updatedUser.getEmail());

                // 해당 데이터의 참조 경로에 updateChildren() 메소드를 호출하여 값을 수정합니다.
                userToUpdateRef.updateChildren(updates1);

                // 수정할 데이터의 참조 경로 가져오기
                DatabaseReference memoRef = mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid());

                // 수정할 데이터의 참조 경로와 고유 ID를 결합하여 해당 데이터의 참조 경로를 가져옵니다.
                DatabaseReference memoToUpdateRef = memoRef.child("/BeforeData/profile");

                // 수정할 데이터의 값을 Map 객체로 만듭니다.
                Map<String, Object> updates2 = new HashMap<>();
                updates2.put("age", updatedUser.getAge());
                updates2.put("height", updatedUser.getHeight());
                updates2.put("weight", updatedUser.getWeight());
                updates2.put("count", updatedUser.getCount());
                updates2.put("sex", updatedUser.getSex());

                // 해당 데이터의 참조 경로에 updateChildren() 메소드를 호출하여 값을 수정합니다.
                memoToUpdateRef.updateChildren(updates2);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("MainActivity", "getCountValue:onCancelled", databaseError.toException());
            }
        });




    }
}