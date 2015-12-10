package com.xmx.mygallery.ImageView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class GifImageView extends ImageView {
    static final int DEFAULT_MOVIE_DURATION = 100;
    static final int DEFAULT_MOVIE_FRAME_TIME = 100;

    static final int GIF_DECODER = 1;
    static final int MOVIE = 2;
    int loaderType = MOVIE;

    protected int mFrameTime = 1;
    protected float customScale = 5f;
    protected String mPath;
    protected Movie mMovie;
    protected GifDecoder mGif;
    protected Bitmap mBitmap;
    protected long mMovieStart;
    protected int mCurrentAnimationTime = 0;
    protected long mOffset = 0;
    protected int mDuration;
    protected long mLatestTime;
    protected float mLeft;
    protected float mTop;
    protected float mScale;

    static final int PLAY = 0;
    static final int PAUSE = 1;
    static final int UPEND = -1;
    protected int mStatus = PLAY;

    public GifImageView(Context context) {
        this(context, null);
    }

    public GifImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void setImageMovie(Movie movie) {
        if (movie != null) {
            mMovie = movie;
            mGif = null;
            mBitmap = null;

            mDuration = mMovie.duration();
            if (mDuration == 0) {
                mDuration = DEFAULT_MOVIE_DURATION;
            }
            mLatestTime = android.os.SystemClock.uptimeMillis();
            setupMovie();
        }
    }

    public void setImageGif(GifDecoder gif) {
        if (gif != null) {
            mGif = gif;
            mMovie = null;
            mBitmap = null;

            //mDuration = mMovie.duration();
            mDuration = 0;
            for (int i = 0; i < mGif.getFrameCount(); ++i) {
                mDuration += mGif.getDelay(i);
            }
            mFrameTime = mDuration / mGif.getFrameCount();
            if (mFrameTime == 0) {
                mFrameTime = DEFAULT_MOVIE_FRAME_TIME;
                mDuration = mFrameTime * mGif.getFrameCount();
            }
            mLatestTime = android.os.SystemClock.uptimeMillis();
            setupMovie();
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (bm != null) {
            mBitmap = bm;
            mGif = null;
            mMovie = null;
            setupBitmap();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mGif = null;
        mMovie = null;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mGif = null;
        mMovie = null;
    }

    public boolean setImageByPath(String path) {
        mPath = path;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        if (opts.outMimeType != null && opts.outMimeType.equals("image/gif")) {
            switch (loaderType) {
                case MOVIE:
                    Movie movie = Movie.decodeFile(path);
                    setImageMovie(movie);
                    break;

                case GIF_DECODER:
                    GifDecoder gif = new GifDecoder();
                    try {
                        File file = new File(mPath);
                        byte[] bytes = new byte[(int) file.length()];
                        InputStream inputStream = new FileInputStream(file);
                        inputStream.read(bytes);
                        gif.read(bytes);
                        setImageGif(gif);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                break;
            }
            return true;
        } else {
            setImageBitmap(BitmapFactory.decodeFile(path));
            return false;
        }
    }

    public boolean setImageByPathLoader(String path) {
        return setImageByPathLoader(path, GifImageLoader.Type.LIFO);
    }

    public boolean setImageByPathLoader(String path, GifImageLoader.Type type) {
        GifImageLoader.getInstance(5, type).loadImage(path, this, loaderType);
        mPath = path;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        return opts.outMimeType != null && opts.outMimeType.equals("image/gif");
    }

    public String getPath() {
        return mPath;
    }

    private int resolveActualSize(int desiredSize, int measureSpec) {
        int result;
        int maxSize = MeasureSpec.getSize(measureSpec);
        int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case MeasureSpec.EXACTLY:
                result = maxSize;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(desiredSize, maxSize);
                break;
            case MeasureSpec.UNSPECIFIED:
                result = desiredSize;
                break;
            default:
                result = 1;
                break;
        }
        return result;
    }

    private int resolveAdjustedSize(int desiredSize, int maxSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = Math.min(desiredSize, maxSize);
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(Math.min(desiredSize, specSize), maxSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    public static int resolveSizeAndState(int size, int measureSpec, int childMeasuredState) {
        final int specMode = MeasureSpec.getMode(measureSpec);
        final int specSize = MeasureSpec.getSize(measureSpec);
        final int result;
        switch (specMode) {
            case MeasureSpec.AT_MOST:
                if (specSize < size) {
                    result = specSize | MEASURED_STATE_TOO_SMALL;
                } else {
                    result = size;
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                result = size;
        }
        return result | (childMeasuredState & MEASURED_STATE_MASK);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mGif != null || mMovie != null) {
            int movieWidth = 1;
            int movieHeight = 1;
            if (mGif != null) {
                movieWidth = mGif.width();
                movieHeight = mGif.height();
            } else {
                movieWidth = mMovie.width();
                movieHeight = mMovie.height();
            }
            int defaultWidth = MeasureSpec.getSize(widthMeasureSpec);
            int defaultHeight = MeasureSpec.getSize(heightMeasureSpec);

            boolean widthFlag = ((float) movieWidth / defaultWidth) > ((float) movieHeight / defaultHeight);

            int actualWidth = resolveActualSize((int) (movieWidth * customScale), widthMeasureSpec);
            int actualHeight = resolveActualSize((int) (movieHeight * customScale), heightMeasureSpec);

            if (widthFlag) {
                mScale = (float) actualWidth / (float) movieWidth;
            } else {
                mScale = (float) actualHeight / (float) movieHeight;
            }

            int w = (int) (movieWidth * mScale);
            int h = (int) (movieHeight * mScale);

            boolean resizeWidth = MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY;
            boolean resizeHeight = MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
            float desiredAspect = (float) w / (float) h;

            int widthSize;
            int heightSize;

            if (resizeWidth || resizeHeight) {
                widthSize = resolveAdjustedSize(w, Integer.MAX_VALUE, widthMeasureSpec);
                heightSize = resolveAdjustedSize(h, Integer.MAX_VALUE, heightMeasureSpec);

                if (desiredAspect != 0.0f) {
                    float actualAspect = (float) (widthSize) / (heightSize);
                    if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {
                        boolean done = false;
                        if (resizeWidth) {
                            int newWidth = (int) (desiredAspect * (heightSize));
                            if (!resizeHeight) {
                                widthSize = resolveAdjustedSize(newWidth, Integer.MAX_VALUE, widthMeasureSpec);
                            }
                            if (newWidth <= widthSize) {
                                widthSize = newWidth;
                                done = true;
                            }
                        }
                        if (!done && resizeHeight) {
                            int newHeight = (int) ((widthSize) / desiredAspect);
                            if (!resizeWidth) {
                                heightSize = resolveAdjustedSize(newHeight, Integer.MAX_VALUE, heightMeasureSpec);
                            }
                            if (newHeight <= heightSize) {
                                heightSize = newHeight;
                            }
                        }
                    }
                }
            } else {
                w = Math.max(w, getSuggestedMinimumWidth());
                h = Math.max(h, getSuggestedMinimumHeight());

                widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
                heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);
            }

            mLeft = (widthSize - w) / 2f;
            mTop = (heightSize - h) / 2f;
            setMeasuredDimension(widthSize, heightSize);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mGif != null || mMovie != null) {
            updateAnimationTime();
            drawMovieFrame(canvas);
            invalidateView();
        } else {
            super.onDraw(canvas);
        }
    }

    @SuppressLint("NewApi")
    private void invalidateView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            postInvalidateOnAnimation();
        } else {
            invalidate();
        }
    }

    private void updateAnimationTime() {
        if (mGif != null || mMovie != null) {
            long now = android.os.SystemClock.uptimeMillis();
            // 如果第一帧，记录起始时间
            if (mMovieStart == 0) {
                mMovieStart = now;
            }

            switch (mStatus) {
                case PAUSE:
                    mOffset += now - mLatestTime;
                    break;

                case UPEND:
                    mOffset += (now - mLatestTime) * 2;
                    break;

                default:
                    break;
            }
            mLatestTime = now;
            long time = now - mMovieStart - mOffset;
            mCurrentAnimationTime = (int) (time % mDuration);
            if (time < 0) {
                mCurrentAnimationTime += mDuration;
            }
        }
    }

    private void drawMovieFrame(Canvas canvas) {
        if (mGif != null) {
            // 设置要显示的帧，绘制即可
            int index = mCurrentAnimationTime / mFrameTime;
            int current = mGif.getCurrentFrameIndex();
            if (current != index) {
                if (current > index) {
                    index += mGif.getFrameCount();
                }
                for (int i = current; i < index; ++i) {
                    mGif.advance();
                }
            }
            Bitmap b = mGif.getNextFrame();
            //mMovie.setTime(mCurrentAnimationTime);
            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            canvas.scale(mScale, mScale);
            canvas.drawBitmap(b, mLeft / mScale, mTop / mScale, null);
            //mMovie.draw(canvas, mLeft / mScale, mTop / mScale);
            canvas.restore();
        }
        if (mMovie != null) {
            mMovie.setTime(mCurrentAnimationTime);
            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            canvas.scale(mScale, mScale);
            mMovie.draw(canvas, mLeft / mScale, mTop / mScale);
            canvas.restore();
        }
    }

    protected void setupMovie() {
        requestLayout();
        postInvalidate();
    }

    protected void setupBitmap() {
        requestLayout();
        postInvalidate();
    }
}