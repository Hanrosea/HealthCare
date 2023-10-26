package com.healthcare.healthing;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MenuFragment extends Fragment {

    private Button StatBtn;
    private TextView BTN_N;
    private String stat;

    private int clickCount = 0;
    private AlertDialog alertDialog;

    private float progressValue;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private FirebaseDatabase mFirebaseDatabase;


    public MenuFragment() {
    }

    public static MenuFragment newInstance(String param1, String param2) {
        MenuFragment fragment = new MenuFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    private Button btn_logout;
    private Button btn_withdrawal;
    private Button btn_profile;
    private Button btn_record;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        User user = new User();


        //내정보
        btn_profile = view.findViewById(R.id.btn_proflie);
        btn_profile.setOnClickListener(new View.OnClickListener() {
            Intent intent = null;
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    intent = new Intent(getActivity(), ProfileActivity.class);
                }
                startActivity(intent);
            }
        });

        //최근 운동 기록
        btn_record = view.findViewById(R.id.btn_record);
        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    intent = new Intent(getActivity(), RecordListActivity.class);
                }
                startActivity(intent);
            }
        });

        // 원형 프로그래스 바 설정 뷰
        PieChart pieChart = view.findViewById(R.id.piechart);

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(1000f, "저체중"));
        entries.add(new PieEntry(1000f, "정상체중"));
        entries.add(new PieEntry(1000f, "과체중"));
        entries.add(new PieEntry(1000f, "경도비만"));
        entries.add(new PieEntry(1000f, "고도비만"));

        pieChart.getDescription().setEnabled(false);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(0f); // 숫자 값 크기를 0으로 설정하여 숨김

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        pieChart.animateXY(3000, 3000);

        DatabaseReference userRef = mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid()).child("AfterData");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String childPath = dataSnapshot.exists() ? "AfterData" : "BeforeData";

                DatabaseReference dataRef = mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid()).child(childPath);
                dataRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        // 변경된 데이터 처리
                        User user = dataSnapshot.getValue(User.class);
                        // BMI 계산
                        BMI_Calc(user, view);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        // 변경된 데이터 처리
                        User user = dataSnapshot.getValue(User.class);
                        // BMI 계산
                        BMI_Calc(user, view);
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

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 처리할 오류가 있으면 여기에 작성
            }
        });

        Button StatBtn = view.findViewById(R.id.StatBtn);
        StatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭 횟수 증가
                clickCount++;

                // 클릭 횟수가 10 이상인지 확인
                if (clickCount >= 5) {
                    // 팝업 대화상자를 표시합니다.
                    showPopupDialog("저체중ㅡ : ~ 18.5 " + "\n" +
                            "정상체중 : 18.5 ~ 24.9" + "\n" +
                            "과체중ㅡ : 25.0 ~ 29.9" + "\n" +
                            "경도비만 :  30.0 ~ 34.9" + "\n" +
                            "고도비만 : 35.0 ~ 39.9" + "\n" +
                            "고위험군 : 40 ~");
                }
            }
        });

        Button btn_food = view.findViewById(R.id.btn_food);
        btn_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭 횟수 증가
                clickCount++;

                // 클릭 횟수가 10 이상인지 확인
                if (clickCount >= 1) {
                    // 팝업 대화상자를 표시합니다.
                    showPopupDialog("아직 준비중입니다!");
                }
            }
        });

        Button btn_map = view.findViewById(R.id.btn_map);
        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭 횟수 증가
                clickCount++;

                // 클릭 횟수가 10 이상인지 확인
                if (clickCount >= 1) {
                    // 팝업 대화상자를 표시합니다.
                    showPopupDialog("아직 준비중입니다!");
                }
            }
        });

        //로그아웃
        btn_logout = view.findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                new AlertDialog.Builder(view.getContext())
                        .setMessage("로그아웃 하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mFirebaseAuth.signOut();
                                GoogleSignIn.getClient(getContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
                                Intent intent = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    intent = new Intent(getActivity(), AuthActivity.class);
                                }
                                startActivity(intent);
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton("아니오", null)
                        .show();
            }
        });

        //회원탈퇴
        btn_withdrawal = view.findViewById(R.id.btn_withdrawal);
        btn_withdrawal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(view.getContext())
                        .setMessage("정말 회원탈퇴 하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new AlertDialog.Builder(view.getContext())
                                        .setMessage("탈퇴하시면 정보는 전부 삭제됩니다.\n정말 삭제하시겠습니까?")
                                        .setCancelable(false)
                                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which){


                                                mFirebaseDatabase.getReference("memos/" + mFirebaseAuth.getUid()+"/BeforeData")
                                                        .addChildEventListener(new ChildEventListener() {
                                                            @Override
                                                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                                                Intent intent = null;
                                                                User user = dataSnapshot.getValue(User.class);

                                                                Log.d("FCM_TEST", Long.toString(user.getCount()));
                                                                mFirebaseDatabase.getReference("memos/users/usersID" + user.getCount() + "/userData").removeValue();
                                                                mFirebaseDatabase.getReference("memos/" + mFirebaseUser.getUid()).removeValue();
                                                                mFirebaseAuth.getCurrentUser().delete();
                                                                GoogleSignIn.getClient(getContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();

                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                                    intent = new Intent(getActivity(), AuthActivity.class);
                                                                }
                                                                startActivity(intent);
                                                                getActivity().finish();
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
                                        })
                                        .setNegativeButton("아니오", null)
                                        .show();
                            }
                        })
                        .setNegativeButton("아니오", null)
                        .show();
            }
        });
        return view;
    }

    // 성별과 progressValue를 기반으로 Stat을 계산하는 메서드
    private String calculateStat(String sex, float progressValue) {
        if ("남".equals(sex)) {
            if (progressValue < 20) {
                return "저체중";
            } else if (progressValue >= 20 && progressValue <= 24.9) {
                return "정상체중";
            } else if (progressValue >= 25 && progressValue <= 29.9) {
                return "과체중";
            } else if (progressValue >= 30 && progressValue <= 34.9) {
                return "경도비만";
            } else if (progressValue >= 35 && progressValue <= 39.9) {
                return "고도비만";
            } else {
                return "고위험군";
            }
        } else {
            if (progressValue < 18.5) {
                return "저체중";
            } else if (progressValue >= 18.5 && progressValue <= 23.9) {
                return "정상체중";
            } else if (progressValue >= 24 && progressValue <= 29.9) {
                return "과체중";
            } else if (progressValue >= 30 && progressValue <= 34.9) {
                return "경도비만";
            } else if (progressValue >= 35 && progressValue <= 39.9) {
                return "고도비만";
            } else {
                return "고위험군";
            }
        }
    }

    // BMI 지수 색상
    private int getBackgroundColorForStat(String stat) {
        int backgroundColor; // 기본 배경 색상 (예시: 흰색)

        switch (stat) {
            case "저체중":
                backgroundColor = Color.parseColor("#B74C7A"); // 저체중에 대한 배경 색상
                break;
            case "정상체중":
                backgroundColor = Color.parseColor("#FF8C00"); // 정상체중에 대한 배경 색상
                break;
            case "과체중":
                backgroundColor = Color.parseColor("#FFD700"); // 과체중에 대한 배경 색상
                break;
            case "경도비만":
                backgroundColor = Color.parseColor("#6B8E23"); // 경도비만에 대한 배경 색상
                break;
            case "고도비만":
                backgroundColor = Color.parseColor("#CD853F"); // 고도비만에 대한 배경 색상
                break;
            default:
                backgroundColor = Color.parseColor("#FFFFFF"); // 기본 배경 색상 (흰색)
        }
        return backgroundColor;
    }

    // BMI 계산
    private void BMI_Calc(User user, View view) {
        float weight = Float.parseFloat(user.getWeight());
        float height = Float.parseFloat(user.getHeight());
        String sex = user.getSex();

        float progressValue = (float) (weight / Math.pow(height / 100, 2));
        String stat = calculateStat(sex, progressValue);

        Button Stat = view.findViewById(R.id.StatBtn);
        TextView BMI_N = view.findViewById(R.id.BMI_N);

        String formattedValue = String.format("%.1f", progressValue);
        Stat.setText(stat);
        BMI_N.setText(formattedValue);

        int backgroundColor = getBackgroundColorForStat(stat);
        Stat.setBackgroundColor(backgroundColor);
    }

    // 팝업
    private void showPopupDialog(String info) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(info)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 사용자가 대화상자를 인식한 후 클릭 횟수를 재설정합니다.
                        clickCount = 0;
                    }
                });
        alertDialog = builder.create();
        alertDialog.show();
    }
}