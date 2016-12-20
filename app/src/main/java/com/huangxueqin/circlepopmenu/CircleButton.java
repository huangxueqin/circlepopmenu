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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangxueqin on 16/9/10.
 */
public class CircleButton extends View {


    private static final int[] STATE_PRESSED =    { R.attr.state_pressed };
    private static final int[] STATE_UNPRESSED =  { -R.attr.state_pressed };

    private int mRadius = -1;
    private IconType mIconType;

    private Drawable mIconDrawable;
    private StateListDrawable mRoundBGDrawable;
    private HashMap<Drawable, Bitmap> mRoundBGBitmaps = new HashMap<>();
    private BitmapShader mRoundBGShader;
    private Paint mRoundBGPaint = new Paint();
    private Paint mShadowPaint = new Paint();
    private Paint mIconPaint = new Paint();

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
        setClickable(true);
        clearViewBackground();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleButton);
        Drawable d = ta.getDrawable(R.styleable.CircleButton_button_background);
        Drawable icon = ta.getDrawable(R.styleable.CircleButton_button_icon);
        String typeStr = ta.getNonResourceString(R.styleable.CircleButton_button_icon_type);
        ta.recycle();
        mIconType = typeStr != null && typeStr.equals(TYPE_IMAGE_STR) ? ICON_TYPE.IMAGE : ICON_TYPE.PLUS;
        mIconDrawable = icon;
        setButtonBackgroundDrawable(d, true);
        initPaints();
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleButton);

    }

    private void clearViewBackground() {
        // set origin background null
        if(Build.VERSION.SDK_INT > 15) {
            setBackground(new ColorDrawable(Color.TRANSPARENT));
        }
        else {
            setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void initPaints() {
        mRoundBGPaint.setStyle(Paint.Style.FILL);
        mRoundBGPaint.setAntiAlias(true);

        mShadowPaint.setColor(Color.GRAY);
        mShadowPaint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL));
        setLayerType(LAYER_TYPE_SOFTWARE, mShadowPaint);

        mIconPaint.setAntiAlias(true);
    }

    public void setButtonBackgroundDrawable(Drawable d) {
        setButtonBackgroundDrawable(d, false);
    }

    private void setButtonBackgroundDrawable(Drawable d, boolean forceSet) {
        if(forceSet || d != mRoundBGDrawable) {
            if (d != null && d instanceof StateListDrawable) {
                mRoundBGDrawable = (StateListDrawable) d;
            } else if (d == null) {
                Drawable normal = new ColorDrawable(DEFAULT_MAIN_BUTTON_COLOR);
                Drawable pressed = new ColorDrawable(DEFAULT_MAIN_BUTTON_DARK_COLOR);
                mRoundBGDrawable = new StateListDrawable();
                mRoundBGDrawable.addState(STATE_PRESSED, pressed);
                mRoundBGDrawable.addState(STATE_EMPTY, normal);
            } else {
                mRoundBGDrawable = new StateListDrawable();
                mRoundBGDrawable.addState(new int[]{}, d);
            }
            clearCachedBGImages();
            refreshRoundBGDrawableState();
        }
    }

    public static Drawable getDefaultBGDrawable() {
        Drawable normal = new ColorDrawable(DEFAULT_MAIN_BUTTON_COLOR);
        Drawable pressed = new ColorDrawable(DEFAULT_MAIN_BUTTON_DARK_COLOR);
        StateListDrawable d = new StateListDrawable();
        d.addState(STATE_PRESSED, pressed);
        d.addState(STATE_EMPTY, normal);
        return d;
    }

    public void setButtonIconDrawable(Drawable d) {
        if(mIconDrawable != d) {
            mIconDrawable = d;
            invalidate();
        }
    }

    public void setIconType(ICON_TYPE t) {
        if(mIconType != t) {
            mIconType = t;
            if(mIconType == ICON_TYPE.IMAGE && mIconDrawable == null) {
                mIconDrawable = new ColorDrawable(DEFAULT_MAIN_BUTTON_COLOR);
            }
            invalidate();
        }
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
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        mCx = w/2;
        mCy = h/2;
        int size = Math.min(w, h);
        mRadius = size / 2 - 5;
        setMeasuredDimension(size, size);
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
        if(mRoundBGDrawable != null && mRoundBGDrawable.isStateful()) {
            mRoundBGDrawable.setState(onCreateRoundBGDrawableState());
            mRoundBGShader = new BitmapShader(getCurrRoundBGBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
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
        canvas.drawCircle(mCx, mCy, mRadius, mShadowPaint);
        // draw round background
        mRoundBGPaint.setShader(mRoundBGShader);
        canvas.drawCircle(mCx, mCy, mRadius, mRoundBGPaint);

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
        Drawable key = mRoundBGDrawable.getCurrent();
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
        PLUS(0), IMAGE(1), CUSTOM(3);

        int id;
        IconType(int id) {
            this.id = id;
        }

        public IconType fromId(int id) {
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
