package com.rncrop.components.cropImage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.Closeable;
import java.util.concurrent.CountDownLatch;

public class CropImageCore {
    public static Bitmap crop(Bitmap origin, Bitmap mask) {
        int width = origin.getWidth();
        int height = origin.getHeight();
        int newPixel = Color.TRANSPARENT;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int maskPixel = mask.getPixel(x, y);
                if (maskPixel == Color.BLACK) {
                    origin.setPixel(x, y, newPixel);
                }
            }
        }
        return origin;
    }

    public static Bitmap getBitmapByUri(Context context, Uri uri) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final ValueHolder<Bitmap> result = new ValueHolder<>();

        ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(uri)
                .setProgressiveRenderingEnabled(true)
                .build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource =
                imagePipeline.fetchDecodedImage(imageRequest, context);

        dataSource.subscribe(new BaseBitmapDataSubscriber() {
            @Override
            public void onNewResultImpl(@Nullable Bitmap bitmap) {
                if (dataSource.isFinished() && bitmap != null){
                    result.value = bitmap;
                    dataSource.close();
                    latch.countDown();
                }
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
                if (dataSource != null) {
                    dataSource.close();
                    latch.countDown();
                }
            }
        }, CallerThreadExecutor.getInstance());

        latch.await();

        return result.value;
    }

    public static void closeIO(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Exception exception) {
            exception.printStackTrace();;
        }
    }

    private static class ValueHolder<T> {
        @Nullable
        public T value = null;
    }
}
