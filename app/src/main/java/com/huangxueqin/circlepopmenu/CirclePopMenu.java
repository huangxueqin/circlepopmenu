package com.huangxueqin.circlepopmenu;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxueqin on 16/9/10.
 */
public class CirclePopMenu extends ViewGroup {
    private static final int DEFAULT_MAIN_BUTTON_SIZE = 50;
    private static final int DEFAULT_MENU_BUTTON_SIZE = 40;
    private static final int DEFAULT_MENU_BUTTON_DIST = 10;
    private static final int DEFAULT_MENU_LABEL_TEXT_SIZE = 15;

    public static enum MenuLabelPosition{LEFT, RIGHT};

    private CircleButton mMainButton;
    private int mMainButtonSize;
    private Drawable mMainButtonBG;
    private int mMainButtonIndex = -1;

    private List<CirclePopMenuItem> mMenuItems = new ArrayList<>();
    private List<Drawable> mMenuButtonIcons = new ArrayList<>();
    private List<String> mMenuButtonLabelTexts = new ArrayList<>();

    private int mMenuButtonSize;
    private Drawable mMenuButtonBG;
    private int mMenuButtonSpacing;

    private int mMenuLabelTextSize;
    private Drawable mMenuLabelBG;

    private boolean mIsExpanded = false;
    private boolean mExpandAnimPending = false;
    private MenuLabelPosition mMenuItemLabelPos = MenuLabelPosition.RIGHT;

    public CirclePopMenu(Context context) {
        this(context, null);
    }

