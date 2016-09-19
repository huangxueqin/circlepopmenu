package com.huangxueqin.circlepopmenu;

import android.animation.ObjectAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class MainActivity extends AppCompatActivity {
    CirclePopMenu mCirclePopMenu;
    CircleButton mCircleButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mCirclePopMenu = (CirclePopMenu) findViewById(R.id.circle_pop_menu);
    }
    private static void D(String msg) {
        Log.d(CircleButton.class.getSimpleName(), msg);
    }
}
