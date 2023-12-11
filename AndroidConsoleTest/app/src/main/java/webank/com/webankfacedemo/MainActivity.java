package webank.com.webankfacedemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

//import webank.com.common.util.CommonInterface;
import webank.com.faceattribute.FaceAttribute;
import webank.com.facetracker.FaceTracker;
import webank.com.faceretrieve.FaceRetrieve;
import webank.com.facequality.FaceQuality;
import webank.com.faceliveir.FaceLiveIR;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "webank-MainActivity";
    private static long global_start = System.currentTimeMillis();
    Handler handler;

    Mocker mocker;
    WeBankSDKManager manager;

    static {
        // 1. 加载依赖库
        WeBankSDKManager.loadLibs();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView tv = (TextView) findViewById(R.id.textview);
        handler = new Handler() {
            public void handleMessage(Message msg) {
                tv.setText((String) msg.obj);
            }
        };

        mocker = new Mocker(this.getAssets());

//        // 2. 鉴权：输入 `assets` 目录下鉴权文件的相对地址，例如 `dev_com.lic`
//        // 请输入鉴权文件的 `*.lic` 所在 assets 下的相对路径
//        int result = CommonInterface.initAuth(this, "test.lic", 0);
//        if (result != 0) {
//            Log.d(TAG, "auth failed with error code: " + result + "    reason:" + CommonInterface.getFailedReason());
//            return;
//        }

//        // 2. [可选] 鉴权：输入鉴权文件绝对路径
//        String license_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WeBankFaceSDK/test.lic";
//
//        if (ActivityCompat.checkSelfPermission(this,"android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
//            // 没有读的权限，去申请读的权限，会弹出对话框
//            ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_EXTERNAL_STORAGE"},1);
//        }
//        int result = CommonInterface.initAuth(this, license_path, 2);
//        if (result != 0) {
//            Log.d(TAG, "auth failed with error code: " + result + "    reason:" + CommonInterface.getFailedReason());
//            return;
//        }

        // 3. 加载所有需要的模型，并 log 版本信息
        // 模型全局加载必须先于 SDK 实例化
        WeBankSDKManager.loadModels(this.getAssets());

        // 4. 实例化 SDK
        manager = new WeBankSDKManager(this.getAssets());

        // 5. TODO: [可选][离线人脸识别比对] 创建人脸检索库zqw
        createFaceLib();

        // 6. 模拟取流，并检测，具体的取流业务代码实现，请联系摄像头厂商
        // 这里只是模拟视频取流，请业务方自行实行 双目/单目摄像头 的取流逻辑
        // Mock 模拟数据，读取 assets 下的文件进行模拟
        mocker = new Mocker(this.getAssets());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        new Thread() {
            @Override
            public void run() {
                super.run();
                mocker.onPreviewFrame(feedFrame);
            }
        }.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 注意：一定要先 destroy 实例，再全局释放 models
        manager.destroy();
        WeBankSDKManager.releaseModels();
    }

    private AtomicBoolean isFeeding = new AtomicBoolean(false);
    private Runnable feedFrame = new Runnable() {
        public void run() {
            if (!isFeeding.compareAndSet(false, true)) {
                Log.d(TAG, "feedFrame: feeding frame now, skip current frame");
                return;
            }

            try {
                // 6.0 取流，用户自行实现摄像头取流，此处使用 mock 数据
                // 注意，所有 SDK 关于图片的 data，一定不能是 null
                Mocker.Frame[] frames = mocker.getNextFrames();
                // 普通摄像头 mock 数据
                Mocker.Frame frameColor = frames[0];
//                // 3D 活体摄像头 mock 数据，此处的图片数据为 depth 16bits 图片，只能用于 3D 活体
//                Mocker.Frame frameLive3D = frames[1];
                // 红外活体摄像头 mock 数据，此处的图片数据为 rgb 数据格式，用于 红外 活体
                Mocker.Frame frameLiveIR = frames[2];

                long start = System.currentTimeMillis();

                FaceTracker.Image image = new FaceTracker.Image();
                image.rgbData = new byte[3 * frameColor.width * frameColor.height];

//                FaceTracker.Options options = manager.mFaceTracker.getOptions();
//                if (options.needDenseKeyPoints) {
//                    options.needDenseKeyPoints = false;
//                    options.needPoseEstimate = false;
//                } else {
//                    options.needDenseKeyPoints = true;
//                    options.needPoseEstimate = true;
//                }
//
//                manager.mFaceTracker.setOptions(options);


                // 6.1 将每一帧在 FaceTracker 中进行快速跟踪配准获得五点
//                 FaceTracker.TrackedFace[] trackedFaces = manager.mFaceTracker.trackRGB(frameColor.data, frameColor.width, frameColor.height, WeBankSDKManager.FRAME_ORIENTATION, image);

                FaceTracker.TrackedFace[] trackedFaces = manager.mFaceTracker.detectRGB(frameColor.data, frameColor.width, frameColor.height, WeBankSDKManager.FRAME_ORIENTATION, null, null);
                Log.d(TAG, "feedFrame [" + frameColor.id + "]: tracked face count: " + trackedFaces.length);

                // 测试查询异常
                manager.mFaceRetrieve.queryFeature("fefefefe", "2");

                // 要对追踪的结果进行 null 判断
                if (trackedFaces == null || trackedFaces.length == 0) {
                    isFeeding.set(false);
                    return;
                }

                // 6.2 对于 FaceTracker 追踪的结果，一张图片上，可能存在多个人脸，业务方应该根据自己的规则，过滤出最合适的人脸
                // 6.2.1 过滤不符合要求的人脸，如过滤掉人脸大小过小的人脸或者角度姿态过小的人脸
                FaceTracker.TrackedFace[] filteredFaces = filterFaces(trackedFaces, frameColor.width, frameColor.height);
                if (filteredFaces == null) {
                    isFeeding.set(false);
                    return;
                }

                // 6.2.2 优选最符合要求的人脸，如最中心的人脸，最大的人脸等等
                FaceTracker.TrackedFace optimizedFace = optimizeFaces(filteredFaces);
                if (optimizedFace == null) {
                    isFeeding.set(false);
                    return;
                }

//                int[] bbox = {541, 254, 788, 573};
//                float[] xy5points = {611.2741f, 380.78494f, 710.1086f, 372.56836f, 660.8356f, 439.20984f, 623.77637f, 501.56387f, 705.7237f, 494.5121f};
//                FaceTracker.TrackedFace optimizedFace = new FaceTracker.TrackedFace();
//                optimizedFace.faceHeight = 320;
//                optimizedFace.faceWidth = 248;
//                optimizedFace.bbox = new int[4];
//                optimizedFace.xy5Points = new float[10];
//                System.arraycopy(bbox, 0, optimizedFace.bbox, 0, 4);
//                System.arraycopy(xy5points, 0, optimizedFace.xy5Points, 0, 10);


                // 6.3 TODO: [可选] 质量分判断，过滤掉不合格的人脸质量
                FaceQuality.OutQuality quality = manager.mFaceQuality.evaluateRGB(optimizedFace.bbox, frameColor.data, frameColor.width, frameColor.height, WeBankSDKManager.FRAME_ORIENTATION);
                Log.d(TAG, "feedFrame [" + frameColor.id + "]: FaceQuality.Score quality=" + quality.blur);
                if (quality.blur < WeBankSDKManager.FACE_QUALITY_THRESHOLD) {
                    Log.e(TAG, "feedFrame [" + frameColor.id + "]: FaceQuality.evaluate less than threshold: " + quality);
                    isFeeding.set(false);
                    return;
                }

                // 6.4 TODO: [可选] 属性判断，判断是性别、年龄、是否戴口罩
                FaceAttribute.OutAttribute outAttribute = manager.mFaceAttribute.extractRGB(optimizedFace.xy5Points, frameColor.data, frameColor.width, frameColor.height, WeBankSDKManager.FRAME_ORIENTATION);
                Log.d(TAG, "feedFrame [" + frameColor.id + "]: FaceAttribute.male probability =" + outAttribute.genderMaleProbability + ", mask probability =" + outAttribute.wearMaskProbability + ", age=" + outAttribute.age + ", wear hat probability =" + outAttribute.wearHatProbability  + ", wear glasses probability =" + outAttribute.wearGlassesProbability);

                // 6.5 TODO: [可选] 活体判断 [彩色图活体、3D活体、红外活体] 根据业务方设备的摄像头种类进行选择
                // 6.5.1 彩色图活体，用当前彩色帧进行活体判断
                float liveColorScore = manager.mFaceLiveColor.detectRGB(optimizedFace.bbox, frameColor.data, frameColor.width, frameColor.height, WeBankSDKManager.FRAME_ORIENTATION);
                Log.d(TAG, "feedFrame [" + frameColor.id + "]: FaceLiveColor score=" + liveColorScore);
                if (liveColorScore < WeBankSDKManager.FACE_LIVE_COLOR_THRESHOLD) {
                    Log.d(TAG, "feedFrame [" + frameColor.id + "]: FaceLiveColor not live, score=" + liveColorScore);
                    isFeeding.set(false);
                    return;
                }

                // 6.5.2 TODO: 红外活体
                // 由于红外活体使用的是另外一个摄像头，因此业务方需要自行保证 **多目摄像头的帧同步**
                // 保证进行活体检测的 frame 和人脸识别的 frame 的时序要一致
                FaceLiveIR.Image ir_image = new FaceLiveIR.Image();
                ir_image.rgbData = new byte[3 * frameLiveIR.width * frameLiveIR.height];

                float liveIRScore = manager.mFaceLiveIR.detectRGB(
                        optimizedFace.xy5Points, frameColor.data, frameColor.width, frameColor.height, WeBankSDKManager.FRAME_ORIENTATION,
                        optimizedFace.xy5Points, frameLiveIR.data, frameLiveIR.width, frameLiveIR.height, WeBankSDKManager.FRAME_ORIENTATION, ir_image);
                Log.d(TAG, "feedFrame [" + frameLiveIR.id + "]: FaceLiveIR score=" + liveIRScore);
                if (liveIRScore < WeBankSDKManager.FACE_LIVE_IR_THRESHOLD) {
                    Log.d(TAG, "feedFrame [" + frameLiveIR.id + "] FaceLiveIR not live");
                    isFeeding.set(false);
                    return;
                }

//                // 6.5.3 TODO: 3D活体
//                // 由于3D活体使用的是另外一个摄像头，因此业务方需要自行保证 **多目摄像头的帧同步**
//                // 保证进行活体检测的 frame 和人脸识别的 frame 的时序要一致
//                float live3DScore = manager.mFaceLive3D.detect(optimizedFace.xy5Points, frameLive3D.data, frameLive3D.width, frameLive3D.height);
//                Log.d(TAG, "feedFrame [" + frameLive3D.id + "]: FaceLive3D score=" + live3DScore);
//                if (live3DScore < WeBankSDKManager.FACE_LIVE_3D_THRESHOLD) {
//                    Log.d(TAG, "feedFrame [" + frameLiveIR.id + "] FaceLive3D not live");
//                    isFeeding.set(false);
//                    return;
//                }

                // 6.6 TODO: [可选] 业务方实现，如果不需要离线人脸识别，则可以将优选出的人脸裁剪并保存发送至后台
                // saveOptimizedFace(frameColor.data, frameColor.width, frameColor.height, optimizedFace);

                // 6.7 TODO: [可选][离线人脸识别比对] 人脸检索，在指定检索库中检索最终优选出的人脸是否在库
                FaceRetrieve.RetrievedFace[] faces = retrieveFaceLib(optimizedFace, frameColor.data, frameColor.width, frameColor.height);
                // 要对检索的结果进行 null 判断
                if (faces == null || faces.length == 0) {
                    Log.d(TAG, "feedFrame [" + frameColor.id + "]: no retrieved faces");
                    isFeeding.set(false);
                    return;
                }

                long end = System.currentTimeMillis();
                Log.i(TAG, "feedFrame [" + frameColor.id + "]: cost: " + (end - start));

                // 6.8 TODO: [可选] 业务方实现，处理最终检索出来的人脸
                handleFaces(optimizedFace, faces);

                isFeeding.set(false);

                long global_end = System.currentTimeMillis();
                long duration = (global_end - global_start);
                long sec = duration / 1000L % 60L;
                long min = duration / 60000L % 60L;
                long hour = duration / 3600000L;
                String hms = "已运行时间: " + hour + "时" + min + "分" + sec + "秒";
                handler.sendMessage(handler.obtainMessage(100, hms));

            } catch (Exception e) {
                isFeeding.set(false);
                Log.e(TAG, "run: mock error", e);
            }

        }
    };


    // TODO: 处理最终检索匹配到的人脸信息
    private void handleFaces(FaceTracker.TrackedFace optimizedFace, FaceRetrieve.RetrievedFace[] faces) {
        Log.d(TAG, "handleFaces: count " + faces.length);
        for (FaceRetrieve.RetrievedFace face : faces) {
            Log.d(TAG, "handleFaces: [id=" + face.featId + ",score=" + face.score + "]");
        }
        Log.d(TAG, "handleFaces: need dense keypoints: " + manager.mFaceTracker.getOptions().needDenseKeyPoints + ", need pose: " + manager.mFaceTracker.getOptions().needPoseEstimate);
        Log.d(TAG, "handleFaces: pitch, yaw, roll: " + optimizedFace.pitch + ", " + optimizedFace.yaw + ", " + optimizedFace.roll);
        Log.d(TAG, "handleFaces: optimizedFace.bbox=" + Arrays.toString(optimizedFace.bbox));
        Log.d(TAG, "handleFaces: optimizedFace.xy5Points=" + Arrays.toString(optimizedFace.xy5Points)) ;
    }

    // TODO: 业务方可以根据人脸的角度，人脸位置过滤掉不符合要求的人脸
    private FaceTracker.TrackedFace[] filterFaces(FaceTracker.TrackedFace[] trackedFaces, int frameWidth, int frameHeight) {
        ArrayList<FaceTracker.TrackedFace> result = new ArrayList<>();

        for (FaceTracker.TrackedFace face : trackedFaces) {
            // 判断人脸的立体角度，给出合适的姿态提示
            if (Math.abs(face.pitch) > 40 || Math.abs(face.yaw) > 40 || Math.abs(face.roll) > 40) {
                Log.d(TAG, "姿态不符合要求");
                continue;
            }
            // 去除掉人脸过小的脸
            if (face.faceWidth < 40 || face.faceHeight < 40) {
                Log.d(TAG, "人脸太小");
                continue;
            }
            // 去除人脸框有一部分不在摄像头内的人脸
            if (face.bbox[0] <= 0 || face.bbox[1] <= 0
                    || face.bbox[0] + face.faceWidth >= frameWidth
                    || face.bbox[1] + face.faceHeight >= frameHeight) {
                Log.d(TAG, "该人脸出框，不完整，不做活体判断");
                continue;
            }
            result.add(face);
        }
        return result.toArray(new FaceTracker.TrackedFace[result.size()]);
    }

    // TODO: 根据业务方自己的规则，自行优选出最佳的人脸
    // 如人脸大小，人脸数量，人脸位置等规则
    private FaceTracker.TrackedFace optimizeFaces(FaceTracker.TrackedFace[] trackedFaces) {
        if (trackedFaces == null) {
            return null;
        }
        // TODO: 此处为找出面积最大的脸
        FaceTracker.TrackedFace bigger = null;
        int maxArea = -1;
        for (FaceTracker.TrackedFace face : trackedFaces) {
            int area = face.faceHeight * face.faceWidth;
            if (area > maxArea) {
                maxArea = area;
                bigger = face;
            }
        }
        return bigger;
    }

    private FaceRetrieve.RetrievedFace[] retrieveFaceLib(FaceTracker.TrackedFace optimizedFace, byte[] rgbData, int width, int height) {
        float[] feat = new float[WeBankSDKManager.FACE_FEAT_LENGTH];

        int ret = manager.mFaceFeature.extractRGB(optimizedFace.xy5Points, rgbData, width, height, WeBankSDKManager.FRAME_ORIENTATION, feat);

        if (ret != 0) {
            Log.d(TAG, "feedFrame: extract error: " + ret);
            isFeeding.set(false);
            return null;
        }

        // 根据自己业务的检索条件设置
        String[] libs = {WeBankSDKManager.FACE_RETRIEVE_LIB_ID};
        int topN = 1;

        long retrieve_start = System.currentTimeMillis();
        FaceRetrieve.RetrievedFace[] result = manager.mFaceRetrieve.retrieve(libs, topN, feat, WeBankSDKManager.FACE_RETRIEVE_THRESHOLD);
        long retrieve_time = System.currentTimeMillis() - retrieve_start;
        Log.d(TAG, "retrieve using time: " + retrieve_time + "ms. lib size:" + manager.mFaceRetrieve.getFeatureCount(WeBankSDKManager.FACE_RETRIEVE_LIB_ID));

        return result;
    }

    private void createFaceLib() {
        // TODO: 获得已经提取好特征的人脸特征和对应 ID，此处为 mock 数据，模拟的已经注册的人脸
        Mocker.Face[] faces = mocker.getFaces(manager);

        float[] feats;
        String[] featIds;

        if (true) {
            // feats 和 featIds 的详细关系请参考 docs/微众银行人脸检索.html 文档
            feats = new float[faces.length * WeBankSDKManager.FACE_FEAT_LENGTH];
            featIds = new String[faces.length];
            for (int i = 0; i < faces.length; i++) {
                featIds[i] = faces[i].id;
                System.arraycopy(faces[i].feats, 0, feats, i * WeBankSDKManager.FACE_FEAT_LENGTH, WeBankSDKManager.FACE_FEAT_LENGTH);
            }
        } else {
            int test_size = 20000;

            // feats 和 featIds 的详细关系请参考 docs/微众银行人脸检索.html 文档
            feats = new float[(test_size + faces.length) * WeBankSDKManager.FACE_FEAT_LENGTH];
            featIds = new String[(test_size + faces.length)];
            for (int i = 0; i < faces.length; i++) {
                featIds[i] = faces[i].id;
                System.arraycopy(faces[i].feats, 0, feats, i * WeBankSDKManager.FACE_FEAT_LENGTH, WeBankSDKManager.FACE_FEAT_LENGTH);
            }

            for (int i=faces.length;i < faces.length + test_size;i ++) {
                featIds[i] = faces[0].id + "_" + i;
                System.arraycopy(faces[0].feats, 0, feats, i * WeBankSDKManager.FACE_FEAT_LENGTH, WeBankSDKManager.FACE_FEAT_LENGTH);
            }
        }

        boolean ok = manager.mFaceRetrieve.createLib(feats, featIds, WeBankSDKManager.FACE_RETRIEVE_LIB_ID);
        if (!ok) {
            // TODO: 处理创建检索库的异常
            Log.e(TAG, "createFaceLib: false");
        }

        Log.d(TAG, "createFaceLib: face count: " + manager.mFaceRetrieve.getFeatureCount(WeBankSDKManager.FACE_RETRIEVE_LIB_ID));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
