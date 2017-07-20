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
import com.simoncherry.arcamera.util.LandmarkUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SwapFaceActivity extends AppCompatActivity {
    private static final String TAG = SwapFaceActivity.class.getSimpleName();
    private static final int RESULT_LOAD_IMG = 123;
    private static final int ST_MOBILE_TRACKING_ENABLE_FACE_ACTION = 0x00000020;

    private ImageView ivImg;

    private STMobileMultiTrack106 tracker;

    private String mCurrentImgPath = null;


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
                replaceTexture();
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
                mCurrentImgPath = cursor.getString(columnIndex);
                if (mCurrentImgPath != null) {
                    ivImg.setImageBitmap(BitmapUtils.decodeSampledBitmapFromFilePath(mCurrentImgPath, ivImg.getWidth(), ivImg.getHeight()));
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

    private void replaceTexture() {
        if (mCurrentImgPath == null) {
            Toast.makeText(this, "先点击Load加载第一张图片", Toast.LENGTH_SHORT).show();
        } else {
            String dirName = LandmarkUtils.getDir("/OpenGLDemo/txt/");
            String fileName = LandmarkUtils.getMD5(mCurrentImgPath);
            String filePath = dirName + fileName + ".txt";
            File file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(this, "关键点文本不存在", Toast.LENGTH_SHORT).show();
            } else {
//                Toast.makeText(this, "关键点文本存在", Toast.LENGTH_SHORT).show();
                List<String> coordinates = readCoordinatesFromTxt(filePath);
                if (coordinates != null && coordinates.size() >= 106) {
                    int size[] = BitmapUtils.getImageWidthHeight(mCurrentImgPath);
                    int imgWidth = size[0];
                    int imgHeight = size[1];

                    PointF[] tmp = new PointF[106];
                    PointF[] points = new PointF[44];
                    for (int i=0; i<106; i++) {
                        String coordinate = coordinates.get(i);
                        String[] point_str = coordinate.split(" ");
                        float x = Integer.parseInt(point_str[0]) / (float)imgWidth;
                        float y = 1.0f - Integer.parseInt(point_str[1]) / (float)imgHeight  + 0.03f;  // FIXME -- why + 0.03
                        tmp[i] = new PointF(x, y);
                    }

                    for (int i=0; i<44; i++) {
                        points[i] = getRemapPoint(tmp, i);
                    }

                    // =========
                    // 读取预设模型base_mask_obj
                    StringBuilder stringBuilder = new StringBuilder();
                    InputStream is = getResources().openRawResource(R.raw.base_face_uv3_obj);
                    try {
                        InputStreamReader reader = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(reader);
                        for(String str; (str = br.readLine()) != null; ) {  // 这里不能用while(br.readLine()) != null) 因为循环条件已经读了一条
                            stringBuilder.append(str).append("\n");
                        }
                        br.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String obj_str = stringBuilder.toString();
                    Log.e(TAG, "read base_mask_obj: " + obj_str);
                    // ========

                    DecimalFormat decimalFormat = new DecimalFormat(".0000");
                    // base_mask_obj第49至第92行定义UV坐标
                    String[] ss = obj_str.split("\n");
                    int i = 48;
                    for (PointF point : points) {
                        float x = point.x ;
                        float y = point.y;
                        String string = "vt " + decimalFormat.format(x) + " " + decimalFormat.format(y);
                        ss[i] = string;
                        i++;
                    }

                    String objPath = dirName + fileName + "_obj";
                    try {
                        FileWriter writer = new FileWriter(objPath);
                        for (String s : ss) {
                            writer.write(s + "\n");
                        }
                        writer.flush();
                        writer.close();
                        Toast.makeText(this, "生成新的OBJ", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }
                }
            }
        }
    }

    private static List<String> readCoordinatesFromTxt(String filePath) {
        final List<String> coordinate = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader br = new BufferedReader(fileReader);
            for(String str; (str = br.readLine()) != null; ) {  // 这里不能用while(br.readLine()) != null) 因为循环条件已经读了一条
                coordinate.add(str);
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return coordinate;
    }

    private PointF getRemapPoint(PointF[] tmp, int index) {
        index += 1;
        switch (index) {
            case 1:
                return tmp[0];
            case 2:
                return tmp[52];
            case 3:
                return tmp[34];
            case 4:
                return tmp[3];
            case 5:
                return tmp[8];
            case 6:
                return tmp[12];
            case 7:
                return tmp[84];
            case 8:
                return tmp[61];
            case 9:
                return tmp[90];
            case 10:
                return tmp[20];
            case 11:
                return tmp[24];
            case 12:
                return tmp[29];
            case 13:
                return tmp[32];
            case 14:
                return tmp[41];
            case 15:
                return tmp[39];
            case 16:
                return tmp[43];
            case 17:
                return tmp[58];
            case 18:
                return new PointF((tmp[36].x + tmp[39].x) * 0.5f, (tmp[36].y + tmp[39].y) * 0.5f);
            case 19:
                return tmp[36];
            case 20:
                return tmp[55];
            case 21:
                return tmp[82];
            case 22:
                return tmp[46];
            case 23:
                return tmp[83];
            case 24:
                return tmp[49];
            case 25:
                return tmp[53];
            case 26:
                return tmp[72];
            case 27:
                return tmp[54];
            case 28:
                return tmp[57];
            case 29:
                return tmp[73];
            case 30:
                return tmp[56];
            case 31:
                return tmp[59];
            case 32:
                return tmp[75];
            case 33:
                return tmp[60];
            case 34:
                return tmp[63];
            case 35:
                return tmp[76];
            case 36:
                return tmp[62];
            case 37:
                return tmp[97];
            case 38:
                return tmp[98];
            case 39:
                return tmp[99];
            case 40:
                return tmp[102];
            case 41:
                return tmp[103];
            case 42:
                return tmp[93];
            case 43:
                return tmp[101];
            case 44:
                return tmp[16];
            default:
                return tmp[0];
        }
    }
}
