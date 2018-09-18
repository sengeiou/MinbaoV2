package com.hlkj.minbao.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;

import com.hlkj.minbao.R;
import com.hlkj.minbao.presenter.GoodsReceiveListPresenter;
import com.hlkj.minbao.ui.adapter.GoodsReceiveListAdapter;
import com.hlkj.minbao.view.IGoodsReceiveListView;
import com.wxh.common4mvp.base.baseImpl.BaseActivity;
import com.wxh.common4mvp.customView.RecycleViewDivider;
import com.wxh.common4mvp.util.NetWorkUtils;
import com.wxh.common4mvp.util.SystemUtils;
import com.wxh.common4mvp.util.ToastUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class GoodsReceiveListActivity extends BaseActivity<GoodsReceiveListPresenter> implements IGoodsReceiveListView {

    @BindView(R.id.recycle)
    RecyclerView rvRecycle;
    @BindView(R.id.layout_refresh)
    SwipeRefreshLayout layoutRefresh;
    @BindView(R.id.layout_common_nodata)
    FrameLayout layoutCommonNodata;

    private GoodsReceiveListAdapter mAdapter;
    private Handler mHandler;

    private boolean isLoading = false;
    private boolean hasMore = false;
    private int pageIndex;

    private List<JSONObject> mdataList;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_goods_receive_list;
    }

    @Override
    protected void initToolBar() {
        setToolbarBtnLeft(R.mipmap.btn_common_back);
        setToolbarCenter(R.string.goods_receive_title);
    }

    @Override
    public GoodsReceiveListPresenter initPresenter() {
        return new GoodsReceiveListPresenter(this, mActivityName, this);
    }

    @Override
    public void onViewInit() {
        super.onViewInit();
        try {
            mdataList = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                JSONObject jo = new JSONObject();
                jo.put("title", String.format(getResources().getString(R.string.goodsReceive_list_item_title), "浙B66666", i));
                jo.put("person", "田泽平(保管员)\t\t李成博(爆破员)\n李国志(驾驶员)\t\t吴恒华(押运员)");
                jo.put("time", System.currentTimeMillis());

                JSONArray jaZhayao = new JSONArray();
                JSONObject joZhayao1 = new JSONObject();
                joZhayao1.put("content", "共使用100公斤XXX型号老干爹炸药");
                JSONObject joZhayao2 = new JSONObject();
                joZhayao2.put("content", "共使用200公斤XXX型号老干妈炸药");
                jaZhayao.put(joZhayao1);
                jaZhayao.put(joZhayao2);

                JSONArray jaLeyguan = new JSONArray();
                JSONObject joLeiguan1 = new JSONObject();
                joLeiguan1.put("content", "共使用1段1米100发老干爹雷管");
                JSONObject joLeiguan2 = new JSONObject();
                joLeiguan2.put("content", "共使用5段5米200发老干妈雷管");
                jaLeyguan.put(joLeiguan1);
                jaLeyguan.put(joLeiguan2);

                JSONArray jaSuolei = new JSONArray();
                JSONObject joSuolei = new JSONObject();
                joSuolei.put("content", "共使用200米老干爹导爆管");
                jaSuolei.put(joSuolei);

                jo.put("leiguan", jaLeyguan);
                jo.put("zhayao", jaZhayao);
                jo.put("suolei", jaSuolei);

                mdataList.add(jo);
            }

            pageIndex = 1;
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case GoodsReceiveListAdapter.MSG_LISTITEM_FOOTVIEW_CLICK:
                            break;
                        case GoodsReceiveListAdapter.MSG_LISTITEM_ITEM_CLICK:
                            Intent intent = new Intent(GoodsReceiveListActivity.this, GoodsReceiveActivity.class);
                            startActivity(intent);
                            break;
                        default:
                            break;
                    }
                }
            };

            mAdapter = new GoodsReceiveListAdapter(this, mHandler);

            rvRecycle.setLayoutManager(new LinearLayoutManager(this));
            rvRecycle.setItemAnimator(new DefaultItemAnimator());
            rvRecycle.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.VERTICAL,
                    SystemUtils.dp2px(this, 6), Color.parseColor("#00000000")));
            rvRecycle.setAdapter(mAdapter);
            rvRecycle.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && !rvRecycle.canScrollVertically(1)) {
                        //当正在刷新的时候，屏蔽加载更多
                        if (layoutRefresh.isRefreshing()) {
                            return;
                        }
                        if (!isLoading && hasMore) {
                            //分页加载
                            mAdapter.updateFootView(mAdapter.FOOTVIEW_TYPE_LOADING);
//                            actionGetList(true);
                        }
                    }
                }
            });

            layoutRefresh.setColorSchemeColors(getResources().getColor(R.color.color_common_blue));
            layoutRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    //下拉刷新
                    mAdapter.updateFootView(mAdapter.FOOTVIEW_TYPE_LOADING);
                    actionGetList(false);
                }
            });

            layoutRefresh.post(new Runnable() {
                @Override
                public void run() {
                    layoutRefresh.setRefreshing(true);
                    actionGetList(false);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actionGetList(final boolean isLoadMore) {
        if (isLoadMore) {
            pageIndex++;
            isLoading = true;
        }

        if (!NetWorkUtils.isNetworkAvailable(this)) {
            ToastUtils.showToast(R.string.toast_network_unable);
            layoutRefresh.setRefreshing(false);
            if (isLoadMore) {
                pageIndex--;
                isLoading = false;
                mAdapter.updateFootView(mAdapter.FOOTVIEW_TYPE_ERROR);
            }
            return;
        }

        Observable
                .timer(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        layoutRefresh.setRefreshing(false);
                        pageIndex = 1;
                        mAdapter.updateData(mdataList);
                    }
                });
    }
}
