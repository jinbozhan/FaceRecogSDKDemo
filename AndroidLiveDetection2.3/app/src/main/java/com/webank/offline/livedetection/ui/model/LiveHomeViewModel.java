package com.webank.offline.livedetection.ui.model;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.webank.offline.livedetection.app.WeBankApp;
import com.webank.offline.livedetection.face.WbFaceSdkManager;

//import webank.com.common.util.CommonInterface;

/**
 * @ProjectName: LiveDetection
 * @Package: com.webank.offline.livedetection.ui.model
 * @ClassName: LiveHomeViewModel
 * @Description: java类作用描述
 * @Author: Andy
 * @Email: v_wbzyan@webank.com
 * @CreateDate: 2020/3/26 17:24
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/3/26 17:24
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class LiveHomeViewModel extends ViewModel {

    private final String TAG = LiveHomeViewModel.class.getSimpleName();

    /**
     * 授权
     */
    public boolean webankAuth() {
//        int result = CommonInterface.initAuth(WeBankApp.getInstance().getApplicationContext(), "test.lic", 0);
//        if (result != 0) {
//            Log.e(TAG, "auth failed with error code: " + result + "    reason:" + CommonInterface.getFailedReason());
//            return false;
//        }
        return true;
    }

    /**
     * 初始化
     */
    public boolean initSDK() {
        boolean isSuccess = WbFaceSdkManager.getInstance().initModels();
        if (!isSuccess) {
            Log.e(TAG, "SDK模型初始化失败");
            return false;
        }
        return true;
    }

    public void destroy() {
        // 注意：一定要先 destroy 实例，再全局释放 models
        WbFaceSdkManager.getInstance().destroy();
    }

}
