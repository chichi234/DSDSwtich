package com.reb.switchbt.ui.frag

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.*
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.data.BleScanState
import com.clj.fastble.scan.BleScanRuleConfig
import com.lzj.pass.dialog.PayPassDialog
import com.lzj.pass.dialog.PayPassView
import com.reb.switchbt.db.DBManager
import com.reb.switchbt.db.DeviceBond
import com.reb.switchbt.profile.BTCMD
import com.reb.switchbt.R
import com.reb.switchbt.profile.ConnectManager
import com.reb.switchbt.ui.adapter.DeviceAdapter
import com.reb.switchbt.ui.adapter.MyDeviceAdapter
import com.reb.switchbt.ui.base.BaseActivity
import com.reb.switchbt.ui.base.BaseFragment
import com.reb.switchbt.ui.util.ScanController
import com.reb.switchbt.util.DebugLog
import kotlinx.android.synthetic.main.frag_devices.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * File description
 *
 * @author tonly
 * @date 2019/6/25 18:05
 * @version 1.0
 * @package_name com.reb.switchbt.ui.frag
 * @project_name DSDSwtich
 * @history At 2019/6/25 18:05 created by tonly
 */
class BTFragment : BaseFragment() {

    private val sdf = SimpleDateFormat("yyyyMMDDHHmmss")
    private lateinit var myAdapter: MyDeviceAdapter
    private lateinit var availableAdapter: DeviceAdapter
    private var currentOperateDevice: DeviceBond? = null
    private var pop: PopupWindow? = null
    private var removeDialog: Dialog? = null
    private var renameDialog: Dialog? = null
    private var passwordDialog: Dialog? = null
    private val scanControler = ScanController()
    private lateinit var connectManager: ConnectManager

    override fun getLayoutId(): Int {
        return R.layout.frag_devices
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myAdapter = MyDeviceAdapter(activity!!, scanControler)
        availableAdapter = DeviceAdapter(activity, scanControler)
        my_devices.adapter = myAdapter
        connectManager = ConnectManager(myAdapter, scanControler, activity!! as BaseActivity)
        available_devices.adapter = availableAdapter
        my_devices.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, p1, p2, _ ->
            showPopWindow(view = p1, position = p2)
            true
        }
        my_devices.setOnItemClickListener { _, _, position, _ ->
            myAdapter.setSelect(position)
            changeMyDeviceHeight()
        }
        scan.setOnClickListener {
            if (BleManager.getInstance().scanSate == BleScanState.STATE_SCANNING) {
                BleManager.getInstance().cancelScan()
            } else {
                startScan()
            }
        }
        availableAdapter.setOnAddMyDeviceListener {
            it.isOnline = true
            myAdapter.addDevice(it)
            changeMyDeviceHeight()
            it.insert_time = sdf.format(Date())
            DBManager.getInstance().deviceBondDao.insert(it)
            connectManager.connect(it)
            noDevice.visibility = View.GONE
        }
        myAdapter.setConnectDeviceListener(object : MyDeviceAdapter.ConnectDeviceListener {
            override fun connectDevice(device: DeviceBond, isChecked: Boolean) {
//                DebugLog.i("isChecked:$isChecked")
//                if (!isChecked) {
//                    BleManager.getInstance().disconnect(device.device)
//                } else {
//                    connectManager.connect(device)
//                }
            }

            override fun switchRelay(device: DeviceBond, index: Int, isChecked: Boolean) {
                for (i in 0..3) {
                    device.relayTempState[i] = device.relayState[i]
                }
                device.relayTempState[index - 1] = isChecked
                connectManager.write(device, BTCMD.controlSingleRelay(device.psw.toInt(), index, isChecked))
            }

        })
        scanControler.addView(my_devices)
        scanControler.addView(available_devices)
        scanControler.addView(scan)
        activity!!.registerReceiver(btStateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        startScan()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity!!.unregisterReceiver(btStateReceiver)
    }

