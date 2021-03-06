package com.xmx.mygallery.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;

public class GifImageLoader {

    class Image {
        Bitmap bitmap;
        Movie movie;
        GifDecoder gif;
    }

    private class ImgBeanHolder {
        GifImageView imageView;
        String path;
        Image image;
    }

    static final int GIF_DECODER = 1;
    static final int MOVIE = 2;
    int loaderType = MOVIE;

    /**
     * 图片缓存的核心类
     */
    private LruCache<String, Image> mLruCache;
    /**
     * 线程池
     */
    private ExecutorService mThreadPool;
    /**
     * 线程池的线程数量，默认为1
     */
    private int mThreadCount = 1;
    /**
     * 队列的调度方式
     */
    private Type mType = Type.LIFO;
    /**
     * 任务队列
     */
    private LinkedList<Runnable> mTasks;
    /**
     * 轮询的线程
     */
    private Thread mPoolThread;
    private Handler mPoolThreadHander;

    /**
     * 运行在UI线程的handler，用于给ImageView设置图片
     */
    private LoadImageHandler mHandler;

    /**
     * 引入一个值为1的信号量，防止mPoolThreadHander未初始化完成
     */
    private volatile Semaphore mSemaphore = new Semaphore(0);

    /**
     * 引入一个值为1的信号量，由于线程池内部也有一个阻塞线程，防止加入任务的速度过快，使LIFO效果不明显
     */
    private volatile Semaphore mPoolSemaphore;

    private static GifImageLoader mInstance;

    /**
     * 队列的调度方式
     *
     * @author zhy
     */
    public enum Type {
        FIFO, LIFO
    }

    private GifImageLoader(int threadCount, Type type) {
        init(threadCount, type);
    }