    public CirclePopMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CirclePopMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int defaultMainButtonSize = (int) (getResources().getDisplayMetrics().density * DEFAULT_MAIN_BUTTON_SIZE + 0.5);
        int defaultMenuButtonSize = (int) (getResources().getDisplayMetrics().density * DEFAULT_MENU_BUTTON_SIZE + 0.5);
        int defaultMenuButtonDist = (int) (getResources().getDisplayMetrics().density * DEFAULT_MENU_BUTTON_DIST + 0.5);
        int defaultMenuLabelSize = (int) (getResources().getDisplayMetrics().scaledDensity * DEFAULT_MENU_LABEL_TEXT_SIZE + 0.5);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CirclePopMenu, defStyleAttr, 0);
        mMainButtonSize = ta.getDimensionPixelSize(R.styleable.CirclePopMenu_main_button_size, defaultMainButtonSize);
        mMenuButtonSize = ta.getDimensionPixelSize(R.styleable.CirclePopMenu_menu_button_size, defaultMenuButtonSize);
        mMenuButtonSpacing = ta.getDimensionPixelSize(R.styleable.CirclePopMenu_menu_button_spacing, defaultMenuButtonDist);
        mMenuLabelTextSize = ta.getDimensionPixelSize(R.styleable.CirclePopMenu_menu_label_text_size, defaultMenuLabelSize);
        mMainButtonBG = ta.getDrawable(R.styleable.CirclePopMenu_main_button_background);
        ta.recycle();

        // setup main button
        mMainButton = new CircleButton(getContext());
        addView(mMainButton);
        mMainButton.setOnClickListener(mMainButtonOnClickListener);

        // test
        for(int i = 0; i < 5; i++) {
            CirclePopMenuItem menuItem = new CirclePopMenuItem(context, "menu "+i, getResources().getDrawable(R.drawable.copy));
            menuItem.setMenuButtonSize(mMenuButtonSize);
            menuItem.setAlpha(0);
            addView(menuItem);
            mMenuItems.add(menuItem);
        }

        setChildrenDrawingOrderEnabled(true);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if(mMainButtonIndex < 0) {
            return super.getChildDrawingOrder(childCount, i);
        }
        if(i == childCount-1) {
            return mMainButtonIndex;
        }
        else if (i >= mMainButtonIndex){
            return i+1;
        }
        else {
            return i;
        }
    }

    public boolean isExpanded() {
        return mIsExpanded;
    }

    public void setExpand(boolean expand) {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        mMainButton.measure(MeasureSpec.makeMeasureSpec(mMainButtonSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mMainButtonSize, MeasureSpec.EXACTLY));
        int maxMenuItemWidth = 0;
        int totalMenuItemHeight = 0;
        for(int i = 0; i < mMenuItems.size(); i++) {
            mMenuItems.get(i).measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
            maxMenuItemWidth = Math.max(maxMenuItemWidth, mMenuItems.get(i).getMeasuredWidth());
            totalMenuItemHeight += mMenuItems.get(i).getMeasuredHeight();
        }

        int totalContentWidth = maxMenuItemWidth;
        int totalContentHeight = totalMenuItemHeight + mMainButtonSize + mMenuItems.size()*mMenuButtonSpacing;
        if(mMainButtonSize > mMenuButtonSize) {
            totalContentWidth = Math.max(mMainButtonSize,
                    totalContentWidth + (mMainButtonSize-mMenuButtonSize)/2);
        }

        int vertPadding = getPaddingBottom() + getPaddingTop();
        int horiPadding = getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(horiPadding + totalContentWidth, vertPadding + totalContentHeight);

        for(int i = 0; i < getChildCount(); i++) {
            if(getChildAt(i) == mMainButton) {
                mMainButtonIndex = i;
                break;
            }
        }
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        // layout main button
        int ml = getPaddingLeft();
        int mr = getMeasuredHeight()-getPaddingBottom();
        int mb = getMeasuredHeight() - getPaddingBottom();
        if(mMenuItemLabelPos == MenuLabelPosition.RIGHT) {
            mMainButton.layout(ml, mb-mMainButtonSize, ml+mMainButtonSize, mb);
        }
        else {
            mMainButton.layout(mr-mMainButtonSize, mb-mMainButtonSize, mr, mb);
        }

        // layout menu buttons
        for(int j = mMenuItems.size() - 1; j >= 0; j--) {
            View menu = mMenuItems.get(j);
            int bottom = mb - (mMainButtonSize - menu.getMeasuredHeight()) / 2;
            if (mMenuItemLabelPos == MenuLabelPosition.RIGHT) {
                int left = ml + (mMainButtonSize-mMenuButtonSize)/2 - menu.getPaddingLeft();
                menu.layout(left, bottom-menu.getMeasuredHeight(), left+menu.getMeasuredWidth(), bottom);
            }
            else if(mMenuItemLabelPos == MenuLabelPosition.LEFT) {
                int right = mr-(mMainButtonSize-mMenuButtonSize)/2+menu.getPaddingRight();
                menu.layout(right-menu.getMeasuredWidth(), bottom-menu.getMeasuredHeight(), right, bottom);
            }
        }
    }

    private View.OnClickListener mMainButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            float from = 0f, to = 45f;
            if(mIsExpanded) {
                from = 45f;
                to = 0f;
            }
            mIsExpanded = !mIsExpanded;
            ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(mMainButton, "rotation", from, to);
            rotateAnimator.setDuration(200);
            rotateAnimator.start();
            startMenuAnimation();
        }
    };

    private void startMenuAnimation() {
        float fromAlpha = mIsExpanded ? 0: 1;
        float toAlpha = mIsExpanded ? 1 : 0;
        float unExpandTop = getMeasuredHeight() - getPaddingBottom() - (mMainButtonSize + mMenuButtonSize)/2;
        float firstExpandTop = getHeight() - getPaddingBottom() - mMainButtonSize - mMenuButtonSpacing - mMenuButtonSize;
        for(int i = mMenuItems.size()-1; i >= 0; i--) {
            float fromY = mMenuItems.get(i).getTop();
            float toY = mIsExpanded ? firstExpandTop : unExpandTop;
            firstExpandTop -= (mMenuButtonSize + mMenuButtonSpacing);

            AnimatorSet as = new AnimatorSet();
            as.playTogether(ObjectAnimator.ofFloat(mMenuItems.get(i), "translationY", toY-fromY),
                    ObjectAnimator.ofFloat(mMenuItems.get(i), "alpha", fromAlpha, toAlpha));
            as.setDuration(100 + (mMenuItems.size()-i)*20);
            as.setInterpolator(new DecelerateInterpolator());
            as.start();
        }
    }

    private static void D(String msg) {
        Log.d(CircleButton.class.getSimpleName(), msg);
    }
}
