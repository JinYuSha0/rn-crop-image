package com.rncrop.components.cropImage;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

public class CropImageManagerModule extends ReactContextBaseJavaModule {
    public static final String NAME = "CropImage";

    public CropImageManagerModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }
}
