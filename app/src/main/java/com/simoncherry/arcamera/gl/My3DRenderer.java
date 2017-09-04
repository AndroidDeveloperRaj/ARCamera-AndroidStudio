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
import com.simoncherry.arcamera.rajawali.MyFragmentShader;
import com.simoncherry.arcamera.rajawali.MyVertexShader;
import com.simoncherry.arcamera.util.BitmapUtils;
import com.simoncherry.arcamera.util.OrnamentFactory;

import org.rajawali3d.Geometry3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;
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
    private List<Object3D> mObject3DList = new ArrayList<>();
    private Object3D mShaderPlane;
    private Material mCustomMaterial;
    private MyFragmentShader mMyFragmentShader;

    private Ornament mOrnamentModel;
    private boolean mIsNeedUpdateOrnament = false;
    private boolean mIsOrnamentVisible = true;
    private int mScreenW = 1;
    private int mScreenH = 1;

    private int mModelType = Ornament.TYPE_NONE;
    // 用于静态3D模型
    private Vector3 mAccValues;
    private float mTransX = 0.0f;
    private float mTransY = 0.0f;
    private float mScale = 1.0f;
    // 用于动态3D模型
    private List<Geometry3D> mGeometry3DList = new ArrayList<>();
    private List<DynamicPoint> mPoints = new ArrayList<>();
    private boolean mIsChanging = false;
    // 用于Rajawali内置模型
    private List<Material> mMaterialList = new ArrayList<>();
    private float mMaterialTime = 0;

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
        if (mModelType == Ornament.TYPE_STATIC || mModelType == Ornament.TYPE_BUILT_IN) {
            mTransX = x;
            mTransY = y;
            setScale(z);
        }
    }

    // 设置3D模型的缩放比例
    public void setScale(float scale) {
        mScale = scale;
    }

    public void setScreenW(int width) {
        mScreenW = width;
    }

    public void setScreenH(int height) {
        mScreenH = height;
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

        if (mModelType == Ornament.TYPE_STATIC || mModelType == Ornament.TYPE_BUILT_IN) {
            // 处理3D模型的旋转
            mContainer.setRotation(mAccValues.x, mAccValues.y, mAccValues.z);
            // 处理3D模型的缩放
            mContainer.setScale(mScale);
            // 处理3D模型的平移
            getCurrentCamera().setX(mTransX);
            getCurrentCamera().setY(mTransY);

            if (mOrnamentModel != null && mOrnamentModel.getTimeStep() > 0 && mMaterialList != null) {
                for (int i = 0; i < mMaterialList.size(); i++) {
                    Material material = mMaterialList.get(i);
                    if (material != null) {
                        material.setTime(mMaterialTime);
                        mMaterialTime += mOrnamentModel.getTimeStep();
                        if (mMaterialTime > 1000) {
                            mMaterialTime = 0;
                        }
                    }
                }
            }

        } else if (mModelType == Ornament.TYPE_DYNAMIC) {
            if (!mIsChanging && mPoints != null && mPoints.size() > 0) {
                mIsChanging = true;

                try {  // FIXME
                    if (mGeometry3DList != null && mGeometry3DList.size() > 0) {
                        for (Geometry3D geometry3D : mGeometry3DList) {
                            FloatBuffer vertBuffer = geometry3D.getVertices();
                            for (int i = 0; i < mPoints.size(); i++) {
                                DynamicPoint point = mPoints.get(i);
                                changePoint(vertBuffer, point.getIndex(), point.getX(), point.getY(), point.getZ());
                            }
                            geometry3D.changeBufferData(geometry3D.getVertexBufferInfo(), vertBuffer, 0, vertBuffer.limit());
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }

                mIsChanging = false;
            }
        }

        // TODO
        if (mShaderPlane != null && mOrnamentModel != null && mMyFragmentShader != null && mCustomMaterial != null) {
            mMyFragmentShader.setScreenW(mScreenW);
            mMyFragmentShader.setScreenH(mScreenH);

            if (mMaterialTime == 0) {
                mMyFragmentShader.setFlag(1);
            }

            mMaterialTime += mOrnamentModel.getTimeStep();
            mCustomMaterial.setTime(mMaterialTime);

            if (mMaterialTime > 0.125f) {
                mMyFragmentShader.setFlag(0);
            }

            if (mMaterialTime > 1) {
                mMaterialTime = 0;
            }
        }
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
    }

    private void clearScene() {
        if (mObject3DList != null && mObject3DList.size() > 0) {
            for (int i = 0; i < mObject3DList.size(); i++) {
                Object3D object3D = mObject3DList.get(i);
                if (object3D != null) {
                    mContainer.removeChild(object3D);
                }
            }
            mObject3DList.clear();
        }

        if (mMaterialList != null && mMaterialList.size() > 0) {
            mMaterialList.clear();
        }

        if (mGeometry3DList != null && mGeometry3DList.size() > 0) {
            mGeometry3DList.clear();
        }

        if (mShaderPlane != null) {
            mContainer.removeChild(mShaderPlane);
            mShaderPlane = null;
        }

        mMaterialTime = 0;
    }

    private void loadOrnament() {
        try {
            clearScene();

            if (mOrnamentModel != null) {
                mModelType = mOrnamentModel.getType();
                switch (mModelType) {
                    case Ornament.TYPE_BUILT_IN:
                        loadBuildInModel();
                        break;
                    case Ornament.TYPE_STATIC:
                    case Ornament.TYPE_DYNAMIC:
                        loadExtraModel();
                        initOrnamentParams();
                        break;
                }

                boolean isHasShaderPlane = mOrnamentModel.isHasShaderPlane();
                if (isHasShaderPlane) {
                    loadShaderPlane();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBuildInModel() {
        try {
            List<Object3D> object3DList = mOrnamentModel.getObject3DList();
            List<List<IMaterialPlugin>> materialList = mOrnamentModel.getMaterialList();
            if (object3DList != null && materialList != null) {
                mObject3DList.addAll(object3DList);

                for (List<IMaterialPlugin> pluginList : materialList) {
                    Material material = new Material();
                    material.enableTime(true);
                    for (IMaterialPlugin plugin : pluginList) {
                        material.addPlugin(plugin);
                    }
                    mMaterialList.add(material);
                }

                if (mObject3DList != null && mObject3DList.size() > 0) {
                    for (int i = 0; i < mObject3DList.size(); i++) {
                        Object3D object3D = mObject3DList.get(i);
                        if (object3D != null) {
                            Material material = mMaterialList.get(i);
                            if (material != null) {
                                object3D.setMaterial(material);
                            }
                            mContainer.addChild(object3D);
                        }
                    }

                    mContainer.setScale(mOrnamentModel.getScale());
                    mContainer.setPosition(mOrnamentModel.getOffsetX(), mOrnamentModel.getOffsetY(), mOrnamentModel.getOffsetZ());
                    mContainer.setRotation(mOrnamentModel.getRotateX(), mOrnamentModel.getRotateY(), mOrnamentModel.getRotateZ());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadExtraModel() {
        try {
            List<Ornament.Model> modelList = mOrnamentModel.getModelList();
            if (modelList != null && modelList.size() > 0) {
                for (Ornament.Model model : modelList) {
                    String texturePath = model.getTexturePath();

                    Object3D object3D;
                    if (texturePath != null) {
                        object3D = loadDynamicModel(model);
                    } else {
                        object3D = loadStaticModel(model);
                    }

                    if (object3D != null) {
                        mObject3DList.add(object3D);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object3D loadStaticModel(Ornament.Model model) {
        try {
            int modelResId = model.getModelResId();
            LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, modelResId);
            objParser.parse();
            Object3D object3D = objParser.getParsedObject();

            String name = model.getName();
            object3D.setName(name == null ? "" : name);
            object3D.setScale(model.getScale());
            object3D.setPosition(model.getOffsetX(), model.getOffsetY(), model.getOffsetZ());
            object3D.setRotation(model.getRotateX(), model.getRotateY(), model.getRotateZ());

            int textureResId = model.getTextureResId();
            if (textureResId > 0) {
                ATexture texture = object3D.getMaterial().getTextureList().get(0);
                object3D.getMaterial().removeTexture(texture);

                Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), textureResId);
                if (bitmap != null) {
                    mIsChanging = true;
                    // 调整肤色
                    if (model.isNeedSkinColor()) {
                        bitmap = changeSkinColor(bitmap, mSkinColor);
                    }
                    object3D.getMaterial().addTexture(new Texture("canvas", bitmap));
                    mIsChanging = false;
                }
            }

            int color = model.getColor();
            if (color != OrnamentFactory.NO_COLOR) {
                object3D.getMaterial().setColor(color);
            }

            return object3D;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Object3D loadDynamicModel(Ornament.Model model) {
        try {
            String objDir = "OpenGLDemo/txt/";
            String objName = "base_face_uv3_obj";
            LoaderOBJ parser = new LoaderOBJ(this, objDir + objName);
            parser.parse();
            Object3D object3D = parser.getParsedObject();

            object3D.setScale(model.getScale());
            object3D.setPosition(model.getOffsetX(), model.getOffsetY(), model.getOffsetZ());
            object3D.setRotation(model.getRotateX(), model.getRotateY(), model.getRotateZ());

            ATexture texture = object3D.getMaterial().getTextureList().get(0);
            object3D.getMaterial().removeTexture(texture);

            String texturePath = model.getTexturePath();
            Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromFilePath(texturePath, 300, 300);
            // 调整肤色
            if (model.isNeedSkinColor()) {
                bitmap = changeSkinColor(bitmap, mSkinColor);
            }
            object3D.getMaterial().addTexture(new Texture("canvas", bitmap));
            object3D.getMaterial().enableLighting(false);

            int color = model.getColor();
            if (color != OrnamentFactory.NO_COLOR) {
                object3D.getMaterial().setColor(color);
            }

            return object3D;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void initOrnamentParams() {
        if (mObject3DList != null && mObject3DList.size() > 0) {
            for (Object3D object3D : mObject3DList) {
                mContainer.addChild(object3D);

                Geometry3D geometry3D = object3D.getGeometry();
                mGeometry3DList.add(geometry3D);
            }
        }

        mContainer.setTransparent(false);
        mContainer.setScale(1.0f);
        mContainer.setRotation(0, 0, 0);
        mContainer.setPosition(0, 0, 0);
        getCurrentCamera().setX(0);
        getCurrentCamera().setY(0);
    }

    private void loadShaderPlane() {
        int vertResId = mOrnamentModel.getVertResId();
        int fragResId = mOrnamentModel.getFragResId();
        if (vertResId > 0 && fragResId > 0) {
            mMyFragmentShader = new MyFragmentShader(fragResId);

            mCustomMaterial = new Material(
                    new MyVertexShader(vertResId),
                    mMyFragmentShader);
            mCustomMaterial.enableTime(true);

            float offsetX = mOrnamentModel.getPlaneOffsetX();
            float offsetY = mOrnamentModel.getPlaneOffsetY();
            float offsetZ = mOrnamentModel.getPlaneOffsetZ();
            mShaderPlane = new Plane(5, 5, 1, 1);
            mShaderPlane.setPosition(offsetX, offsetY, offsetZ);
            mShaderPlane.setMaterial(mCustomMaterial);
            mShaderPlane.setTransparent(true);
            mContainer.addChild(mShaderPlane);
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
