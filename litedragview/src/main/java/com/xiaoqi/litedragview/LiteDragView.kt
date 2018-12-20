package com.xiaoqi.litedragview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView


class LiteDragView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mContext: Context = context // 上下文对象

    private lateinit var mFixCircle: Circle // 固定圆，拖拽过程圆心不变，半径改变
    private lateinit var mDrgCircle: Circle // 拖拽圆，拖拽过程圆心改变，半径不变

    private var mIsEvenOverRange = false // 拖拽过程中是否超过拖拽距离

    private val mPath: Path? // 贝塞尔曲线路径
        get() {
            val distance = getDistanceByPoints(mFixCircle, mDrgCircle) // 获取圆心距
            mFixCircle.radius = RADIUS_INIT - distance / 10 // 固定圆半径
            mDrgCircle.radius = RADIUS_INIT - distance / 10 // 拖拽圆半径
            if (mFixCircle.radius <= RADIUS_MIN) {
                mIsEvenOverRange = true
                return null
            }
            if (!mIsEvenOverRange) {
                val path = Path()
                val offsetX = (mFixCircle.radius * Math.sin(Math.atan(((mDrgCircle.point.y - mFixCircle.point.y) / (mDrgCircle.point.x - mFixCircle.point.x)).toDouble()))).toFloat()
                val offsetY = (mFixCircle.radius * Math.cos(Math.atan(((mDrgCircle.point.y - mFixCircle.point.y) / (mDrgCircle.point.x - mFixCircle.point.x)).toDouble()))).toFloat()
                val x1 = mFixCircle.point.x + offsetX
                val y1 = mFixCircle.point.y - offsetY
                val x2 = mDrgCircle.point.x + offsetX
                val y2 = mDrgCircle.point.y - offsetY
                val x3 = mDrgCircle.point.x - offsetX
                val y3 = mDrgCircle.point.y + offsetY
                val x4 = mFixCircle.point.x - offsetX
                val y4 = mFixCircle.point.y + offsetY
                val bezierPoint = PointF() // 贝塞尔一阶曲线控制点，为起点和终点的中点
                bezierPoint.x = (mFixCircle.point.x + mDrgCircle.point.x) / 2
                bezierPoint.y = (mFixCircle.point.y + mDrgCircle.point.y) / 2
                path.moveTo(x1, y1)
                path.quadTo(bezierPoint.x, bezierPoint.y, x2, y2)
                path.lineTo(x3, y3)
                path.quadTo(bezierPoint.x, bezierPoint.y, x4, y4)
                path.lineTo(x1, y1)
                path.close()
                return path
            }
            return null
        }

    private var mPaint: Paint = Paint() // 画笔


    private lateinit var mOrgView: View // 实际被拖拽的View
    private lateinit var mDrgBmp: Bitmap // 被拖拽的View的快照，ACTION_DOWN的时候才去赋值

    private val mWindowManager: WindowManager
    private val mParams: WindowManager.LayoutParams

    private val mBombLayout: FrameLayout
    private val mBombView: ImageView

    private var mDragListener: OnDragListener

    init {
        /*
        画笔
         */
        mPaint.isAntiAlias = true
        mPaint.color = Color.RED
        /*
        WindowManager
         */
        mWindowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mParams = WindowManager.LayoutParams()
        mParams.format = PixelFormat.TRANSPARENT
        /*
        爆炸效果
         */
        mBombLayout = FrameLayout(mContext)
        mBombView = ImageView(mContext)
        mBombView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mBombLayout.addView(mBombView)

        mDragListener = object : OnDragListener {
            override fun onBomb(pointF: PointF) {
                // 移除消息气泡贝塞尔View,同时添加一个爆炸的View动画(帧动画)
                mWindowManager.removeView(this@LiteDragView)
                mBombView.setBackgroundResource(R.drawable.anim_bomb)
                val bombDrawable = mBombView.background as AnimationDrawable
                mBombView.x = pointF.x - bombDrawable.intrinsicWidth / 2
                mBombView.y = pointF.y - bombDrawable.intrinsicHeight / 2
                mWindowManager.addView(mBombLayout, mParams)
                bombDrawable.start()

                mBombView.postDelayed({
                    mWindowManager.removeView(mBombLayout) // 动画执行完毕,把爆炸布局及时从WindowManager移除
                }, 1000)
            }

            override fun onRestore() {
                mWindowManager.removeView(this@LiteDragView)
                mOrgView.visibility = View.VISIBLE
            }

        }
    }

    fun getPaint(): Paint {
        return mPaint
    }

    fun getOrgView(): View {
        return mOrgView
    }

    fun setOrgView(view: View) {
        mOrgView = view
    }

    /**
     * 初始化固定圆和拖拽圆
     *
     * @param x      x坐标
     * @param y      y坐标
     * @param radius 半径
     */
    private fun initCircle(x: Float, y: Float, radius: Float = RADIUS_DEFAULT) {
        RADIUS_INIT = radius
        mFixCircle = Circle(PointF(x, y), RADIUS_INIT)
        mDrgCircle = Circle(PointF(x, y), RADIUS_INIT)
        invalidate()
    }

    /**
     * 更新拖拽圆
     *
     * @param x 拖拽圆的圆心x坐标
     * @param y 拖拽圆的圆心y坐标
     */
    private fun updateDrgCircle(x: Float, y: Float) {
        mDrgCircle.point.set(x, y)
        invalidate()
    }

    /**
     * 处理手势按下操作
     */
    fun handleActionDown(event: MotionEvent) {
        mWindowManager.addView(this, mParams) // 添加LiteDragView
        val location = IntArray(2)
        mOrgView.getLocationInWindow(location) // 将mOrgView左上角的坐标存放在location中
        val c1x = location[0] + mOrgView.width / 2
        val c1y = location[1] + mOrgView.height / 2 - LiteDragUtils.getStatusBarHeight(mContext) // 圆心y坐标，要减去状态栏的高度
        initCircle(c1x.toFloat(), c1y.toFloat())
        mDrgBmp = getViewBitmap(mOrgView)
    }

    /**
     * 处理手势移动操作
     */
    fun handleActionMove(event: MotionEvent) {
        if (mOrgView.visibility == View.VISIBLE) {
            mOrgView.visibility = View.INVISIBLE
        }
        updateDrgCircle(event.rawX, event.rawY - LiteDragUtils.getStatusBarHeight(mContext))
    }

    /**
     * 处理手势抬起操作
     */
    fun handleActionUp(event: MotionEvent) {
        if (mFixCircle.radius < RADIUS_MIN) { // 超过拖拽距离，消失
            mDragListener.onBomb(mDrgCircle.point)
        } else { // 没超过拖拽距离，回弹
            val animator = ObjectAnimator.ofFloat(0f, 1f)
            animator.duration = 300

            val startPoint = mDrgCircle.point
            val endPoint = mFixCircle.point

            animator.addUpdateListener { animation ->
                val percent = animation.animatedValue as Float
                val point = LiteDragUtils.getPointByPercent(startPoint, endPoint, percent)
                updateDrgCircle(point.x, point.y)
            }

            animator.interpolator = OvershootInterpolator(3f)
            animator.start()
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mIsEvenOverRange = false
                    mDragListener.onRestore() // 当动画结束的时候,重新让View可拖动
                }
            })

        }
    }

    /**
     * 获取两圆的圆心距
     *
     * @param c1 固定圆
     * @param c2 拖拽圆
     * @return 圆心距
     */
    private fun getDistanceByPoints(c1: Circle, c2: Circle): Float {
        return Math.sqrt(Math.pow((c1.point.x - c2.point.x).toDouble(), 2.0) + Math.pow((c1.point.y - c2.point.y).toDouble(), 2.0)).toFloat()
    }

    private fun getViewBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(mDrgCircle.point.x, mDrgCircle.point.y, mDrgCircle.radius, mPaint) // 画拖拽圆
        mPath?.let {
            canvas.drawCircle(mFixCircle.point.x, mFixCircle.point.y, mFixCircle.radius, mPaint) // 画固定圆
            canvas.drawPath(it, mPaint) // 画两圆粘稠连线
        }
        canvas.drawBitmap(mDrgBmp, mDrgCircle.point.x - mDrgBmp.width / 2, mDrgCircle.point.y - mDrgBmp.height / 2, null) // 在拖拽圆上面画被拖拽View

    }

    /**
     * 拖动结果接口
     */
    interface OnDragListener {

        /**
         * 爆炸
         *
         * @param pointF 爆炸点
         */
        fun onBomb(pointF: PointF)

        /**
         * 回弹
         */
        fun onRestore()
    }

    companion object {

        private var RADIUS_INIT = 30f // 圆的初始半径，px
        private val RADIUS_MIN = 10f // 圆的最小半径，px
        private val RADIUS_DEFAULT = 30f

    }
}
