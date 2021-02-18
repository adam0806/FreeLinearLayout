package com.example.lenovo.adamnote

import android.app.Activity
import android.os.Bundle
import com.adam.freeLinearlayout.sample.R
import com.adam.freelinearlayout.FreeLinearLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {
    var mList = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var builder = FreeLinearLayout.Builder()
            .setVerticalScrollable(true)
            .setHorizontalScrollable(true)
            .setXVelocity(0.8f)
            .setYVelocity(1f)
            .setVelMaxValue(3000)
            .setEventListener(object : FreeLinearLayout.EventListener {
                override fun eventChange(type: FreeLinearLayout.TYPE, index: Int) {
                    var str = ""
                    if (type == FreeLinearLayout.TYPE.VERTICAL) {
                        str = "Vertical: $index\n"
                    } else if (type == FreeLinearLayout.TYPE.HORIZONTAL) {
                        str = "Horizontal: $index\n"
                    } else if (type == FreeLinearLayout.TYPE.CLICK) {
                        str = "Click: $index\n"
                    } else if (type == FreeLinearLayout.TYPE.LONG_CLICK) {
                        str = "Long Click: $index\n"
                    }
                    mList.add(str)
                    setMessage()
                }

                override fun visibleChange(firstVisible: Int, lastVisible: Int) {
                    mList.add("Head: $firstVisible, Tail: $lastVisible\n")
                    setMessage()
                }
            })
        freeLinearLayout.initBuilder(builder)
    }

    fun setMessage() {
        if (mList.size > 5) {
            mList.removeAt(0)
        }
        var message = ""
        for (str in mList) {
            message += str
        }
        tv_type.text = message
    }
}