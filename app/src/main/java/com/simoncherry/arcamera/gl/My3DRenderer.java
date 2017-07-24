package com.simoncherry.arcamera.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

import com.simoncherry.arcamera.model.DynamicPoint;
import com.simoncherry.arcamera.model.Ornament;
import com.simoncherry.arcamera.util.BitmapUtils;
import com.simoncherry.arcamera.util.OrnamentFactory;

import org.rajawali3d.Geometry3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Simon on 2017/7/19.
 */

public class My3DRenderer extends Renderer {
    private final static String TAG = My3DRenderer.class.getSimpleName();

    private Object3D mContainer;
    private Object3D mOrnament;
    private Geometry3D mGeometry3D;

    private Ornament mOrnamentModel;
    private boolean mIsNeedUpdateOrnament = false;
    private boolean mIsOrnamentVisible = true;

    private boolean mIsFaceMask = false;
    // 用于静态3D模型
    private Vector3 mAccValues;
    private float mTransX = 0.0f;
    private float mTransY = 0.0f;
    private float mScale = 1.0f;
    // 用于动态3D模型
    private List<DynamicPoint> mPoints = new ArrayList<>();
    private boolean mIsChanging = false;

    // 根据肤色更改模型贴图的颜色
    private int mSkinColor = 0xffd4c9b5;

    public void setSkinColor(int mSkinColor) {
        this.mSkinColor = mSkinColor;
    }


    public My3DRenderer(Context context) {
        super(context);
        mAccValues = new Vector3();
    }

    public void setOrnamentModel(Ornament mOrnamentModel) {
        this.mOrnamentModel = mOrnamentModel;
    }

    public void setIsNeedUpdateOrnament(boolean mIsNeedUpdateOrnament) {
        this.mIsNeedUpdateOrnament = mIsNeedUpdateOrnament;
    }

    // 设置装饰品可见性
    public void setIsOrnamentVisible(boolean mIsOrnamentVisible) {
        this.mIsOrnamentVisible = mIsOrnamentVisible;
    }

    // 设置3D模型的转动角度
    public void setAccelerometerValues(float x, float y, float z) {
        mAccValues.setAll(x, y, z);
    }

    // 设置3D模型的平移
    public void setTransition(float x, float y, float z) {
        if (!mIsFaceMask) {
            mTransX = x;
            mTransY = y;
            setScale(z);
        }
    }

    // 设置3D模型的缩放比例
    public void setScale(float scale) {
        mScale = scale;
    }

    @Override
    protected void initScene() {
        try {
            mContainer = new Object3D();
            getCurrentScene().addChild(mContainer);
            getCurrentScene().getCamera().setZ(5.5);

        } catch (Exception e) {
            e.printStackTrace();
        }

        getCurrentScene().setBackgroundColor(0);
    }

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        super.onRender(ellapsedRealtime, deltaTime);

        if (mIsNeedUpdateOrnament) {
            mIsNeedUpdateOrnament = false;
            loadOrnament();
        }

