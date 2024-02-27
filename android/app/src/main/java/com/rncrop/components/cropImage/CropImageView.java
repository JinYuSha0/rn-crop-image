package com.rncrop.components.cropImage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.Nullable;
import com.facebook.common.internal.Objects;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.drawee.controller.AbstractDraweeControllerBuilder;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.controller.ForwardingControllerListener;
import com.facebook.drawee.drawable.AutoRotateDrawable;
import com.facebook.drawee.drawable.RoundedColorDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.generic.RoundingParams.RoundingMethod;
import com.facebook.drawee.view.GenericDraweeView;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.postprocessors.IterativeBoxBlurPostProcessor;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.build.ReactBuildConfig;
import com.facebook.react.modules.fresco.ReactNetworkImageRequest;
import com.facebook.react.uimanager.FloatUtil;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.UIManagerHelper;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.facebook.react.util.RNLog;
import com.facebook.react.views.image.GlobalImageLoadListener;
import com.facebook.react.views.image.ImageLoadEvent;
import com.facebook.react.views.image.ImageResizeMethod;
import com.facebook.react.views.image.ImageResizeMode;
import com.facebook.react.views.image.MultiPostprocessor;
import com.facebook.react.views.image.ReactImageDownloadListener;
import com.facebook.react.views.imagehelper.ImageSource;
import com.facebook.react.views.imagehelper.MultiSourceHelper;
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper;
import com.facebook.yoga.YogaConstants;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CropImageView extends GenericDraweeView {
    public static final int REMOTE_IMAGE_FADE_DURATION_MS = 300;
    private static float[] sComputedCornerRadii = new float[4];
    private ImageResizeMethod mResizeMethod;
    private static final Matrix sTileMatrix = new Matrix();
    private final List<ImageSource> mSources;
    private final List<ImageSource> mMasks;
    @Nullable
    private ImageSource mImageSource;
    @Nullable
    private ImageSource mImageMask;
    @Nullable
    private Bitmap mImageMaskBitmap;
    @Nullable
    private ImageSource mCachedImageSource;
    @Nullable
    private Drawable mDefaultImageDrawable;
    @Nullable
    private Drawable mLoadingImageDrawable;
    @Nullable
    private RoundedColorDrawable mBackgroundImageDrawable;
    private int mBackgroundColor;
    private int mBorderColor;
    private int mOverlayColor;
    private float mBorderWidth;
    private float mBorderRadius;
    @Nullable
    private float[] mBorderCornerRadii;
    private ScalingUtils.ScaleType mScaleType;
    private Shader.TileMode mTileMode;
    private boolean mIsDirty;
    private final AbstractDraweeControllerBuilder mDraweeControllerBuilder;
    @Nullable
    private CropImageView.TilePostprocessor mTilePostprocessor;
    @Nullable
    private IterativeBoxBlurPostProcessor mIterativeBoxBlurPostProcessor;
    @Nullable
    private ReactImageDownloadListener mDownloadListener;
    @Nullable
    private ControllerListener mControllerForTesting;
    @Nullable
    private GlobalImageLoadListener mGlobalImageLoadListener;
    @Nullable
    private Object mCallerContext;
    private int mFadeDurationMs;
    private boolean mProgressiveRenderingEnabled;
    private ReadableMap mHeaders;
    private Postprocessor cropImagePostprocessor;
    private int mReplacePixelColor = Color.TRANSPARENT;

    public void updateCallerContext(@Nullable Object callerContext) {
        if (!Objects.equal(this.mCallerContext, callerContext)) {
            this.mCallerContext = callerContext;
            this.mIsDirty = true;
        }

    }

    private static GenericDraweeHierarchy buildHierarchy(Context context) {
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(0.0F);
        roundingParams.setPaintFilterBitmap(true);
        return (new GenericDraweeHierarchyBuilder(context.getResources())).setRoundingParams(roundingParams).build();
    }

    public CropImageView(Context context, AbstractDraweeControllerBuilder draweeControllerBuilder, @Nullable GlobalImageLoadListener globalImageLoadListener, @Nullable Object callerContext) {
        super(context, buildHierarchy(context));
        this.mResizeMethod = ImageResizeMethod.AUTO;
        this.mSources = new LinkedList();
        this.mMasks = new LinkedList<>();
        this.mBackgroundColor = 0;
        this.mBorderRadius = Float.NaN;
        this.mScaleType = ImageResizeMode.defaultValue();
        this.mTileMode = ImageResizeMode.defaultTileMode();
        this.mFadeDurationMs = -1;
        this.mDraweeControllerBuilder = draweeControllerBuilder;
        this.mGlobalImageLoadListener = globalImageLoadListener;
        this.mCallerContext = callerContext;
        this.setLegacyVisibilityHandlingEnabled(true);
        cropImagePostprocessor = new BasePostprocessor() {
            @Override
            public String getName() {
                return "cropImagePostprocessor";
            }

            @Override
            public void process(Bitmap origin) {
                CropImageView self = CropImageView.this;
                Bitmap mask = self.mImageMaskBitmap;
                if (mask != null && self.validMaskSize()) {
                    CropImageCore.crop(origin, mask, self.mReplacePixelColor);
                }
                self.releaseMaskBitmap();
            }
        };
    }

    public void setShouldNotifyLoadEvents(boolean shouldNotify) {
        if (shouldNotify != (this.mDownloadListener != null)) {
            if (!shouldNotify) {
                this.mDownloadListener = null;
            } else {
                final EventDispatcher mEventDispatcher = UIManagerHelper.getEventDispatcherForReactTag((ReactContext)this.getContext(), this.getId());
                this.mDownloadListener = new ReactImageDownloadListener<ImageInfo>() {
                    public void onProgressChange(int loaded, int total) {
                        mEventDispatcher.dispatchEvent(ImageLoadEvent.createProgressEvent(UIManagerHelper.getSurfaceId(CropImageView.this), CropImageView.this.getId(), CropImageView.this.mImageSource.getSource(), loaded, total));
                    }

                    public void onSubmit(String id, Object callerContext) {
                        mEventDispatcher.dispatchEvent(ImageLoadEvent.createLoadStartEvent(UIManagerHelper.getSurfaceId(CropImageView.this), CropImageView.this.getId()));
                    }

                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        if (imageInfo != null) {
                            mEventDispatcher.dispatchEvent(ImageLoadEvent.createLoadEvent(UIManagerHelper.getSurfaceId(CropImageView.this), CropImageView.this.getId(), CropImageView.this.mImageSource.getSource(), imageInfo.getWidth(), imageInfo.getHeight()));
                            mEventDispatcher.dispatchEvent(ImageLoadEvent.createLoadEndEvent(UIManagerHelper.getSurfaceId(CropImageView.this), CropImageView.this.getId()));
                        }

                    }

                    public void onFailure(String id, Throwable throwable) {
                        mEventDispatcher.dispatchEvent(ImageLoadEvent.createErrorEvent(UIManagerHelper.getSurfaceId(CropImageView.this), CropImageView.this.getId(), throwable));
                    }
                };
            }

            this.mIsDirty = true;
        }
    }

    public void setBlurRadius(float blurRadius) {
        int pixelBlurRadius = (int)PixelUtil.toPixelFromDIP(blurRadius) / 2;
        if (pixelBlurRadius == 0) {
            this.mIterativeBoxBlurPostProcessor = null;
        } else {
            this.mIterativeBoxBlurPostProcessor = new IterativeBoxBlurPostProcessor(2, pixelBlurRadius);
        }

        this.mIsDirty = true;
    }

    public void setBackgroundColor(int backgroundColor) {
        if (this.mBackgroundColor != backgroundColor) {
            this.mBackgroundColor = backgroundColor;
            this.mBackgroundImageDrawable = new RoundedColorDrawable(backgroundColor);
            this.mIsDirty = true;
        }

    }

    public void setBorderColor(int borderColor) {
        if (this.mBorderColor != borderColor) {
            this.mBorderColor = borderColor;
            this.mIsDirty = true;
        }

    }

    public void setOverlayColor(int overlayColor) {
        if (this.mOverlayColor != overlayColor) {
            this.mOverlayColor = overlayColor;
            this.mIsDirty = true;
        }

    }

    public void setBorderWidth(float borderWidth) {
        float newBorderWidth = PixelUtil.toPixelFromDIP(borderWidth);
        if (!FloatUtil.floatsEqual(this.mBorderWidth, newBorderWidth)) {
            this.mBorderWidth = newBorderWidth;
            this.mIsDirty = true;
        }

    }

    public void setBorderRadius(float borderRadius) {
        if (!FloatUtil.floatsEqual(this.mBorderRadius, borderRadius)) {
            this.mBorderRadius = borderRadius;
            this.mIsDirty = true;
        }

    }

    public void setBorderRadius(float borderRadius, int position) {
        if (this.mBorderCornerRadii == null) {
            this.mBorderCornerRadii = new float[4];
            Arrays.fill(this.mBorderCornerRadii, Float.NaN);
        }

        if (!FloatUtil.floatsEqual(this.mBorderCornerRadii[position], borderRadius)) {
            this.mBorderCornerRadii[position] = borderRadius;
            this.mIsDirty = true;
        }

    }

    public void setScaleType(ScalingUtils.ScaleType scaleType) {
        if (this.mScaleType != scaleType) {
            this.mScaleType = scaleType;
            this.mIsDirty = true;
        }

    }

    public void setTileMode(Shader.TileMode tileMode) {
        if (this.mTileMode != tileMode) {
            this.mTileMode = tileMode;
            if (this.isTiled()) {
                this.mTilePostprocessor = new CropImageView.TilePostprocessor();
            } else {
                this.mTilePostprocessor = null;
            }

            this.mIsDirty = true;
        }

    }

    public void setResizeMethod(ImageResizeMethod resizeMethod) {
        if (this.mResizeMethod != resizeMethod) {
            this.mResizeMethod = resizeMethod;
            this.mIsDirty = true;
        }

    }

    public void setSource(@Nullable ReadableArray sources) {
        List<ImageSource> tmpSources = new LinkedList();
        ImageSource src;
        if (sources != null && sources.size() != 0) {
            if (sources.size() == 1) {
                ReadableMap source = sources.getMap(0);
                src = new ImageSource(this.getContext(), source.getString("uri"));
                if (Uri.EMPTY.equals(src.getUri())) {
                    this.warnImageSource(source.getString("uri"));
                    src = ImageSource.getTransparentBitmapImageSource(this.getContext());
                }

                tmpSources.add(src);
            } else {
                for(int idx = 0; idx < sources.size(); ++idx) {
                    ReadableMap source = sources.getMap(idx);
                    ImageSource imageSource = new ImageSource(this.getContext(), source.getString("uri"), source.getDouble("width"), source.getDouble("height"));
                    if (Uri.EMPTY.equals(imageSource.getUri())) {
                        this.warnImageSource(source.getString("uri"));
                        imageSource = ImageSource.getTransparentBitmapImageSource(this.getContext());
                    }

                    tmpSources.add(imageSource);
                }
            }
        } else {
            tmpSources.add(ImageSource.getTransparentBitmapImageSource(this.getContext()));
        }

        if (!this.mSources.equals(tmpSources)) {
            this.mSources.clear();
            Iterator var7 = tmpSources.iterator();

            while(var7.hasNext()) {
                src = (ImageSource)var7.next();
                this.mSources.add(src);
            }

            this.mIsDirty = true;
        }
    }

    public void setMasks(@Nullable ReadableArray masks) {
        releaseMaskBitmap();
        List<ImageSource> tmpMasks = new LinkedList();
        ImageSource src;
        if (masks != null && masks.size() != 0) {
            if (masks.size() == 1) {
                ReadableMap source = masks.getMap(0);
                src = new ImageSource(this.getContext(), source.getString("uri"));
                if (Uri.EMPTY.equals(src.getUri())) {
                    this.warnImageMask(source.getString("uri"));
                } else {
                    tmpMasks.add(src);
                }

            } else {
                for(int idx = 0; idx < masks.size(); ++idx) {
                    ReadableMap source = masks.getMap(idx);
                    ImageSource imageSource = new ImageSource(this.getContext(), source.getString("uri"), source.getDouble("width"), source.getDouble("height"));
                    if (Uri.EMPTY.equals(imageSource.getUri())) {
                        this.warnImageMask(source.getString("uri"));
                        imageSource = ImageSource.getTransparentBitmapImageSource(this.getContext());
                    }

                    tmpMasks.add(imageSource);
                }
            }
        } else {
            this.warnImageMask("null");
        }

        if (!this.mMasks.equals(tmpMasks)) {
            this.mMasks.clear();
            Iterator var7 = tmpMasks.iterator();

            while(var7.hasNext()) {
                src = (ImageSource)var7.next();
                this.mMasks.add(src);
            }

            this.mIsDirty = true;
        }
    }

    public void setReplacePixelColor(@Nullable String replacePixelColor) {
        this.mReplacePixelColor = replacePixelColor == null ? Color.TRANSPARENT : Color.parseColor(replacePixelColor);
    }

    public void setDefaultSource(@Nullable String name) {
        Drawable newDefaultDrawable = ResourceDrawableIdHelper.getInstance().getResourceDrawable(this.getContext(), name);
        if (!Objects.equal(this.mDefaultImageDrawable, newDefaultDrawable)) {
            this.mDefaultImageDrawable = newDefaultDrawable;
            this.mIsDirty = true;
        }

    }

    public void setLoadingIndicatorSource(@Nullable String name) {
        Drawable drawable = ResourceDrawableIdHelper.getInstance().getResourceDrawable(this.getContext(), name);
        Drawable newLoadingIndicatorSource = drawable != null ? new AutoRotateDrawable(drawable, 1000) : null;
        if (!Objects.equal(this.mLoadingImageDrawable, newLoadingIndicatorSource)) {
            this.mLoadingImageDrawable = newLoadingIndicatorSource;
            this.mIsDirty = true;
        }

    }

    public void setProgressiveRenderingEnabled(boolean enabled) {
        this.mProgressiveRenderingEnabled = enabled;
    }

    public void setFadeDuration(int durationMs) {
        this.mFadeDurationMs = durationMs;
    }

    private void getCornerRadii(float[] computedCorners) {
        float defaultBorderRadius = !YogaConstants.isUndefined(this.mBorderRadius) ? this.mBorderRadius : 0.0F;
        computedCorners[0] = this.mBorderCornerRadii != null && !YogaConstants.isUndefined(this.mBorderCornerRadii[0]) ? this.mBorderCornerRadii[0] : defaultBorderRadius;
        computedCorners[1] = this.mBorderCornerRadii != null && !YogaConstants.isUndefined(this.mBorderCornerRadii[1]) ? this.mBorderCornerRadii[1] : defaultBorderRadius;
        computedCorners[2] = this.mBorderCornerRadii != null && !YogaConstants.isUndefined(this.mBorderCornerRadii[2]) ? this.mBorderCornerRadii[2] : defaultBorderRadius;
        computedCorners[3] = this.mBorderCornerRadii != null && !YogaConstants.isUndefined(this.mBorderCornerRadii[3]) ? this.mBorderCornerRadii[3] : defaultBorderRadius;
    }

    public void setHeaders(ReadableMap headers) {
        this.mHeaders = headers;
    }

    public void maybeUpdateView() {
        if (this.mIsDirty) {
            if (!this.hasMultipleSources() || this.getWidth() > 0 && this.getHeight() > 0) {
                this.setMaskImage();
                this.setSourceImage();
                if (this.mImageSource != null) {
                    boolean doResize = this.shouldResize(this.mImageSource);
                    if (!doResize || this.getWidth() > 0 && this.getHeight() > 0) {
                        if (!this.isTiled() || this.getWidth() > 0 && this.getHeight() > 0) {
                            GenericDraweeHierarchy hierarchy = (GenericDraweeHierarchy)this.getHierarchy();
                            hierarchy.setActualImageScaleType(this.mScaleType);
                            if (this.mDefaultImageDrawable != null) {
                                hierarchy.setPlaceholderImage(this.mDefaultImageDrawable, this.mScaleType);
                            }

                            if (this.mLoadingImageDrawable != null) {
                                hierarchy.setPlaceholderImage(this.mLoadingImageDrawable, ScalingUtils.ScaleType.CENTER);
                            }

                            this.getCornerRadii(sComputedCornerRadii);
                            RoundingParams roundingParams = hierarchy.getRoundingParams();
                            roundingParams.setCornersRadii(sComputedCornerRadii[0], sComputedCornerRadii[1], sComputedCornerRadii[2], sComputedCornerRadii[3]);
                            if (this.mBackgroundImageDrawable != null) {
                                this.mBackgroundImageDrawable.setBorder(this.mBorderColor, this.mBorderWidth);
                                this.mBackgroundImageDrawable.setRadii(roundingParams.getCornersRadii());
                                hierarchy.setBackgroundImage(this.mBackgroundImageDrawable);
                            }

                            roundingParams.setBorder(this.mBorderColor, this.mBorderWidth);
                            if (this.mOverlayColor != 0) {
                                roundingParams.setOverlayColor(this.mOverlayColor);
                            } else {
                                roundingParams.setRoundingMethod(RoundingMethod.BITMAP_ONLY);
                            }

                            hierarchy.setRoundingParams(roundingParams);
                            hierarchy.setFadeDuration(this.mFadeDurationMs >= 0 ? this.mFadeDurationMs : (this.mImageSource.isResource() ? 0 : 300));
                            List<Postprocessor> postprocessors = new LinkedList();
                            if (this.mIterativeBoxBlurPostProcessor != null) {
                                postprocessors.add(this.mIterativeBoxBlurPostProcessor);
                            }

                            if (this.mTilePostprocessor != null) {
                                postprocessors.add(this.mTilePostprocessor);
                            }

                            Postprocessor postprocessor = MultiPostprocessor.from(postprocessors);
                            ResizeOptions resizeOptions = doResize ? new ResizeOptions(this.getWidth(), this.getHeight()) : null;
                            ImageRequestBuilder imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(this.mImageSource.getUri()).setPostprocessor(postprocessor).setResizeOptions(resizeOptions).setAutoRotateEnabled(true).setProgressiveRenderingEnabled(this.mProgressiveRenderingEnabled);
                            // https://frescolib.org/docs/modifying-image.html
                            if (this.mImageMaskBitmap != null) {
                                imageRequestBuilder.setPostprocessor(cropImagePostprocessor);
                            }
                            ImageRequest imageRequest = ReactNetworkImageRequest.fromBuilderWithHeaders(imageRequestBuilder, this.mHeaders);
                            if (this.mGlobalImageLoadListener != null) {
                                this.mGlobalImageLoadListener.onLoadAttempt(this.mImageSource.getUri());
                            }

                            this.mDraweeControllerBuilder.reset();
                            this.mDraweeControllerBuilder.setAutoPlayAnimations(true).setCallerContext(this.mCallerContext).setOldController(this.getController()).setImageRequest(imageRequest);
                            if (this.mCachedImageSource != null) {
                                ImageRequest cachedImageRequest = ImageRequestBuilder.newBuilderWithSource(this.mCachedImageSource.getUri()).setPostprocessor(postprocessor).setResizeOptions(resizeOptions).setAutoRotateEnabled(true).setProgressiveRenderingEnabled(this.mProgressiveRenderingEnabled).build();
                                this.mDraweeControllerBuilder.setLowResImageRequest(cachedImageRequest);
                            }

                            if (this.mDownloadListener != null && this.mControllerForTesting != null) {
                                ForwardingControllerListener combinedListener = new ForwardingControllerListener();
                                combinedListener.addListener(this.mDownloadListener);
                                combinedListener.addListener(this.mControllerForTesting);
                                this.mDraweeControllerBuilder.setControllerListener(combinedListener);
                            } else if (this.mControllerForTesting != null) {
                                this.mDraweeControllerBuilder.setControllerListener(this.mControllerForTesting);
                            } else if (this.mDownloadListener != null) {
                                this.mDraweeControllerBuilder.setControllerListener(this.mDownloadListener);
                            }

                            if (this.mDownloadListener != null) {
                                hierarchy.setProgressBarImage(this.mDownloadListener);
                            }

                            this.setController(this.mDraweeControllerBuilder.build());
                            this.mIsDirty = false;
                            this.mDraweeControllerBuilder.reset();
                        }
                    }
                }
            }
        }
    }

    public void setControllerListener(ControllerListener controllerListener) {
        this.mControllerForTesting = controllerListener;
        this.mIsDirty = true;
        this.maybeUpdateView();
    }

    @Nullable
    public ImageSource getImageSource() {
        return this.mImageSource;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            this.mIsDirty = this.mIsDirty || this.hasMultipleSources() || this.isTiled();
            this.maybeUpdateView();
        }

    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    private boolean hasMultipleSources() {
        return this.mSources.size() > 1;
    }

    private boolean hasMultipleMasks() {
        return this.mMasks.size() > 1;
    }

    private boolean isTiled() {
        return this.mTileMode != TileMode.CLAMP;
    }

    private void setSourceImage() {
        this.mImageSource = null;
        if (this.mSources.isEmpty()) {
            this.mSources.add(ImageSource.getTransparentBitmapImageSource(this.getContext()));
        } else if (this.hasMultipleSources()) {
            MultiSourceHelper.MultiSourceResult multiSource = MultiSourceHelper.getBestSourceForSize(this.getWidth(), this.getHeight(), this.mSources);
            this.mImageSource = multiSource.getBestResult();
            this.mCachedImageSource = multiSource.getBestResultInCache();
            return;
        }

        this.mImageSource = (ImageSource)this.mSources.get(0);
    }

    private void setMaskImage() {
        this.mImageMask = null;
        if (this.mMasks.isEmpty() || this.hasMultipleMasks()) return;

        this.mImageMask = (ImageSource)this.mMasks.get(0);

        if (this.mImageMaskBitmap != null) {
            releaseMaskBitmap();
        }

        try {
            this.mImageMaskBitmap = CropImageCore.getBitmapByUri(this.getContext(), this.mImageMask.getUri());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void releaseMaskBitmap() {
        if (this.mImageMaskBitmap != null && !this.mImageMaskBitmap.isRecycled()) {
            this.mImageMaskBitmap.recycle();
        }
        this.mImageMaskBitmap = null;
    }

    private boolean shouldResize(ImageSource imageSource) {
        if (this.mResizeMethod != ImageResizeMethod.AUTO) {
            return this.mResizeMethod == ImageResizeMethod.RESIZE;
        } else {
            return UriUtil.isLocalContentUri(imageSource.getUri()) || UriUtil.isLocalFileUri(imageSource.getUri());
        }
    }

    private void warnImageSource(String uri) {
        if (ReactBuildConfig.DEBUG) {
            RNLog.w((ReactContext)this.getContext(), "CropImageView: Image source \"" + uri + "\" doesn't exist");
        }

    }

    private void warnImageMask(String uri) {
        if (ReactBuildConfig.DEBUG) {
            RNLog.w((ReactContext)this.getContext(), "CropImageView: Image mask \"" + uri + "\" doesn't exist");
        }

    }

    private boolean validMaskSize() {
        if (this.mImageSource == null || this.mImageMask == null) return false;
        // fixme: size = width * height, it should be judged whether width and height are equal
        return mImageSource.getSize() == mImageMask.getSize();
    }

    private class TilePostprocessor extends BasePostprocessor {
        private TilePostprocessor() {
        }

        public CloseableReference<Bitmap> process(Bitmap source, PlatformBitmapFactory bitmapFactory) {
            Rect destRect = new Rect(0, 0, CropImageView.this.getWidth(), CropImageView.this.getHeight());
            CropImageView.this.mScaleType.getTransform(CropImageView.sTileMatrix, destRect, source.getWidth(), source.getHeight(), 0.0F, 0.0F);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            Shader shader = new BitmapShader(source, CropImageView.this.mTileMode, CropImageView.this.mTileMode);
            shader.setLocalMatrix(CropImageView.sTileMatrix);
            paint.setShader(shader);
            CloseableReference<Bitmap> output = bitmapFactory.createBitmap(CropImageView.this.getWidth(), CropImageView.this.getHeight());

            CloseableReference var8;
            try {
                Canvas canvas = new Canvas((Bitmap)output.get());
                canvas.drawRect(destRect, paint);
                var8 = output.clone();
            } finally {
                CloseableReference.closeSafely(output);
            }

            return var8;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        releaseMaskBitmap();
        super.onDetachedFromWindow();
    }
}
