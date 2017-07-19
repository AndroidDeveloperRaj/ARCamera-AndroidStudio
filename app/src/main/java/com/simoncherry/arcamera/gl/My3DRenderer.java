package com.simoncherry.arcamera.gl;

import android.content.Context;
import android.view.MotionEvent;

import com.simoncherry.arcamera.model.Ornament;
import com.simoncherry.arcamera.util.OrnamentFactory;

import org.rajawali3d.Object3D;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;

/**
 * Created by Simon on 2017/7/19.
 */

public class My3DRenderer extends Renderer {
    private Object3D mContainer;
    private Object3D mOrnament;

    private Ornament mOrnamentModel;
    private boolean mIsNeedUpdateOrnament = false;
    private boolean mIsOrnamentVisible = true;
    private Vector3 mAccValues;
    private float mScale = 1.0f;

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
            try {
                loadOrnament();
            } catch (ParsingException e) {
                e.printStackTrace();
            }
        }

        // 处理3D模型的旋转
        mContainer.setRotation(mAccValues.x, mAccValues.y, mAccValues.z);
        // 处理3D模型的缩放
        mContainer.setScale(mScale);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
    }

    private void loadOrnament() throws ParsingException {
        if (mOrnament != null) {
            mIsOrnamentVisible = mOrnament.isVisible();
            mOrnament.setScale(1.0f);
            mOrnament.setPosition(0, 0, 0);
            mContainer.removeChild(mOrnament);
        }

        if (mOrnamentModel != null) {
            LoaderOBJ objParser1 = new LoaderOBJ(mContext.getResources(), mTextureManager, mOrnamentModel.getModelResId());
            objParser1.parse();
            mOrnament = objParser1.getParsedObject();
            mOrnament.setScale(mOrnamentModel.getScale());
            mOrnament.setPosition(mOrnamentModel.getOffsetX(), mOrnamentModel.getOffsetY(), mOrnamentModel.getOffsetZ());
            mOrnament.setRotation(mOrnamentModel.getRotateX(), mOrnamentModel.getRotateY(), mOrnamentModel.getRotateZ());
            int color = mOrnamentModel.getColor();
            if (color != OrnamentFactory.NO_COLOR) {
                mOrnament.getMaterial().setColor(color);
            }
            mOrnament.setVisible(mIsOrnamentVisible);
            mContainer.addChild(mOrnament);
        }
    }
}
