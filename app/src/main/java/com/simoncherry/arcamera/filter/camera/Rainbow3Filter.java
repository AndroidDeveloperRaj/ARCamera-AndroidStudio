package com.simoncherry.arcamera.filter.camera;

import android.content.res.Resources;
import android.opengl.GLES20;

/**
 * Created by Simon on 2017/7/6.
 */

public class Rainbow3Filter extends LandmarkFilter {

    private int gLandmarkX;
    private int gLandmarkY;
    private int gStarPosX;
    private int gStarPosY;
    private int gMouthOpen;
    private int gGlobalTime;

    private float[] uLandmarkX;
    private float[] uLandmarkY;
    private float[] uStarPosX;
    private float[] uStarPosY;
    private int isMouthOpen = 0;

    public Rainbow3Filter(Resources mRes) {
        super(mRes);
        uLandmarkX = new float[106];
        uLandmarkY = new float[106];
        uStarPosX = new float[]{-0.05f, 0.01f, 0.06f, 0.04f, -0.08f, 0.03f, 0.1f};
        uStarPosY = new float[]{0.02f, 0.08f, 0.17f, 0.25f, 0.31f, 0.36f, 0.42f};
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/base_vertex.sh",
                "shader/test/rainbow3_fragment.frag");

        gLandmarkX = GLES20.glGetUniformLocation(mProgram, "uLandmarkX");
        gLandmarkY = GLES20.glGetUniformLocation(mProgram, "uLandmarkY");
        gStarPosX = GLES20.glGetUniformLocation(mProgram, "uStarPosX");
        gStarPosY = GLES20.glGetUniformLocation(mProgram, "uStarPosY");
        gMouthOpen = GLES20.glGetUniformLocation(mProgram, "uMouthOpen");
        gGlobalTime = GLES20.glGetUniformLocation(mProgram, "iGlobalTime");
    }

    @Override
    protected void onSizeChanged(int width, int height) {
    }

    public void setLandmarks(float[] landmarkX, float[] landmarkY) {
        uLandmarkX = landmarkX;
        uLandmarkY = landmarkY;
    }

    public void setMouthOpen(int isOpen) {
        isMouthOpen = isOpen;
    }

    @Override
    protected void onSetExpandData() {
        super.onSetExpandData();
        GLES20.glUniform1fv(gLandmarkX, uLandmarkX.length, uLandmarkX, 0);
        GLES20.glUniform1fv(gLandmarkY, uLandmarkY.length, uLandmarkY, 0);
        GLES20.glUniform1fv(gStarPosX, uStarPosX.length, uStarPosX, 0);
        GLES20.glUniform1fv(gStarPosY, uStarPosY.length, uStarPosY, 0);
        GLES20.glUniform1i(gMouthOpen, isMouthOpen);

        float time = ((float) (System.currentTimeMillis() - START_TIME)) / 1000.0f;
        GLES20.glUniform1f(gGlobalTime, time);
    }
}
