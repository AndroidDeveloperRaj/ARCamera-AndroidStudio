package com.simoncherry.arcamera.gl;

import android.content.Context;
import android.view.MotionEvent;

import com.simoncherry.arcamera.R;

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
    private Object3D mMask;
    private Vector3 mAccValues;
    private float mScale = 1.0f;

    public My3DRenderer(Context context) {
        super(context);
        mAccValues = new Vector3();
    }

    @Override
    protected void initScene() {
        try {
            // 老虎鼻子
            final LoaderOBJ parser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.tiger_nose_obj);
            parser.parse();
            mMask = parser.getParsedObject();
            mMask.setScale(0.002f);
            mMask.setY(-0.2f);
            mMask.setZ(0.4f);

            mContainer = new Object3D();
            mContainer.addChild(mMask);
            getCurrentScene().addChild(mContainer);

            getCurrentScene().getCamera().setZ(5.5);

        } catch (ParsingException e) {
            e.printStackTrace();
        }

        getCurrentScene().setBackgroundColor(0);
    }

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        super.onRender(ellapsedRealtime, deltaTime);
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

    // 设置3D模型的转动角度
    public void setAccelerometerValues(float x, float y, float z) {
        mAccValues.setAll(x, y, z);
    }
    // 设置3D模型的缩放比例
    public void setScale(float scale) {
        mScale = scale;
    }
}
