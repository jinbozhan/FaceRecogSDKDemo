package com.webank.offline.livedetection.weight;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * @ProjectName: LiveDetection
 * @Package: com.webank.offline.livedetection.weight
 * @ClassName: HandlerThreadUtil
 * @Description: java类作用描述
 * @Author: Andy
 * @Email: v_wbzyan@webank.com
 * @CreateDate: 2020/3/28 15:29
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/3/28 15:29
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class HandlerThreadUtil extends HandlerThread {

    private Handler handler;

    public Handler getHandler() {
        return this.handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public HandlerThreadUtil(String name) {
        super(name);
        this.start();
        this.handler = new Handler(this.getLooper());
    }

    public void stopThread() {
        if (null != this.handler) {
            this.handler.removeCallbacksAndMessages((Object) null);
        }

        if (Build.VERSION.SDK_INT >= 18) {
            this.quitSafely();
        } else {
            this.quit();
        }

    }

}
