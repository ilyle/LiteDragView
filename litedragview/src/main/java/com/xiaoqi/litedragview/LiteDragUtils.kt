package com.xiaoqi.litedragview

import android.content.Context
import android.graphics.PointF

/**
 * Created by xujie on 2018/12/17.
 * Mail : 617314917@qq.com
 */

object LiteDragUtils {
    fun dp2px(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun px2dp(context: Context, px: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (px / scale + 0.5f).toInt()
    }

    /**
     * 根据百分比获取两点之间的某个点坐标
     *
     * @param p1      p1
     * @param p2      p2
     * @param percent 百分比
     * @return 点
     */
    fun getPointByPercent(p1: PointF, p2: PointF, percent: Float): PointF {
        val x = p1.x + (p2.x - p1.x) * percent
        val y = p1.y + (p2.y - p1.y) * percent
        return PointF(x, y)
    }

    /**
     * 获取状态栏高度
     *
     * @return 状态栏高度
     */
    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId) // 根据资源ID获取响应的尺寸值
        } else dp2px(context, 18f)
    }
}
