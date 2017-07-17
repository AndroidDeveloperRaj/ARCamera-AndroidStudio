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
            case R.id.menu_camera_mirror:
                return new MirrorFilter(res);
            case R.id.menu_camera_fisheye:
                return new FishEyeFilter(res);
            case R.id.menu_camera_anim:
                ZipPkmAnimationFilter zipPkmAnimationFilter = new ZipPkmAnimationFilter(res);
                zipPkmAnimationFilter.setAnimation("assets/etczip/dragon.zip");
                return zipPkmAnimationFilter;
            case R.id.menu_camera_point:
                return new DrawPointFilter(res);
            case R.id.menu_camera_landmark:
                return new LandmarkFilter(res);
            case R.id.menu_camera_big_eye:
                return new BigEyeFilter(res);
            case R.id.menu_camera_small_eye:
                return new SmallEyeFilter(res);
            case R.id.menu_camera_fire_eye:
                return new FireEyeFilter(res);
            case R.id.menu_camera_black_eye:
                return new BlackEyeFilter(res);
            case R.id.menu_camera_flush:
                return new FlushFilter(res);
            case R.id.menu_camera_fat_face:
                return new FatFaceFilter(res);
            case R.id.menu_camera_rainbow:
                return new RainbowFilter(res);
            case R.id.menu_camera_rainbow2:
                return new Rainbow2Filter(res);
            case R.id.menu_camera_rainbow3:
                return new Rainbow3Filter(res);
            default:
                return new NoFilter(res);
        }
    }
}
