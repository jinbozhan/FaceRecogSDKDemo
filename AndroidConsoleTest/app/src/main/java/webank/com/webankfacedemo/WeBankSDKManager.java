package webank.com.webankfacedemo;

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;


import java.io.File;

import webank.com.facefeature.FaceFeature;
import webank.com.faceattribute.FaceAttribute;
import webank.com.facelivecolor.FaceLiveColor;
import webank.com.faceliveir.FaceLiveIR;
import webank.com.facequality.FaceQuality;
import webank.com.facetracker.FaceTracker;
import webank.com.faceretrieve.FaceRetrieve;


public class WeBankSDKManager {
    private static final String TAG = "WeBankSDKManager";

    // 人脸特征长度：512 维，取决于 FaceFeature 的版本
    public static final int FACE_FEAT_LENGTH = FaceFeature.FEATURE_LENGTH;

    // 输入图片旋转flag， 可取1,2,3,4,5,6,7,8, 1代表不旋转
    public static final int FRAME_ORIENTATION = 1;

    // 阈值：一般会跟 **模型版本/使用场景** 相关，所以请咨询优图开发人员获取对应 SDK 恰当的阈值
    public static final float FACE_RETRIEVE_THRESHOLD = 0.0f; // 人脸检索阈值
    public static final float FACE_QUALITY_THRESHOLD = 0.0f; // 人脸质量评估阈值

    // 人脸活体分数阈值：用于活体是否真人判断
    public static final float FACE_LIVE_COLOR_THRESHOLD = 0.0f; // 彩色图活体阈值
    public static final float FACE_LIVE_IR_THRESHOLD = 0.0f;  // 红外活体阈值
    //public static final float FACE_LIVE_3D_THRESHOLD = 0.9f;  // 3D活体阈值

    // 人脸检索库 ID
    public static final String FACE_RETRIEVE_LIB_ID = "1";

    FaceFeature mFaceFeature;
    FaceAttribute mFaceAttribute;
    FaceRetrieve mFaceRetrieve;
    FaceTracker mFaceTracker;
    FaceQuality mFaceQuality;
    FaceLiveIR mFaceLiveIR;
    FaceLiveColor mFaceLiveColor;

    // 实例化相关 SDK
    // 请根据需要使用到的 SDK 删减相关实例
    // 多线程处理，请自行控制 SDK 实例
    public WeBankSDKManager(AssetManager assetManager) {
        // 初始化需要指定实例的配置参数
        // 详细说明可以在 人脸追踪文档 下查询
        FaceTracker.Options options = new FaceTracker.Options();
        options.biggerFaceMode = false; // 门禁场景设置为false，检测多个人脸
        options.maxFaceSize = 999999;
        options.minFaceSize = 40;
        options.needDenseKeyPoints = true; // 门禁场景设置为true，输出关键点遮挡信息
        options.needPoseEstimate = true;  // 门禁场景设置为true，输出姿态角度信息
        options.detectInterval = 6; // 追踪检测， 检测 + (detectInterval次)追踪 + 检测 + (detectInterval次)追踪 ...

        mFaceTracker = new FaceTracker(options);

        // FaceRetrieve 初始化实例时，需要指定检索的特征长度
        // 详细说明可以在 人脸检索文档 下查询
        mFaceRetrieve = new FaceRetrieve(FACE_FEAT_LENGTH);
        mFaceFeature = new FaceFeature();
        mFaceAttribute = new FaceAttribute();
        mFaceQuality = new FaceQuality();
        mFaceLiveIR = new FaceLiveIR();
        mFaceLiveColor = new FaceLiveColor();
    }

    public void destroy() {
        mFaceFeature.destroy();
        mFaceAttribute.destroy();
        mFaceTracker.destroy();
        mFaceRetrieve.destroy();
        mFaceQuality.destroy();
        mFaceLiveIR.destroy();
        mFaceLiveColor.destroy();
    }

