package com.zs.demo.selectlabeltest.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.widget.Toast;

import java.util.List;

public class DensityUtil {
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        if (context != null) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale);
        } else {
            return 0;
        }
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        if (context != null) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (pxValue / scale + 0.5f);
        } else {
            return 0;
        }
    }

    /**
     * 获得屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getDisplayWidth(Context context) {
        initDisplayMetrics(context);
        if (sDisplayMetrics != null) {
            return sDisplayMetrics.widthPixels;
        } else {
            return 0;
        }
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
//        int statusBarHeight = -1;
//        try {
//            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
//            Object object = clazz.newInstance();
//            int height = Integer.parseInt(clazz.getField("status_bar_height") .get(object).toString());
//            statusBarHeight = context.getResources().getDimensionPixelSize(height);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return statusBarHeight;
        if (context != null) {
            Rect frame = new Rect();
            ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            return frame.top;
        } else {
            return 0;
        }
    }


    // 屏幕高度（像素）
    public static int getWindowHeight(Activity activity) {
        if (activity != null) {
            DisplayMetrics metric = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
            return metric.heightPixels;
        } else {
            return 0;
        }
    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    public static int getDisplayHeight(Context context) {
        initDisplayMetrics(context);
        if (sDisplayMetrics != null) {
            return sDisplayMetrics.heightPixels;
        } else {
            return 0;
        }
    }

    private static DisplayMetrics sDisplayMetrics;

    /**
     * init display metrics
     *
     * @param context
     */
    private static synchronized void initDisplayMetrics(Context context) {
        if (context != null) {
            sDisplayMetrics = context.getResources().getDisplayMetrics();
        }
    }


    //打电话
    public void callSystemPhone(String phone_number, Activity activity) {
        if (phone_number != null && !phone_number.equals("") && activity != null) {
            phone_number = phone_number.trim();//删除字符串首部和尾部的空格
            //封装一个拨打电话的intent，并且将电话号码包装成一个Uri对象传入
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + phone_number));
            activity.startActivity(intent);//内部类
        }
    }

    public static void openAppInMarket(Context context) {
        if (context == null) return;
        String pckName = context.getPackageName();
        gotoMarket(context, pckName);
    }

    public static void gotoMarket(Context context, String pck) {
        if (!isHaveMarket(context)) {
            Toast.makeText(context, "你手机中没有安装应用市场！", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + pck));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public static boolean isHaveMarket(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_APP_MARKET);
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
        return infos.size() > 0;
    }
}
