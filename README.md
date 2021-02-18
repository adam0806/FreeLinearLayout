<p><h1>FreeLinearLayout</h1></p>

1. Support vertical and horizontal scroll and fling.<br>
2. Listen the callback of click, long click event.<br>
3. Catch the touch, head visible and tail visible index.<br>

![image](https://github.com/adam0806/FreeLinearLayout/blob/master/imgs/freeLinearLayout.gif)

Just add some child views inside it, then you can control all you want! 

<p><h1>How to use</h1></p>

```java

 <com.adam.freelinearlayout.FreeLinearLayout
        android:id="@+id/freeLinearLayout"
        android:clickable="true"
        android:background="#FFCCDD"
        android:layout_width="@dimen/myproduct_item_width"
        android:layout_height="300dp"
        android:orientation="vertical">
        <TextView
            android:layout_width="@dimen/myproduct_item_width"
            android:layout_height="40dp"
            android:gravity="left|center_vertical"
            android:background="#002000"
            android:textColor="@color/white"
            android:text="You can add many different height child view"/>
            
</com.example.lenovo.adamnote.myproject.FreeLinearLayout>
```

<p><h1>Builder Mode</h1></p>

You can disable vertial or horizontal scroll, change velocity.
```java

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
```
  
<p><h1>Download</h1></p>

```java
allprojects{
  repositories {
    maven { url 'https://jitpack.io' }  
  }  
}  

dependencies {  
  implementation 'com.github.adam0806:FreeLinearLayout:1.0.0'  
}  
```