    // TODO: [可选][离线人脸识别比对] 提取单张图片的人脸特征
    public float[] extractFaceFeature(byte[] data, int width, int height) {
        float[] feat = new float[FACE_FEAT_LENGTH];

        FaceTracker.Options options = new FaceTracker.Options();
        options.biggerFaceMode = true;
        options.maxFaceSize = 999999;
        options.minFaceSize = 40;
        options.needDenseKeyPoints = false;
        options.needPoseEstimate = false;
        options.detectInterval = 6;

        FaceTracker.TrackedFace[] faceBoxes = mFaceTracker.detectRGB(data, width, height, FRAME_ORIENTATION, null, null);

        if (faceBoxes.length == 0) {
            // TODO: 处理注册照不存在人脸的情况
            return feat;
        } else if (faceBoxes.length > 1) {
            // TODO: 处理注册照存在多张人脸的异常情况
            return feat;
        }

        int ret = mFaceFeature.extractRGB(faceBoxes[0].xy5Points, data, width, height, FRAME_ORIENTATION, feat);

        if (ret != 0) {
            // TODO: 处理提取特征失败
            return feat;
        }
        return feat;
    }

    public static void loadLibs() {
//        System.loadLibrary("opencv_java3");
        System.loadLibrary("WeBankCommon");
        System.loadLibrary("WeBankFaceFeature");
        System.loadLibrary("WeBankFaceAttribute");
        System.loadLibrary("WeBankFaceRetrieve");
        System.loadLibrary("WeBankFaceLiveIR");
        System.loadLibrary("WeBankFaceLiveColor");
        System.loadLibrary("WeBankFaceTracker");
        System.loadLibrary("WeBankFaceQuality");
    }

    // 加载模型文件：输入 `assets` 目录下模型配置文件的目录和 config 文件名，例如 `models/v7114/config.ini`
    // 请输入模型文件的 `config.ini` 所在 assets 下的相对路径，和 `config.ini` 文件名。
    // 请确保 `config.ini` 文件与模型文件处于同一目录下
    // globalInit 需要判断返回值是否成功，具体接口内容请查看提供的 docs/api.md 文件
    public static void loadModels(AssetManager assetManager) {
        int result;

        result = FaceTracker.globalInit(assetManager, "models/face-tracker", "config.ini");
        Log.d(TAG, "FaceTracker Version: " + FaceTracker.getVersion() + "; global result = " + result);


        result = FaceFeature.globalInit(assetManager, "models/face-feature", "config.ini");
        Log.d(TAG, "FaceFeature Version: " + FaceFeature.getVersion() + "; global result = " + result);

        result = FaceAttribute.globalInit(assetManager, "models/face-attribute", "config.ini");
        Log.d(TAG, "FaceAttribute Version: " + FaceAttribute.getVersion() + "; global result = " + result);

        result = FaceLiveIR.globalInit(assetManager, "models/face-live-ir", "config.ini");
        Log.d(TAG, "FaceLiveIR Version: " + FaceLiveIR.getVersion() + "; global result = " + result);

        result = FaceLiveColor.globalInit(assetManager, "models/face-live-color", "config.ini");
        Log.d(TAG, "FaceLiveColor Version: " + FaceLiveColor.getVersion() + "; global result = " + result);

        // FaceRetrieve 不包含模型，只包含具体的检索算法
        Log.d(TAG, "FaceRetrieve Version: " + FaceRetrieve.getVersion());

        result = FaceQuality.globalInit(assetManager, "models/face-quality", "config.ini");
        Log.d(TAG, "FaceQuality Version: " + FaceQuality.getVersion() + "; global result = " + result);
    }

    // 释放模型文件
    public static void releaseModels() {
        FaceFeature.globalRelease();
        FaceAttribute.globalRelease();
        FaceTracker.globalRelease();
        FaceLiveIR.globalRelease();
        FaceLiveColor.globalRelease();
        FaceQuality.globalRelease();
    }
}
