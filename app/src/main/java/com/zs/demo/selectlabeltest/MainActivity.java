package com.zs.demo.selectlabeltest;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.zs.demo.selectlabeltest.bean.Channel;
import com.zs.demo.selectlabeltest.listener.OnChannelListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
public class MainActivity extends FragmentActivity implements OnChannelListener {

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

    private void setTitleData(){
        mChannel = "";
        for (int i = 0;i< mNormalDatas.size();i++){
            mChannel += "\u3000" + mNormalDatas.get(i).Title;
        }

        for (int i = 0;i< mSelectedDatas.size();i++){
            mChannel += "\u3000" + mSelectedDatas.get(i).Title;
        }

        tv_main.setText(mChannel);
    }

    public void addLabel1(View view){
        ChannelDialogFragment dialogFragment = ChannelDialogFragment.newInstance(mNormalDatas,mSelectedDatas, mUnSelectedDatas);
        dialogFragment.setOnChannelListener(this);
        dialogFragment.show(getSupportFragmentManager(), "CHANNEL");
        dialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                setTitleData();


//                adapter.notifyDataSetChanged();
//                mViewPager.setOffscreenPageLimit(mSelectedDatas.size());
                //tab.setCurrentItem(tab.getSelectedTabPosition());

//                ViewGroup slidingTabStrip = (ViewGroup) tablayout.getChildAt(0);
//                //注意：因为最开始设置了最小宽度，所以重新测量宽度的时候一定要先将最小宽度设置为0
//                slidingTabStrip.setMinimumWidth(0);
//                slidingTabStrip.measure(0, 0);

                //保存选中和未选中的channel
//                SharedPreferencesMgr.setString(ConstanceValue.TITLE_SELECTED, mGson.toJson(mSelectedDatas));
//                SharedPreferencesMgr.setString(ConstanceValue.TITLE_UNSELECTED, mGson.toJson(mUnSelectedDatas));
            }
        });
    }


    public void addLabel2(View view){
        ChannelDialogFragment2 dialogFragment = ChannelDialogFragment2.newInstance(mNormalDatas,mSelectedDatas, mUnSelectedDatas);
        dialogFragment.setOnChannelListener(this);
        dialogFragment.show(getSupportFragmentManager(), "CHANNEL");
        dialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                setTitleData();
            }
        });
    }

    @Override
    public void onItemMove(int starPos, int endPos) {
        listMove(mSelectedDatas, starPos , endPos );
//        listMove(mFragments, starPos, endPos);
    }

    private void listMove(List datas, int starPos, int endPos) {
        Object o = datas.get(starPos);
        //先删除之前的位置
        datas.remove(starPos);
        //添加到现在的位置
        datas.add(endPos, o);
    }

    @Override
    public void onMoveToMyChannel(int starPos, int endPos) {
        //移动到我的频道
        Channel channel = mUnSelectedDatas.remove(starPos);
        mSelectedDatas.add(endPos, channel);
//        mFragments.add(NewListFragment.newInstance(channel.Title, channel.TitleCode));
    }

    @Override
    public void onMoveToOtherChannel(int starPos, int endPos) {
        //移动到推荐频道
        mUnSelectedDatas.add(endPos, mSelectedDatas.remove(starPos));
//        mFragments.remove(starPos);
    }
}
