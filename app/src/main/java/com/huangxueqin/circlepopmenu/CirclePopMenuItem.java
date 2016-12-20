package com.huangxueqin.circlepopmenu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by huangxueqin on 16/9/12.
 */
public class CirclePopMenuItem extends ViewGroup {
    private static final int MENU_LABEL_SPACING = 10;
    private static final int MENU_LABEL_PADDING = 10;
    private static final float[] MENU_LABEL_CONOR_RADIUS = {10, 10, 10, 10, 10, 10, 10, 10};
    private static final int MENU_LABEL_TEXT_COLOR = 0xff666666;
    private static final int MENU_LABEL_BACKGROUND = 0x80ffffff;
    private static final int MENU_BUTTON_SIZE = 50;
    private static final int MENU_LABEL_TEXT_SIZE = 13;

    private int mButtonSize;
    private int mLabelTextSize;
    private int mButtonLabelSpacing;
    private CircleButton mIcon;
    private TextView mLabel;
    private CirclePopMenu.MenuLabelPosition mLabelPosition = CirclePopMenu.MenuLabelPosition.RIGHT;

    public CirclePopMenuItem(Context context) {
        this(context, null);
    }

    public CirclePopMenuItem(Context context, String label) {
        this(context, label, null);
    }

    public CirclePopMenuItem(Context context, String label, Drawable icon) {
        super(context);
        if(!TextUtils.isEmpty(label)) {
            mLabel = createLabel(context, label);
            addView(mLabel);
        }
        mIcon = new CircleButton(context);
        mIcon.setIconType(CircleButton.ICON_TYPE.IMAGE);
        mIcon.setButtonIconDrawable(icon);
        addView(mIcon);

        setClickable(true);

        mButtonLabelSpacing = (int) (getResources().getDisplayMetrics().density * MENU_LABEL_SPACING + 0.5);
        mButtonSize = (int) (getResources().getDisplayMetrics().density * MENU_BUTTON_SIZE + 0.5);
        mLabelTextSize = (int) (getResources().getDisplayMetrics().scaledDensity * MENU_LABEL_TEXT_SIZE + 0.5);
    }

    private TextView createLabel(Context context, String labelText) {
        TextView label = new TextView(context);
        label.setText(labelText);
        label.setTextColor(MENU_LABEL_TEXT_COLOR);
        label.setPadding(MENU_LABEL_PADDING, MENU_LABEL_PADDING, MENU_LABEL_PADDING, MENU_LABEL_PADDING);
        label.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        label.setClickable(true);
        label.setFocusableInTouchMode(true);
        Drawable labelBG = createLabelDrawableBG(MENU_LABEL_BACKGROUND);
        if(Build.VERSION.SDK_INT > 15) {
            label.setBackground(labelBG);
        }
        else {
            label.setBackgroundDrawable(labelBG);
        }
        return label;
    }

    public void setMenuButtonSize(int buttonSize) {
        mButtonSize = buttonSize;
        requestLayout();
    }

    public void setMenuLabelTextSize(int textSize) {
        mLabelTextSize = textSize;
        requestLayout();
    }

    public void setMenuLabelPosition(CirclePopMenu.MenuLabelPosition pos) {
        if(pos != mLabelPosition) {
            mLabelPosition = pos;
            requestLayout();
        }
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if(mLabel != null) {
            mLabel.setPressed(pressed);
        }
        mIcon.setPressed(pressed);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int horiPadding = getPaddingLeft() + getPaddingRight();
        int vertPadding = getPaddingBottom() + getPaddingTop();

        mIcon.measure(MeasureSpec.makeMeasureSpec(mButtonSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mButtonSize, MeasureSpec.EXACTLY));

        int labelWidth = width - horiPadding - mIcon.getMeasuredWidth() - mButtonLabelSpacing;
        int labelHeight = height - vertPadding;
        if(mLabel != null) {
            mLabel.measure(MeasureSpec.makeMeasureSpec(labelWidth, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(labelHeight, MeasureSpec.AT_MOST));
            labelWidth = mLabel.getMeasuredWidth();
            labelHeight = mLabel.getMeasuredHeight();
        }
        else {
            labelWidth = labelHeight = 0;
        }
        setMeasuredDimension(horiPadding + mButtonSize + mButtonLabelSpacing + labelWidth,
                vertPadding + Math.max(mButtonSize, labelHeight));
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        int left = getPaddingLeft();
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int labelWidth = mLabel != null ? mLabel.getMeasuredWidth() : 0;
        int labelHeight = mLabel != null ? mLabel.getMeasuredHeight() : 0;

        if(mLabelPosition == CirclePopMenu.MenuLabelPosition.RIGHT) {
            mIcon.layout(left, (height-mButtonSize)/2, left+mButtonSize, (height+mButtonSize)/2);
            if (mLabel != null) {
                left += mButtonLabelSpacing + mButtonSize;
                mLabel.layout(left, (height-labelHeight)/2, left+labelWidth, (height+labelHeight)/2);
            }
        }
        else if(mLabelPosition == CirclePopMenu.MenuLabelPosition.LEFT){
            if(mLabel != null) {
                mLabel.layout(left, (height-labelHeight)/2, left+labelWidth, (height+labelHeight)/2);
                left += labelWidth + mButtonLabelSpacing;
            }
            mIcon.layout(left, (height-mButtonSize)/2, left+mButtonSize, (height+mButtonSize)/2);
        }
    }

    private Drawable createLabelDrawableBG(int color) {
        int darkColor = darker(color, 0.7f);
        StateListDrawable d = new StateListDrawable();
        // normal state drawable
        ShapeDrawable normal = new ShapeDrawable(new RoundRectShape(MENU_LABEL_CONOR_RADIUS, new RectF(0, 0, 0, 0), new float[8]));
        normal.getPaint().setColor(color);
        d.addState(new int[]{-android.R.attr.state_pressed}, normal);
        // dark state drawable
        ShapeDrawable pressed = new ShapeDrawable(new RoundRectShape(MENU_LABEL_CONOR_RADIUS, new RectF(0, 0, 0, 0), new float[8]));
        pressed.getPaint().setColor(darkColor);
        d.addState(new int[]{android.R.attr.state_pressed}, pressed);

        return d;
    }

    private static int darker(int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a,
                Math.max((int)(r*factor), 0),
                Math.max((int)(g*factor), 0),
                Math.max((int)(b*factor), 0));
    }

    private static void D(String msg) {
        Log.d(CircleButton.class.getSimpleName(), msg);
    }
}
