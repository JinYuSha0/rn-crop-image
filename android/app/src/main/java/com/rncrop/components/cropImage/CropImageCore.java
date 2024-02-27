package com.rncrop.components.cropImage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.CountDownLatch;

public class CropImageCore {
    private static final String CROP_IMG_DIR_NAME = "CropedImgTemp";

    public static Bitmap crop(Bitmap origin, Bitmap mask, int replacePixelColor) {
        int width = origin.getWidth();
        int height = origin.getHeight();
        int newPixel = replacePixelColor;
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
                    result.value = Bitmap.createBitmap(bitmap);
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

    public static String saveBitmapToImg(Context context, Bitmap bitmap, String filename) {
        File cropImgDir = context.getDir(CROP_IMG_DIR_NAME, Context.MODE_PRIVATE);
        if (!cropImgDir.exists()) {
            cropImgDir.mkdirs();
        }
        File imageFile = new File(cropImgDir, filename);

        Bitmap transparentBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(transparentBitmap);
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            transparentBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeIO(fos);
        }
        return null;
    }

    public static String resolveImgPath(Context context, String imgName) {
        File cropImgDir = context.getDir(CROP_IMG_DIR_NAME, Context.MODE_PRIVATE);
        File imageFile = new File(cropImgDir, imgName);
        return imageFile.getAbsolutePath();
    }

    public static Boolean judgeImgExists(String filepath) {
        File imageFile = new File(filepath);
        return imageFile.exists();
    }

    public static void clearCropImgTemp(Context context) {
        File cropImgDir = context.getDir(CROP_IMG_DIR_NAME, Context.MODE_PRIVATE);
        deleteDirectory(cropImgDir);
    }

    private static boolean deleteDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return false;
        }
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        return directory.delete();
    }

    public static void closeIO(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static class ValueHolder<T> {
        @Nullable
        public T value = null;
    }
}
