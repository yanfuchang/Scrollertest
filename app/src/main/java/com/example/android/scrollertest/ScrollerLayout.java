package com.example.android.scrollertest;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;


/**
 * Created by Yan fuchang on 2016/12/30.
 */

public class ScrollerLayout extends ViewGroup {
    private static final String TAG= "ScrollerLayout";
    /**
     * 用于完成滚动操作
     * */
    private Scroller mScroller;

    /**
     * 最小的有效滑动距离
     * */
    private int mTouchSlop;

    /**
     * 手按下时的屏幕的坐标
     * */
    private float xDowm;

    /**
     * 手指上一次滑动到的坐标
     * */
    private float lastX;

    /**
     * 手指当前的坐标
     * */
    private float curX;



    public ScrollerLayout(Context context) {
        super(context);
    }

    public ScrollerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(context));
    }

    /**
     * 测量每一个子控件在父布局中的大小,否则不会显示在屏幕上
     *
     * */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = getChildCount();
        for(int i = 0; i < count; i++)
            measureChild(getChildAt(i),widthMeasureSpec,heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!changed) return;
        int count = getChildCount();

        /**
         * 使每一个控件水平排列在屏幕中，因为每一个 item 的宽度都是 占满整个屏幕
         * 所以，当前只会显示 一个 item
         * */
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            view.layout(i * view.getMeasuredWidth(),0,(i + 1) * view.getMeasuredWidth(),view.getMeasuredHeight());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {

            /**
             * 记录手指按下的 X 值
             * */
            case MotionEvent.ACTION_DOWN:
                xDowm = ev.getRawX();
                lastX = xDowm;
                break;

            /**
             * 判断是否滑动，是滑动的话就拦截
             * */
            case MotionEvent.ACTION_MOVE:
                curX = ev.getRawX();
                int dis = (int) Math.abs(curX - xDowm);
                lastX = curX;
                if (dis > mTouchSlop) return true;
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                curX = event.getRawX();
                lastX = curX;
                Log.d(TAG,"我在 onTouchEvent() 中 进入 ACTION_DOWN 了" + curX);
                return true;

            case MotionEvent.ACTION_MOVE:
                curX = event.getRawX();
                int distance = (int) (lastX - curX);
                //滑动距离当前位置 dis 的距离
                scrollBy(distance,0);
                lastX = curX;
                break;
            case MotionEvent.ACTION_UP:
                // 将页面对齐，可能滑动对中间就松手了
                // 如果滑动超多一半，就往少的那一边移动
                // 当左边没有下一个我们依然向右边滑动的时候，index = 0，
                // getScrollX() 为负数，所以 ds = 0 - getScrollX(),松手的时候刚好滑动回左边

                // 当右边没有下一个的时候，我们依然向左边滑动，此时的 index = getChildCount();
                // 如果我们依然按照 当 item 滑动超过一般屏幕的时候 就滑动下一个上来的话，
                // 右边已经没有下一个了，所以我们要判定 index 是不是 等于 getChildCount()
                // 如果等于的话，我们就不能在按照原来的加载方案，而是 让 当前的item 回弹到最右边，
                // 让屏幕还是显示当前的 item
                int index = (getScrollX() + getWidth() / 2) / getWidth();
                int ds = (int) (index * getWidth() - getScrollX());

                // item 回弹到 最右边的距离 other 为负值，因为向右移动的话，偏移值 是负数
                int other = ds - getWidth();

                // == 的话，表明 index 是最后一个 item 了，需要回弹
                if (index  == getChildCount()) {
                    mScroller.startScroll(getScrollX(),0,other,0);
                    invalidate();
                } else {
                    mScroller.startScroll(getScrollX(),0,ds,0);
                    invalidate();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }
}
