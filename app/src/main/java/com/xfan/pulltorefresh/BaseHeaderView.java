package com.xfan.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by 范强 on 2016/4/18.
 */
public abstract class BaseHeaderView extends RelativeLayout{

    public BaseHeaderView(Context context) {
        this(context, null);
    }

    public BaseHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public abstract void pulling(boolean reBack);
    public abstract void willRefresh();
    public abstract void refreshing();
    public abstract void complete();
}
