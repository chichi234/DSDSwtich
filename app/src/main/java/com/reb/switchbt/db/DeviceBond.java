package com.reb.switchbt.db;

import android.content.ContentValues;
import android.text.TextUtils;

import com.clj.fastble.data.BleDevice;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;
import org.jetbrains.annotations.Nullable;

/**
 * File description
 *
 * @author Reb
 * @version 1.0
 * @date 2019-3-6 21:01
 * @package_name com.reb.bluetooth.db
 * @project_name DSD_SPP
 * @history At 2019-3-6 21:01 created by Reb
 */
@Entity
public class DeviceBond {
    @Id
    private String mac;
    private String name;
    private String insert_time;
    private String display_name;
    private String psw = "1234";
    private int type = 1;
    @Transient
    public boolean isOnline = false;
    @Transient
    public boolean isSelect = false;
    @Transient
    public boolean isConnected = false;
    @Transient
    public boolean isConnectting = false;
    @Transient
    private String msg = "OffLine";
    @Transient
    public boolean[] relayState = new boolean[3];
    @Transient
    public boolean[] relayTempState = new boolean[3];
    @Transient
    public BleDevice device;
    @Transient
    public String tempPsw = psw;
    @Transient
    public Integer relayCount = 3;

    public String getMsg() {
        if (isOnline) {
            if (isConnected) {
                msg = "";
            } else {
               msg = "";
            }
        } else {
            msg = "OffLine";
        }
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Generated(hash = 1547833244)
    public DeviceBond() {
    }

    @Generated(hash = 715912915)
    public DeviceBond(String mac, String name, String insert_time,
            String display_name, String psw, int type) {
        this.mac = mac;
        this.name = name;
        this.insert_time = insert_time;
        this.display_name = display_name;
        this.psw = psw;
        this.type = type;
    }
    public String getMac() {
        return this.mac;
    }
    public void setMac(String mac) {
        this.mac = mac;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getInsert_time() {
        return this.insert_time;
    }
    public void setInsert_time(String insert_time) {
        this.insert_time = insert_time;
    }
    public String getDisplay_name() {
        if (TextUtils.isEmpty(display_name)) {
            return "UNKNOWN";
        }
        return this.display_name;
    }
    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }
    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
    }

    public String getPsw() {
        return this.psw;
    }

    public void setPsw(String psw) {
        this.psw = psw;
    }
}
