package com.webank.offline.livedetection.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.util.Log;

import com.webank.offline.livedetection.app.WeBankApp;
import com.webank.offline.livedetection.constant.Constant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @ProjectName: WeBankFaceAndroid_new
 * @Package: webank.com.webankfacedemo.util
 * @ClassName: PicUtils
 * @Description: java类作用描述
 * @Author: Andy
 * @Email: v_wbzyan@webank.com
 * @CreateDate: 2020/3/4 10:22
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/3/4 10:22
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class PicUtils {

    public static void savePic(final String fileName, final byte[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/" + WeBankApp.getInstance().getPackageName() + "/photos/");
                try {
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    File imgFile = new File(file.getPath(), fileName);
                    Log.e("filePath", imgFile.getPath());
                    if (!imgFile.exists()) {
                        imgFile.createNewFile();
                    }
                    yuv2File(data, imgFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("e", e.getMessage());
                }
            }
        }).start();
    }

    public static void yuv2File(byte[] yuv, File file) {
        if (yuv == null) return;
        YuvImage image = new YuvImage(yuv, ImageFormat.NV21, Constant.PREVIEW_SIZE_WIDTH, Constant.PREVIEW_SIZE_HEIGHT, null);
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, Constant.PREVIEW_SIZE_WIDTH, Constant.PREVIEW_SIZE_HEIGHT), 100, stream);
            Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            //TODO：此处可以对位图进行处理，如显示，保存等
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 复制单个文件
     *
     * @param oldPath$Name String 原文件路径+文件名 如：data/user/0/com.test/files/abc.txt
     * @param newPath$Name String 复制后路径+文件名 如：data/user/0/com.test/cache/abc.txt
     * @return <code>true</code> if and only if the file was copied;
     * <code>false</code> otherwise
     */
    public static boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists()) {
                Log.e("--Method--", "copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                Log.e("--Method--", "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                Log.e("--Method--", "copyFile:  oldFile cannot read.");
                return false;
            }

            /* 如果不需要打log，可以使用下面的语句
            if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                return false;
            }
            */

            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //文件写入
    public static void writeFileData(String filename, String content) {
        try {
            FileWriter writer = new FileWriter(new File(filename), true);
            writer.write(content);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}