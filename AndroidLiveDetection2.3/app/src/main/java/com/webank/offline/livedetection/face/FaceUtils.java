package com.webank.offline.livedetection.face;

import webank.com.facetracker.FaceTracker;

/**
 * @ProjectName: WeBankFaceAndroid_new
 * @Package: webank.com.webankfacedemo.util
 * @ClassName: FaceUtils
 * @Description: java类作用描述
 * @Author: Andy
 * @Email: v_wbzyan@webank.com
 * @CreateDate: 2020/3/1 14:21
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/3/1 14:21
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class FaceUtils {

    /**
     * 人脸追踪
     *
     * @param rgbData 图片数据
     * @param width   图片宽
     * @param height  图片高
     * @return
     */
    public static FaceTracker.TrackedFace[] trackedFaces(byte[] rgbData, int width, int height) {
        FaceTracker.TrackedFace[] faces = WbFaceSdkManager.getInstance().mFaceTracker.trackYUV(rgbData, width, height, WbFaceSdkManager.FRAME_ORIENTATION, null);
        return faces;
    }

    /**
     * 过滤人脸
     *
     * @param face
     * @param frameWidth
     * @param frameHeight
     * @return
     */
    public static String filterFaces(FaceTracker.TrackedFace face, int frameWidth, int frameHeight) {
        // 人脸过大
        if ((face.xyAllPoints[64] - face.xyAllPoints[0]) >= frameWidth * 0.7 ||                     // 人脸宽不能超过框的0.8倍
                (face.xyAllPoints[33] - face.xyAllPoints[71]) >= frameHeight * 0.7) {               // 人脸高不能超过框的0.8倍
            return "离远一点";
        }
        // 人脸不在框内
        if (face.xyAllPoints[0] < frameWidth * 0.05 ||                       // 宽大于框宽的0.05倍
                face.xyAllPoints[71] < frameHeight * 0.05 ||                  // 高大于框高的0.05倍
                face.xyAllPoints[64] > frameWidth * 0.95 ||  // 宽小于框宽的0.95倍
                face.xyAllPoints[33] > frameHeight * 0.95) {// 高小于框高的0.95倍
            return "请勿将脸移出框外";
        }
        // 人脸过小
        if ((face.xyAllPoints[64] - face.xyAllPoints[0]) < frameWidth * 0.25 ||                      // 人脸宽不能小于框的0.5倍
                (face.xyAllPoints[33] - face.xyAllPoints[71]) < frameHeight * 0.15) {                // 人脸宽不能小于框的0.5倍
            return "靠近一点";
        }
        return null;
    }

    /**
     * 是否眨眼
     *
     * @param face_bbox
     * @param face_landmarks
     * @return
     */
    public static int blinkAction(int[] face_bbox, float[] face_landmarks) {
        int result = WbFaceSdkManager.getInstance().mFaceLiveAction.Blink(true, face_bbox, face_landmarks);
        return result;
    }

    /**
     * 是否张嘴
     *
     * @param face_bbox
     * @param face_landmarks
     * @return
     */
    public static int mouthAction(int[] face_bbox, float[] face_landmarks) {
        int result = WbFaceSdkManager.getInstance().mFaceLiveAction.Mouth(true, face_bbox, face_landmarks);
        return result;
    }

    /**
     * 是否摇头
     *
     * @param yaw
     * @return
     */
    public static int shakeAction(float yaw) {
        int result = WbFaceSdkManager.getInstance().mFaceLiveAction.Shake(true, yaw);
        return result;
    }

    /**
     * 是否点头
     *
     * @param pitch
     * @return
     */
    public static int nodeAction(float pitch) {
//        int result = WbFaceSdkManager.getInstance().mFaceLiveAction.Node(true, pitch);
        int result = WbFaceSdkManager.getInstance().mFaceLiveAction.Nod(true, pitch);
        return result;
    }

    /**
     * 重置动作状态
     *
     * @param mode 动作
     * @return
     */
    public static int actionReset(String mode) {
        int result = WbFaceSdkManager.getInstance().mFaceLiveAction.Reset(mode);
        return result;
    }

    /**
     * 人脸质量过滤
     *
     * @param optimizedFace 人脸框位置
     * @param yuvData       YUV图的图像数据
     * @param width         YUV图的宽
     * @param height        YUV图的高
     * @return
     */
    public static float qualityFaceNum(FaceTracker.TrackedFace optimizedFace, byte[] yuvData, int width, int height) {
        // 计算模糊度和亮度分数
        //float blur_illumination_score = WbFaceSdkManager.getInstance().mFaceQuality.evaluateYUV(optimizedFace.bbox, yuvData, width, height, WbFaceSdkManager.FRAME_ORIENTATION);
        float blur_illumination_score = 1.0f;
        // 计算正脸分数（通过三个欧拉角计算，计算方式根据需求调节）
        float front_score = 1.0f - (Math.abs(optimizedFace.pitch) + Math.abs(optimizedFace.pitch) + Math.abs(optimizedFace.pitch)) / 270.0f;

        // 计算总的质量分数（加权系数根据需求调节）
        float score = 0.3f * blur_illumination_score + 0.7f * front_score;
        return score;
    }

}
