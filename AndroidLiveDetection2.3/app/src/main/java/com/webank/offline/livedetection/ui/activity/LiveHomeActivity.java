package com.webank.offline.livedetection.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import com.webank.offline.livedetection.R;
import com.webank.offline.livedetection.ui.model.LiveHomeViewModel;

/**
 * @ProjectName: 活体检测离线版
 * @Package: com.webank.offline.livedetection.ui.activity
 * @ClassName: LiveHomeActivity
 * @Description: 主页
 * @Author: Andy
 * @Email: v_wbzyan@webank.com
 * @CreateDate: 2020/3/26 17:23
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/3/26 17:23
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class LiveHomeActivity extends AppCompatActivity {

    private final String TAG = LiveHomeActivity.class.getSimpleName();

    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    private final int RC_PERMISSION = 101;

    private LiveHomeViewModel model;

    private Button btnNext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_home_layout);

        model = ViewModelProviders.of(this).get(LiveHomeViewModel.class);

        btnNext = findViewById(R.id.btn_start);

        checkPermission();

    }

    private void checkPermission() {
        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                || !(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
            // 没有权限，申请权限
            // 申请权限，其中RC_PERMISSION是权限申请码，用来标志权限申请的
            ActivityCompat.requestPermissions(this, permissions, RC_PERMISSION);
        } else {
            //拥有权限
            initData();
        }

    }

    private void initData() {
        if (!model.webankAuth()) {
            Toast.makeText(this, "授权失败，请检查文件", Toast.LENGTH_LONG).show();
            return;
        }
        if (!model.initSDK()) {
            Toast.makeText(this, "人脸SDK初始化失败", Toast.LENGTH_LONG).show();
            return;
        }
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LiveHomeActivity.this, LiveDetectionActivity.class));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("onDestroy", "onDestroy");
        model.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PERMISSION && grantResults.length == 2
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "权限申请成功");
            initData();
        } else {
            Log.e(TAG, "权限申请失败");
            this.finish();
        }
    }

}
