package com.simoncherry.arcamera.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.MotionEvent;

import com.simoncherry.arcamera.model.DynamicPoint;
import com.simoncherry.arcamera.model.Ornament;
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
    private float mScale = 1.0f;
    // 用于动态3D模型
    private List<DynamicPoint> mPoints = new ArrayList<>();
    private boolean mIsChanging = false;


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

    // 设置3D模型的缩放比例
    public void setScale(float scale) {
        mScale = scale;
    }

    @Override
    protected void initScene() {
        try {
            mContainer = new Object3D();
            mContainer.setScale(1.0f);
            mContainer.setRotation(0, 0, 0);
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
            Log.e(TAG, "静态模型");
            // 处理3D模型的旋转
            mContainer.setRotation(mAccValues.x, mAccValues.y, mAccValues.z);
            // 处理3D模型的缩放
            mContainer.setScale(mScale);
        } else {
            Log.e(TAG, "动态模型");
            if (mPoints != null && mPoints.size() > 0) {
                mIsChanging = true;
                FloatBuffer vertBuffer = mGeometry3D.getVertices();

                try {  // FIXME
                    for (int i=0; i<mPoints.size(); i++) {
                        DynamicPoint point = mPoints.get(i);
                        Log.e(TAG, "No." + i + ": " + point.toString());
                        changePoint(vertBuffer, point.getIndex(), point.getX(), point.getY(), point.getZ());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mGeometry3D.changeBufferData(mGeometry3D.getVertexBufferInfo(), vertBuffer, 0, vertBuffer.limit());
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
                mContainer.setScale(1.0f);
                mContainer.setRotation(0, 0, 0);
            }

            if (mOrnamentModel != null) {
                LoaderOBJ objParser1 = new LoaderOBJ(mContext.getResources(), mTextureManager, mOrnamentModel.getModelResId());
                objParser1.parse();
                mOrnament = objParser1.getParsedObject();

                mIsFaceMask = mOrnamentModel.isFaceMask();
                if (mIsFaceMask) {
                    int textureResId = mOrnamentModel.getTextureResId();
                    if (textureResId <= 0) {  // 如果没有有效的贴图资源Id
                        mIsFaceMask = false;
                    } else {
                        ATexture texture = mOrnament.getMaterial().getTextureList().get(0);
                        mOrnament.getMaterial().removeTexture(texture);

                        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), textureResId);
                        if (bitmap == null) {  // 如果无法生成的贴图Bitmap
                            mIsFaceMask = false;
                        } else {
                            mOrnament.getMaterial().addTexture(new Texture("canvas", bitmap));
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
}
