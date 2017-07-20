package com.simoncherry.arcamera.util;

import android.graphics.PointF;
import android.os.Environment;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * Created by Simon on 2017/7/20.
 */

public class LandmarkUtils {

    private final static String TAG = LandmarkUtils.class.getSimpleName();

    public static String getDir(String dir) {
        File sdcard = Environment.getExternalStorageDirectory();
        return sdcard.getAbsolutePath() + File.separator + dir + File.separator;
    }

    public static void writeLandmarkToDisk(PointF[] landmarks, String path, String name) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean isSuccess = dir.mkdirs();
            if (!isSuccess) {
                return;
            }
        }

        String jsonString = JSON.toJSONString(landmarks);
        Log.e(TAG, "landmarks: " + jsonString);

        String fileName = path + name + ".txt";
        try {
            int i = 0;
            FileWriter writer = new FileWriter(fileName);
            for (PointF point : landmarks) {
                int pointX = (int) point.x;
                int pointY = (int) point.y;
                String landmark = String.valueOf(pointX) + " " + String.valueOf(pointY) + "\n";
                Log.e(TAG, "write landmark[" + String.valueOf(i) + "]: " + landmark);
                i++;
                writer.write(landmark);
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    public static String getMD5(String message) {
        String md5str = "";
        try {
            //1 创建一个提供信息摘要算法的对象，初始化为md5算法对象
            MessageDigest md = MessageDigest.getInstance("MD5");

            //2 将消息变成byte数组
            byte[] input = message.getBytes();

            //3 计算后获得字节数组,这就是那128位了
            byte[] buff = md.digest(input);

            //4 把数组每一字节（一个字节占八位）换成16进制连成md5字符串
            md5str = bytesToHex(buff);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5str;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer md5str = new StringBuffer();
        //把数组每一字节换成16进制连成md5字符串
        int digital;
        for (int i = 0; i < bytes.length; i++) {
            digital = bytes[i];

            if(digital < 0) {
                digital += 256;
            }
            if(digital < 16){
                md5str.append("0");
            }
            md5str.append(Integer.toHexString(digital));
        }
        //return md5str.toString().toUpperCase();
        return md5str.toString().toLowerCase();
    }
}
