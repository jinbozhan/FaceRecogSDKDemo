package com.webank.offline.livedetection.constant;

import android.os.Environment;

/**
 * @ProjectName: LiveDetection
 * @Package: com.webank.offline.livedetection.constant
 * @ClassName: Constant
 * @Description: java类作用描述
 * @Author: Andy
 * @Email: v_wbzyan@webank.com
 * @CreateDate: 2020/3/27 16:01
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/3/27 16:01
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class Constant {

    public static final String CRASH_LOG_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/live_detect/crash/";

    public static final int PREVIEW_SIZE_WIDTH = 640;
    public static final int PREVIEW_SIZE_HEIGHT = 480;

    public static final int FACE_FRAME_SIZE_WIDTH = 300;
    public static final int FACE_FRAME_SIZE_HEIGHT = 400;

}