    /**
     * 单例获得该实例对象
     *
     * @return
     */
    public static GifImageLoader getInstance() {
        if (mInstance == null) {
            synchronized (GifImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new GifImageLoader(1, Type.LIFO);
                }
            }
        }
        return mInstance;
    }

    /**
     * 单例获得该实例对象
     *
     * @return
     */
    public static GifImageLoader getInstance(int threadCount, Type type) {
        if (mInstance == null) {
            synchronized (GifImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new GifImageLoader(threadCount, type);
                }
            }
        }
        return mInstance;
    }

    private void init(int threadCount, Type type) {
        // loop thread
        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();

                mPoolThreadHander = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        mThreadPool.execute(getTask());
                        try {
                            mPoolSemaphore.acquire();
                        } catch (InterruptedException e) {
                        }
                    }
                };
                // 释放一个信号量
                mSemaphore.release();
                Looper.loop();
            }
        };
        mPoolThread.start();

        // 获取应用程序最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 3;
        mLruCache = new LruCache<String, Image>(cacheSize) {
            @Override
            protected int sizeOf(String key, Image value) {
                if (value.bitmap != null) {
                    return value.bitmap.getRowBytes() * value.bitmap.getHeight();
                } else if (value.gif != null) {
                    try {
                        File f = new File(key);
                        FileInputStream fis = new FileInputStream(f);
                        return fis.available();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return 0;
            }
        };

        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mPoolSemaphore = new Semaphore(threadCount);
        mTasks = new LinkedList<>();
        mType = type == null ? Type.LIFO : type;

    }

    static class LoadImageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
            GifImageView imageView = holder.imageView;
            String path = holder.path;
            Image im = holder.image;
            if (imageView.getTag().toString().equals(path)) {
                if (im.bitmap != null) {
                    imageView.setImageBitmap(im.bitmap);
                } else if (im.gif != null) {
                    imageView.setImageGif(im.gif);
                } else if (im.movie != null) {
                    imageView.setImageMovie(im.movie);
                }
            }
        }
    }

    private String getPath(String s) {
        if (!s.endsWith("#")) {
            return s;
        } else {
            return s.substring(0, s.length() - 1);
        }
    }

    /**
     * 加载图片
     *
     * @param path
     * @param imageView
     */
    public void loadImage(final String path, final GifImageView imageView, final int loaderType) {
        // set tag
        imageView.setTag(path);
        // UI线程
        if (mHandler == null) {
            mHandler = new LoadImageHandler();
        }

        Image im = getImageFromLruCache(path);
        boolean existFlag = false;
        if (im != null) {
            if (im.bitmap != null) {
                existFlag = true;
            }
            else {
                switch (loaderType) {
                    case MOVIE:
                        if (im.movie != null) {
                            existFlag = true;
                        }
                        break;

                    case GIF_DECODER:
                        if (im.gif != null) {
                            existFlag = true;
                        }
                        break;
                }
            }
        }

        if (existFlag) {
            ImgBeanHolder holder = new ImgBeanHolder();
            holder.image = im;
            holder.imageView = imageView;
            holder.path = path;
            Message message = Message.obtain();
            message.obj = holder;
            mHandler.sendMessage(message);
        } else {
            addTask(new Runnable() {
                @Override
                public void run() {
                    Image im = new Image();
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(getPath(path), opts);
                    if (opts.outMimeType != null && opts.outMimeType.equals("image/gif")) {
                        switch (loaderType) {
                            case MOVIE:
                                Movie movie = null;
                                try {
                                    movie = Movie.decodeStream(new FileInputStream(getPath(path)));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }

                                im.bitmap = null;
                                im.gif = null;
                                im.movie = movie;
                                break;

                            case GIF_DECODER:
                                GifDecoder gif = new GifDecoder();
                                try {
                                    File file = new File(getPath(path));
                                    byte[] bytes = new byte[(int) file.length()];
                                    InputStream inputStream = new FileInputStream(file);
                                    inputStream.read(bytes);
                                    gif.read(bytes);
                                    im.bitmap = null;
                                    im.movie = null;
                                    im.gif = gif;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    } else {
                        im.gif = null;
                        im.movie = null;
                        //im.bitmap = BitmapFactory.decodeFile(path);
                        ImageSize imageSize = getImageViewWidth(imageView);
                        int reqWidth = imageSize.width;
                        int reqHeight = imageSize.height;

                        im.bitmap = decodeSampledBitmapFromResource(getPath(path), reqWidth, reqHeight);
                    }

                    addImageToLruCache(path, im);
                    ImgBeanHolder holder = new ImgBeanHolder();
                    holder.image = getImageFromLruCache(path);
                    holder.imageView = imageView;
                    holder.path = path;
                    Message message = Message.obtain();
                    message.obj = holder;
                    mHandler.sendMessage(message);
                    mPoolSemaphore.release();
                }
            });
        }
    }

    /**
     * 添加一个任务
     *
     * @param runnable
     */
    private synchronized void addTask(Runnable runnable) {
        try {
            // 请求信号量，防止mPoolThreadHander为null
            if (mPoolThreadHander == null)
                mSemaphore.acquire();
        } catch (InterruptedException e) {
        }
        mTasks.add(runnable);

        mPoolThreadHander.sendEmptyMessage(0x110);
    }

    /**
     * 取出一个任务
     *
     * @return
     */
    private synchronized Runnable getTask() {
        if (mType == Type.FIFO) {
            return mTasks.removeFirst();
        } else if (mType == Type.LIFO) {
            return mTasks.removeLast();
        }
        return null;
    }

    /**
     * 从LruCache中获取一张图片，如果不存在就返回null。
     */
    private Image getImageFromLruCache(String key) {
        return mLruCache.get(key);
    }

    /**
     * 往LruCache中添加一张图片
     *
     * @param key
     * @param im
     */
    private void addImageToLruCache(String key, Image im) {
        if (getImageFromLruCache(key) == null) {
            if (im != null)
                mLruCache.put(key, im);
        }
        else {
            if (im != null) {
                mLruCache.remove(key);
                mLruCache.put(key, im);
            }
        }
    }


    private class ImageSize {
        int width;
        int height;
    }

    /**
     * 反射获得ImageView设置的最大宽度和高度
     *
     * @param object
     * @param fieldName
     * @return
     */
    private static int getImageViewFieldValue(Object object, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = (Integer) field.get(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (Exception e) {
        }
        return value;
    }

    /**
     * 根据ImageView获得适当的压缩的宽和高
     *
     * @param imageView
     * @return
     */
    private ImageSize getImageViewWidth(ImageView imageView) {
        ImageSize imageSize = new ImageSize();
        final DisplayMetrics displayMetrics = imageView.getContext()
                .getResources().getDisplayMetrics();
        final ViewGroup.LayoutParams params = imageView.getLayoutParams();

        int width = params.width == ViewGroup.LayoutParams.WRAP_CONTENT ? 0 : imageView
                .getWidth(); // Get actual image width
        if (width <= 0)
            width = params.width; // Get layout width parameter
        if (width <= 0)
            width = getImageViewFieldValue(imageView, "mMaxWidth"); // Check
        // maxWidth
        // parameter
        if (width <= 0)
            width = displayMetrics.widthPixels;
        int height = params.height == ViewGroup.LayoutParams.WRAP_CONTENT ? 0 : imageView
                .getHeight(); // Get actual image height
        if (height <= 0)
            height = params.height; // Get layout height parameter
        if (height <= 0)
            height = getImageViewFieldValue(imageView, "mMaxHeight"); // Check
        // maxHeight
        // parameter
        if (height <= 0)
            height = displayMetrics.heightPixels;
        imageSize.width = width;
        imageSize.height = height;
        return imageSize;

    }

    /**
     * 计算inSampleSize，用于压缩图片
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的宽度
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        if (width > reqWidth && height > reqHeight) {
            // 计算出实际宽度和目标宽度的比率
            int widthRatio = Math.round((float) width / (float) reqWidth);
            int heightRatio = Math.round((float) height / (float) reqHeight);
            inSampleSize = Math.max(widthRatio, heightRatio);
        }
        return inSampleSize;
    }

    /**
     * 根据计算的inSampleSize，得到压缩后图片
     *
     * @param pathName
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap decodeSampledBitmapFromResource(String pathName, int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);

        return bitmap;
    }
}