        if (!mIsFaceMask) {
            Log.i(TAG, "静态模型");
            // 处理3D模型的旋转
            mContainer.setRotation(mAccValues.x, mAccValues.y, mAccValues.z);
            // 处理3D模型的缩放
            mContainer.setScale(mScale);
            // 处理3D模型的平移
            getCurrentCamera().setX(mTransX);
            getCurrentCamera().setY(mTransY);
        } else {
            Log.i(TAG, "动态模型");
            if (!mIsChanging && mPoints != null && mPoints.size() > 0) {
                mIsChanging = true;

                try {  // FIXME
                    FloatBuffer vertBuffer = mGeometry3D.getVertices();
                    for (int i=0; i<mPoints.size(); i++) {
                        DynamicPoint point = mPoints.get(i);
                        Log.i(TAG, "No." + i + ": " + point.toString());
                        changePoint(vertBuffer, point.getIndex(), point.getX(), point.getY(), point.getZ());
                    }
                    mGeometry3D.changeBufferData(mGeometry3D.getVertexBufferInfo(), vertBuffer, 0, vertBuffer.limit());

                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }

                mIsChanging = false;
            }
        }
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
    }

    private void loadOrnament() {
        try {
            if (mOrnament != null) {
                mIsOrnamentVisible = mOrnament.isVisible();
                mContainer.removeChild(mOrnament);
            }

            if (mOrnamentModel != null) {
                mIsFaceMask = mOrnamentModel.isFaceMask();
                int textureResId = mOrnamentModel.getTextureResId();
                String texturePath = mOrnamentModel.getTexturePath();

                if (texturePath != null) {
                    String objDir = "OpenGLDemo/txt/";
                    String objName = "base_face_uv3_obj";
                    LoaderOBJ parser = new LoaderOBJ(this, objDir + objName);
                    parser.parse();
                    mOrnament = parser.getParsedObject();
                    ATexture texture = mOrnament.getMaterial().getTextureList().get(0);
                    mOrnament.getMaterial().removeTexture(texture);

                    Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromFilePath(texturePath, 300, 300);
                    // 调整肤色
                    bitmap = changeSkinColor(bitmap, mSkinColor);
                    mOrnament.getMaterial().addTexture(new Texture("canvas", bitmap));
                    mOrnament.getMaterial().enableLighting(false);

                } else {
                    LoaderOBJ objParser1 = new LoaderOBJ(mContext.getResources(), mTextureManager, mOrnamentModel.getModelResId());
                    objParser1.parse();
                    mOrnament = objParser1.getParsedObject();

                    if (mIsFaceMask) {
                        if (textureResId <= 0) {  // 如果没有有效的贴图资源Id
                            mIsFaceMask = false;
                        } else {
                            ATexture texture = mOrnament.getMaterial().getTextureList().get(0);
                            mOrnament.getMaterial().removeTexture(texture);

                            Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), textureResId);
                            if (bitmap == null) {  // 如果无法生成的贴图Bitmap
                                mIsFaceMask = false;
                            } else {
                                mIsChanging = true;
                                // 调整肤色
                                bitmap = changeSkinColor(bitmap, mSkinColor);
                                mOrnament.getMaterial().addTexture(new Texture("canvas", bitmap));
                                mIsChanging = false;
                            }
                        }
                    }
                }

                mOrnament.setScale(mOrnamentModel.getScale());
                mOrnament.setPosition(mOrnamentModel.getOffsetX(), mOrnamentModel.getOffsetY(), mOrnamentModel.getOffsetZ());
                mOrnament.setRotation(mOrnamentModel.getRotateX(), mOrnamentModel.getRotateY(), mOrnamentModel.getRotateZ());
                int color = mOrnamentModel.getColor();
                if (color != OrnamentFactory.NO_COLOR) {
                    mOrnament.getMaterial().setColor(color);
                }
                mGeometry3D = mOrnament.getGeometry();

                mContainer.setScale(1.0f);
                mContainer.setRotation(0, 0, 0);
                mContainer.setPosition(0, 0, 0);
                getCurrentCamera().setX(0);
                getCurrentCamera().setY(0);

                mOrnament.setVisible(mIsOrnamentVisible);
                mContainer.addChild(mOrnament);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int faceIndices[][]={
            {66, 68, 123, 125, 128, 132, 135, 137},
            {57, 59, 63, 64, 110, 114, 116, 120, 124},
            {51, 53, 67, 71, 86, 90, 92, 96, 121},
            {54, 56, 65, 69},
            {35, 39, 41, 45, 49, 52, 55, 58},
            {15, 70, 122, 129, 146, 152, 158},
            {17, 61, 126, 136, 150, 156, 162},
            {139, 144},
            {141, 142, 147, 149, 151, 154},
            {153, 155, 157, 160},
            {33, 34, 37, 99, 101, 105, 107},
            {100, 103},
            {36, 60, 97, 109},
            {112, 115},
            {16, 21, 24, 27, 30, 31, 62, 106, 118},
            {40, 43, 47, 75, 77, 81, 84},
            {76, 79},
            {44, 50, 83, 94},
            {88, 91},
            {2, 6, 9, 12, 13, 46, 72, 73, 85},
            {38, 42},
            {1, 4},
            {5, 7},
            {8, 10},
            {11, 14, 159},
            {18, 19, 161},
            {20, 22},
            {23, 25},
            {26, 28},
            {3, 48},
            {29, 32},
            {80, 82},
            {74, 78},
            {93, 95},
            {87, 89},
            {98, 102},
            {104, 108},
            {117, 119},
            {111, 113},
            {140, 145},
            {143, 148},
            {127, 130},
            {131, 133},
            {134, 138},
    };

    private int[] getIndexArrayByFace(int faceIndex) {
        return faceIndices[faceIndex];
    }

    private void changePoint(FloatBuffer vertBuffer, int faceIndex, float x, float y, float z) {
        int[] indices = getIndexArrayByFace(faceIndex);
        if (indices != null) {
            int len = indices.length;
            for (int i=0; i<len; i++) {
                int index = indices[i]-1;
                vertBuffer.put(index * 3, x);
                vertBuffer.put(index * 3 + 1, y);
                vertBuffer.put(index * 3 + 2, z);
            }
        }
    }

    public void setDynamicPoints(List<DynamicPoint> mPoints) {
        if (!mIsChanging) {
            this.mPoints = mPoints;
        }
    }

    private Bitmap changeSkinColor(Bitmap bitmap, int skinColor) {
        if (bitmap != null) {
            Bitmap texture = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            int width = texture.getWidth();
            int height = texture.getHeight();

            int skinRed = Color.red(skinColor);
            int skinGreen = Color.green(skinColor);
            int skinBlue = Color.blue(skinColor);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = texture.getPixel(x, y);
                    int red = Color.red(pixel);
                    int green = Color.green(pixel);
                    int blue = Color.blue(pixel);

                    // TODO
                    // 将肤色与该点颜色进行混合
                    // 在Photoshop里面看，“柔光”的效果是比较合适的。 “叠加”也类似，不过画面有点过饱和
                    // 调色层在顶层并设为“柔光”，和人脸层在顶层并设为“柔光”是不同的
                    // 理想的效果是前者，但是在网上找到的“柔光”代码实现的是后者
                    // 由于没弄明白怎么改写，暂时先用“叠加”效果，然后降低饱和度
                    red = overlay(skinRed, red);
                    green = overlay(skinGreen, green);
                    blue = overlay(skinBlue, blue);

                    pixel = Color.rgb(red, green, blue);
                    texture.setPixel(x, y, pixel);
                }
            }

            // 降低饱和度
            float saturation = 0.35f;
            ColorMatrix cMatrix = new ColorMatrix();
            cMatrix.setSaturation(saturation);

            Paint paint = new Paint();
            paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));

            Canvas canvas = new Canvas(texture);
            canvas.drawBitmap(texture, 0, 0, paint);

            return texture;
        }
        return null;
    }

    // 混合模式 -- 柔光
    private int softLight(int A, int B) {
        return (B < 128) ? (2 * ((A >> 1) + 64)) * (B / 255) : (255 - (2 * (255 - ((A >> 1) + 64)) * (255 - B) / 255));
    }

    // 混合模式 -- 叠加
    private int overlay(int A, int B) {
        return ((B < 128) ? (2 * A * B / 255) : (255 - 2 * (255 - A) * (255 - B) / 255));
    }
}
