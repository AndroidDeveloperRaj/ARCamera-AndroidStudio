package com.simoncherry.arcamera.filter.camera;

import android.content.res.Resources;

/**
 * Created by Simon on 2017/7/6.
 */

public class BinaryFilter extends AFilter {

    public BinaryFilter(Resources mRes) {
        super(mRes);
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/base_vertex.sh",
                "shader/color/binary_fragment.frag");
    }

    @Override
    protected void onSizeChanged(int width, int height) {
    }
}
