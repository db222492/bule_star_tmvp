package com.xinzeyijia.houselocks.ui.view;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class SampleTitleBehavior extends CoordinatorLayout.Behavior<View> {
    // 列表顶部和title底部重合时，列表的滑动距离。
    private float deltaY;

    public SampleTitleBehavior() {
    }

    public SampleTitleBehavior(Context context, AttributeSet attrs) {
        super( context, attrs );
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof ViewPager;
    }

    //被观察的view发生改变时回调
    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if (deltaY == 0) {
            deltaY = dependency.getY() - child.getHeight();
        }
        float dy = dependency.getY() - child.getHeight();
        dy = dy < 0 ? 0 : dy;
        float y = -(dy / deltaY) * child.getHeight();
        child.setTranslationY( y );

//        LogUtils.i( "滑动值", "dy--->" + dy + "y--->" + y + "deltaY---->" + deltaY );
//        float alpha = 1 - (dy / deltaY);
//        child.setAlpha(alpha);

        return true;
    }
}
