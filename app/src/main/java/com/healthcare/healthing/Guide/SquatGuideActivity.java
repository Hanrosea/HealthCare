package com.healthcare.healthing.Guide;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.healthcare.healthing.R;
import com.healthcare.healthing.java.CameraXLivePreviewActivity;

public class SquatGuideActivity extends AppCompatActivity {

    private Button startButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squat_guide);

        startButton = (Button) findViewById(R.id.squat_start_btn);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SquatGuideActivity.this, CameraXLivePreviewActivity.class);
                intent.putExtra("Health", 1);
                startActivity(intent);
                finish();
            }
        });
    }
}