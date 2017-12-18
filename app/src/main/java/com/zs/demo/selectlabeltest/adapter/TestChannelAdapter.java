package com.zs.demo.selectlabeltest.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.zs.demo.selectlabeltest.R;
import com.zs.demo.selectlabeltest.bean.Channel;
import com.zs.demo.selectlabeltest.listener.OnChannelDragListener;

import java.util.List;


/**
 * @author Administrator
 */
public class TestChannelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private BaseViewHolder mEditViewHolder;
    private boolean mIsEdit;
    private long startTime;
    // touch 间隔时间  用于分辨是否是 "点击"
    private static final long SPACE_TIME = 100;
    private RecyclerView mRecyclerView;
    private Context mContext;
    private List<Channel> mData;

    public TestChannelAdapter(Context context , List<Channel> data) {
        this.mContext = context;
        this.mData = data;
        //默认没有编辑
        mIsEdit = false;

    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getItemType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mRecyclerView = (RecyclerView) parent;
        View view;
        if (viewType == 1 || viewType == 2){
            view = View.inflate(mContext,R.layout.item_channel_title,null);
            return new TitleViewHolder(view);
        }else{
            view = View.inflate(mContext,R.layout.item_channel_layout,null);
            return new TagViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final Channel channel = mData.get(position);
        int type = getItemViewType(position);
        if (holder instanceof TitleViewHolder){
            TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
            titleViewHolder.tvTitle.setText(channel.getTitle());
            if (type == 1){
                titleViewHolder.tvTitle2.setVisibility(View.VISIBLE);
                titleViewHolder.tvEdit.setVisibility(View.VISIBLE);
            }else{
                titleViewHolder.tvTitle2.setVisibility(View.GONE);
                titleViewHolder.tvEdit.setVisibility(View.GONE);
            }
            titleViewHolder.tvEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }else{
            TagViewHolder tagViewHolder = (TagViewHolder) holder;
            tagViewHolder.tvChannel.setText(channel.getTitle());
            if (type == 3){
                tagViewHolder.tvChannel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Log.d("My_Log","移动到推荐频道列表");
                        //执行删除，移动到推荐频道列表
                        int otherFirstPosition = getOtherFirstPosition();
                        int currentPosition = holder.getAdapterPosition();
                        //获取到目标View
                        View targetView = mRecyclerView.getLayoutManager().findViewByPosition(otherFirstPosition);
                        //获取当前需要移动的View
                        View currentView = mRecyclerView.getLayoutManager().findViewByPosition(currentPosition);
                        // 如果targetView不在屏幕内,则indexOfChild为-1  此时不需要添加动画,因为此时notifyItemMoved自带一个向目标移动的动画
                        // 如果在屏幕内,则添加一个位移动画
                        if (mRecyclerView.indexOfChild(targetView) >= 0 && otherFirstPosition != -1) {
                            RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
                            int spanCount = ((GridLayoutManager) manager).getSpanCount();
                            int targetX = targetView.getLeft();
                            int targetY = targetView.getTop();
                            int myChannelSize = getMyChannelSize();//这里我是为了偷懒 ，算出来我的频道的大小
                            if (myChannelSize % spanCount == 1) {
                                //我的频道最后一行 只有一个，移动后
                                targetY -= targetView.getHeight();
                            }

                            //我的频道 移动到 推荐频道的第一个
                            channel.setItemType(Channel.TYPE_OTHER_CHANNEL);//改为推荐频道类型

                            if (onChannelDragListener != null) {
                                onChannelDragListener.onMoveToOtherChannel(currentPosition, otherFirstPosition - 1);
                            }
                            startAnimation(currentView, targetX, targetY);
                        } else {
                            channel.setItemType(Channel.TYPE_OTHER_CHANNEL);//改为推荐频道类型
                            if (otherFirstPosition == -1) {
                                otherFirstPosition = mData.size();
                            }
                            if (onChannelDragListener != null) {
                                onChannelDragListener.onMoveToOtherChannel(currentPosition, otherFirstPosition - 1);
                            }
                        }
                    }
                });

                tagViewHolder.tvChannel.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (onChannelDragListener != null) {
                            onChannelDragListener.onStarDrag(holder);
                        }
                        return true;
                    }
                });
            }else if (type == 5){

            }else if (type == 4){
                tagViewHolder.tvChannel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("My_Log","移动");
                        int myLastPosition = getMyLastPosition();
                        int currentPosition = holder.getAdapterPosition();
                        //获取到目标View
                        View targetView = mRecyclerView.getLayoutManager().findViewByPosition(myLastPosition);
                        //获取当前需要移动的View
                        View currentView = mRecyclerView.getLayoutManager().findViewByPosition(currentPosition);
                        // 如果targetView不在屏幕内,则indexOfChild为-1  此时不需要添加动画,因为此时notifyItemMoved自带一个向目标移动的动画
                        // 如果在屏幕内,则添加一个位移动画
                        if (mRecyclerView.indexOfChild(targetView) >= 0 && myLastPosition != -1) {
                            RecyclerView.LayoutManager manager = mRecyclerView.getLayoutManager();
                            int spanCount = ((GridLayoutManager) manager).getSpanCount();
                            int targetX = targetView.getLeft() + targetView.getWidth();
                            int targetY = targetView.getTop();

                            int myChannelSize = getMyChannelSize();//这里我是为了偷懒 ，算出来我的频道的大小
                            if (myChannelSize % spanCount == 0) {
                                //添加到我的频道后会换行，所以找到倒数第4个的位置
                                View lastFourthView = mRecyclerView.getLayoutManager().findViewByPosition(getMyLastPosition() - (spanCount - 1));
//                                        View lastFourthView = mRecyclerView.getChildAt(getMyLastPosition() - (spanCount - 1));
                                targetX = lastFourthView.getLeft();
                                targetY = lastFourthView.getTop() + lastFourthView.getHeight();
                            }

                            // 推荐频道 移动到 我的频道的最后一个
                            channel.setItemType(Channel.TYPE_MY_CHANNEL);//改为推荐频道类型
                            if (onChannelDragListener != null){
                                onChannelDragListener.onMoveToMyChannel(currentPosition, myLastPosition + 1);
                            }

                            startAnimation(currentView, targetX, targetY);
                        } else {
                            channel.setItemType(Channel.TYPE_MY_CHANNEL);//改为推荐频道类型
                            if (myLastPosition == -1) {
                                myLastPosition = 0;//我的频道没有了，改成0
                            }
                            if (onChannelDragListener != null) {
                                onChannelDragListener.onMoveToMyChannel(currentPosition, myLastPosition + 1);
                            }
                        }
                    }
                });
            }else{

            }
        }

    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    private OnChannelDragListener onChannelDragListener;

    public void setOnChannelDragListener(OnChannelDragListener onChannelDragListener) {
        this.onChannelDragListener = onChannelDragListener;
    }


    /**
     * 包括固定标签和选择的标签
     * @return
     */
    public int getMyChannelSize() {
        int size = 0;
        for (int i = 0; i < mData.size(); i++) {
            Channel channel = mData.get(i);
            if (channel.getItemType() == Channel.TYPE_MY_CHANNEL || channel.getItemType() == Channel.TYPE_NORMAL) {
                size++;
            }
        }
        return size;

    }

    /**
     * 开启移动动画
     * @param currentView
     * @param targetX
     * @param targetY
     */
    private void startAnimation(final View currentView, int targetX, int targetY) {
        final ViewGroup parent = (ViewGroup) mRecyclerView.getParent();
        final ImageView mirrorView = addMirrorView(parent, currentView);
        TranslateAnimation animator = getTranslateAnimator(targetX - currentView.getLeft(), targetY - currentView.getTop());
        currentView.setVisibility(View.INVISIBLE);
        mirrorView.startAnimation(animator);
        animator.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                parent.removeView(mirrorView);//删除添加的镜像View
                if (currentView.getVisibility() == View.INVISIBLE) {
                    //显示隐藏的View
                    currentView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 添加需要移动的 镜像View
     *
     * 我们要获取cache首先要通过setDrawingCacheEnable方法开启cache，
     * 然后再调用getDrawingCache方法就可以获得view的cache图片了。
      buildDrawingCache方法可以不用调用，因为调用getDrawingCache方法时，
      如果cache没有建立，系统会自动调用buildDrawingCache方法生成cache。
      若想更新cache, 必须要调用destoryDrawingCache方法把旧的cache销毁，才能建立新的。
      当调用setDrawingCacheEnabled方法设置为false, 系统也会自动把原来的cache销毁。
     */
    private ImageView addMirrorView(ViewGroup parent, View view) {
        view.destroyDrawingCache();
        //首先开启Cache图片 ，然后调用view.getDrawingCache()就可以获取Cache图片
        view.setDrawingCacheEnabled(true);
        ImageView mirrorView = new ImageView(view.getContext());
        //获取该view的Cache图片
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        mirrorView.setImageBitmap(bitmap);
        //销毁掉cache图片
        view.setDrawingCacheEnabled(false);
        int[] locations = new int[2];
        // 获取当前View的坐标 包括状态栏高度
        view.getLocationOnScreen(locations);
        int[] parenLocations = new int[2];
        // 获取RecyclerView所在坐标 包括状态栏高度
        mRecyclerView.getLocationOnScreen(parenLocations);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
        // 计算镜像view所在位置
        params.setMargins(locations[0],  locations[1] - parenLocations[1], 0, 0);
        //在RecyclerView的Parent添加我们的镜像View，parent要是FrameLayout这样才可以放到那个坐标点
        parent.addView(mirrorView, params);
        return mirrorView;
    }

    private int ANIM_TIME = 360;

    /**
     * 获取位移动画
     */
    private TranslateAnimation getTranslateAnimator(float targetX, float targetY) {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetX,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetY);
        Log.i("toast","myA:"+targetX+"  :  "+targetY);
        // RecyclerView默认移动动画250ms 这里设置360ms 是为了防止在位移动画结束后 remove(view)过早 导致闪烁
        translateAnimation.setDuration(ANIM_TIME);
        translateAnimation.setFillAfter(true);
        return translateAnimation;
    }

    /**
     * 获取推荐频道列表的第一个position
     *
     * @return
     */
    private int getOtherFirstPosition() {
        //之前找到了第一个pos直接返回
//        if (mOtherFirstPosition != 0) return mOtherFirstPosition;
        for (int i = 0; i < mData.size(); i++) {
            Channel channel = (Channel) mData.get(i);
            if (Channel.TYPE_OTHER_CHANNEL == channel.getItemType()) {
                //找到第一个直接返回
                return i;
            }
        }
        return -1;
    }

    /**
     * 我的频道最后一个的position
     *
     * @return
     */
    private int getMyLastPosition() {
        for (int i =  0; i < mData.size(); i++) {
            Channel channel = mData.get(i);
            if (Channel.TYPE_OTHER == channel.getItemType()) {
                // 找到推荐频道坐标减1
                return i - 1;
            }
        }
        return -1;
    }

    /**
     * 开启编辑模式
     */
    private void startEditMode(boolean isEdit) {
        mIsEdit = isEdit;
        int visibleChildCount = mRecyclerView.getChildCount();
        for (int i = 0; i < visibleChildCount; i++) {
            View view = mRecyclerView.getChildAt(i);
            ImageView imgEdit = (ImageView) view.findViewById(R.id.ivDelete);
            if (imgEdit != null) {
                boolean isVis = imgEdit.getTag() == null ? false : (boolean) imgEdit.getTag();
                imgEdit.setVisibility(isVis && isEdit ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }

    class TitleViewHolder extends RecyclerView.ViewHolder{

        private TextView tvTitle;
        private TextView tvTitle2;
        private TextView tvEdit;

        public TitleViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTitle2 = itemView.findViewById(R.id.tvTitle2);
            tvEdit = itemView.findViewById(R.id.tvEdit);
        }
    }

    class TagViewHolder extends RecyclerView.ViewHolder{

        private TextView tvChannel;

        public TagViewHolder(View itemView) {
            super(itemView);
            tvChannel = itemView.findViewById(R.id.tvChannel);
        }
    }

}
