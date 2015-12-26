package com.xmx.mygallery.ImageView;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class BigGifImageView extends GifImageView {
    PointF prev = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    float rotation;
    float oldRotation = 0;
    float oldScale = 1f;
    Matrix sourceMatrix = new Matrix();
    float sourceScale;
    Matrix matrix = new Matrix();
    Matrix tempMatrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    float startOffsetX;
    float startOffsetY;

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;
    int widthScreen;
    int heightScreen;

    boolean unlimitedFlag = false;
    boolean translatedFlag = false;

    long startTime;

    float DRAG_SENSITIVITY;
    float ZOOM_SENSITIVITY;
    float SWIPE_SPEED;
    float SWIPE_SENSITIVITY;

    public BigGifImageView(Context context) {
        this(context, null);
    }

    public BigGifImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BigGifImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean setImageByPathLoader(String path) {
        return setImageByPathLoader(path, GifImageLoader.Type.LIFO);
    }

    public boolean setImageByPathLoader(String path, GifImageLoader.Type type) {
        GifImageLoader.getInstance(3, type).loadImage(path + "#", this, loaderType);
        mPath = path;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        return opts.outMimeType != null && opts.outMimeType.equals("image/gif");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mGif != null || mMovie != null) {
            int movieWidth;
            int movieHeight;
            if (mGif != null) {
                movieWidth = mGif.width();
                movieHeight = mGif.height();
            } else {
                movieWidth = mMovie.width();
                movieHeight = mMovie.height();
            }
            int defaultWidth = MeasureSpec.getSize(widthMeasureSpec);
            int defaultHeight = MeasureSpec.getSize(heightMeasureSpec);
            int width = (int) (movieWidth * customScale);

            /*if (width > defaultWidth) {
                width = defaultWidth;
                customScale = (float) width / (float) movieWidth;
            }*/
            mScale = (float) width / (float) movieWidth;
            /*if (movieHeight * mScale > defaultHeight) {
                mScale = defaultHeight / movieHeight;
                customScale = mScale;
            }*/

            mLeft = (defaultWidth - movieWidth * mScale) / 2f;
            mTop = (defaultHeight - movieHeight * mScale) / 2f;
            setMeasuredDimension(defaultWidth, defaultHeight);
        }
    }

    public class MovieTouchListener implements View.OnTouchListener {
        private final GestureDetector gestureDetector =
                new GestureDetector(getContext(), new GestureListener());

        MovieTouchListener() {
            gestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
                @Override
                public boolean onDoubleTapEvent(MotionEvent e) {
                    return false;
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    return false;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (unlimitedFlag) {
                        customScale = sourceScale;
                        mOffsetX = 0;
                        mOffsetY = 0;
                        requestLayout();
                        postInvalidate();
                    } else {
                        if (translatedFlag) {
                            customScale = sourceScale;
                            mOffsetX = 0;
                            mOffsetY = 0;
                            requestLayout();
                            postInvalidate();
                            translatedFlag = false;
                        } else {
                            DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
                            widthScreen = dm.widthPixels;
                            heightScreen = dm.heightPixels;

                            int movieWidth;
                            int movieHeight;
                            if (mGif != null) {
                                movieWidth = mGif.width();
                                movieHeight = mGif.height();
                            } else {
                                movieWidth = mMovie.width();
                                movieHeight = mMovie.height();
                            }
                            float s1 = (float) widthScreen / (float) movieWidth;
                            float s2 = (float) heightScreen / (float) movieHeight;
                            customScale = (s1 < s2) ? s1 : s2;
                            mOffsetX = 0;
                            mOffsetY = 0;
                            requestLayout();
                            postInvalidate();
                            translatedFlag = true;
                        }
                    }
                    return false;
                }
            });
        }

        public boolean onTouch(final View v, final MotionEvent event) {
            return onTouch(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return false;
            }
        }

        private boolean onTouch(MotionEvent event) {
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            } else {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    // 主点按下
                    case MotionEvent.ACTION_DOWN:
                        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
                        widthScreen = dm.widthPixels;
                        heightScreen = dm.heightPixels;
                        float param = widthScreen / 1000f;
                        DRAG_SENSITIVITY = 32 * param;
                        ZOOM_SENSITIVITY = 32 * param;
                        SWIPE_SPEED = 0.5f;
                        SWIPE_SENSITIVITY = 200 * param;

                        mode = DRAG;
                        prev.set(event.getX(), event.getY());
                        startOffsetX = mOffsetX;
                        startOffsetY = mOffsetY;
                        startTime = android.os.SystemClock.uptimeMillis();
                        break;

                    // 副点按下
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mode = ZOOM;
                        oldDist = spacing(event);
                        oldScale = customScale;
                        oldRotation = rotation(event);
                        midPoint(mid, event);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (mode == ZOOM) {
                            float newDist = spacing(event);
                            if (newDist > ZOOM_SENSITIVITY) {
                                PointF newMid = new PointF();
                                midPoint(newMid, event);
                                customScale = oldScale * newDist / oldDist;

                                mOffsetX = startOffsetX + newMid.x - mid.x;
                                mOffsetY = startOffsetY + newMid.y - mid.y;

                                float newRotation = rotation(event);
                                rotation = newRotation - oldRotation;

                                requestLayout();
                                postInvalidate();
                                translatedFlag = true;
                            }
                        } else if (mode == DRAG) {
                            float tx = event.getX() - prev.x;
                            float ty = event.getY() - prev.y;
                            long time = android.os.SystemClock.uptimeMillis();
                            float speed = tx / (time - startTime);

                            if (Math.sqrt(tx * tx + ty * ty) > DRAG_SENSITIVITY) {
                                mOffsetX = startOffsetX + tx;
                                mOffsetY = startOffsetY + ty;
                            }

                            if (Math.abs(tx) < SWIPE_SENSITIVITY) {
                                getParent().requestDisallowInterceptTouchEvent(true);
                            } else if (Math.abs(speed) > SWIPE_SPEED) {
                                getParent().requestDisallowInterceptTouchEvent(false);
                            }
                        }
                        break;

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_UP:
                        mode = NONE;
                        if (!unlimitedFlag) {
                            /*rotation = (rotation + 360) % 360;
                            float[] angle = {0, 90, 180, 270, 360};
                            float f = 0;
                            for (float a : angle) {
                                if (a - 45 < rotation && rotation <= a + 45) {
                                    f = a;
                                }
                            }
                            float r = f - rotation;
                            rotation = f;*/

                            int movieWidth;
                            int movieHeight;
                            if (mGif != null) {
                                movieWidth = mGif.width();
                                movieHeight = mGif.height();
                            } else {
                                movieWidth = mMovie.width();
                                movieHeight = mMovie.height();
                            }
                            float width = movieWidth * customScale;
                            float height = movieHeight * customScale;

                            float deltaWidth = (width-widthScreen) / 2;
                            if (width < widthScreen) {
                                mOffsetX = 0;
                            } else if (deltaWidth - mOffsetX < 0) {
                                mOffsetX = deltaWidth;
                            } else if (deltaWidth + mOffsetX < 0) {
                                mOffsetX = -deltaWidth;
                            }

                            float deltaHeight = (height-heightScreen) / 2;
                            if (height < heightScreen) {
                                mOffsetY = 0;
                            } else if (deltaHeight - mOffsetY < 0) {
                                mOffsetY = deltaHeight;
                            } else if (deltaWidth + mOffsetX < 0) {
                                mOffsetY = -deltaHeight;
                            }
                        }
                        break;
                }
                return true;
            }
        }
    }

    @Override
    protected void setupMovie() {
        requestLayout();
        postInvalidate();

        if (loaderType == MOVIE) {
            GifDecoder gif = new GifDecoder();
            try {
                File file = new File(mPath);
                byte[] bytes = new byte[(int) file.length()];
                InputStream inputStream = new FileInputStream(file);
                inputStream.read(bytes);
                gif.read(bytes);
                mFrameTime = mDuration / gif.getFrameCount();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int movieWidth;
        int movieHeight;
        if (mGif != null) {
            movieWidth = mGif.width();
            movieHeight = mGif.height();
        } else {
            movieWidth = mMovie.width();
            movieHeight = mMovie.height();
        }
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        float widthScale = (float) width / (float) movieWidth;
        float heightScale = (float) height / (float) movieHeight;
        customScale = Math.min(customScale, Math.min(widthScale, heightScale));
        sourceScale = customScale;

        this.setOnTouchListener(new MovieTouchListener());
    }

    public class BitmapTouchListener implements View.OnTouchListener {
        private final GestureDetector gestureDetector =
                new GestureDetector(getContext(), new GestureListener());

        BitmapTouchListener() {
            gestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
                @Override
                public boolean onDoubleTapEvent(MotionEvent e) {
                    return false;
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    return false;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (unlimitedFlag) {
                        matrix.set(sourceMatrix);
                        center(true, true);
                        setImageMatrix(matrix);
                    } else {
                        if (translatedFlag) {
                            matrix.set(sourceMatrix);
                            center(true, true);
                            setImageMatrix(matrix);
                            translatedFlag = false;
                        } else {
                            matrix.set(sourceMatrix);
                            RectF rect = getMatrixRectF();
                            float height = rect.height();
                            float width = rect.width();
                            float sw = widthScreen / width;
                            float sh = heightScreen / height;
                            float scale = sw > sh ? sw : sh;
                            matrix.postScale(scale, scale, e.getX(), e.getY());
                            center(true, true);
                            setImageMatrix(matrix);
                            translatedFlag = true;
                        }
                    }
                    return false;
                }
            });
        }

        public boolean onTouch(final View v, final MotionEvent event) {
            return onTouch(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return false;
            }
        }

        private boolean onTouch(MotionEvent event) {
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            } else {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
                        widthScreen = dm.widthPixels;
                        float param = widthScreen / 1000f;
                        DRAG_SENSITIVITY = 32 * param;
                        ZOOM_SENSITIVITY = 32 * param;
                        SWIPE_SPEED = 0.5f;
                        SWIPE_SENSITIVITY = 200 * param;

                        mode = DRAG;
                        prev.set(event.getX(), event.getY());
                        savedMatrix.set(matrix);
                        startTime = android.os.SystemClock.uptimeMillis();
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        mode = ZOOM;
                        oldDist = spacing(event);
                        oldRotation = rotation(event);
                        savedMatrix.set(matrix);
                        midPoint(mid, event);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (mode == ZOOM) {
                            float newDist = spacing(event);
                            if (newDist > ZOOM_SENSITIVITY) {
                                PointF newMid = new PointF();
                                midPoint(newMid, event);

                                tempMatrix.set(savedMatrix);

                                tempMatrix.postTranslate(newMid.x - mid.x, newMid.y - mid.y);// 平移

                                float newRotation = rotation(event);
                                rotation = newRotation - oldRotation;
                                tempMatrix.postRotate(rotation, newMid.x, newMid.y);// 旋轉

                                float scale = newDist / oldDist;
                                tempMatrix.postScale(scale, scale, newMid.x, newMid.y);// 縮放
                                matrix.set(tempMatrix);
                                translatedFlag = true;
                            }
                        } else if (mode == DRAG) {
                            float tx = event.getX() - prev.x;
                            float ty = event.getY() - prev.y;
                            long time = android.os.SystemClock.uptimeMillis();
                            float speed = tx / (time - startTime);

                            if (Math.sqrt(tx * tx + ty * ty) > DRAG_SENSITIVITY) {
                                tempMatrix.set(savedMatrix);
                                tempMatrix.postTranslate(tx, ty);// 平移
                                matrix.set(tempMatrix);
                            }

                            if (Math.abs(tx) < SWIPE_SENSITIVITY) {
                                getParent().requestDisallowInterceptTouchEvent(true);
                            } else if (Math.abs(speed) > SWIPE_SPEED) {
                                getParent().requestDisallowInterceptTouchEvent(false);
                            }
                        }
                        break;

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_UP:
                        mode = NONE;
                        if (!unlimitedFlag) {
                            rotation = (rotation + 360) % 360;
                            float[] angle = {0, 90, 180, 270, 360};
                            float f = 0;
                            for (float a : angle) {
                                if (a - 45 < rotation && rotation <= a + 45) {
                                    f = a;
                                }
                            }
                            float r = f - rotation;
                            rotation = f;
                            matrix.postRotate(r);
                            center(true, true);
                        }
                        break;
                }
                setImageMatrix(matrix);
                return true;
            }
        }
    }

    @Override
    protected void setupBitmap() {
        setScaleType(ScaleType.MATRIX);
        Context context = getContext();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        widthScreen = dm.widthPixels;
        heightScreen = dm.heightPixels;
        sourceMatrix.set(getImageMatrix());
        matrix.set(getImageMatrix());
        center(true, true);
        setImageMatrix(matrix);

        setOnTouchListener(new BitmapTouchListener());
    }

    private RectF getMatrixRectF() {
        Matrix m = matrix;
        RectF rect = new RectF();
        Drawable d = getDrawable();
        if (null != d) {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            m.mapRect(rect);
        }
        return rect;
    }

    protected void center(boolean horizontal, boolean vertical) {
        RectF rect = getMatrixRectF();
        float deltaX = 0, deltaY = 0;
        float height = rect.height();
        float width = rect.width();
        if (vertical) {
            int screenHeight = heightScreen;
            if (height < screenHeight) {
                deltaY = (screenHeight - height) / 2 - rect.top;
            } else if (rect.top > 0) {
                deltaY = -rect.top;
            } else if (rect.bottom < screenHeight) {
                deltaY = getHeight() - rect.bottom;
            }
        }

        if (horizontal) {
            int screenWidth = widthScreen;
            if (width < screenWidth) {
                deltaX = (screenWidth - width) / 2 - rect.left;
            } else if (rect.left > 0) {
                deltaX = -rect.left;
            } else if (rect.right < screenWidth) {
                deltaX = screenWidth - rect.right;
            }
        }
        matrix.postTranslate(deltaX, deltaY);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) (Math.toDegrees(radians));
    }

    public void play() {
        mStatus = PLAY;
    }

    public void pause() {
        mStatus = PAUSE;
    }

    public void upend() {
        mStatus = UPEND;
    }

    public void nextFrame() {
        mOffset -= mFrameTime;
    }

    public void lastFrame() {
        mOffset += mFrameTime;
    }

    public boolean limited() {
        unlimitedFlag = !unlimitedFlag;
        matrix.set(sourceMatrix);
        center(true, true);
        setImageMatrix(matrix);
        mOffsetX = 0;
        mOffsetY = 0;
        return unlimitedFlag;
    }
}