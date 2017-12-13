package com.zs.demo.selectlabeltest;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.zs.demo.selectlabeltest.adapter.ChannelAdapter;
import com.zs.demo.selectlabeltest.bean.Channel;
import com.zs.demo.selectlabeltest.listener.ItemDragHelperCallBack;
import com.zs.demo.selectlabeltest.listener.OnChannelDragListener;
import com.zs.demo.selectlabeltest.listener.OnChannelListener;
import com.zs.demo.selectlabeltest.util.ConstanceValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 移动的动画需要把RecyclerView 放在FrameLayout
 */
public class ChannelDialogFragment extends DialogFragment implements OnChannelDragListener {
    private List<Channel> mDatas = new ArrayList<>();
    /**
     * 固定标题的个数
     */
    private int mNormalSize ;
    private int spanCount = 3;
    private ChannelAdapter mAdapter;
    private ImageView mIv;
    private RecyclerView mRecyclerView;
    private ItemTouchHelper mHelper;

    private OnChannelListener mOnChannelListener;

    public void setOnChannelListener(OnChannelListener onChannelListener) {
        mOnChannelListener = onChannelListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Dialog dialog = getDialog();
        if (dialog != null) {
            //添加动画
            dialog.getWindow().setWindowAnimations(R.style.dialogSlideAnim);
//            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return inflater.inflate(R.layout.dialog_channel, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mIv= (ImageView) view.findViewById(R.id.icon_collapse);
        processLogic();


        mIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public static ChannelDialogFragment newInstance(List<Channel> normalDatas,List<Channel> selectedDatas, List<Channel> unselectedDatas) {
        ChannelDialogFragment dialogFragment = new ChannelDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ConstanceValue.DATA_NORMAL, (Serializable) normalDatas);
        bundle.putSerializable(ConstanceValue.DATA_SELECTED, (Serializable) selectedDatas);
        bundle.putSerializable(ConstanceValue.DATA_UNSELECTED, (Serializable) unselectedDatas);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    private void setDataType(List<Channel> datas, int type) {
        for (int i = 0; i < datas.size(); i++) {
            datas.get(i).setItemType(type);
            Log.i("toast","myType:"+type);
        }
    }

    private void processLogic() {
        mDatas.add(new Channel(Channel.TYPE_MY, "我的频道", ""));
        Bundle bundle = getArguments();
        List<Channel> normalDatas = (List<Channel>) bundle.getSerializable(ConstanceValue.DATA_NORMAL);
        List<Channel> selectedDatas = (List<Channel>) bundle.getSerializable(ConstanceValue.DATA_SELECTED);
        List<Channel> unselectedDatas = (List<Channel>) bundle.getSerializable(ConstanceValue.DATA_UNSELECTED);
        mNormalSize = normalDatas.size();
        setDataType(normalDatas, Channel.TYPE_NORMAL);
        setDataType(selectedDatas, Channel.TYPE_MY_CHANNEL);
        setDataType(unselectedDatas, Channel.TYPE_OTHER_CHANNEL);

        mDatas.addAll(normalDatas);
        mDatas.addAll(selectedDatas);
        mDatas.add(new Channel(Channel.TYPE_OTHER, "频道推荐", ""));
        mDatas.addAll(unselectedDatas);

        mAdapter = new ChannelAdapter(mDatas);
        GridLayoutManager manager = new GridLayoutManager(getActivity(), spanCount);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int itemViewType = mAdapter.getItemViewType(position);
                return itemViewType == Channel.TYPE_MY_CHANNEL || itemViewType == Channel.TYPE_OTHER_CHANNEL || itemViewType == Channel.TYPE_NORMAL ? 1 : spanCount;
            }
        });
        ItemDragHelperCallBack callBack = new ItemDragHelperCallBack(this);
        mHelper = new ItemTouchHelper(callBack);
        mAdapter.setOnChannelDragListener(this);
        //attachRecyclerView
        mHelper.attachToRecyclerView(mRecyclerView);
    }


    private DialogInterface.OnDismissListener mOnDismissListener;

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss(dialog);
        }
    }

    @Override
    public void onStarDrag(BaseViewHolder baseViewHolder) {
        //开始拖动
        Log.i("toast", "开始拖动");
        mHelper.startDrag(baseViewHolder);
    }

    @Override
    public void onItemMove(int starPos, int endPos) {
//        if (starPos < 0||endPos<0) return;
        //我的频道之间移动
        if (mOnChannelListener != null){
            // 去除标题所占的一个index 和 固定标题所占的个数
            mOnChannelListener.onItemMove(starPos - 1 - mNormalSize, endPos - 1 - mNormalSize);
        }
        onMove(starPos, endPos);
    }

    private void onMove(int starPos, int endPos) {
        Channel startChannel = mDatas.get(starPos);
        //先删除之前的位置
        mDatas.remove(starPos);
        //添加到现在的位置
        mDatas.add(endPos, startChannel);
        mAdapter.notifyItemMoved(starPos, endPos);
    }

    @Override
    public void onMoveToMyChannel(int starPos, int endPos) {
        // 移动到我的频道
        onMove(starPos, endPos);
        Log.d("My_Log","title = " + mDatas.get(endPos).getTitle());
        if (mOnChannelListener != null){
            // 去除标题所占的一个index 和 固定标题所占的个数
            mOnChannelListener.onMoveToMyChannel(starPos - 1 - mAdapter.getMyChannelSize(), endPos - 1 - mNormalSize);
        }
    }

    @Override
    public void onMoveToOtherChannel(int starPos, int endPos) {
        // 移动到推荐频道
        onMove(starPos, endPos);
        Log.d("My_Log","title = " + mDatas.get(endPos).getTitle());
        if (mOnChannelListener != null){
            // 去除标题所占的一个index 和 固定标题所占的个数
            mOnChannelListener.onMoveToOtherChannel(starPos - 1 - mNormalSize, endPos - 2 - mAdapter.getMyChannelSize());
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        // FragmentDialog中默认消失 不想消失让重现
        if (getDialog() != null){
            getDialog().show();
        }
    }
}