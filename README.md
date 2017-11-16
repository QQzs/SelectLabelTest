# SelectLabelTest
Android 选择标签 标签拖动
## 效果图
![](https://github.com/QQzs/Image/blob/master/SelectLabelTest/lable_move_art.gif)
        ![](https://github.com/QQzs/Image/blob/master/SelectLabelTest/lable_transition_art.gif)
<br>支持标签拖动排序 和 添加删除的动画效果，这个效果是从其他项目提取出来的，
[原项目地址](http://www.jianshu.com/p/af0e5459748e)
下面我来分析下这个效果实现方式，如有不足请指出。
<br>首先，整个表是写在一个RecyclerView中的布局文件很简单,需要注意的是这个RecyclerView一定要放在一个FrameLayout中，下面我会讲到。
### 布局文件
```Java
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical" android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/white">
    <ImageView
        android:id="@+id/icon_collapse"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="right"
        android:layout_marginTop="3.0dip"
        android:padding="10.0dip"
        android:scaleType="center"
        android:src="@drawable/category_edit_close"
        android:visibility="visible"/>
    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <android.support.v7.widget.RecyclerView
            android:id="@+id/recyclerView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:padding="10dp">

        </android.support.v7.widget.RecyclerView>
    </FrameLayout>
</LinearLayout>
```

### 代码部分
<br>添加三个数组，默认固定不可变的标签，选择的标签，未被选择的标签，固定标签不可添加删除，选择和未选择的标签可以转换，
因为所有标签都在有个Recyclerview中，把两个标题（我的频道和频道选择）也算进去了，所有标签类型有五种。

```Java
public List<Channel> mNormalDatas = new ArrayList<>();
    public List<Channel> mSelectedDatas = new ArrayList<>();
    public List<Channel> mUnSelectedDatas = new ArrayList<>();

    private String mChannel;

    private TextView tv_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_main = findViewById(R.id.tv_main);

        mNormalDatas.add(new Channel("手机"));
        mNormalDatas.add(new Channel("搞笑"));
        mNormalDatas.add(new Channel("关注"));

        mSelectedDatas.add(new Channel("头条"));
        mSelectedDatas.add(new Channel("新闻"));
        mSelectedDatas.add(new Channel("财经"));
        mSelectedDatas.add(new Channel("体育"));
        mSelectedDatas.add(new Channel("娱乐"));
        mSelectedDatas.add(new Channel("军事"));

        mUnSelectedDatas.add(new Channel("股票"));
        mUnSelectedDatas.add(new Channel("健康"));
        mUnSelectedDatas.add(new Channel("NBA"));
        mUnSelectedDatas.add(new Channel("教育"));
        mUnSelectedDatas.add(new Channel("星座"));
        mUnSelectedDatas.add(new Channel("科技"));
        mUnSelectedDatas.add(new Channel("育儿"));
        for(int i = 0;i< 50;i++){
            mUnSelectedDatas.add(new Channel("测试"));
        }
        setTitleData();

    }
    
    public static final int TYPE_MY = 1;
    public static final int TYPE_OTHER = 2;
    public static final int TYPE_MY_CHANNEL = 3;
    public static final int TYPE_OTHER_CHANNEL = 4;
    public static final int TYPE_NORMAL = 5;

    
```

这是定义了一个接口，里面有三个方法，分别是回调标签拖动，移动到我的频道 ，移动到其他频道。
```Java
public interface OnChannelListener {
    void onItemMove(int starPos, int endPos);
    void onMoveToMyChannel(int starPos, int endPos);
    void onMoveToOtherChannel(int starPos, int endPos);
}

```

所有的处理逻辑在adapter中：关于标签的拖动，这个不多说，和RecyclerView相关的ItemTouchHelper中，自带就有这个处理的效果，标签拖动只在我选择的标签中交换，前面标签定义了五种类型，移动过程中限制了，只在相同类型标签中交换。
```Java
@Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // 不同Type之间不可移动
        if (viewHolder.getItemViewType() != target.getItemViewType()) {
            return false;
        }
        if (onChannelDragListener != null)
            onChannelDragListener.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }
```
### Adapter中数据的处理
移动过程是，当标签发生移动时，先获取当前标签的坐标和目标位置的坐标，记下这个两个标签在Recyclerview中的position，同时还有这两个位置在屏幕具体的坐标，要注意，添加到我的频道时出现换行的情况 和 移动到其他频道时要换行的情况，还有不可见的情况，adapter注释中都已经很清楚，仔细分析即可，然后进行回调，在数据中先删除之前位置的标签，然后添加到目标位置，刷新数据。然后开启移动的动画效果，先生成一个标签的镜像View，要移动的并不是当前标签，而是这个镜像View，把动画的时间调大，就能看明白这个过程，为了防止移动的距离太长时，标签就已经出现了，需要把标签暂时隐藏，动画结束后再显示出来。上面说到的FrameLayout的作用在这，只有Recyclerview的parent是FrameLayout，镜像View的才能添加到指定的位置。
```Java
@Override
    protected void convert(final BaseViewHolder baseViewHolder, final Channel channel) {
        switch (baseViewHolder.getItemViewType()) {
            case Channel.TYPE_MY:
                //我的频道
                //赋值，以便之后修改文字
                mEditViewHolder = baseViewHolder;
                baseViewHolder.setText(R.id.tvTitle, channel.Title)
                        .setOnClickListener(R.id.tvEdit, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!mIsEdit) {
                                    startEditMode(true);
                                    baseViewHolder.setText(R.id.tvEdit, "完成");
                                } else {
                                    startEditMode(false);
                                    baseViewHolder.setText(R.id.tvEdit, "编辑");
                                }
                            }
                        });
                break;
            case Channel.TYPE_OTHER:
                //频道推荐
                baseViewHolder.setText(R.id.tvTitle, channel.Title)
                        .setVisible(R.id.tvEdit, false).setVisible(R.id.tvTitle2,false);
                break;
            case Channel.TYPE_NORMAL:
                baseViewHolder.setText(R.id.tvChannel, channel.Title);
                break;
            case Channel.TYPE_MY_CHANNEL:
                //我的频道列表
                baseViewHolder
                        .setVisible(R.id.ivDelete, mIsEdit)//编辑模式就显示删除按钮
                        .setOnLongClickListener(R.id.rlItemView, new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                if (!mIsEdit) {
                                    //开启编辑模式
                                    startEditMode(true);
                                    mEditViewHolder.setText(R.id.tvEdit, "完成");
                                }
                                if (onChannelDragListener != null) {
                                    onChannelDragListener.onStarDrag(baseViewHolder);
                                }
                                return true;
                            }
                        }).setOnTouchListener(R.id.tvChannel, new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!mIsEdit) return false;//正常模式无需监听触摸
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                startTime = System.currentTimeMillis();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                if (System.currentTimeMillis() - startTime > SPACE_TIME) {
                                    //当MOVE事件与DOWN事件的触发的间隔时间大于100ms时，则认为是拖拽starDrag
                                    if (onChannelDragListener != null) {
                                        onChannelDragListener.onStarDrag(baseViewHolder);
                                    }
                                }
                                break;
                            case MotionEvent.ACTION_CANCEL:
                            case MotionEvent.ACTION_UP:
                                startTime = 0;
                                break;
                        }
                        return false;
                    }
                }).getView(R.id.ivDelete).setTag(true);//在我的频道里面设置true标示，之后会根据这个标示来判断编辑模式是否显示
                baseViewHolder.setText(R.id.tvChannel, channel.Title).setOnClickListener(R.id.ivDelete, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //执行删除，移动到推荐频道列表
                        if (mIsEdit) {
                            int otherFirstPosition = getOtherFirstPosition();
                            int currentPosition = baseViewHolder.getAdapterPosition();
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
//                            GlobalParams.mRemovedChannels.add(channel);
                        }
                    }


                });
                break;
            case Channel.TYPE_OTHER_CHANNEL:
                //频道推荐列表
                baseViewHolder.setText(R.id.tvChannel, channel.Title).setVisible(R.id.ivDelete, false)
                        .setOnClickListener(R.id.tvChannel, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int myLastPosition = getMyLastPosition();
                                int currentPosition=baseViewHolder.getAdapterPosition();
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
                                        View lastFourthView = mRecyclerView.getLayoutManager().findViewByPosition(getMyLastPosition() - 3);
//                                        View lastFourthView = mRecyclerView.getChildAt(getMyLastPosition() - 3);
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
//                                GlobalParams.mRemovedChannels.remove(channel);

                            }
                        });
                break;
        }
    }
    
      private void onMove(int starPos, int endPos) {
        Channel startChannel = mDatas.get(starPos);
        //先删除之前的位置
        mDatas.remove(starPos);
        //添加到现在的位置
        mDatas.add(endPos, startChannel);
        mAdapter.notifyItemMoved(starPos, endPos);
    }
```
### 动画过程
```Java
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
```
