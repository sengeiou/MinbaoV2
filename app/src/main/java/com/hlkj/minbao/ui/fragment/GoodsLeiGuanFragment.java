package com.hlkj.minbao.ui.fragment;


import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hlkj.minbao.R;
import com.hlkj.minbao.presenter.GoodsLeiGuanPresenter;
import com.hlkj.minbao.ui.adapter.GoodsLeiGuanListAdapter;
import com.hlkj.minbao.view.IGoodsLeiGuanView;
import com.wxh.common4mvp.base.baseImpl.BaseFragment;
import com.wxh.common4mvp.customView.RecycleViewDivider;
import com.wxh.common4mvp.util.SystemUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class GoodsLeiGuanFragment extends BaseFragment<GoodsLeiGuanPresenter> implements IGoodsLeiGuanView {

    @BindView(R.id.rv_list_leiguan)
    RecyclerView rvListLeiguan;

    private GoodsLeiGuanListAdapter mAdapter;
    private Handler mHandler;

    private List<JSONObject> mdataList;

    @Override
    public GoodsLeiGuanPresenter initPresenter() {
        return new GoodsLeiGuanPresenter(mContext, mFragmentName, this);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_goods_leiguan;
    }

    @Override
    public void onUserVisible() {

    }

    @Override
    public void onViewInit() {
        super.onViewInit();
        try {
            mdataList = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                JSONObject jo = new JSONObject();
                jo.put("name", "火雷管");
                jo.put("type", "1-1");
                jo.put("count", 200);

                mdataList.add(jo);
            }

            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case GoodsLeiGuanListAdapter.MSG_LISTITEM_FOOTVIEW_CLICK:
                            break;
                        case GoodsLeiGuanListAdapter.MSG_LISTITEM_ITEM_CLICK:
                            break;
                        default:
                            break;
                    }
                }
            };

            mAdapter = new GoodsLeiGuanListAdapter(getActivity(), mHandler);

            rvListLeiguan.setLayoutManager(new LinearLayoutManager(mContext));
            rvListLeiguan.setItemAnimator(new DefaultItemAnimator());
            rvListLeiguan.addItemDecoration(new RecycleViewDivider(mContext, LinearLayoutManager.VERTICAL,
                    SystemUtils.dp2px(mContext, 10), Color.parseColor("#00000000")));
            rvListLeiguan.setAdapter(mAdapter);

            Observable
                    .timer(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            mAdapter.updateData(mdataList);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            default:
                break;
        }
    }

    public GoodsLeiGuanFragment() {
        // Required empty public constructor
    }

    public static GoodsLeiGuanFragment newInstance() {

        Bundle args = new Bundle();

        GoodsLeiGuanFragment fragment = new GoodsLeiGuanFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
