package com.webank.offline.livedetection.face;

import android.text.TextUtils;
import android.util.Log;

import com.webank.offline.livedetection.constant.Constant;
import com.webank.offline.livedetection.ui.activity.LiveDetectionActivity;
import com.webank.offline.livedetection.util.DateUtils;
import com.webank.offline.livedetection.util.FloatQueue;
import com.webank.offline.livedetection.util.PicUtils;
import com.webank.offline.livedetection.weight.HandlerThreadUtil;

import java.util.concurrent.atomic.AtomicBoolean;

import webank.com.facetracker.FaceTracker;

/**
 * @ProjectName: WeBankFaceAndroid_new
 * @Package: webank.com.webankfacedemo.face
 * @ClassName: FaceRecognizedManager
 * @Description: java类作用描述
 * @Author: Andy
 * @Email: v_wbzyan@webank.com
 * @CreateDate: 2020/3/17 9:54
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/3/17 9:54
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class FaceLiveActionManager {

    private final String TAG = FaceLiveActionManager.class.getSimpleName();

    private int traceId = -1; // 追踪人脸的id

    private boolean isPause = false;  // 是否暂停检测
    private boolean isAction = false; // 动作检测是否结束
    private boolean isSave = true;           // 是否保存rgb数据
    private AtomicBoolean isFeeding = new AtomicBoolean(false);
    private FloatQueue floatQueue;

    private float maxScore = 0;
    private byte[] rgbDate;

    private static FaceLiveActionManager manager;

    private HandlerThreadUtil trackerThread;

    public static FaceLiveActionManager getInstance() {
        if (manager == null)
            manager = new FaceLiveActionManager();
        return manager;
    }

    public FaceLiveActionManager() {
        trackerThread = new HandlerThreadUtil("we-live-action");
        floatQueue = new FloatQueue(3);
    }

    int count = 2;
    int i = 0;

    private void faceTracker(final byte[] rbg) {
        trackerThread.getHandler().post(new Runnable() {
            @Override
            public void run() {

                // 如果当前正在人脸检测，则不处理当前传过来的帧数据
                if (!isFeeding.compareAndSet(false, true)) {
                    return;
                }

                // 1.检测人脸
                FaceTracker.TrackedFace[] trackedFaces = FaceUtils.trackedFaces(rbg, Constant.PREVIEW_SIZE_WIDTH, Constant.PREVIEW_SIZE_HEIGHT);
                if (trackedFaces == null || trackedFaces.length == 0) { // 人脸检测，没有检测到人脸的情况
                    actionCallback.onShowResult(false, "未识别到人脸");
                    isFeeding.set(false);
                    return;
                } else if (trackedFaces.length > 1) {  // 人脸检测，检测到多人脸的情况
                    actionCallback.onShowResult(false, "识别到多张人脸");
                    isFeeding.set(false);
                    return;
                }

                FaceTracker.TrackedFace targetFace = trackedFaces[0];  // 获取当前人脸数据
//                actionCallback.onFaceTrack(targetFace.bbox, floatQueue.average(targetFace.xyAllPoints));
//                actionCallback.onZitaiJiao(targetFace.pitch, targetFace.yaw, targetFace.roll);

                // 2.过滤人脸(判断人脸是否完整，过大，过小)
                String faceFilter = FaceUtils.filterFaces(targetFace, Constant.PREVIEW_SIZE_HEIGHT, Constant.PREVIEW_SIZE_WIDTH);
                if (!TextUtils.isEmpty(faceFilter)) {
                    actionCallback.onShowResult(false, faceFilter);
                    isFeeding.set(false);
                    return;
                }

                // 3.选择人脸
                float score = FaceUtils.qualityFaceNum(targetFace, rbg, Constant.PREVIEW_SIZE_WIDTH, Constant.PREVIEW_SIZE_HEIGHT);
                if (score > maxScore) { // 得分大于最高得分，获得rgb数据
                    rgbDate = rbg;
                }

                // 4.是否需要重置动作(上帧faceId与当前帧的人脸)
                if (traceId != targetFace.traceId) {
                    int result = FaceUtils.actionReset(LiveDetectionActivity.action);
                    if (result != 0) {
                        Log.e(TAG, "reset action failed, check the action string.");
                        actionCallback.onShowResult(false, "动作重置失败");
                        isFeeding.set(false);
                        return;
                    }
                    traceId = targetFace.traceId;
                }

                String msg;
                switch (LiveDetectionActivity.action) {
                    case "blink":
                        msg = "眨眨眼";
                        break;

                    case "mouth":
                        msg = "张张嘴";
                        break;

                    case "shake":
                        msg = "摇摇头";
                        break;

                    case "node":
                        msg = "点点头";
                        break;

                    default:
                        msg = "错误的动作类型";
                        break;
                }
                actionCallback.onShowAction(msg);

                if (!isAction) {
                    isFeeding.set(false);
                    return;
                }

                // 5.动作检测
                int result;
                switch (LiveDetectionActivity.action) {
                    case "blink":
                        result = FaceUtils.blinkAction(targetFace.bbox, targetFace.xyAllPoints);
                        break;

                    case "mouth":
                        result = FaceUtils.mouthAction(targetFace.bbox, targetFace.xyAllPoints);
                        break;

                    case "shake":
                        result = FaceUtils.shakeAction(targetFace.yaw);
                        break;

                    case "node":
                        result = FaceUtils.nodeAction(targetFace.pitch);
                        break;

                    default:
                        result = -1;
                        break;
                }

                if (result != 0) {
                    isFeeding.set(false);
                    return;
                }

                i++;
                if (i < count) {
                    isFeeding.set(false);
                } else { // 动作两次检测成功，即通过验证
                    // 检测成功，则可以保存rgb数据
                    if (isSave) {
                        String createTime = DateUtils.times2Long(System.currentTimeMillis() + "");
                        String irFileName = "color_" + createTime + ".jpg";  //jpeg文件名定义
                        Log.e("rgbDate", rgbDate + "");
                        PicUtils.savePic(irFileName, rgbDate);
                    }
                    i = 0;
                    actionCallback.onShowResult(true, "通过验证");
                    setAction(false);
                }

            }
        });
    }

    public void start(byte[] rgb) {
        if (!isPause) {
            faceTracker(rgb);
        }
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public void setAction(boolean action) {
        isAction = action;
    }

    public void stop() {
        setPause(true);
        if (trackerThread != null) {
            trackerThread.stopThread();
            trackerThread = null;
        }
        if (manager != null) {
            manager = null;
        }
    }

    private LiveActionCallback actionCallback;

    public interface LiveActionCallback {

        void onShowAction(String title);

        void onShowResult(boolean success, String result);
    }

    public void setActionCallback(LiveActionCallback actionCallback) {
        this.actionCallback = actionCallback;
    }

}
