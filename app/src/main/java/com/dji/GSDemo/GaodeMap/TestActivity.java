package com.dji.GSDemo.GaodeMap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mWayPointButton;
    private Button mHotPointButton;
    private Button mVirtualStickyButton;
    private Button mCameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);

        initView();

        setListener();
    }

    private void initView() {
        mWayPointButton = findViewById(R.id.btn_way_point);
        mHotPointButton = findViewById(R.id.btn_hot_point);
        mVirtualStickyButton = findViewById(R.id.btn_virtual_sticky_point);
        mCameraButton = findViewById(R.id.btn_way_camera);
    }

    private void setListener() {
        mWayPointButton.setOnClickListener(this);
        mHotPointButton.setOnClickListener(this);
        mVirtualStickyButton.setOnClickListener(this);
        mCameraButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_way_point:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_hot_point:
                showToast("hot point");
                break;
            case R.id.btn_virtual_sticky_point:
                showToast("virtual sticky");
                break;
            case R.id.btn_way_camera:
                showToast("camera");
                break;
            default:
                break;
        }
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(TestActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