    private var itemHeight: Int? = 0
    private var itemExpendHeight: Int? = 0
    private fun changeMyDeviceHeight() {
        if (myAdapter.count == 0) {
            val lp = my_devices.layoutParams
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            my_devices.layoutParams = lp
            return
        }
        my_devices.post {
            if (myAdapter.count <= 3) {

                val lp = my_devices.layoutParams
                if (itemHeight == null || itemHeight == 0) {
                    for (i in 0 until myAdapter.count) {
                        val child = my_devices.getChildAt(i)
                        val height = child?.measuredHeight
                        if (height != null) {
                            if (itemHeight!! > height || itemHeight!! == 0) {
                                itemHeight = height
                            }
                            if (itemHeight!! < height) {
                                itemExpendHeight = height
                            }
                        }
                    }
                }
                if (myAdapter.hasSelect() && itemExpendHeight == 0) {
                    for (i in 0 until myAdapter.count) {
                        val child = my_devices.getChildAt(i)
                        val height = child?.measuredHeight
                        if (height != null && itemHeight!! < height) {
                            itemExpendHeight = height
                        }
                    }
                }
                DebugLog.i("itemHeight:$itemHeight,itemExpendHeight$itemExpendHeight")
                lp.height = itemHeight!! * myAdapter.count
                if (myAdapter.hasSelect()) {
                    lp.height += (itemExpendHeight!! - itemHeight!!)
                }
                my_devices.layoutParams = lp

            } else {
                val lp = my_devices.layoutParams
                if (itemHeight == null || itemHeight == 0) {
                    for (i in 0 until myAdapter.count) {
                        val child = my_devices.getChildAt(i)
                        val height = child?.measuredHeight
                        if (height != null) {
                            if (itemHeight!! > height || itemHeight!! == 0) {
                                itemHeight = height
                            }
                            if (itemHeight!! < height) {
                                itemExpendHeight = height
                            }
                        }
                    }
                }
                if (myAdapter.hasSelect() && itemExpendHeight == 0) {
                    for (i in 0 until myAdapter.count) {
                        val child = my_devices.getChildAt(i)
                        val height = child?.measuredHeight
                        if (height != null && itemHeight!! < height) {
                            itemExpendHeight = height
                        }
                    }
                }
                DebugLog.i("itemHeight:$itemHeight,itemExpendHeight$itemExpendHeight")
                lp.height = itemHeight!! * Math.min(myAdapter.count, 3)
                my_devices.layoutParams = lp
            }
        }
    }

