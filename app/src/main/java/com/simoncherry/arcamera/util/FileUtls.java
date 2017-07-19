package com.simoncherry.arcamera.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by Simon on 2017/7/19.
 */

public class FileUtls {

    public static String getBaseFolder(Context context){
        String baseFolder = Environment.getExternalStorageDirectory() + "/OpenGLDemo/";
        File f = new File(baseFolder);
        if(!f.exists()){
            boolean b = f.mkdirs();
            if(!b){
                baseFolder = context.getExternalFilesDir(null).getAbsolutePath() + "/";
            }
        }
        return baseFolder;
    }

    public static String getPath(Context context, String path,String fileName){
        String p = getBaseFolder(context) + path;
        File f = new File(p);
        if(!f.exists() && !f.mkdirs()){
            return getBaseFolder(context) + fileName;
        }
        return p + fileName;
    }
}
