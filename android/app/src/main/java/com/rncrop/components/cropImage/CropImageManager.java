package com.rncrop.components.cropImage;

import android.graphics.PorterDuff;

import androidx.annotation.Nullable;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeControllerBuilder;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.annotations.ReactPropGroup;
import com.facebook.react.views.image.GlobalImageLoadListener;
import com.facebook.react.views.image.ImageLoadEvent;
import com.facebook.react.views.image.ImageResizeMethod;
import com.facebook.react.views.image.ImageResizeMode;
import com.facebook.react.views.image.ReactCallerContextFactory;
import com.facebook.yoga.YogaConstants;

import java.util.HashMap;
import java.util.Map;

public class CropImageManager extends SimpleViewManager<CropImageView> {
    public static final String REACT_CLASS = "RCTCropImageView";
    @Nullable
    private AbstractDraweeControllerBuilder mDraweeControllerBuilder;
    @Nullable
    private GlobalImageLoadListener mGlobalImageLoadListener;
    @Nullable
    private final Object mCallerContext;
    @Nullable
    private final ReactCallerContextFactory mCallerContextFactory;

    /** @deprecated */
    @Deprecated
    public CropImageManager(@Nullable AbstractDraweeControllerBuilder draweeControllerBuilder, @Nullable Object callerContext) {
        this(draweeControllerBuilder, (GlobalImageLoadListener)null, (Object)callerContext);
    }

    /** @deprecated */
    @Deprecated
    public CropImageManager(@Nullable AbstractDraweeControllerBuilder draweeControllerBuilder, @Nullable GlobalImageLoadListener globalImageLoadListener, @Nullable Object callerContext) {
        this.mDraweeControllerBuilder = draweeControllerBuilder;
        this.mGlobalImageLoadListener = globalImageLoadListener;
        this.mCallerContext = callerContext;
        this.mCallerContextFactory = null;
    }

    public CropImageManager(@Nullable AbstractDraweeControllerBuilder draweeControllerBuilder, @Nullable ReactCallerContextFactory callerContextFactory) {
        this(draweeControllerBuilder, (GlobalImageLoadListener)null, (ReactCallerContextFactory)callerContextFactory);
    }

    public CropImageManager(@Nullable AbstractDraweeControllerBuilder draweeControllerBuilder, @Nullable GlobalImageLoadListener globalImageLoadListener, @Nullable ReactCallerContextFactory callerContextFactory) {
        this.mDraweeControllerBuilder = draweeControllerBuilder;
        this.mGlobalImageLoadListener = globalImageLoadListener;
        this.mCallerContextFactory = callerContextFactory;
        this.mCallerContext = null;
    }

    public CropImageManager() {
        this.mDraweeControllerBuilder = null;
        this.mCallerContext = null;
        this.mCallerContextFactory = null;
    }

    public AbstractDraweeControllerBuilder getDraweeControllerBuilder() {
        if (this.mDraweeControllerBuilder == null) {
            this.mDraweeControllerBuilder = Fresco.newDraweeControllerBuilder();
        }

        return this.mDraweeControllerBuilder;
    }

    /** @deprecated */
    @Deprecated
    public Object getCallerContext() {
        return this.mCallerContext;
    }

    public CropImageView createViewInstance(ThemedReactContext context) {
        Object callerContext = this.mCallerContextFactory != null ? this.mCallerContextFactory.getOrCreateCallerContext(context.getModuleName(), (String)null) : this.getCallerContext();
        return new CropImageView(context, this.getDraweeControllerBuilder(), this.mGlobalImageLoadListener, callerContext);
    }

    public String getName() {
        return REACT_CLASS;
    }

    @ReactProp(
            name = "accessible"
    )
    public void setAccessible(CropImageView view, boolean accessible) {
        view.setFocusable(accessible);
    }

    @ReactProp(
            name = "src"
    )
    public void setSource(CropImageView view, @Nullable ReadableArray sources) {
        view.setSource(sources);
    }

    @ReactProp(
            name = "mask"
    )
    public void setMask(CropImageView view, @Nullable ReadableArray masks) {
        view.setMasks(masks);
    }

    @ReactProp(
            name = "replacePixelColor"
    )
    public void setReplacePixelColor(CropImageView view, @Nullable String replacePixelColor) {
        view.setReplacePixelColor(replacePixelColor);
    }

    @ReactProp(
            name = "blurRadius"
    )
    public void setBlurRadius(CropImageView view, float blurRadius) {
        view.setBlurRadius(blurRadius);
    }

    @ReactProp(
            name = "internal_analyticTag"
    )
    public void setInternal_AnalyticsTag(CropImageView view, @Nullable String analyticTag) {
        if (this.mCallerContextFactory != null) {
            view.updateCallerContext(this.mCallerContextFactory.getOrCreateCallerContext(((ThemedReactContext)view.getContext()).getModuleName(), analyticTag));
        }

    }

