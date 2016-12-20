package com.huangxueqin.circlepopmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Map;

/**
 * Created by huangxueqin on 16/9/10.
 */
public class CircleButton extends View {
    private static final int[] STATE_PRESSED =    { android.R.attr.state_pressed };
    private static final int[] STATE_UNPRESSED =  {-android.R.attr.state_pressed };

    private int mRadius = -1;
    private IconType mIconType;

    private Drawable mIconDrawable;
    private Drawable mCircleDrawable;
    private BitmapShader mCircleShader;
    private BitmapShader mPressedCircleShader;
    private Paint mCirclePaint;
    private Paint mShadowPaint;
    private Paint mIconPaint;

    private RectF mTempRect = new RectF();
    private Path mTempPath = new Path();


    public CircleButton(Context context) {
        this(context, null);
    }

    public CircleButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttributes(context, attrs);
        initPaints();

        setBackgroundColor(Color.TRANSPARENT);
        setClickable(true);
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleButton);
        mCircleDrawable = ta.getDrawable(R.styleable.CircleButton_background);
        mIconDrawable = ta.getDrawable(R.styleable.CircleButton_icon);
        mIconType = IconType.fromId(ta.getInt(R.styleable.CircleButton_iconType, mIconDrawable == null ? -1 : 1));
        ta.recycle();
        if (mCircleDrawable == null) {
            mCircleDrawable = getDefaultCircleDrawable(context);
        }
    }

    private Drawable getDefaultCircleDrawable(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDrawable(R.drawable.default_circle_button_bg);
        } else {
            return getResources().getDrawable(R.drawable.default_circle_button_bg, context.getTheme());
        }
    }

    private void initPaints() {
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.FILL);

        if (mCircleDrawable instanceof StateListDrawable) {
            StateListDrawable stateListDrawable = (StateListDrawable) mCircleDrawable;

        }

        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setColor(Color.BLACK);
        mShadowPaint.setAlpha(255/2);
        mShadowPaint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL));
        setLayerType(LAYER_TYPE_SOFTWARE, mShadowPaint);

        mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    private void clearCachedBGImages() {
        for(Map.Entry<Drawable, Bitmap> entry : mRoundBGBitmaps.entrySet()) {
            entry.getValue().recycle();
        }
        mRoundBGBitmaps.clear();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearCachedBGImages();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = Math.max(0, widthSize);
        int height = Math.max(0, heightSize);

        if (widthMode == MeasureSpec.EXACTLY) {
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(width, height);
            } else if (heightMode == MeasureSpec.UNSPECIFIED) {
                height = width;
            }
        } else if (width == MeasureSpec.EXACTLY) {
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, height);
            } else {
                width = height;
            }
        } else if (widthMode == MeasureSpec.AT_MOST && height == MeasureSpec.AT_MOST) {
            width = Math.min(width, height);
            height = Math.min(width, height);
        } else if (widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.UNSPECIFIED) {
            width = Math.max(width, height);
            height = Math.max(width, height);
        } else {
            if (widthMode == MeasureSpec.UNSPECIFIED) {
                width = height;
            } else {
                height = width;
            }
        }

        setMeasuredDimension(width, height);
        final int padding = getPaddingLeft() + getPaddingRight();
        mRadius = Math.min(width-padding, height-padding) / 2;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTempRect.set(mCx-mRadius, mCy-mRadius, mCx+mRadius, mCy+mRadius);
        mTempPath.addOval(mTempRect, Path.Direction.CW);
    }

    private int[] onCreateRoundBGDrawableState() {
        if(isPressed()) {
            return STATE_PRESSED;
        }
        else {
            return STATE_EMPTY;
        }
    }

    private void refreshRoundBGDrawableState() {
        if(mCircleDrawable != null && mCircleDrawable.isStateful()) {
            mCircleDrawable.setState(onCreateRoundBGDrawableState());
            mCircleShader = new BitmapShader(getCurrRoundBGBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            invalidate();
        }
    }


    @Override
    public void setPressed(boolean pressed) {
        boolean needUpdate = pressed != isPressed();
        super.setPressed(pressed);
        if(needUpdate) {
            refreshRoundBGDrawableState();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw shadow
        final int cx = getWidth() / 2;
        final int cy = getHeight() / 2;
        canvas.drawCircle(cx, cy, mRadius, mShadowPaint);

        // draw round background
        mCirclePaint.setShader(mCircleShader);
        canvas.drawCircle(mCx, mCy, mRadius, mCirclePaint);

        if(mIconType == ICON_TYPE.PLUS) {
            // draw plus
            mIconPaint.setColor(Color.WHITE);
            mIconPaint.setStyle(Paint.Style.FILL);
            int t = (int) (1.0 / 2 * mRadius + 0.5);
            int m = (int) (getContext().getResources().getDisplayMetrics().density * 1);
            mTempRect.set(mCx - t, mCy - m, mCx + t, mCy + m);
            canvas.drawRoundRect(mTempRect, m, m, mIconPaint);
            mTempRect.set(mCx - m, mCy - t, mCx + m, mCy + t);
            canvas.drawRoundRect(mTempRect, m, m, mIconPaint);
        }
        else if(mIconType == ICON_TYPE.IMAGE && mIconDrawable != null) {
            canvas.save();
            int dh = mIconDrawable.getIntrinsicHeight();
            int dw = mIconDrawable.getIntrinsicWidth();
            mIconDrawable.setBounds(0, 0, dw, dh);
            int translateX = mRadius - dw/2;
            int translateY = mRadius - dh/2;
            canvas.translate(translateX, translateY);
            if(dh > 1.0*mRadius || dw > 1.0*mRadius) {
                float scale = Math.min(1.0f*mRadius/dh, 1.0f*mRadius/dw);
                canvas.scale(scale, scale, mCx-translateX, mCy-translateY);
            }
            mIconDrawable.draw(canvas);
            canvas.restore();
        }
    }

    private Bitmap getCurrRoundBGBitmap() {
        Drawable key = mCircleDrawable.getCurrent();
        Bitmap b = mRoundBGBitmaps.get(key);
        if(b != null) {
            return b;
        }
        b = createBitmapFromDrawable(key);
        int bw = b.getWidth();
        int bh = b.getHeight();
        float targetSize = 2*mRadius;
        float scale = Math.min(1f, Math.min(targetSize/bw, targetSize/bh));
        if(scale < 1f) {
            Matrix sm = new Matrix();
            sm.postScale(scale, scale);
            Bitmap newB = Bitmap.createBitmap(b, 0, 0, bw, bh, sm, true);
            b.recycle();
            b = newB;
        }
        mRoundBGBitmaps.put(key, b);
        return b;
    }

    private Bitmap createBitmapFromDrawable(Drawable sld) {
        Drawable d = sld.getCurrent();
        if (d instanceof BitmapDrawable) {
            return ((BitmapDrawable) d).getBitmap();
        }

        Bitmap bitmap = null;
        try {
            if(d instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_4444);
            }
            else {
                bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_4444);
            }
            Canvas canvas = new Canvas(bitmap);
            d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            d.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            if(bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
        }
        return null;
    }

    public enum IconType {
        NONE(-1), PLUS(0), IMAGE(1), CUSTOM(3);

        int id;
        IconType(int id) {
            this.id = id;
        }

        public static IconType fromId(int id) {
            for (IconType it : values()) {
                if (it.id == id) {
                    return it;
                }
            }
            throw new IllegalArgumentException("" + id + " is an invalid IconType");
        }
    }

    private static void D(String msg) {
        Log.d(CircleButton.class.getSimpleName(), msg);
    }
}
