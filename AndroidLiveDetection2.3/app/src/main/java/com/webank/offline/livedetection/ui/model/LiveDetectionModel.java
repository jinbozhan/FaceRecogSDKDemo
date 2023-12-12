package com.webank.offline.livedetection.ui.model;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;

import androidx.lifecycle.ViewModel;

import com.webank.offline.livedetection.app.WeBankApp;

/**
 * @ProjectName: LiveDetection
 * @Package: com.webank.offline.livedetection.ui.model
 * @ClassName: LiveDetectionModel
 * @Description: java类作用描述
 * @Author: Andy
 * @Email: v_wbzyan@webank.com
 * @CreateDate: 2020/3/26 17:14
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/3/26 17:14
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class LiveDetectionModel extends ViewModel {

    @SuppressLint("UnsupportedChromeOsCameraSystemFeature")
    private boolean checkCameraHardware() {
        if (WeBankApp.getInstance().getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

}
