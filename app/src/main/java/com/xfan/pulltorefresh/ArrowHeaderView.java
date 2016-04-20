package com.xfan.pulltorefresh;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by 范强 on 2016/4/20.
 */
public class ArrowHeaderView extends BaseHeaderView {

    private ImageView mArrow;
    private TextView mTipText;
    private TextView mRefreshTime;
    private ProgressBar mProgressBar;
    private ObjectAnimator rorateAnim;
    private ObjectAnimator reBackAnim;

    public ArrowHeaderView(Context context) {
        this(context, null);
    }

    public ArrowHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.header_pull_to_refresh, this);
        mArrow = (ImageView) findViewById(R.id.head_arrowImageView);
        mTipText = (TextView) findViewById(R.id.head_tipsTextView);
        mRefreshTime = (TextView) findViewById(R.id.head_lastUpdatedTextView);
        mProgressBar = (ProgressBar) findViewById(R.id.head_progressBar);

        rorateAnim= ObjectAnimator.ofFloat(mArrow, View.ROTATION, 0, 180f).setDuration(100);
        reBackAnim= ObjectAnimator.ofFloat(mArrow, View.ROTATION, 180f, 0).setDuration(100);
    }

    @Override
    public void pulling(boolean reBack) {
        if(reBack){
            mArrow.setVisibility(VISIBLE);
            reBackAnim.start();
            mProgressBar.setVisibility(GONE);
            mTipText.setText("下拉刷新");
            mRefreshTime.setVisibility(GONE);
        } else {
            mArrow.setVisibility(VISIBLE);
            mArrow.setRotation(0);
            mProgressBar.setVisibility(GONE);
            mTipText.setText("下拉刷新");
            mRefreshTime.setVisibility(GONE);
        }
    }

    @Override
    public void willRefresh() {
        mArrow.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mTipText.setVisibility(View.VISIBLE);
        mRefreshTime.setVisibility(View.VISIBLE);
        rorateAnim.start();
        mTipText.setText("松开刷新");
    }

    @Override
    public void refreshing() {
        mArrow.setVisibility(GONE);
        mProgressBar.setVisibility(VISIBLE);
        mTipText.setText("正在刷新 ...");
        mRefreshTime.setVisibility(VISIBLE);
        mRefreshTime.setText("最近刷新：19:00");
    }

    @Override
    public void complete() {
        mArrow.setVisibility(VISIBLE);
        mArrow.setRotation(0);
        mProgressBar.setVisibility(GONE);
        mTipText.setText("下拉刷新");
        mRefreshTime.setVisibility(GONE);
    }
}
