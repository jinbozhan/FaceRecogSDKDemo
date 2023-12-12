package com.webank.offline.livedetection.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.webank.offline.livedetection.R;

/**
 * @ProjectName: LiveDetection
 * @Package: com.webank.offline.livedetection.ui.activity
 * @ClassName: DetectionSuccessActivity
 * @Description: java类作用描述
 * @Author: Andy
 * @Email: v_wbzyan@webank.com
 * @CreateDate: 2020/3/29 15:34
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/3/29 15:34
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class DetectionSuccessActivity extends AppCompatActivity {

    private Button btnSure;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection_success_layout);

        btnSure = findViewById(R.id.btn_sure);
        btnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DetectionSuccessActivity.this.finish();
            }
        });
    }
}
