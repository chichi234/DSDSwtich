package com.reb.switchbt.profile

import android.bluetooth.BluetoothGatt
import android.os.Handler
import android.os.Message
import android.widget.Toast
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.reb.switchbt.db.DBManager
import com.reb.switchbt.db.DeviceBond
import com.reb.switchbt.R
import com.reb.switchbt.ui.adapter.MyDeviceAdapter
import com.reb.switchbt.ui.base.BaseActivity
import com.reb.switchbt.ui.util.ScanController
import com.reb.switchbt.util.DebugLog
import com.reb.switchbt.util.HexStringConver
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DefaultObserver
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

/**
 * File description
 *
 * @author tonly
 * @date 2019/7/2 16:07
 * @version 1.0
 * @package_name com.reb.switchbt.profile
 * @project_name DSDSwtich
 * @history At 2019/7/2 16:07 created by tonly
 */
class ConnectManager(val adapter: MyDeviceAdapter, val scanController: ScanController, val context: BaseActivity) {

    private val devies = mutableSetOf<DeviceBond>()
    private val executor = Executors.newSingleThreadExecutor()
    private val scheduler = Schedulers.from(executor)
    private val lock = Object()
    private val handler = object : Handler(context.mainLooper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                msg_stopProcess -> {
                    DebugLog.e("Process timeout")
                    devies.clear()
                    resetState(null)
                }
                msg_clearBuffer -> {
                    DebugLog.e("clear Parser")
                    val dataParser = msg.obj as DataParser
                    dataParser.clearBuffer()
                }
            }
        }
    }

    fun connectAll() {
        handler.removeMessages(msg_stopProcess)
        handler.sendEmptyMessageDelayed(msg_stopProcess, DeviceConfig.connectTimeOut)// 连接超时
        Observable.fromIterable(adapter.data.asIterable())
                .observeOn(scheduler)
                .map {
                    if (devies.size >= BleManager.getInstance().maxConnectCount) {
                        synchronized(lock) {
                            lock.wait()
                        }
                    }
                    devies.add(it)
                    it
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DefaultObserver<DeviceBond>() {
                    override fun onComplete() {
                        resetState(null)
                    }

                    override fun onNext(t: DeviceBond) {
                        connect(t)
                    }

                    override fun onError(e: Throwable) {
                    }

                })
    }

    fun connect(bondDevice: DeviceBond) {
        if (BleManager.getInstance().isBlueEnable) {
            BleManager.getInstance().connect(bondDevice.device, object : BleGattCallback() {
                override fun onStartConnect() {
                    bondDevice.isConnectting = true
                    adapter.notifyDataSetChanged()
                }

                override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                    bondDevice.isConnectting = false
                    resetState(bondDevice)
                    adapter.notifyDataSetChanged()
                }

                override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt?, status: Int) {
                    bondDevice.isConnectting = false
                    bondDevice.isConnected = true
                    adapter.notifyDataSetChanged()
                    notify(bondDevice)
                }

                override fun onDisConnected(isActiveDisConnected: Boolean, device: BleDevice, gatt: BluetoothGatt, status: Int) {
                    DebugLog.e("device is disConnect:${device.mac},isActiveDisConnected = $isActiveDisConnected")
                    bondDevice.isConnected = false
                    adapter.notifyDataSetChanged()
                }
            })
        } else {
            BleManager.getInstance().enableBluetooth()
        }
    }

    private fun notify(deviceBond: DeviceBond) {
        DebugLog.i("notify")
        BleManager.getInstance().notify(
                deviceBond.device,
                DeviceConfig.serviceUUID,
                DeviceConfig.notifyUUID,
                object : BleNotifyCallback() {
                    private val dataParaser = DataParser()
                    override fun onNotifySuccess() {
                        DebugLog.i("onNotifySuccess")
                        write(deviceBond, BTCMD.queryCmd(deviceBond.psw.toInt()))
                    }

                    override fun onNotifyFailure(exception: BleException?) {
                        BleManager.getInstance().disconnect(deviceBond.device)
                        resetState(deviceBond)
                    }

                    override fun onCharacteristicChanged(data: ByteArray) {
                        DebugLog.i("notify:${HexStringConver.bytes2HexStr(data)}")
                        handler.removeMessages(msg_clearBuffer)
                        handler.sendMessage(handler.obtainMessage(msg_clearBuffer, dataParaser))
                        dataParaser.parse(data, deviceBond, context, adapter, this@ConnectManager)
                    }
                }
        )
    }

    fun write(deviceBond: DeviceBond, data: ByteArray) {
        DebugLog.i("write:${HexStringConver.bytes2HexStr(data)}")
        BleManager.getInstance().write(
                deviceBond.device,
                DeviceConfig.serviceUUID,
                DeviceConfig.writeUUID,
                data,
                object : BleWriteCallback() {
                    override fun onWriteFailure(exception: BleException?) {
                        DebugLog.i("Error:$exception?.description")
                        Toast.makeText(context, context.getString(R.string.operation_failed), Toast.LENGTH_SHORT).show()
                        resetState(deviceBond)
                    }

                    override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                        DebugLog.i("write:${HexStringConver.bytes2HexStr(justWrite)}")
//                        // TODO testCode
//                        resetState(deviceBond)
                    }
                })
    }

    public fun resetState(deviceBond: DeviceBond?) {
        if (devies.remove(deviceBond)) {
            synchronized(lock) {
                lock.notifyAll()
            }
        }
        if (devies.size == 0) {
            handler.removeMessages(msg_stopProcess)
            context.dismissLoadingDialog()
            scanController.setIsScanning(false)
        }
    }

    companion object MSG{
         const val  msg_stopProcess = 0x10001
         const val  msg_clearBuffer = 0x10002
    }

}