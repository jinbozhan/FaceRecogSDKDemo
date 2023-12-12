package com.webank.offline.livedetection.face;

import android.content.res.AssetManager;
import android.util.Log;

import com.webank.offline.livedetection.app.WeBankApp;

import webank.com.faceliveaction.FaceLiveAction;
import webank.com.facequality.FaceQuality;
import webank.com.facetracker.FaceTracker;


public class WbFaceSdkManager {

    private static final String TAG = WbFaceSdkManager.class.getSimpleName();

    // 输入图片旋转flag， 可取1,2,3,4,5,6,7,8, 1代表不旋转
    public static final int FRAME_ORIENTATION = 7;

    public FaceTracker mFaceTracker; // 人脸追踪
    public FaceQuality mFaceQuality; // 质量检测
    public FaceLiveAction mFaceLiveAction; // 动作活体

    static boolean isSuccess = true;

    // 加载模型文件：输入 `assets` 目录下模型配置文件的目录和 config 文件名，例如 `models/v7114/config.ini`
    // 请输入模型文件的 `config.ini` 所在 assets 下的相对路径，和 `config.ini` 文件名。
    // 请确保 `config.ini` 文件与模型文件处于同一目录下
    // globalInit 需要判断返回值是否成功，具体接口内容请查看提供的 docs/api.md 文件
    static {
//        System.loadLibrary("omp");
//        System.loadLibrary("opencv_java3");
//        System.loadLibrary("WeBankCommon");
        System.loadLibrary("WeBankFaceLiveAction");
        System.loadLibrary("WeBankFaceTracker");
        System.loadLibrary("WeBankFaceQuality");

        AssetManager manager = WeBankApp.getInstance().getApplicationContext().getAssets();
        int result = FaceTracker.globalInit(manager, "models/face-tracker", "config.ini");
        Log.d(TAG, "FaceTracker Version: " + FaceTracker.getVersion() + "; global result = " + result);
        if (result < 0) isSuccess = false;

        result = FaceQuality.globalInit(manager, "models/face-quality", "config.ini");
        Log.d(TAG, "FaceQuality Version: " + FaceQuality.getVersion() + "; global result = " + result);

        // FaceLiveAction 不包含模型，只包含具体的检测算法
        Log.d(TAG, "FaceLiveAction Version: " + FaceLiveAction.getVersion());
    }

    private static WbFaceSdkManager manager;

    public static WbFaceSdkManager getInstance() {
        if (manager == null) {
            manager = new WbFaceSdkManager();
        }
        return manager;
    }

    // 实例化相关 SDK
    // 请根据需要使用到的 SDK 删减相关实例
    // 多线程处理，请自行控制 SDK 实例
    public WbFaceSdkManager() {
    }

    public boolean initModels() {
        if (!isSuccess) {
            return false;
        }
        // 初始化需要指定实例的配置参数
        // 详细说明可以在 人脸追踪文档 下查询
        FaceTracker.Options options = new FaceTracker.Options();
        options.biggerFaceMode = true; // 门禁场景设置为false，检测多个人脸
        options.maxFaceSize = 999999;
        options.minFaceSize = 40;
        options.needDenseKeyPoints = true; // 门禁场景设置为true，输出关键点遮挡信息
        options.needPoseEstimate = true;  // 门禁场景设置为true，输出姿态角度信息
        options.detectInterval = 6; // 追踪检测， 检测 + (detectInterval次)追踪 + 检测 + (detectInterval次)追踪 ...

        mFaceTracker = new FaceTracker(options);
        mFaceQuality = new FaceQuality();
        mFaceLiveAction = new FaceLiveAction();
        return true;
    }


    /**
     * 销毁实例
     */
    public void destroy() {
        mFaceTracker.destroy();
        mFaceQuality.destroy();
        mFaceLiveAction.destroy();
        releaseModels();
    }

    // 释放模型文件
    private void releaseModels() {
        FaceTracker.globalRelease();
    }

}
