package com.rncrop.components.cropImage;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.views.imagehelper.ImageSource;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CropImageManagerModule extends ReactContextBaseJavaModule {
    public static final String NAME = "CropImage";
    private final ReactApplicationContext mContext;

    public CropImageManagerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mContext = reactContext;
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @ReactMethod()
    public void crop(ReadableMap originSource, ReadableMap maskSource, @Nullable String replacePixelColor, Promise promise) {
        new Thread(() -> {
            try {
                ImageSource origin = new ImageSource(mContext, originSource.getString("uri"));
                ImageSource mask = new ImageSource(mContext, maskSource.getString("uri"));
                int pixelColor = replacePixelColor == null ? Color.TRANSPARENT : Color.parseColor(replacePixelColor);
                String md5Str = origin.getSource() + "_" + mask.getSource() + "_" + pixelColor;
                String filename = calculateMD5(md5Str) + ".png";
                String filepath = CropImageCore.resolveImgPath(mContext, filename);
                if (!CropImageCore.judgeImgExists(filepath)) {
                    Bitmap originBm = CropImageCore.getBitmapByUri(mContext, origin.getUri());
                    Bitmap maskBm = CropImageCore.getBitmapByUri(mContext, mask.getUri());
                    Bitmap result = CropImageCore.crop(originBm, maskBm, pixelColor);
                    CropImageCore.saveBitmapToImg(mContext, result, filename);
                }
                promise.resolve("file://" + filepath);
            } catch (Exception exception) {
                promise.reject(exception);
            }
        }).start();
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public void clearTemp() {
        new Thread(() -> {
            CropImageCore.clearCropImgTemp(mContext);
        }).start();
    }

    private String calculateMD5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] inputBytes = input.getBytes();
        byte[] hashBytes = md.digest(inputBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
