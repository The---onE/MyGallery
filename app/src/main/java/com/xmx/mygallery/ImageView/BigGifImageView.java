package com.xmx.mygallery.ImageView;

import android.annotation.SuppressLint;
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
    float oldRotation = 0;//第二个手指放下时的两点的旋转角度
    Matrix sourceMatrix = new Matrix();
    Matrix matrix = new Matrix();
    Matrix tempMatrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    GestureDetector gestureDetector;

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;
    int widthScreen;
    int heightScreen;

    float scale = 1f;

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

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    // 主点按下
                    case MotionEvent.ACTION_DOWN:
                        prev.set(event.getX(), event.getY());
                        break;
                    // 副点按下
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = spacing(event);
                        scale = customScale;
                        midPoint(mid, event);
                        mode = ZOOM;
                        break;
                    case MotionEvent.ACTION_UP: {
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == ZOOM) {
                            float newDist = spacing(event);
                            if (newDist > 32f) {
                                float tScale = newDist / oldDist;
                                customScale = scale * tScale;
                                requestLayout();
                                postInvalidate();
                            }
                        }
                        break;
                }
                return true;
            }
        });
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

        gestureDetector = new GestureDetector(
                getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent e) {
                        return false;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        super.onLongPress(e);
                    }
                });
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
                matrix.set(sourceMatrix);
                center(true, true);
                setImageMatrix(matrix);
                return false;
            }
        });

        setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEvent(event);
                return true;
            }
        });
    }


    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        } else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mode = DRAG;
                    prev.set(event.getX(), event.getY());
                    savedMatrix.set(matrix);
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
                        if (newDist > 32f) {
                            PointF newMid = new PointF();
                            midPoint(newMid, event);
                            tempMatrix.set(savedMatrix);
                            float newRotation = rotation(event);
                            float rotation = newRotation - oldRotation;
                            float scale = newDist / oldDist;
                            tempMatrix.postTranslate(newMid.x - mid.x, newMid.y - mid.y);// 平移
                            tempMatrix.postScale(scale, scale, newMid.x, newMid.y);// 縮放
                            tempMatrix.postRotate(rotation, newMid.x, newMid.y);// 旋轉
                            matrix.set(tempMatrix);
                            setImageMatrix(matrix);
                        }
                    } else if (mode == DRAG) {
                        float tx = event.getX() - prev.x;
                        float ty = event.getY() - prev.y;
                        if (Math.sqrt(tx * tx + ty * ty) > 32f) {
                            tempMatrix.set(savedMatrix);
                            tempMatrix.postTranslate(tx, ty);// 平移
                            matrix.set(tempMatrix);
                            setImageMatrix(matrix);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
            }
            return true;
        }
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
}