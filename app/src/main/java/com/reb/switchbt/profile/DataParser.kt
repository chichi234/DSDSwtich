package com.reb.switchbt.profile

import android.content.Context
import com.clj.fastble.BleManager
import com.reb.switchbt.db.DBManager
import com.reb.switchbt.db.DeviceBond
import com.reb.switchbt.ui.adapter.MyDeviceAdapter
import com.reb.switchbt.util.DebugLog
import com.reb.switchbt.util.HexStringConver
import com.reb.switchbt.R
import java.util.*

/**
 * File description
 *
 * @author tonly
 * @date 2019/7/8 9:58
 * @version 1.0
 * @package_name com.reb.switchbt.profile
 * @project_name DSDSwtich
 * @history At 2019/7/8 9:58 created by tonly
 */
class DataParser {
    private val byteBuffer = LinkedList<Byte>()
    private var isStart: Boolean = false
    fun parse(data: ByteArray, deviceBond : DeviceBond, context : Context, adapter: MyDeviceAdapter, connectManager: ConnectManager) {
        for (b in data) {
            if (isStart) {
                byteBuffer.add(b)
                if (b == 0xAA.toByte()) {
                    // 包尾
                    val byteArray = byteBuffer.toByteArray()
                    DebugLog.i("receive data:${HexStringConver.bytes2HexStr(byteArray)}")
                    if (BTCMD.check(byteArray)) {
                        parseData(byteArray, deviceBond, context, adapter, connectManager)
                    } else {
                        DebugLog.e("Wrong Data check")
                    }
                    byteBuffer.clear()
                    isStart = false
                }
            } else if (b == 0xA1.toByte()){
                // 包头
                byteBuffer.add(b)
                isStart = true
            }
        }
    }

    private fun parseData(data: ByteArray, deviceBond: DeviceBond, context: Context, adapter: MyDeviceAdapter, connectManager: ConnectManager) {
        if (data.size >= 5) {
            when (data[1]) {
                0x01.toByte(),0x02.toByte() -> when(data[2]){
                    0x00.toByte() -> showWrongPassword(deviceBond, context, adapter)
                    0x01.toByte() -> {
                        for (i in 0..3) {
                            deviceBond.relayState[i] = deviceBond.relayTempState[i]
                        }
                        adapter.notifyDataSetChanged()
                    }
                }// 开关继电器
                0x05.toByte() -> when(data[2]){
                    0x00.toByte() -> showWrongPassword(deviceBond, context, adapter)
                    else -> {
                        val channels = data[2].toInt() and (0xFF)
                        deviceBond.relayCount = channels
                        for (i in 1..channels) {
                            deviceBond.relayState[i-1] = data[3 + channels - i] == 0x01.toByte()
                        }
                        connectManager.resetState(deviceBond)
                        adapter.notifyDataSetChanged()
                    }
                }// 查询继电器状态
                0x11.toByte() -> when(data[2]){
                    0x00.toByte() -> showWrongPassword(deviceBond, context, adapter)
                    0x01.toByte() -> {
                        deviceBond.psw = deviceBond.tempPsw
                        DBManager.getInstance().deviceBondDao.update(deviceBond)
                    }
                }// 设置密码
            }
        }
    }

    private fun showWrongPassword(deviceBond: DeviceBond, context: Context, adapter: MyDeviceAdapter) {
        deviceBond.msg = context.getString(R.string.Wrong_Password)
        deviceBond.isWrong = true
//        BleManager.getInstance().disconnect(deviceBond.device)
        adapter.notifyDataSetChanged()
    }

    fun clearBuffer() {
        isStart = false
        byteBuffer.clear()
    }
}