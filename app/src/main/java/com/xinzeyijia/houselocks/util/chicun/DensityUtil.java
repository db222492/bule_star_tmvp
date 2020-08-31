package com.xinzeyijia.houselocks.util.chicun;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.math.BigDecimal;

public class DensityUtil {
    public static String float2String(float value) {
        return String.valueOf(formatFloat(value));
    }

    static float formatFloat(float value) {
        BigDecimal bigDecimal = BigDecimal.valueOf(value);
        return bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    public static float getScreenDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    public static int getStatusBarHeight(Context ctx) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = ctx.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }

    public static int getScreenHeightWithoutTitlebar(Context ctx) {
        int[] screenWidthAndHeight = getScreenWidthAndHeight(ctx);
        return screenWidthAndHeight[1] - getStatusBarHeight(ctx) - com.xinzeyijia.houselocks.utils.DensityUtil.dp2px(48);
    }

    public static int[] getScreenWidthAndHeight(Context ctx) {
        WindowManager mWm = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        mWm.getDefaultDisplay().getMetrics(dm);

        int screenWidth = dm.widthPixels;

        int screenHeigh = dm.heightPixels;

        return new int[]{screenWidth, screenHeigh};
    }

    public static void addOnSoftKeyBoardVisibleListener(final Activity activity, final ScrollView scrollView) {
        final View decorView = activity.getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect rect = new Rect();
                        decorView.getWindowVisibleDisplayFrame(rect);
                        int displayHight = rect.bottom - rect.top;
                        int hight = decorView.getHeight();
                        boolean visible = (double) displayHight / hight < 0.8;// 决断键盘是弹�?
//                        System.out.println( "===监听" + visible );
                        if (visible) {

                            Handler mHandler = new Handler();
                            mHandler.postDelayed(() -> {
//									scrollView.fullScroll(ScrollView.FOCUS_DOWN);// ScrollView滚动到底

                                scrollView.scrollTo(0, com.xinzeyijia.houselocks.utils.DensityUtil.dp2px(167));
                            }, 50);
                        }
                    }
                });
    }

    //获取TextView被截断之后的字符串
    public static String getEllipsisedText(TextView textView) {
        try {
            String text = textView.getText().toString();
            int lines = textView.getLineCount();
            int width = textView.getWidth();
            int len = text.length();

            TextUtils.TruncateAt where = TextUtils.TruncateAt.END;
            TextPaint paint = textView.getPaint();

            StringBuffer result = new StringBuffer();

            int spos = 0, cnt, tmp, hasLines = 0;

            while (hasLines < lines - 1) {
                cnt = paint.breakText(text, spos, len, true, width, null);
                if (cnt >= len - spos) {
                    result.append(text.substring(spos));
                    break;
                }

                tmp = text.lastIndexOf('\n', spos + cnt - 1);

                if (tmp >= 0 && tmp < spos + cnt) {
                    result.append(text.substring(spos, tmp + 1));
                    spos += tmp + 1;
                } else {
                    tmp = text.lastIndexOf(' ', spos + cnt - 1);
                    if (tmp >= spos) {
                        result.append(text.substring(spos, tmp + 1));
                        spos += tmp + 1;
                    } else {
                        result.append(text.substring(spos, cnt));
                        spos += cnt;
                    }
                }

                hasLines++;
            }

            if (spos < len) {
                result.append(TextUtils.ellipsize(text.subSequence(spos, len), paint, (float) width, where));
            }

            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
