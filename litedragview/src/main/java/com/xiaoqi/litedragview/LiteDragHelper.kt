package com.xiaoqi.litedragview

import android.content.Context
import android.graphics.Color
import android.support.annotation.ColorInt
import android.view.MotionEvent
import android.view.View

/**
 * Created by xujie on 2018/12/18.
 * Mail : 617314917@qq.com
 */
object LiteDragHelper {
    @JvmStatic
    @JvmOverloads
    fun bind(context: Context, view: View, @ColorInt color: Int = Color.RED) {
        val liteDragView = LiteDragView(context) // 初始化LiteDragView
        liteDragView.setOrgView(view)
        liteDragView.getPaint().color = color
        liteDragView.getOrgView().setOnTouchListener { _, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    liteDragView.handleActionDown(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    liteDragView.handleActionMove(event)
                }
                MotionEvent.ACTION_UP -> {
                    liteDragView.handleActionUp(event)
                }
            }
            true
        }
    }
}