package com.mimi.xichelapp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.RequiresApi;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by: dan
 * Created time: 2020-04-01
 * Description: bitmap 处理器 （压缩）
 * Modify time:
 */
public class BitmapProcessor {

    private Builder mBuilder;
    private Future<Bitmap> mTask;

    private BitmapProcessor(Builder builder) {
        this.mBuilder = builder;
    }

    public void process(final Bitmap bitmap) {
        //1. BitmapFactory.decode..
        //2. Bitmap.create..
        //3. bitmap.compress..
        if (mBuilder == null) return;
        if (bitmap == null || bitmap.isRecycled()) {
            throw new IllegalArgumentException("bitmap can't be null or recycled");
        }

        mTask = Executors.newSingleThreadExecutor().submit(new Callable<Bitmap>() {
            @Override
            public Bitmap call() {
                return execute(bitmap);
            }
        });

        try {
            while (!mTask.isDone() && !mTask.isCancelled() && mBuilder.listener != null) {
                if (mTask.isDone()) {
                    Bitmap result = mTask.get();
                    mBuilder.listener.onFinish(result);
                    return;
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            if (mBuilder.listener != null) {
                mBuilder.listener.onFinish(bitmap);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        if (mTask != null && !mTask.isCancelled() && !mTask.isDone()) {
            mTask.cancel(true);
        }
    }

    private Bitmap execute(Bitmap bitmap) {

        String pictureSuffix = getFormat(mBuilder.format);
        String mimiDir = FileUtil.getMimiDir();
        String cacheFile = mimiDir + "compressScanPicture" + pictureSuffix;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(cacheFile);
            bitmap.compress(mBuilder.format, mBuilder.quality, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        if (mBuilder.isSampleSize) {
            options.inSampleSize = mBuilder.sampleSize;
        }
        options.inPreferredConfig = mBuilder.config;
        Bitmap result = BitmapFactory.decodeFile(cacheFile, options);

        if (result != null) {
            if (mBuilder.isCropSize) {
                result = Bitmap.createScaledBitmap(result, mBuilder.width, mBuilder.height, true);
            }
            if (mBuilder.isScale) {
                Matrix matrix = new Matrix();
                matrix.setScale(mBuilder.xScale, mBuilder.yScale);
                result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), bitmap.getHeight(), matrix, true);
            }
        }
        FileUtil.removeFile(cacheFile);
        return result;
    }

    private String getFormat(Bitmap.CompressFormat format) {
        if (format == Bitmap.CompressFormat.JPEG) {
            return ".jpg";
        } else if (format == Bitmap.CompressFormat.PNG) {
            return ".png";
        } else if (format == Bitmap.CompressFormat.WEBP) {
            return ".webp";
        }
        return "";
    }

    public static class Builder {
        private int quality = 100;

        private int sampleSize;
        private boolean isSampleSize;
        private int width, height;
        private boolean isCropSize;
        private Bitmap.Config config = Bitmap.Config.RGB_565;
        private float xScale, yScale;
        private boolean isScale;
        private ExecutorListener listener;
        private Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;

        public CompressBuilder withCompress() {
            return new CompressBuilder(this);
        }

        public ConfigBuilder withConfig() {
            return new ConfigBuilder(this);
        }

        public ScaleBuilder withScale() {
            isScale = true;
            return new ScaleBuilder(this);
        }

        public SampleBuilder withSampleSize() {
            this.isSampleSize = true;
            return new SampleBuilder(this);
        }

        public SizeBuilder withSize() {
            this.isCropSize = true;
            return new SizeBuilder(this);
        }

        private void setQuality(int quality) {
            this.quality = quality;
        }

        private void setBitmapConfig(Bitmap.Config config) {
            this.config = config;
        }

        private void setScale(float xScale, float yScale) {
            this.xScale = xScale;
            this.yScale = yScale;
        }

        private void setSampleSize(int sampleSize) {
            this.sampleSize = sampleSize;
        }

        private void setSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public Builder setFormat(Bitmap.CompressFormat format) {
            this.format = format;
            return this;
        }

        public Builder setListener(ExecutorListener listener) {
            this.listener = listener;
            return this;
        }

        public BitmapProcessor build() {
            return new BitmapProcessor(this);
        }
    }

    public static class SizeBuilder {
        private Builder builder;

        private SizeBuilder(Builder builder) {
            this.builder = builder;
        }

        public Builder setSize(int width, int height) {
            builder.setSize(width, height);
            return builder;
        }
    }

    public static class SampleBuilder {
        private Builder builder;

        private SampleBuilder(Builder builder) {
            this.builder = builder;
        }

        public Builder setSampleSize(@IntRange(from = 0) int sampleSize) {
            builder.setSampleSize(sampleSize);
            return builder;
        }
    }

    public static class ScaleBuilder {
        private Builder builder;

        private ScaleBuilder(Builder builder) {
            this.builder = builder;
        }

        public Builder setScale(@FloatRange(from = 0) float xScale, @FloatRange(from = 0) float yScale) {
            builder.setScale(xScale, yScale);
            return builder;
        }
    }

    public static class ConfigBuilder {
        private Builder builder;

        private ConfigBuilder(Builder builder) {
            this.builder = builder;
        }

        public Builder setBitmapConfig(Bitmap.Config config) {
            builder.setBitmapConfig(config);
            return builder;
        }
    }

    public static class CompressBuilder {
        private Builder builder;

        private CompressBuilder(Builder builder) {
            this.builder = builder;
        }

        public Builder setQuality(@IntRange(from = 0, to = 100) int quality) {
            builder.setQuality(quality);
            return builder;
        }
    }


    public interface ExecutorListener {
        void onFinish(Bitmap bitmap);
    }
}