    @ReactProp(
            name = "defaultSrc"
    )
    public void setDefaultSource(CropImageView view, @Nullable String source) {
        view.setDefaultSource(source);
    }

    @ReactProp(
            name = "loadingIndicatorSrc"
    )
    public void setLoadingIndicatorSource(CropImageView view, @Nullable String source) {
        view.setLoadingIndicatorSource(source);
    }

    @ReactProp(
            name = "borderColor",
            customType = "Color"
    )
    public void setBorderColor(CropImageView view, @Nullable Integer borderColor) {
        if (borderColor == null) {
            view.setBorderColor(0);
        } else {
            view.setBorderColor(borderColor);
        }

    }

    @ReactProp(
            name = "overlayColor",
            customType = "Color"
    )
    public void setOverlayColor(CropImageView view, @Nullable Integer overlayColor) {
        if (overlayColor == null) {
            view.setOverlayColor(0);
        } else {
            view.setOverlayColor(overlayColor);
        }

    }

    @ReactProp(
            name = "borderWidth"
    )
    public void setBorderWidth(CropImageView view, float borderWidth) {
        view.setBorderWidth(borderWidth);
    }

    @ReactPropGroup(
            names = {"borderRadius", "borderTopLeftRadius", "borderTopRightRadius", "borderBottomRightRadius", "borderBottomLeftRadius"},
            defaultFloat = Float.NaN
    )
    public void setBorderRadius(CropImageView view, int index, float borderRadius) {
        if (!YogaConstants.isUndefined(borderRadius)) {
            borderRadius = PixelUtil.toPixelFromDIP(borderRadius);
        }

        if (index == 0) {
            view.setBorderRadius(borderRadius);
        } else {
            view.setBorderRadius(borderRadius, index - 1);
        }

    }

    @ReactProp(
            name = "resizeMode"
    )
    public void setResizeMode(CropImageView view, @Nullable String resizeMode) {
        view.setScaleType(ImageResizeMode.toScaleType(resizeMode));
        view.setTileMode(ImageResizeMode.toTileMode(resizeMode));
    }

    @ReactProp(
            name = "resizeMethod"
    )
    public void setResizeMethod(CropImageView view, @Nullable String resizeMethod) {
        if (resizeMethod != null && !"auto".equals(resizeMethod)) {
            if ("resize".equals(resizeMethod)) {
                view.setResizeMethod(ImageResizeMethod.RESIZE);
            } else if ("scale".equals(resizeMethod)) {
                view.setResizeMethod(ImageResizeMethod.SCALE);
            } else {
                view.setResizeMethod(ImageResizeMethod.AUTO);
                FLog.w("ReactNative", "Invalid resize method: '" + resizeMethod + "'");
            }
        } else {
            view.setResizeMethod(ImageResizeMethod.AUTO);
        }

    }

    @ReactProp(
            name = "tintColor",
            customType = "Color"
    )
    public void setTintColor(CropImageView view, @Nullable Integer tintColor) {
        if (tintColor == null) {
            view.clearColorFilter();
        } else {
            view.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
        }

    }

    @ReactProp(
            name = "progressiveRenderingEnabled"
    )
    public void setProgressiveRenderingEnabled(CropImageView view, boolean enabled) {
        view.setProgressiveRenderingEnabled(enabled);
    }

    @ReactProp(
            name = "fadeDuration"
    )
    public void setFadeDuration(CropImageView view, int durationMs) {
        view.setFadeDuration(durationMs);
    }

    @ReactProp(
            name = "shouldNotifyLoadEvents"
    )
    public void setLoadHandlersRegistered(CropImageView view, boolean shouldNotifyLoadEvents) {
        view.setShouldNotifyLoadEvents(shouldNotifyLoadEvents);
    }

    @ReactProp(
            name = "headers"
    )
    public void setHeaders(CropImageView view, ReadableMap headers) {
        view.setHeaders(headers);
    }

    @Nullable
    public Map getExportedCustomDirectEventTypeConstants() {
        Map<String, Object> baseEventTypeConstants = super.getExportedCustomDirectEventTypeConstants();
        Map<String, Object> eventTypeConstants = baseEventTypeConstants == null ? new HashMap() : baseEventTypeConstants;
        ((Map)eventTypeConstants).putAll(MapBuilder.of(ImageLoadEvent.eventNameForType(4), MapBuilder.of("registrationName", "onLoadStart"), ImageLoadEvent.eventNameForType(5), MapBuilder.of("registrationName", "onProgress"), ImageLoadEvent.eventNameForType(2), MapBuilder.of("registrationName", "onLoad"), ImageLoadEvent.eventNameForType(1), MapBuilder.of("registrationName", "onError"), ImageLoadEvent.eventNameForType(3), MapBuilder.of("registrationName", "onLoadEnd")));
        return (Map)eventTypeConstants;
    }

    protected void onAfterUpdateTransaction(CropImageView view) {
        super.onAfterUpdateTransaction(view);
        view.maybeUpdateView();
    }
}
