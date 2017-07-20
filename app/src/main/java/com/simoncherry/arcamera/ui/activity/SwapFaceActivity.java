package com.simoncherry.arcamera.ui.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.sensetime.stmobileapi.STMobileFaceAction;
import com.sensetime.stmobileapi.STMobileMultiTrack106;
import com.sensetime.stmobileapi.STUtils;
import com.simoncherry.arcamera.R;
import com.simoncherry.arcamera.util.BitmapUtils;

public class SwapFaceActivity extends AppCompatActivity {
    private static final String TAG = SwapFaceActivity.class.getSimpleName();
    private static final int RESULT_LOAD_IMG = 123;
    private static final int RESULT_FOR_SWAP = 456;
    private static final int ST_MOBILE_TRACKING_ENABLE_FACE_ACTION = 0x00000020;

    private ImageView ivImg;

    private STMobileMultiTrack106 tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swap_face);

        tracker = new STMobileMultiTrack106(this, ST_MOBILE_TRACKING_ENABLE_FACE_ACTION);
        int max = 1;
        tracker.setMaxDetectableFaces(max);

        ivImg = (ImageView) findViewById(R.id.iv_img);
        Button btnLoad = (Button) findViewById(R.id.btn_load);
        Button btnDetect = (Button) findViewById(R.id.btn_detect);
        Button btnSwap = (Button) findViewById(R.id.btn_swap);

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImage();
            }
        });

        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectFace();
            }
        });

        btnSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void loadImage() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == RESULT_LOAD_IMG) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgPath = cursor.getString(columnIndex);
                if (imgPath != null) {
                    ivImg.setImageBitmap(BitmapUtils.decodeSampledBitmapFromFilePath(imgPath, ivImg.getWidth(), ivImg.getHeight()));
                }
            }
        }
    }

    private void detectFace() {
        Bitmap bitmap = BitmapUtils.getViewBitmap(ivImg);
        bitmap = BitmapUtils.getRequireWidthBitmap(bitmap, 240);
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Log.e(TAG, "bitmap width: " + width);
            Log.e(TAG, "bitmap height: " + height);

            byte[] bytes = getNV21(width, height, bitmap);
            Log.e(TAG, "bytes length: " + bytes.length);

            STMobileFaceAction[] faceActions = tracker.trackFaceAction(bytes, 0, width, height);
            if (faceActions != null && faceActions.length > 0) {
                Toast.makeText(this, "faceActions is good", Toast.LENGTH_SHORT).show();
                STMobileFaceAction faceAction = faceActions[0];
                PointF[] points = faceAction.getFace().getPointsArray();
                if (points != null && points.length > 0) {
                    Canvas canvas = new Canvas(bitmap);
                    STUtils.drawPoints(canvas, points, ivImg.getWidth(), ivImg.getHeight(), false);
                    ivImg.setImageBitmap(bitmap);
                } else {
                    Toast.makeText(this, "cannot get points", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "faceActions is null", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private byte [] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {
        int [] argb = new int[inputWidth * inputHeight];
        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);
        byte [] yuv = new byte[inputWidth*inputHeight*3/2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);
        //scaled.recycle();
        return yuv;
    }

    private void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
                U = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
                V = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte)((V<0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte)((U<0) ? 0 : ((U > 255) ? 255 : U));
                }

                index ++;
            }
        }
    }
}
