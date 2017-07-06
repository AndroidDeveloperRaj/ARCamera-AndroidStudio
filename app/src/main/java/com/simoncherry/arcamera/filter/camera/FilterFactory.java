package com.simoncherry.arcamera.filter.camera;

import android.content.res.Resources;

import com.simoncherry.arcamera.R;

/**
 * Created by Simon on 2017/7/6.
 */

public class FilterFactory {

    public static AFilter getFilter(Resources res, int menuId) {
        switch (menuId) {
            case R.id.menu_camera_beauty:
                return new BeautyFilter(res);
            case R.id.menu_camera_gray:
                return new GrayFilter(res);
            case R.id.menu_camera_binary:
                return new BinaryFilter(res);
            case R.id.menu_camera_cool:
                return new CoolFilter(res);
            case R.id.menu_camera_warm:
                return new WarmFilter(res);
            case R.id.menu_camera_negative:
                return new NegativeFilter(res);
            case R.id.menu_camera_blur:
                return new BlurFilter(res);
            case R.id.menu_camera_mosaic:
                return new MosaicFilter(res);
            case R.id.menu_camera_emboss:
                return new EmbossFilter(res);
            case R.id.menu_camera_mag:
                return new MagFilter(res);
            default:
                return new NoFilter(res);
        }
    }
}
