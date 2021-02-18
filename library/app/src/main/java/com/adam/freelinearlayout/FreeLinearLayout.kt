package com.adam.freelinearlayout

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.widget.LinearLayout
import android.widget.Scroller
import kotlin.math.abs

class FreeLinearLayout : LinearLayout {
    companion object{
        val TAG = javaClass.simpleName
        val CLICK_DIS = 20
        var mEnableLog = false
    }
    private var xScrollable = true
    private var yScrollable = true
    private var mVelocityTracker: VelocityTracker ?= null
    private var mScroller: Scroller
    private var yVelocity = 0f
    private var xVelocity = 0f
    private var yVel = 0f
    private var xVel = 0f
    private var velMaxValue = 0
    private var xCurrentLocation = 0f
    private var yCurrentLocation = 0f
    private var dx = 0f
    private var dy = 0f
    private var xLastLocation = 0f
    private var yLastLocation = 0f
    private var xDown = 0f
    private var yDown = 0f
    private var xDis = 0f
    private var yDis = 0f
    private var childHeight = 0
    private var childWidth = 0
    private var childTotalHeight = 0
    private var mScreenHeight = 0
    private var mScreenWidth = 0
    private var mNowTouchIndex = 0
    private var mFirstVisibleIndex = 0
    private var mLastFirstVisibleIndex = 0
    private var mLastVisibleIndex = 0
    private var mLastLastVisibleIndex = 0
    private var builder: Builder?= null
    private var mEventListener : EventListener?= null
    private var childList : MutableList<ChildWidHei>
    enum class TYPE{
        NON, VERTICAL, HORIZONTAL, CLICK, LONG_CLICK
    }
    private var mType = TYPE.VERTICAL
    private var mLastType = TYPE.NON;
    private var mLogType = TYPE.NON
    private var consumeClick = false

    interface EventListener{
        fun eventChange(type: TYPE, position: Int)
        fun visibleChange(firstVisible: Int, lastVisible: Int)
    }
    class ChildWidHei{
        var height = 0
        var width = 0
    }
    class Builder{
        private var xScrollable = true
        private var yScrollable = true
        private var mEventListener : EventListener?= null
        private var yVel = 0.7f
        private var xVel = 0.7f
        private var velMaxValue = 2500

        fun setVerticalScrollable(enable: Boolean) : Builder {
            yScrollable = enable
            return this
        }
        fun setHorizontalScrollable(enable: Boolean) : Builder {
            xScrollable = enable
            return this
        }
        fun setEventListener(listener: EventListener) : Builder {
            mEventListener = listener
            return this
        }
        fun setYVelocity(yVel: Float) : Builder {
            this.yVel = yVel
            return this
        }
        fun setXVelocity(xVel: Float) : Builder {
            this.xVel = xVel
            return this
        }

        fun setVelMaxValue(velMaxValue: Int) : Builder {
            this.velMaxValue = velMaxValue
            return this
        }
        fun getYVel() : Float{
            return yVel
        }
        fun getXVel() : Float{
            return xVel
        }
        fun getVelMaxValue() : Int{
            return velMaxValue
        }
        fun getEventListener() : EventListener?{
            return mEventListener
        }
        fun xScrollable(): Boolean {
            return xScrollable;
        }
        fun yScrollable(): Boolean {
            return yScrollable;
        }
    }
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init{
        mScroller = Scroller(context);
        mScreenHeight = getScreenHeight(context);
        mScreenWidth = getScreenWidth(context);
        childList = ArrayList()
    }

