package com.webank.offline.livedetection.app;

import android.app.Application;

import com.webank.offline.livedetection.util.CrashHandler;

/**
 * @ProjectName: WeBankFaceAndroid_new
 * @Package: webank.com.webankfacedemo.app
 * @ClassName: WeBankApp
 * @Description: java类作用描述
 * @Author: Andy
 * @Email: v_wbzyan@webank.com
 * @CreateDate: 2020/2/28 9:51
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/2/28 9:51
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class WeBankApp extends Application {

    private static WeBankApp app;

    public static WeBankApp getInstance() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        initParam();

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }

    private void initParam() {

    }
}
