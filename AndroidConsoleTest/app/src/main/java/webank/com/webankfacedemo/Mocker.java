package webank.com.webankfacedemo;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

// Mocker 数据类，模拟数据输入，请根据自己的业务需要调整
public class Mocker {
    private static final String TAG = "Mocker";
    private AssetManager mgr;

    private static String[] frames = {"1", "2", "3"};
//    private static String[] frames = {"1"};
    private static String FrameFolder = "frames/";
    private static String FrameColorSuffix = "_color.jpg";
    private static String Frame3DSuffix = "_depth.png";
    private static String FrameIRSuffix = "_ir.jpg";

    private static int maxFrames = frames.length;
    private static int frameIndex = 1;

    public static class Frame {
        String id;
        byte[] data;
        int height;
        int width;
    }

    public Mocker(AssetManager assetManager) {
        mgr = assetManager;
    }

    public static int byteArrayToInt(byte[] b)
    {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a)
    {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    public static byte[] bitmap2RGB(Bitmap bitmap) {
        int bytes = bitmap.getByteCount();

        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buffer);

        byte[] rgba = buffer.array();
        byte[] pixels = new byte[(rgba.length / 4) * 3];

        int count = rgba.length / 4;

        for (int i = 0; i < count; i++) {
            pixels[i * 3] = rgba[i * 4 ];
            pixels[i * 3 + 1] = rgba[i * 4 + 1];
            pixels[i * 3 + 2] = rgba[i * 4 + 2];
        }
        return pixels;
    }

    public static byte[] bitmap2Grey(Bitmap bitmap) {
        int bytes = bitmap.getByteCount();

        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buffer);

        byte[] rgba = buffer.array();
        byte[] pixels = new byte[(rgba.length / 4)];

        int count = rgba.length / 4;

        for (int i = 0; i < count; i++) {
            pixels[i] = rgba[i * 4];
        }
        return pixels;
    }

    public static class Face {
        String id;
        float[] feats;
    }

    // Mock: 模拟已经存在的人脸特征，Mock 数据位于 `assets/persons`
    public Face[] getFaces(WeBankSDKManager manager) {
        // debug
        // getFacesDat();

        String[] persons = {"3.png", "xiaoyou.jpg", "xiaotu.jpg"};
        Face[] faces = new Face[persons.length];
        try {
            for (int i = 0; i < persons.length; i++) {
                Bitmap bitmap = BitmapFactory.decodeStream(mgr.open("persons/" + persons[i]));

                byte[] rgbData = bitmap2RGB(bitmap);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();

                Face face = new Face();
                face.id = persons[i];
                face.feats = manager.extractFaceFeature(rgbData, width, height);
                faces[i] = face;
            }
        } catch (Exception e) {
            Log.e(TAG, "getAllFaceFeats: ", e);
        }
        return faces;
    }

    // Mock: 将图片转为二进制文件供调试用
    public int getFacesDat() {
        String assetsAbsPath = "/sdcard/Download/";
        String[] persons = {"persons/3.png", "persons/xiaoyou.jpg", "persons/xiaotu.jpg", "frames/1_color.jpg", "frames/1_ir.jpg",
                "frames/2_color.jpg", "frames/2_ir.jpg", "frames/3_color.jpg", "frames/3_ir.jpg"};
        try {
            for (int i = 0; i < persons.length; i++) {
                Bitmap bitmap = BitmapFactory.decodeStream(mgr.open(persons[i]));

                byte[] rgbData = bitmap2RGB(bitmap);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();

                FileOutputStream outputStream = new FileOutputStream(new File(assetsAbsPath + persons[i] + ".dat"));
                byte[] width_bytes = intToByteArray(width);
                byte[] height_bytes = intToByteArray(height);
                outputStream.write(width_bytes);
                outputStream.write(height_bytes);
                outputStream.write(rgbData);

                Log.d(TAG, "write image: " + assetsAbsPath + persons[i] + ".dat");

            }
        } catch (Exception e) {
            Log.e(TAG, "getAllFaceFeats: ", e);
        }
        return 0;
    }

    // Mock: 模拟 双目摄像头 取流操作，一定要注意摄像头的取流格式
    // 如果业务方是双目摄像头，一定要保证进行算法检测的两帧图片的时序一致
    public Frame[] getNextFrames() throws IOException {
        if (frameIndex > maxFrames) {
            frameIndex = 1;
        }

        String frameName = frames[frameIndex - 1];

        Frame frameColor = new Frame();
        frameColor.id = frameName + FrameColorSuffix;
        Bitmap bitmap = BitmapFactory.decodeStream(mgr.open(FrameFolder + frameColor.id));
        frameColor.data = bitmap2RGB(bitmap);
        frameColor.width = bitmap.getWidth();
        frameColor.height = bitmap.getHeight();

        // TODO: 此处深度图不能用 bitmap 构造，需要调整成 3D深度图输出
        Frame frame3D = new Frame();
        frame3D.id = frameName + Frame3DSuffix;
        Bitmap bitmap3d = BitmapFactory.decodeStream(mgr.open(FrameFolder + frame3D.id));
        frame3D.data = bitmap2RGB(bitmap3d);
        frame3D.width = bitmap3d.getWidth();
        frame3D.height = bitmap3d.getHeight();

        Frame frameIR = new Frame();
        frameIR.id = frameName + FrameIRSuffix;
        Bitmap bitmapIR = BitmapFactory.decodeStream(mgr.open(FrameFolder + frameIR.id));
        frameIR.data = bitmap2RGB(bitmapIR);
        frameIR.width = bitmapIR.getWidth();
        frameIR.height = bitmapIR.getHeight();

        Frame[] frames = {frameColor, frame3D, frameIR};

        frameIndex++;
        return frames;
    }

    public void onPreviewFrame(Runnable run) {
        while (true){
            new Thread(run).start();
            SystemClock.sleep(1500);
        }

    }
}