    fun initBuilder(builder: Builder){
        if(builder != null) {
            yScrollable = builder!!.yScrollable()
            xScrollable = builder!!.xScrollable()
            yVel = builder!!.getYVel()
            xVel = builder!!.getXVel()
            velMaxValue = builder!!.getVelMaxValue()
            mEventListener = builder!!.getEventListener()
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        addVelocityTracker(event);
        var logHead = ""
        var len = 0.0
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                xDown = event.x
                yDown = event.y
                xLastLocation = xDown
                yLastLocation = yDown
                mType = TYPE.NON
                consumeClick = false
                logHead = "Down"
                var i = calculateTouchIndex(yDown)
                if(i != -1) {
                    mNowTouchIndex = i
                }
                handler.postDelayed({
                    if((mType != TYPE.VERTICAL && mType != TYPE.HORIZONTAL) && !consumeClick){
                        consumeClick = true
                        mType = TYPE.LONG_CLICK
                        mEventListener?.eventChange(mType, mNowTouchIndex)
                        if(mType == TYPE.NON) {
                        }else{
                            mType = TYPE.NON
                        }
                    }
                }, 500)
            }
            MotionEvent.ACTION_MOVE -> {
                xCurrentLocation = event.x
                yCurrentLocation = event.y
                dx = xCurrentLocation - xLastLocation
                dy = yCurrentLocation - yLastLocation
                xLastLocation = xCurrentLocation
                yLastLocation = yCurrentLocation

                logHead = "Move"

                if(getTouchDis(event) >= CLICK_DIS){
                    if (abs(dx) > abs(dy)) {
                        mType = TYPE.HORIZONTAL
                    } else {
                        mType = TYPE.VERTICAL
                    }
                }
                if(mLastType != mType){
                    mLastType = mType
                    mLogType = mType
                    if(mType != TYPE.NON) {
                        mEventListener?.eventChange(mType, mNowTouchIndex)
                    }
                }
                calculateVisibleIndex()

                var yMove = scrollY + if (mType == TYPE.VERTICAL && yScrollable) (-dy).toInt() else 0
                var xMove = scrollX + if (mType == TYPE.HORIZONTAL && xScrollable) (-dx).toInt() else 0
                if(scrollY - dy < top()){//上邊界
                    yMove = top()
                } else if(scrollY - dy > bottom()) {//下邊界
                    yMove = bottom()
                }
                if(scrollX - dx < left()){
                    xMove = left()
                } else if (scrollX - dx > right()){
                    xMove = right()
                }
                scrollTo(xMove, yMove)
            }
            MotionEvent.ACTION_UP -> {
                mVelocityTracker!!.computeCurrentVelocity(1000)
                yVelocity = mVelocityTracker!!.yVelocity    //>0向下滑
                mVelocityTracker!!.computeCurrentVelocity(1000)
                xVelocity = mVelocityTracker!!.xVelocity    //>0向右滑

                mScroller.forceFinished(true);

                mScroller.fling(scrollX, scrollY,
                        if (mType == TYPE.HORIZONTAL && xScrollable) (-xVel * xVelocity).toInt() else 0,
                        if (mType == TYPE.VERTICAL && yScrollable) (-yVel * yVelocity).toInt() else 0,
                        -velMaxValue, velMaxValue, -velMaxValue, velMaxValue);

                if(getTouchDis(event) < CLICK_DIS && !consumeClick){
                    consumeClick = true
                    mType = TYPE.CLICK
                    calculateVisibleIndex()
                    if(mLastType != mType){
                        mLastType = mType
                        mLogType = mType
                    }
                    mEventListener?.eventChange(mType, mNowTouchIndex)
                }

                handler.removeCallbacksAndMessages(null)

                logHead = "UP  "

                recycleVelocityTracker();
            }
        }
        postInvalidate()

