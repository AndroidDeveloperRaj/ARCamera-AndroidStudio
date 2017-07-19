package com.simoncherry.arcamera.contract;

import android.graphics.Bitmap;

import com.sensetime.stmobileapi.STMobileFaceAction;

/**
 * Created by Simon on 2017/7/19.
 */

public interface ARCamContract {
    interface View {
        void onSavePhotoSuccess(String fileName);
        void onSavePhotoFailed();
        void onGet3dModelRotation(float pitch, float roll, float yaw);
        void onGet3dModelTransition(float x, float y, float z);
    }

    interface Presenter {
        void handlePhotoFrame(byte[] bytes, Bitmap mRajawaliBitmap, int photoWidth, int photoHeight);
        void savePhoto(Bitmap bitmap);
        void handle3dModelRotation(float pitch, float roll, float yaw);
        void handle3dModelTransition(STMobileFaceAction[] faceActions,
                                     int orientation, int eye_dist, float yaw,
                                     int previewWidth, int previewHeight);
    }
}
