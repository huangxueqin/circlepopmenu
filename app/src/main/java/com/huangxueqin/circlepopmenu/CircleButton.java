package com.huangxueqin.circlepopmenu;

import android.content.Context;
import android.content.res.Resources;
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
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Map;

/**
 * Created by huangxueqin on 16/9/10.
 */
public class CircleButton extends View {
    private static final int SHADOW_WIDTH_PX = 5;

    private int mRadius = -1;
    private IconType mIconType;

    private Drawable mIconDrawable;
    private Drawable mCircleDrawable;
    private Paint mCirclePaint;
    private Paint mShadowPaint;
    private Paint mIconPaint;

    private Path mCircleBackgroundPath = new Path();

    private RectF mTempRect = new RectF();


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

        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setColor(Color.BLACK);
        mShadowPaint.setAlpha(255/2);
        mShadowPaint.setMaskFilter(new BlurMaskFilter(SHADOW_WIDTH_PX, BlurMaskFilter.Blur.NORMAL));
        setLayerType(LAYER_TYPE_SOFTWARE, mShadowPaint);

        mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setIconDrawable(Drawable iconDrawable) {
        if (iconDrawable == null) {
            mIconType = IconType.NONE;
            invalidate();
            return;
        }

        mIconType = IconType.IMAGE;
        if (iconDrawable.equals(mIconDrawable)) {
            mIconDrawable = iconDrawable;
            invalidate();
        }
    }

    public void setIconResource(int resId) {
        Resources res = getResources();
        Drawable d = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            d = res.getDrawable(resId, getContext().getTheme());
        } else {
            d = res.getDrawable(resId);
        }
        if (d != null) {
            setIconDrawable(d);
        }
    }

    public void setIconType(IconType type) {
        if (mIconType != type) {
            mIconType = type;
            invalidate();
        }
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
        final int padding = Math.max(SHADOW_WIDTH_PX*2, getPaddingLeft() + getPaddingRight());
        mRadius = Math.min(width-padding, height-padding) / 2;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCircleBackgroundPath.reset();
        mCircleBackgroundPath.addCircle(w/2, h/2, mRadius, Path.Direction.CW);
    }

    private void refreshCircleDrawableState() {
        final int[] state = getDrawableState();
        boolean changed = false;
        Drawable d = mCircleDrawable;
        if (d != null && d.isStateful()) {
            changed |= d.setState(state);
        }

        if (changed) {
            invalidate();
        }
    }



    @Override
    public void setPressed(boolean pressed) {
        boolean pressChange = pressed != isPressed();
        super.setPressed(pressed);

        if(pressChange) {
            refreshCircleDrawableState();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw shadow
        final int cx = getWidth() / 2;
        final int cy = getHeight() / 2;
        canvas.drawCircle(cx, cy, mRadius, mShadowPaint);

        canvas.save();
        // draw circle background
        canvas.clipPath(mCircleBackgroundPath);
        Drawable d = mCircleDrawable.getCurrent();
        d.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
        d.draw(canvas);

        if(mIconType == IconType.PLUS) {
            // draw plus
            mIconPaint.setColor(Color.WHITE);
            mIconPaint.setStyle(Paint.Style.FILL);
            int t = (int) (1.0 / 2 * mRadius + 0.5);
            int m = (int) (getContext().getResources().getDisplayMetrics().density * 1);
            mTempRect.set(cx - t, cy - m, cx + t, cy + m);
            canvas.drawRoundRect(mTempRect, m, m, mIconPaint);
            mTempRect.set(cx - m, cy - t, cx + m, cy + t);
            canvas.drawRoundRect(mTempRect, m, m, mIconPaint);
        }
        else if(mIconType == IconType.IMAGE && mIconDrawable != null) {
            canvas.save();
            int dh = mIconDrawable.getIntrinsicHeight();
            int dw = mIconDrawable.getIntrinsicWidth();
            mIconDrawable.setBounds(0, 0, dw, dh);
            int translateX = mRadius - dw/2;
            int translateY = mRadius - dh/2;
            canvas.translate(translateX, translateY);
            if(dh > 1.0*mRadius || dw > 1.0*mRadius) {
                float scale = Math.min(1.0f*mRadius/dh, 1.0f*mRadius/dw);
                canvas.scale(scale, scale, cx-translateX, cy-translateY);
            }
            mIconDrawable.draw(canvas);
            canvas.restore();
        } else if (mIconType == IconType.CUSTOM) {

        }

        canvas.restore();
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
