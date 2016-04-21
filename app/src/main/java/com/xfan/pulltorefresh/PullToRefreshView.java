package com.xfan.pulltorefresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.LinearLayout;

public class PullToRefreshView extends LinearLayout {
    private static final String TAG = "PullToRefreshViewImpl";

    private static final int STATE_PULLING = 1;
    private static final int STATE_WILL_REFRESH = 2;
    private static final int STATE_REFRESHING = 3;
    private static final int STATE_COMPLETE = 4;
    private int mHeaderState = STATE_COMPLETE;

    private BaseHeaderView mHeaderView;
    private View mTarget;

    private boolean isFirstLayout = true;

    private int mActivePointerId = -1;
    private float mStartY;
    private int mTouchSlop;
    private int mHeaderSize;
    private int mHeaderMargin;
    private OnRefreshListener mOnRefreshListener;
    private int mMaxMargin;

    public PullToRefreshView(Context context) {
        this(context, null);
    }

    public PullToRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setHeaderView(new ArrowHeaderView(context));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(getChildCount() == 2){
            mTarget = getChildAt(1);
        } else if(getChildCount() > 2){
            throw new IllegalArgumentException("Child 不得大于1");
        }
    }

    public void setHeaderView(BaseHeaderView view){
        if(mHeaderView == view){
            return;
        }
        if(mHeaderView != null){
            removeViewAt(0);
        }
        mHeaderView = view;
        isFirstLayout = true;
        addView(mHeaderView, 0, getHeaderLayoutParams());
    }

    public void setTargetView(View target){
        if(mTarget == target){
            return;
        }
        if(mTarget != null){
            removeViewAt(1);
        }
        mTarget = target;
        isFirstLayout = true;
        addView(mTarget, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public void setOnRefreshListener(OnRefreshListener listener){
        this.mOnRefreshListener = listener;
    }

    public void refreshComplete(){
        animToStartLocal();
    }

    protected LayoutParams getHeaderLayoutParams(){
        return new LayoutParams(LayoutParams.MATCH_PARENT, getResources().getDimensionPixelOffset(R.dimen.pull_header_size));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(getChildCount() > 2){
            throw new IllegalArgumentException("Child 不得大于1");
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(isFirstLayout){
            mHeaderSize = mHeaderView.getMeasuredHeight();
            mMaxMargin = mHeaderSize * 2;
            MarginLayoutParams params = (MarginLayoutParams) mHeaderView.getLayoutParams();
            params.topMargin = -mHeaderSize;
            mHeaderMargin = -mHeaderSize;
            isFirstLayout = false;
        }
        super.onLayout(changed, l, t, r, b);
    }

    public boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
            mStartY = ev.getY(newPointerIndex);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isIntercept = false;
        int action = ev.getActionMasked();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                if(mActivePointerId == -1){
                    return false;
                }
                float dy = ev.getY(0);
                if(dy == -1){
                    return false;
                }
                mStartY = dy;
                isIntercept = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == -1) {
                    return false;
                }
                if(!isEnabled() || mTarget == null || mHeaderState != STATE_COMPLETE|| canChildScrollUp()){
                    return false;
                }
                float my = ev.getY(ev.findPointerIndex(mActivePointerId));
                if (my == -1) {
                    return false;
                }
                float offsetY = my - mStartY;
                if (offsetY > mTouchSlop) {
                    mStartY -= mTouchSlop;
                    isIntercept = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isIntercept = false;
                break;
        }

        return isIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action){
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
            case MotionEvent.ACTION_MOVE:
                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                float Y = event.getY(pointerIndex);
                float offY = Y - mStartY;
                updateHeaderMargin(offY);
                mStartY = Y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(mHeaderState == STATE_WILL_REFRESH){
                    animToRefreshLocal();
                } else if(mHeaderState == STATE_PULLING){
                    animToStartLocal();
                }
                mActivePointerId = -1;
                break;
        }
        return false;
    }

    private void updateHeaderMargin(float offY){
        if(mHeaderMargin >= mMaxMargin){
            mHeaderMargin = mMaxMargin;
        }
        float maxMargin = mMaxMargin;
        float scale = .3f;
        if(mHeaderMargin > 0){
            scale = (maxMargin - mHeaderMargin) / mMaxMargin * .3f;
        }
        mHeaderMargin = mHeaderMargin + (int) (offY * scale);
        if (mHeaderMargin <= -mHeaderSize) {
            mHeaderMargin = -mHeaderSize;
        }
        MarginLayoutParams params = (MarginLayoutParams) mHeaderView.getLayoutParams();
        if(params.topMargin == mHeaderMargin){
            return;
        }
        params.topMargin = mHeaderMargin;
        requestLayout();
        updateHeaderState();
    }

    private void updateHeaderState(){
        if(mHeaderState == STATE_COMPLETE || mHeaderState == STATE_WILL_REFRESH&& mHeaderMargin <= mHeaderState){
            mHeaderView.pulling(mHeaderState == STATE_WILL_REFRESH);
            mHeaderState = STATE_PULLING;
        } else if(mHeaderState == STATE_PULLING && mHeaderMargin > mHeaderState){
            mHeaderView.willRefresh();;
            mHeaderState = STATE_WILL_REFRESH;
        }
    }

    private void animToRefreshLocal(){
        if(mHeaderState != STATE_WILL_REFRESH){
            return;
        }
        mHeaderState = STATE_REFRESHING;
        ValueAnimator anim = ValueAnimator.ofInt(mHeaderMargin, 0);
        anim.setDuration(100);
        anim.setInterpolator(new DecelerateInterpolator(2));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int margin = (int) animation.getAnimatedValue();
                MarginLayoutParams params = (MarginLayoutParams) mHeaderView.getLayoutParams();
                params.topMargin = margin;
                mHeaderMargin = margin;
                mHeaderView.setLayoutParams(params);
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mHeaderView.refreshing();
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onRefresh();
                }
            }
        });
        anim.start();
    }

    private void animToStartLocal(){
        ValueAnimator anim = ValueAnimator.ofInt(mHeaderMargin, -mHeaderSize);
        anim.setDuration(100);
        anim.setInterpolator(new DecelerateInterpolator(2));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int margin = (int) animation.getAnimatedValue();
                MarginLayoutParams params = (MarginLayoutParams) mHeaderView.getLayoutParams();
                params.topMargin = margin;
                mHeaderMargin = margin;
                mHeaderView.setLayoutParams(params);
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mHeaderState = STATE_COMPLETE;
            }
        });
        anim.start();
    }

    public interface OnRefreshListener{
        void onRefresh();
    }
}
