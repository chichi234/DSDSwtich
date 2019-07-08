package com.reb.switchbt.ui

import android.app.Application
import com.clj.fastble.BleManager
import com.reb.switchbt.db.DBManager
import com.reb.switchbt.profile.DeviceConfig

/**
 * File description
 *
 * @author tonly
 * @date 2019/6/27 10:46
 * @version 1.0
 * @package_name com.reb.switchbt.ui
 * @project_name DSDSwtich
 * @history At 2019/6/27 10:46 created by tonly
 */
class BTSwitchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DBManager.init(this)
        BleManager.getInstance().init(this)
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(2, 5000)
                .setSplitWriteNum(20)
                .setConnectOverTime(DeviceConfig.connectTimeOut)
                .operateTimeout = 3000
    }
}