        var log = ", dx: " + dx + ", dy: " + dy + ", xV: " + xVelocity + ", yV: " +
                yVelocity +", xCurLoc: " + xCurrentLocation + ", yCurLoc: " + yCurrentLocation +
                ", xLasLoc: " + xLastLocation + ", yLasLoc: " + yLastLocation + ", scrX: "+scrollX+
                ", scrY: "+scrollY + ", scrFX: "+mScroller.finalX+", scrFY: "+mScroller.finalY +
                ", H: "+getHeight() +", ScreH: "+mScreenHeight+", ScreW: "+mScreenWidth+", ChdTH: " +
                ""+childTotalHeight+", CW: "+childWidth + ", xDis: "+xDis+", yDis: "+yDis+", L: "+len+", T: "+mLogType
        log("event, " + logHead + log )
        return super.onTouchEvent(event)
    }
    private fun getTouchDis(event: MotionEvent) : Double{
        xDis = xDown - event.x
        yDis = yDown - event.y
        return Math.sqrt((xDis*xDis + yDis*yDis).toDouble())
    }
    private fun left() : Int{
        return 0
    }
    private fun top() : Int{
        return 0
    }
    private fun right() : Int{
        return width - mScreenWidth
    }
    private fun bottom() : Int{
        return childTotalHeight - height
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        childTotalHeight = 0
        childHeight = 0
        childWidth = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                childHeight = child.measuredHeight
                childWidth = child.measuredWidth
                var childWidHei = ChildWidHei()
                childWidHei.width = child.measuredWidth
                childWidHei.height = child.measuredHeight
                childList.add(childWidHei)
                childTotalHeight += childHeight
            }
        }
        calculateVisibleIndex()
    }
    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            var log = ", dx: " + dx + ", dy: " + dy + ", xV: " + xVelocity + ", yV: " + yVelocity +", xCurLoc: " + xCurrentLocation + ", yCurLoc: " + yCurrentLocation + ", xLasLoc: " + xLastLocation + ", yLasLoc: " + yLastLocation + ", scrX: "+scrollX+", scrY: "+scrollY + ", scrFX: "+mScroller.finalX+", scrFY: "+mScroller.finalY + ", H: "+height +", W: "+width+", ScreH: "+mScreenHeight+", ChdTH: "+childTotalHeight
            var logHead = "cmpS"
            log("event, " + logHead + log)
            var enableScroll = true
            if(yScrollable) {
                if (mScroller.currY < top()) {
                    enableScroll = false
                } else if (mScroller.currY > bottom()) {
                    enableScroll = false
                }
            }
            if(xScrollable){
                if (mScroller.currX < left()) {
                    enableScroll = false
                }else if(mScroller.currX > right()){
                    enableScroll = false
                }
            }

            if(enableScroll) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY())
            }
            postInvalidate()
            calculateVisibleIndex()
        }
    }
    private fun addVelocityTracker(event: MotionEvent){
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
    }

    private fun recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
    }
    private fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }
    private fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }
    private fun log(message: String){
        if(mEnableLog){
            Log.d(TAG, message)
        }
    }
    private fun calculateTouchIndex(eventY: Float) : Int{
        var lastTop = 0
        var height = 0
        var logTail = ""
        for(i in 0 until childList.size){
            height = childList.get(i).height
            if((eventY + scrollY) >= (lastTop) && (eventY + scrollY) < (lastTop + height)){
                return i
                logTail = ", FIT!"
            }
            log("event, ey: "+eventY+", y:"+y+", scrY: "+scrollY+", Hei: "+height+", "+(lastTop) + " < "+(eventY + scrollY) +" < "+(lastTop + height)+logTail)
            lastTop += height
        }
        return -1
    }
    private fun calculateVisibleIndex(){
        var firstY = 0
        var lastY = height-1
        mFirstVisibleIndex = calculateTouchIndex(firstY.toFloat())
        mLastVisibleIndex = calculateTouchIndex(lastY.toFloat())
        if(mFirstVisibleIndex != mLastFirstVisibleIndex || mLastVisibleIndex != mLastLastVisibleIndex) {
            mEventListener?.visibleChange(mFirstVisibleIndex, mLastVisibleIndex)
        }
        mLastFirstVisibleIndex = mFirstVisibleIndex
        mLastLastVisibleIndex = mLastVisibleIndex
    }
}