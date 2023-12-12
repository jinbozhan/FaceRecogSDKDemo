package com.webank.offline.livedetection.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.webank.offline.livedetection.R;
import com.webank.offline.livedetection.camera.CameraSurfaceView;
import com.webank.offline.livedetection.camera.CameraUtils;
import com.webank.offline.livedetection.face.FaceLiveActionManager;
import com.webank.offline.livedetection.ui.model.LiveDetectionModel;
import com.webank.offline.livedetection.weight.HandlerThreadUtil;

import java.util.Random;

/**
 * @ProjectName: 活体检测离线版
 * @Package: com.webank.offline.livedetection.ui.activity
 * @ClassName: LiveDetectionActivity
 * @Description: 主页
 * @Author: Andy
 * @CreateDate: 2020/3/26 17:15
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/3/26 17:15
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public class LiveDetectionActivity extends AppCompatActivity {

    private final String TAG = LiveDetectionActivity.class.getSimpleName();

    private LiveDetectionModel model;

    private ImageView imgBg;
    private TextView tvResult;
    private CameraSurfaceView mSurfaceView;

    private HandlerThreadUtil handlerThreadUtil;

    private FaceLiveActionManager manager;

    private String[] actions = {"blink", "mouth", "shake", "node"};
    public static String action;

    private ProgressDialog dg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_detection_layout);

        model = ViewModelProviders.of(this).get(LiveDetectionModel.class);

        initView();
        initData();

    }

    private void initView() {
        imgBg = findViewById(R.id.image_bg);
        tvResult = findViewById(R.id.text_result);
        mSurfaceView = findViewById(R.id.surface_view);

        CameraUtils.calculateCameraPreviewOrientation(this);
        CameraUtils.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(final byte[] bytes, final Camera camera) {
                handlerThreadUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        manager.start(bytes);
                    }
                });
            }
        });
        handlerThreadUtil = new HandlerThreadUtil("UI_update");
        dg = new ProgressDialog(this);
        dg.setMessage("验证中，请稍等");
    }

    private void initData() {
        manager = FaceLiveActionManager.getInstance();
        manager.setActionCallback(new FaceLiveActionManager.LiveActionCallback() {

            @Override
            public void onShowAction(final String title) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        manager.setAction(true);
                    }
                }, 800);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvResult.setTextColor(getResources().getColor(R.color.text_color_blue));
                        imgBg.setImageResource(R.mipmap.detect_bg_blue);
                        tvResult.setText(title);
                    }
                });
            }

            @Override
            public void onShowResult(final boolean success, final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvResult.setText(result);
                        if (success) {
                            tvResult.setTextColor(getResources().getColor(R.color.text_color_blue));
                            imgBg.setImageResource(R.mipmap.detect_bg_blue);
                            dg.show();
                            CameraUtils.stopPreview();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    dg.dismiss();
                                    startActivity(new Intent(LiveDetectionActivity.this, DetectionSuccessActivity.class));
                                    LiveDetectionActivity.this.finish();
                                }
                            }, 2000);
                        } else {
                            tvResult.setTextColor(getResources().getColor(R.color.text_color_red));
                            imgBg.setImageResource(R.mipmap.detect_bg_red);
                        }
                    }
                });
            }
        });
        action = actions[new Random().nextInt(4)];

    }

    @Override
    protected void onResume() {
        super.onResume();
        CameraUtils.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraUtils.stopPreview();
    }

    @Override
    protected void onStop() {
        super.onStop();
        manager.stop();
    }
}
