package com.simoncherry.arcamera.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

import com.sensetime.stmobileapi.STMobileFaceAction;
import com.sensetime.stmobileapi.STUtils;
import com.simoncherry.arcamera.contract.ARCamContract;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Simon on 2017/7/19.
 */

public class ARCamPresenter implements ARCamContract.Presenter {
    private final static String TAG = ARCamPresenter.class.getSimpleName();

    private Context mContext;
    private ARCamContract.View mView;

    public ARCamPresenter(ARCamContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void handlePhotoFrame(byte[] bytes, Bitmap mRajawaliBitmap, int photoWidth, int photoHeight) {
        // 将相机预览的帧数据转成Bitmap
        Bitmap bitmap = Bitmap.createBitmap(photoWidth, photoHeight, Bitmap.Config.ARGB_8888);
        ByteBuffer b = ByteBuffer.wrap(bytes);
        bitmap.copyPixelsFromBuffer(b);
        // 如果Rajawali渲染的3D模型截图不为空，就将两者合成
        if (mRajawaliBitmap != null) {
            Log.i(TAG, "mRajawaliBitmap != null");
            mRajawaliBitmap = Bitmap.createScaledBitmap(mRajawaliBitmap, photoWidth, photoHeight, false);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(mRajawaliBitmap, 0, 0, null);
            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
            mRajawaliBitmap.recycle();
            mRajawaliBitmap = null;
        } else {
            Log.i(TAG, "mRajawaliBitmap == null");
        }
        // 最后保存
        savePhoto(bitmap);
        bitmap.recycle();
        bitmap = null;
    }

    @Override
    public void savePhoto(Bitmap bitmap) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/OpenGLDemo/photo/";
        File folder = new File(path);
        if(!folder.exists() && !folder.mkdirs()){
            mView.onSavePhotoFailed();
            return;
        }
        long dataTake = System.currentTimeMillis();
        final String jpegName = path + dataTake + ".jpg";
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mView.onSavePhotoSuccess(jpegName);
    }

    // 处理3D模型的旋转
    @Override
    public void handle3dModelRotation(float pitch, float roll, float yaw) {
        mView.onGet3dModelRotation(-pitch, roll+90, -yaw);
    }

    // 处理3D模型的平移
    @Override
    public void handle3dModelTransition(STMobileFaceAction[] faceActions,
                                        int orientation, int eye_dist, float yaw,
                                        int previewWidth, int previewHeight) {
        boolean rotate270 = orientation == 270;
        STMobileFaceAction r = faceActions[0];
        Rect rect;
        if (rotate270) {
            rect = STUtils.RotateDeg270(r.getFace().getRect(), previewWidth, previewHeight);
        } else {
            rect = STUtils.RotateDeg90(r.getFace().getRect(), previewWidth, previewHeight);
        }

        float centerX = (rect.right + rect.left) / 2.0f;
        float centerY = (rect.bottom + rect.top) / 2.0f;
        float x = (centerX / previewHeight) * 2.0f - 1.0f;
        float y = (centerY / previewWidth) * 2.0f - 1.0f;
        float tmp = eye_dist * 0.000001f - 1115;  // 1115xxxxxx ~ 1140xxxxxx - > 0 ~ 25
        tmp = (float) (tmp / Math.cos(Math.PI*yaw/180));  // 根据旋转角度还原两眼距离
        tmp = tmp * 0.04f;  // 0 ~ 25 -> 0 ~ 1
        float z = tmp * 3.0f + 1.0f;
        Log.e(TAG, "transition: x= " + x + ", y= " + y + ", z= " + z);

        mView.onGet3dModelTransition(x, y, z);
    }
}
