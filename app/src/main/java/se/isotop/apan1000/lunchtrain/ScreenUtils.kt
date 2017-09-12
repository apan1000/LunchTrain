package se.isotop.apan1000.lunchtrain

import android.content.Context
import android.graphics.Point
import android.view.WindowManager


/**
 * Created by Fredrik Berglund on 2017-09-12.
 */
fun getNavigationBarSize(context: Context): Point {
    val appUsableSize = getAppUsableScreenSize(context)
    val realScreenSize = getRealScreenSize(context)

    // navigation bar on the right
    if (appUsableSize.x < realScreenSize.x) {
        return Point(realScreenSize.x - appUsableSize.x, appUsableSize.y)
    }

    // navigation bar at the bottom
    return if (appUsableSize.y < realScreenSize.y) {
        Point(appUsableSize.x, realScreenSize.y - appUsableSize.y)
    } else Point() // navigation bar is not present
}

fun getAppUsableScreenSize(context: Context): Point {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val size = Point()
    display.getSize(size)
    return size
}

fun getRealScreenSize(context: Context): Point {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val size = Point()
    display.getRealSize(size)
    return size
}