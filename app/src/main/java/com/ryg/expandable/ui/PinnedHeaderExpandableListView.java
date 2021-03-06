/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014 singwhatiwanna
 * https://github.com/singwhatiwanna
 * http://blog.csdn.net/singwhatiwanna
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.ryg.expandable.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.AbsListView.OnScrollListener;

public class PinnedHeaderExpandableListView extends ExpandableListView implements OnScrollListener {
    private static final String TAG = "PinnedHeaderExpandable";
    private static final boolean DEBUG = true;

    public interface OnHeaderUpdateListener {
        /**
         * 返回一个view对象即可
         * 注意：view必须要有LayoutParams
         */
        public View getPinnedHeader();

        public void updatePinnedHeader(View headerView, int firstVisibleGroupPos);
    }

    private View mHeaderView;
    private int mHeaderWidth;
    private int mHeaderHeight;

    private View mTouchTarget;

    private OnScrollListener mScrollListener;
    private OnHeaderUpdateListener mHeaderUpdateListener;

    private boolean mActionDownHappened = false;
    protected boolean mIsHeaderGroupClickable = true;


    public PinnedHeaderExpandableListView(Context context) {
        super(context);
        initView();
    }

    public PinnedHeaderExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PinnedHeaderExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        setFadingEdgeLength(0);
        setOnScrollListener(this);
    }

    // 这是 AbsListView 里面的方法
    @Override
    public void setOnScrollListener(OnScrollListener l) {
        if (l != this) {
            mScrollListener = l;
        } else {
            mScrollListener = null;
        }
        super.setOnScrollListener(this);
    }

    /**
     * 给group添加点击事件监听
     *
     * @param onGroupClickListener   监听
     * @param isHeaderGroupClickable 表示header是否可点击<br/>
     *                               note : 当不想group可点击的时候，需要在OnGroupClickListener#onGroupClick中返回true，
     *                               并将isHeaderGroupClickable设为false即可
     */
    public void setOnGroupClickListener(OnGroupClickListener onGroupClickListener, boolean isHeaderGroupClickable) {
        mIsHeaderGroupClickable = isHeaderGroupClickable;
        super.setOnGroupClickListener(onGroupClickListener);
    }

    /**
     * 设置头部更新监听
     *
     * @param listener 为 null, 则会取消头部固定的 View; 不为 null, 则会添加头部固定的 View.
     */
    public void setOnHeaderUpdateListener(OnHeaderUpdateListener listener) {
        mHeaderUpdateListener = listener;
        if (listener == null) {
            mHeaderView = null;
            mHeaderWidth = mHeaderHeight = 0;
            return;
        }
        // 获取头部固定的 View.
        mHeaderView = listener.getPinnedHeader();
        int firstVisiblePos = getFirstVisiblePosition();
        long expandableListPos = getExpandableListPosition(firstVisiblePos);
        // 获取第一个可见的组的索引号
        int firstVisibleGroupPos = getPackedPositionGroup(expandableListPos);
        listener.updatePinnedHeader(mHeaderView, firstVisibleGroupPos);
        requestLayout();
        postInvalidate();
    }

    // 测量
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 头部 View 不存在, 不去测量
        if (mHeaderView == null) {
            return;
        }
        // 测量头部 View 的宽和高
        measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec); // 测量 headerView 的宽高
        mHeaderWidth = mHeaderView.getMeasuredWidth();
        mHeaderHeight = mHeaderView.getMeasuredHeight();
        Log.d(TAG, "mHeaderWidth=" + mHeaderWidth + ", mHeaderHeight=" + mHeaderHeight);
    }

    // 布局
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // 头部 View 不存在, 不去布局
        if (mHeaderView == null) {
            return;
        }
        int delta = mHeaderView.getTop();
        Log.d(TAG, "mHeaderView delta=" + delta);
        mHeaderView.layout(0, delta, mHeaderWidth, mHeaderHeight + delta); // 确定 headerView 的位置
    }

    // 用来绘制 Children 的方法
    // 是在 View 的 draw() 方法里面调用的
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHeaderView != null) {
            drawChild(canvas, mHeaderView, getDrawingTime()); // 绘制 headerView 到屏幕上
        }
    }

    // 点击绘制那个头部的事件,对应展开或折叠对应的子组
    // 处理头部固定的 View 的点击事件
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        // 把一个触摸点转为 ListView 中包含这个点的那个条目的 position
        int pos = pointToPosition(x, y);
        if (mHeaderView != null) {
            Log.d(TAG, "dispatchTouchEvent x=" + x + ", y=" + y + ", pos=" + pos + ", mHeaderView.getTop()=" + mHeaderView.getTop()
                    + ", mHeaderView.getBottom()=" + mHeaderView.getBottom());
        }
        // 判断点击事件位于 mHeaderView 上面
        if (mHeaderView != null && y >= mHeaderView.getTop() && y <= mHeaderView.getBottom()) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                // 再具体定位是触摸了头部布局里面的哪个 View, 找不到的话, 就返回头部布局本身了.
                mTouchTarget = getTouchTarget(mHeaderView, x, y);
                mTouchTarget.setPressed(true);
                mActionDownHappened = true;
            } else if (ev.getAction() == MotionEvent.ACTION_UP) {
                View touchTarget = getTouchTarget(mHeaderView, x, y);
                if (touchTarget == mTouchTarget && mTouchTarget.isClickable()) {
                    mTouchTarget.performClick();
                    invalidate(new Rect(0, 0, mHeaderWidth, mHeaderHeight));
                } else if (mIsHeaderGroupClickable) {
                    // flat list position -> packed position -> group position(组的索引号)
                    int groupPosition = getPackedPositionGroup(getExpandableListPosition(pos));
                    if (groupPosition != INVALID_POSITION && mActionDownHappened) {
                        if (isGroupExpanded(groupPosition)) {
                            collapseGroup(groupPosition);
                        } else {
                            expandGroup(groupPosition);
                        }
                    }
                }
                mTouchTarget.setPressed(false);
                mActionDownHappened = false;
            }
            return true;
        }

        return super.dispatchTouchEvent(ev);
    }

    private View getTouchTarget(View view, int x, int y) {
        if (!(view instanceof ViewGroup)) {
            return view;
        }

        ViewGroup parent = (ViewGroup) view;
        int childrenCount = parent.getChildCount();
        final boolean customOrder = isChildrenDrawingOrderEnabled();
        View target = null;
        for (int i = childrenCount - 1; i >= 0; i--) {
            final int childIndex = customOrder ? getChildDrawingOrder(childrenCount, i) : i;
            final View child = parent.getChildAt(childIndex);
            if (isTouchPointInView(child, x, y)) {
                target = child;
                break;
            }
        }
        if (target == null) {
            target = parent;
        }

        return target;
    }

    /**
     * 触摸点是否在指定的 View 上
     *
     * @param view
     * @param x
     * @param y
     * @return
     */
    private boolean isTouchPointInView(View view, int x, int y) {
        // View 是否可以点击
        if (view.isClickable() && y >= view.getTop() && y <= view.getBottom()
                && x >= view.getLeft() && x <= view.getRight()) {
            return true;
        }
        return false;
    }

    public void requestRefreshHeader() {
        refreshHeader();
        invalidate(new Rect(0, 0, mHeaderWidth, mHeaderHeight));
    }

    /**
     * 刷新头部布局
     * <p>
     * 调整头部布局的位置
     */
    protected void refreshHeader() {
        if (mHeaderView == null) {
            return;
        }
        int firstVisiblePos = getFirstVisiblePosition(); // 第一个可见元素的索引
        int pos = firstVisiblePos + 1; // 第一个可见元素的索引加 1 的位置
        // 先把 flat list position -> packed position, 再从 packed position 得到 group position
        int firstVisibleGroupPos = getPackedPositionGroup(getExpandableListPosition(firstVisiblePos)); // 第一个可见的 Group 在所有 Group 中的索引
        int group = getPackedPositionGroup(getExpandableListPosition(pos)); // 获取 pos 位置上的 Group 在所有 Group 中的索引
        if (DEBUG) {
            Log.d(TAG, "refreshHeader firstVisibleGroupPos=" + firstVisibleGroupPos);
        }

        if (group == firstVisibleGroupPos + 1) { // 是两个相邻的 Group
            View view = getChildAt(1); // 所获取的都是屏幕上可见的child, 而不是全部的 child. 这里的 getChildAt(1) 是一个 group 的 View
            Log.d(TAG, "refreshHeader group == firstVisibleGroupPos + 1, view=" + view + ", group=" + group);
            if (view == null) {
                Log.w(TAG, "Warning : refreshHeader getChildAt(1)=null");
                return;
            }
            Log.d(TAG, "refreshHeader view.getTop()=" + view.getTop() + ", mHeaderHeight=" + mHeaderHeight);
            if (view.getTop() <= mHeaderHeight) {
                int delta = mHeaderHeight - view.getTop();
                Log.d(TAG, "refreshHeader delta=" + delta);
                mHeaderView.layout(0, -delta, mHeaderWidth, mHeaderHeight - delta);
            } else {
                //TODO : note it, when cause bug, remove it
                mHeaderView.layout(0, 0, mHeaderWidth, mHeaderHeight);
            }
        } else {
            mHeaderView.layout(0, 0, mHeaderWidth, mHeaderHeight);
        }

        if (mHeaderUpdateListener != null) {
            mHeaderUpdateListener.updatePinnedHeader(mHeaderView, firstVisibleGroupPos);
        }
    }

    // AbsListView 中 OnScrollListener 中的方法
    // 当滑动状态改变的时候回调
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mScrollListener != null) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    // AbsListView 中 OnScrollListener 中的方法
    // 当滑动时候回调
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        Log.d(TAG, "onScroll: view=" + view + ", firstVisibleItem=" + firstVisibleItem + ", visibleItemCount=" + visibleItemCount + ", totalItemCount=" + totalItemCount);
        if (totalItemCount > 0) {
            // 当获取到总的元素个数的时候, 就刷新 header
            refreshHeader();
        }
        if (mScrollListener != null) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

}