    private fun startScan() {
        if (checkPermission()) {
            if (BleManager.getInstance().isBlueEnable) {
                val scanRuleConfig = BleScanRuleConfig.Builder()
                        .setScanTimeOut(5000)              // 扫描超时时间，可选，默认10秒；小于等于0表示不限制扫描时间
                        .build()
                BleManager.getInstance().initScanRule(scanRuleConfig)
                BleManager.getInstance().scan(object : BleScanCallback() {
                    override fun onScanStarted(success: Boolean) {
                        // 开始扫描的回调
                        scan.text = getString(R.string.scanner_action_stop_scanning)
                        myAdapter.clearOnLineState()
                        availableAdapter.clearDevices()
                        if (activity != null) {
                            (activity as BaseActivity).showLoadingDialog(R.string.refreshing)
                        }
                        scanControler.setIsScanning(true)
                    }

                    override fun onScanning(bleDevice: BleDevice) {
                        // 扫描到一个之前没有扫到过的设备的回调
                        if (!myAdapter.refreshOnLineState(bleDevice)) {
                            availableAdapter.addOrUpdateDevice(bleDevice)
                        }
                    }

                    override fun onScanFinished(scanResultList: List<BleDevice>) {
                        // 扫描完成的回调，列表里将不会有重复的设备
                        connectManager.connectAll()
                        scan.text = getString(R.string.scanner_action_scan)
                        availableAdapter.notifyDataSetChanged()
                        myAdapter.notifyDataSetChanged()
                    }
                })
            } else {
                BleManager.getInstance().enableBluetooth()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        myAdapter.setDevices(DBManager.getInstance().deviceBondDao.loadAll())
        noDevice.visibility = if (myAdapter.count > 0) View.GONE else View.VISIBLE
        changeMyDeviceHeight()
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (activity!!.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
                false
            } else {
                true
            }
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan()
            } else {
                Toast.makeText(activity, "PERMISSION WERE DENIED, SCAN WILL NOT WORK", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showPopWindow(view: View, position: Int) {
        currentOperateDevice = myAdapter.getItem(position) as DeviceBond
        if (pop == null) {
            val contentView = LayoutInflater.from(context).inflate(R.layout.menu, null)
            pop = PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            pop!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            pop!!.isOutsideTouchable = true
            pop!!.isTouchable = true
            pop!!.setOnDismissListener {
                backgroundAlpha(activity!!, 1f)
                currentOperateDevice = null
            }
        }
        pop!!.contentView.findViewById<TextView>(R.id.rename).setOnClickListener {
            showRenameDialog(currentOperateDevice)
            pop!!.dismiss()
        }
        pop!!.contentView.findViewById<TextView>(R.id.remove).setOnClickListener {
            showRemoveConfirm(currentOperateDevice)
            pop!!.dismiss()
        }
        val passwordView = pop!!.contentView.findViewById<TextView>(R.id.password)
        passwordView.setOnClickListener {
            showPasswordConfirm(currentOperateDevice!!)
            pop!!.dismiss()
        }
        passwordView.isEnabled = currentOperateDevice!!.isConnected
        pop!!.showAsDropDown(view, 0, 0, Gravity.BOTTOM or Gravity.START)
        backgroundAlpha(activity!!, 0.4f)
    }

    private fun backgroundAlpha(context: Activity, bgAlpha: Float) {
        val lp = context.window.attributes
        lp.alpha = bgAlpha
        context.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        context.window.attributes = lp
    }

    private fun showRemoveConfirm(deviceBond: DeviceBond?) {
        if (removeDialog == null) {
            removeDialog = Dialog(activity!!, R.style.DialogTheme)
            removeDialog!!.setCanceledOnTouchOutside(true)
            removeDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val view = LayoutInflater.from(activity).inflate(R.layout.dialog_remove, null)
            removeDialog!!.setContentView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            removeDialog!!.findViewById<TextView>(R.id.dialog_cancel).setOnClickListener { removeDialog!!.dismiss() }
        }
        removeDialog!!.findViewById<TextView>(R.id.dialog_sure).setOnClickListener {
            myAdapter.deleteDevice(deviceBond)
            BleManager.getInstance().disconnect(deviceBond!!.device)
            DBManager.getInstance().deviceBondDao.delete(deviceBond)
            changeMyDeviceHeight()
            noDevice.visibility = if (myAdapter.count > 0) View.GONE else View.VISIBLE
            removeDialog!!.dismiss()
        }
        removeDialog!!.show()
    }

    private fun showRenameDialog(deviceBond: DeviceBond?) {
        if (renameDialog == null) {
            renameDialog = Dialog(activity!!, R.style.DialogTheme)
            renameDialog!!.setCanceledOnTouchOutside(true)
            val view = LayoutInflater.from(activity).inflate(R.layout.dialog_rename, null)
            renameDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            renameDialog!!.setContentView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            renameDialog!!.findViewById<TextView>(R.id.dialog_cancel).setOnClickListener { renameDialog!!.dismiss() }
        }
        renameDialog!!.findViewById<TextView>(R.id.dialog_sure).setOnClickListener {
            val name = renameDialog!!.findViewById<TextView>(R.id.rename_edit).text.toString()
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(activity, "Rename Failed,Please input the right name", Toast.LENGTH_SHORT).show()
            } else {
                deviceBond?.display_name = name
                myAdapter.updateDevice(deviceBond)
                DBManager.getInstance().deviceBondDao.update(deviceBond)
                renameDialog!!.dismiss()
            }
        }
        renameDialog!!.show()
    }
//    private fun showPasswordConfirm(deviceBond: DeviceBond) {
//        if (passwordDialog == null) {
//            passwordDialog = Dialog(activity!!, R.style.DialogTheme)
//            passwordDialog!!.setCanceledOnTouchOutside(true)
//            val view = LayoutInflater.from(activity).inflate(R.layout.dialog_password_confirm, null)
//            passwordDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            view.findViewById<TextView>(R.id.dialog_cancel).setOnClickListener { passwordDialog!!.dismiss() }
//            passwordDialog!!.setContentView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
//        }
//        passwordDialog!!.findViewById<TextView>(R.id.dialog_sure).setOnClickListener {
//            showPasswordView(deviceBond)
//            passwordDialog!!.dismiss()
//        }
//        val editText = passwordDialog!!.findViewById<EditText>(R.id.rename_edit)
//        editText.setText(deviceBond.psw)
//        editText.setSelection(4)
//        editText.requestFocus()
//        passwordDialog!!.findViewById<TextView>(R.id.save_password).setOnClickListener {
//            val password = editText.text
//            if (!TextUtils.isEmpty(password) && password.length == 4) {
//                deviceBond.psw = password.toString()
//                DBManager.getInstance().deviceBondDao.update(deviceBond)
//                passwordDialog!!.dismiss()
//                val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                imm.hideSoftInputFromWindow(editText.windowToken, 0)
//            } else {
//                Toast.makeText(activity, R.string.password_rule_alert, Toast.LENGTH_SHORT).show()
//            }
//        }
//        passwordDialog!!.show()
//        editText.post {
//            val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.showSoftInput(editText,InputMethodManager.SHOW_IMPLICIT)
//        }
//    }

    private fun showPasswordConfirm(deviceBond: DeviceBond) {
        val passwordDialog = com.reb.switchbt.ui.PayPassDialog(activity!!, com.lzj.pass.dialog.R.style.dialog_pay_theme)
        passwordDialog.setAlertDialog(true)
                .setWindowSize(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, 0.5f)
                .setOutColse(true)
                .setGravity(com.lzj.pass.dialog.R.style.dialogOpenAnimation, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
        passwordDialog.payViewPass.setPass(deviceBond.psw)
        val saveBtn = passwordDialog.payViewPass.findViewById<Button>(R.id.save_password)
        saveBtn.setOnClickListener {
            val password = passwordDialog.payViewPass.strPass
            if (!TextUtils.isEmpty(password) && password.length == 4) {
                deviceBond.psw = password.toString()
                DBManager.getInstance().deviceBondDao.update(deviceBond)
                connectManager.write(deviceBond, BTCMD.queryCmd(deviceBond.psw.toInt()))
                passwordDialog.dismiss()
            } else {
                Toast.makeText(activity, R.string.password_rule_alert, Toast.LENGTH_SHORT).show()
            }
        }
        passwordDialog.payViewPass.findViewById<TextView>(R.id.dialog_sure).setOnClickListener {
            showPasswordView(deviceBond)
            passwordDialog.dismiss()
        }
        passwordDialog.payViewPass.findViewById<TextView>(R.id.dialog_cancel).setOnClickListener { passwordDialog!!.dismiss() }
        passwordDialog.payViewPass.setPayClickListener(object : com.reb.switchbt.ui.PayPassView.OnPayClickListener {
            override fun onPassFinish(passContent: String) {
            }

            override fun onPayClose() {
                passwordDialog.dismiss()
            }
        })
    }

    private fun showPasswordView(deviceBond: DeviceBond) {
        val passwordDialog = PayPassDialog(activity!!, com.lzj.pass.dialog.R.style.dialog_pay_theme)
        passwordDialog.setAlertDialog(false)
                .setWindowSize(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, 0f)
                .setOutColse(false)
                .setGravity(com.lzj.pass.dialog.R.style.dialogOpenAnimation, Gravity.BOTTOM)
        passwordDialog.payViewPass.setPayClickListener(object : PayPassView.OnPayClickListener {
            override fun onPassFinish(passContent: String) {
                if (TextUtils.isEmpty(passContent) || passContent.length != 4) {
                    return
                }
                passwordDialog.dismiss()
                deviceBond.tempPsw = passContent
                connectManager.write(deviceBond, BTCMD.modifyPsw(deviceBond.psw.toInt(), deviceBond.tempPsw.toInt()))
                DebugLog.i("passContent:$passContent")
            }

            override fun onPayClose() {
                passwordDialog.dismiss()
            }
        })
    }


    private val btStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
                if (BluetoothAdapter.STATE_ON == state) {
                    startScan()
                }
            }
        }
    }
}