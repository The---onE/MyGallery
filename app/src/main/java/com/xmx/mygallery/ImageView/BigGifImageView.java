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
    private float x_down = 0;
    private float y_down = 0;
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float oldRotation = 0;//第二个手指放下时的两点的旋转角度
    private float rotation = 0;//旋转角度差值
    private float newRotation = 0;
    private float Reset_scale = 1;
    private Matrix matrix = new Matrix();
    private Matrix matrix1 = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private GestureDetector gestureDetector;

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private boolean isFangda = false;//双击放大还是缩小
    private int widthScreen;
    private int heightScreen;
    boolean isCheckTopAndBottom, isCheckRightAndLeft;

    PointF prev = new PointF();
    float dist = 1f;
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
                        dist = spacing(event);
                        scale = customScale;
                        // 如果连续两点距离大于10，则判定为多点模式
                        if (spacing(event) > 10f) {
                            midPoint(mid, event);
                            mode = ZOOM;
                        }
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
                            if (newDist > 10f) {
                                float tScale = newDist / dist;
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
        matrix.set(getImageMatrix());

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

            /**
             * 双击手势的时候 会触发该方法一次
             * 所以双击手势对应的代码应放在这里
             */
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                mid.x = e.getX();
                mid.y = e.getY();
                matrix1.set(savedMatrix);
                if (isFangda) {
                    matrix1.postScale(0.5f, 0.5f, mid.x, mid.y);//缩小
                    isFangda = false;
                } else {
                    matrix1.postScale(2f, 2f, mid.x, mid.y);// 放大
                    isFangda = true;
                }
                matrix.set(matrix1);
                center(true, true);
                setImageMatrix(matrix);
                return true;
            }

            /**
             * 单击手势 会触发该方法一次
             * 单击对应的代码应放在这里
             */
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                //点击图片 销毁activity
                //TouchImageViewActivity.this.finish();
                return false;
            }
        });
        /**
         * 初始化 将图片放在屏幕中心位置
         */
        center(true, true);
        /**
         * 图片设置中心之后，重新设置图片的缩放矩阵
         */
        setImageMatrix(matrix);
        /**
         * 设置ImageView的触摸事件
         */
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
        /**
         * 在这里调用手势的方法
         * 这是手势和触摸事件同时使用的方法
         */
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        } else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mode = DRAG;
                    x_down = event.getX();
                    y_down = event.getY();
                    /**
                     * 单个手指放下，首先保存图片的缩放矩阵到savedMatrix
                     */
                    savedMatrix.set(matrix);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mode = ZOOM;
                    /**
                     * 第二个手指刚放下时
                     * 计算两个手指间的距离
                     */
                    oldDist = spacing(event);
                    /**
                     * 第二个手指刚放下时
                     * 计算两个手指见的旋转角度
                     */
                    oldRotation = rotation(event);
                    savedMatrix.set(matrix);
                    /**
                     * 第二个手指刚放下时
                     * 计算两个手指见的中间点坐标，并存在mid中
                     */
                    midPoint(mid, event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == ZOOM) {
                        matrix1.set(savedMatrix);
                        /**
                         * 两个手指开始移动
                         * 计算移动后旋转角度
                         */
                        newRotation = rotation(event);
                        /**
                         * 两个角度之差
                         * 即是图片的旋转角度
                         */
                        rotation = newRotation - oldRotation;
                        /**
                         * 计算移动后两点间的中间点
                         */
                        float newDist = spacing(event);
                        /**
                         * 两个中间点的商即时放大倍数
                         */
                        float scale = newDist / oldDist;
                        /**
                         * 放大倍数的倒数即是还原图片原来大小的倍数
                         */
                        Reset_scale = oldDist / newDist;
                        matrix1.postScale(scale, scale, mid.x, mid.y);// 縮放
                        matrix1.postRotate(rotation, mid.x, mid.y);// 旋轉
                        matrix.set(matrix1);
                        /**
                         * 调用该方法即可重新图片
                         */
                        setImageMatrix(matrix);
                    } else if (mode == DRAG) {
                        matrix1.set(savedMatrix);
                        float tx = event.getX() - x_down;
                        float ty = event.getY() - y_down;
                        /**
                         * 单个手指移动后的距离大于20 才算作移动
                         */
                        if (Math.sqrt(tx * tx + ty * ty) > 20f) {
                            /**
                             * 设置图片宽高与屏幕宽高的大小的boolean类型的值
                             */
                            isCheckRightAndLeft = isCheckTopAndBottom = true;
                            /**
                             * 得到目前图片的宽高
                             */
                            RectF rectF = getMatrixRectF();
                            /**
                             * 图片宽度小于屏幕大小
                             * 不移动
                             */
                            if (rectF.width() < widthScreen) {
                                tx = 0;
                                isCheckRightAndLeft = false;
                            }
                            /**
                             * 图片高度小于屏幕高度
                             * 不移动
                             */
                            if (rectF.height() < heightScreen) {
                                ty = 0;
                                isCheckTopAndBottom = false;
                            }
                            matrix1.postTranslate(tx, ty);// 平移
                            /**
                             * 如果想在拖动图片的同时检测图片边缘是否
                             * 到达屏幕的边缘，则取消下面的注释
                             */
//                      matrix.set(matrix1);
//                      checkDxDyBounds();
                            matrix.set(matrix1);
                            setImageMatrix(matrix);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    if (mode == ZOOM) {
                        /**
                         * 双手放开，停止图片的旋转和缩放
                         * Reset_scale还原图片的缩放比例
                         */
                        matrix1.postScale(Reset_scale, Reset_scale, mid.x, mid.y);
                        /**
                         * 双手放开，停止缩放、旋转图片，此时根据已旋转的角度
                         * 计算还原图片的角度，最终的效果是把图片竖直或横平方正。
                         */
                        setRotate();
                        matrix.set(matrix1);
                        /**
                         * 将图片放在屏幕中间位置
                         */
                        center(true, true);
                        setImageMatrix(matrix);
                        matrix1.reset();
                    } else if (mode == DRAG) {
                        /**
                         * 单手拖动图片，放开手指，停止拖动
                         * 此时检测图片是否已经偏离屏幕边缘
                         * 如果偏离屏幕边缘，则图片回弹
                         */
                        checkDxDyBounds();
                        matrix.set(matrix1);
                        setImageMatrix(matrix);
                        matrix1.reset();
                    }
                    mode = NONE;
                    break;
            }
            return true;
        }
    }

    /**
     * 根据当前图片的Matrix获得图片的范围
     * 这里获取的是当前显示的图片的大小。
     * 图片放大后，获取的就是图片放大后的图片的大小。
     * 图片缩小后，获取的就是图片缩小后的图片的大小。
     * <p/>
     * 这个大小与图片的大小是有区别的。
     * 下面是固定用法，记住即可。
     *
     * @return
     */
    private RectF getMatrixRectF() {
        Matrix m = matrix;
        RectF rect = new RectF();
        Drawable d = getDrawable();
        if (null != d) {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            m.mapRect(rect);
        }
        //第二种方法  （两种方法均可）
//        Matrix m = new Matrix();
//        m.set(matrix);
//        RectF rect = new RectF(0, 0, gintama.getWidth(), gintama.getHeight());
//        m.mapRect(rect);
        return rect;
    }

    /**
     * 横向、纵向 图片居中
     */
    protected void center(boolean horizontal, boolean vertical) {
        RectF rect = getMatrixRectF();
        float deltaX = 0, deltaY = 0;
        float height = rect.height();
        float width = rect.width();
        if (vertical) {
            /**
             *  图片小于屏幕大小，则居中显示。
             *  大于屏幕，如果图片上方留空则往上移，
             *  图片下方留空则往下移
             */
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

    // 触碰两点间距离
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    // 取手势中心点
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    // 取旋转角度
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        /**
         * 反正切函数
         * 计算两个坐标点的正切角度
         */
        double radians = Math.atan2(delta_y, delta_x);
        return (float) (Math.toDegrees(radians));
    }

    /**
     * 手指松开，确定旋转的角度
     */
    private void setRotate() {
        if (rotation < -315) {
            matrix1.postRotate(-360 - rotation, mid.x, mid.y);// 旋轉
        } else if (rotation < -270) {
            matrix1.postRotate(-270 - rotation, mid.x, mid.y);// 旋轉
        } else if (rotation < -225) {
            matrix1.postRotate(-270 - rotation, mid.x, mid.y);// 旋轉
        } else if (rotation < -180) {
            matrix1.postRotate(-180 - rotation, mid.x, mid.y);// 旋轉
        } else if (rotation < -135) {
            matrix1.postRotate(-180 - rotation, mid.x, mid.y);// 旋轉
        } else if (rotation < -90) {
            matrix1.postRotate(-90 - rotation, mid.x, mid.y);// 旋轉
        } else if (rotation < -45) {
            matrix1.postRotate(-90 - rotation, mid.x, mid.y);// 旋轉
        } else if (rotation < 0) {
            matrix1.postRotate(0 - rotation, mid.x, mid.y);// 旋轉
        } else if (rotation < 45) {
            matrix1.postRotate(0 - rotation, mid.x, mid.y);// 旋轉
        } else if (rotation < 90) {
            matrix1.postRotate(90 - rotation, mid.x, mid.y);// 旋轉
        } else if (rotation < 135) {
            matrix1.postRotate(90 - rotation, mid.x, mid.y);// 旋轉
        } else if (rotation < 180) {
            matrix1.postRotate(180 - rotation, mid.x, mid.y);// 旋轉
        } else if (rotation < 225) {
            matrix1.postRotate(180 - rotation, mid.x, mid.y);// 旋轉
        } else if (rotation < 270) {
            matrix1.postRotate(270 - rotation, mid.x, mid.y);// 旋轉
        } else if (rotation < 315) {
            matrix1.postRotate(270 - rotation, mid.x, mid.y);// 旋轉
        } else if (rotation < 360) {
            matrix1.postRotate(360 - rotation, mid.x, mid.y);// 旋轉
        }
    }

    /**
     * 检测图片偏离屏幕两边的距离
     * 然后平移，是图片边缘在屏幕边，
     * 使图片周围没有空白
     */
    private void checkDxDyBounds() {
        RectF rectF = getMatrixRectF();
        float dx = 0.0f, dy = 0.0f;
        /**
         * 如果图片的左侧大于零，说明图片左侧向右
         * 偏离了左侧屏幕，则左移偏离的距离.
         * rectF.left的值，是基于左侧坐标计算的。
         * 图片正常情况下，该值为0.
         * 当图片向右侧拖动以后，该值大于0.
         * 当图片向左侧拖动以后，该值小于0.
         */
        if (rectF.left > 0 || isCheckRightAndLeft) {
            dx = -rectF.left;
        }
        /**
         * 如果图片的右侧偏离屏幕的右侧，则
         * 图片右移图片的宽度与图片显示的宽度的差.
         *
         * rectF.right的值，是基于左侧计算的，图片没有缩放旋转情况下，
         * 该值==touchImageView.getWidth()图片的宽度。
         * 当拖动图片以后，该值变化，等于显示的图片的宽度
         */
        if (rectF.right < getWidth() || isCheckRightAndLeft) {
            dx = getWidth() - rectF.right;
        }
        /**
         * 当图片顶部大于0，说明图片向下偏离屏幕顶部，
         * 则图片向上回弹偏离的距离。
         *
         * rectF.top的值基于顶部坐标，
         * 图片正常情况下，该值=0.
         */
        if (rectF.top > 0 || isCheckTopAndBottom) {
            dy = -rectF.top;
        }
        /**
         * 当图片底部小于图片高度时，图片偏离屏幕底部
         * 则图片回弹图片的高度与显示的图片的高度之差。
         *
         * rectF.bottom的值，基于顶部坐标。
         * 图片正常情况下，该值=图片的高度。
         */
        if (rectF.bottom < getHeight() || isCheckTopAndBottom) {
            dy = getHeight() - rectF.bottom;
        }
        /**
         * 计算后，设置图片回弹
         */
        matrix1.postTranslate(dx, dy);
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