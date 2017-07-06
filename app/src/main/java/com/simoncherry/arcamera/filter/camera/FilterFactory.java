package com.simoncherry.arcamera.filter.camera;

import android.content.res.Resources;

import com.simoncherry.arcamera.R;

/**
 * Created by Simon on 2017/7/6.
 */

public class FilterFactory {

    public static AFilter getFilter(Resources res, int menuId) {
        switch (menuId) {
            case R.id.menu_camera_gray:
                return new GrayFilter(res);
            case R.id.menu_camera_binary:
                return new BinaryFilter(res);
            default:
                return new NoFilter(res);
        }
    }
}
