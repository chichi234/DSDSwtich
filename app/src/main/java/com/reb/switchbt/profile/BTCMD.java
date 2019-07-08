package com.reb.switchbt.profile;

/**
 * File description
 *
 * @author tonly
 * @version 1.0
 * @date 2019/7/2 14:20
 * @package_name com.reb.switchbt.profile
 * @project_name DSDSwtich
 * @history At 2019/7/2 14:20 created by tonly
 */
public class BTCMD {
//    byte[] cmd = {(byte)0xA1, (byte)(psw >> 8), (byte)psw, 0x0, , 0, (byte)0xAA};
    public static byte[] queryCmd(int psw) {
        byte[] cmd = {(byte)0xA1, (byte)(psw >> 8), (byte)psw, 0x05, 0, 0, (byte)0xAA};
        setCheck(cmd);
        return cmd;
    }

    public static byte[] modifyPsw(int psw, int newPsw) {
        byte[] cmd = {(byte)0xA1, (byte)(psw >> 8), (byte)psw, 0x11, (byte)(newPsw >> 8), (byte)newPsw, 0, (byte)0xAA};
        setCheck(cmd);
        return cmd;
    }

    public static byte[] controlSingleRelay(int psw, int index, boolean isOpen) {
        byte[] cmd = {(byte)0xA1, (byte)(psw >> 8), (byte)psw, (byte)(isOpen? 0x01:0x02), (byte)index,  0, (byte)0xAA};
        setCheck(cmd);
        return cmd;
    }

    private static void setCheck(byte[] cmd) {
        byte sum = getCheck(cmd);
        cmd[cmd.length - 2] = sum;
    }

    public static boolean check(byte[] cmd) {
        byte sum = getCheck(cmd);
        return  sum == cmd[cmd.length - 2];
    }

    private static byte getCheck(byte[] cmd) {
        int loop = cmd.length - 2;
        byte sum = cmd[0];
        for (int i = 1; i < loop; i++) {
            sum = (byte)(sum ^ cmd[i]);
        }
        return sum;
    }

}
