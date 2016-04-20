package com.xfan.pulltorefresh;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<String> datas;
    private PullToRefreshView mRefreshLayout;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        datas = new ArrayList<>();

        datas.add("AAAA");
        datas.add("BBBB");
        datas.add("CCCC");
        datas.add("DDDD");
        datas.add("EEEE");
        datas.add("FFFF");
        datas.add("GGGG");
        datas.add("HHHH");
        datas.add("IIII");
        datas.add("JJJJ");
        datas.add("KKKK");

        mRefreshLayout = (PullToRefreshView) findViewById(R.id.refresh_view);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(new RecyclerView.Adapter(){
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = View.inflate(parent.getContext(), android.R.layout.simple_list_item_1, null);
                return new ViewHolder(itemView);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ViewHolder h = (ViewHolder) holder;
                h.text.setText(datas.get(position));
            }

            @Override
            public int getItemCount() {
                return datas.size();
            }
        });

        mRefreshLayout.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.refreshComplete();
                    }
                }, 2000);
            }
        });
    }

    private static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView text;

        public ViewHolder(View itemView) {
            super(itemView);

            text = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }
}
