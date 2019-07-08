package com.reb.switchbt.ui.util

import android.view.View

/**
 * 扫描期间不能进行任何操作
 *
 * @author tonly
 * @date 2019/7/2 9:18
 * @version 1.0
 * @package_name com.reb.switchbt.ui.util
 * @project_name DSDSwtich
 * @history At 2019/7/2 9:18 created by tonly
 */
class ScanController {
    private var isScanning:Boolean = false
    private val views:MutableSet<View> = mutableSetOf()
    fun addView(v:View) {
        views.add(v)
        v.isEnabled = !isScanning
    }

    fun  removeView(v:View) {
        views.remove(v)
        v.isEnabled = isScanning
    }

    fun setIsScanning(isScanning: Boolean) {
        this.isScanning = isScanning
        for (v in views) {
            v.isEnabled = !isScanning
        }
    }

    fun isScanning():Boolean{
        return isScanning
    }
}