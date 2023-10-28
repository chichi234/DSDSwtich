package com.reb.switchbt

import com.reb.switchbt.profile.BTCMD
import com.reb.switchbt.util.HexStringConver
import org.junit.Test

import org.junit.Assert.*
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val i = (0xA1) xor (0x04)
        System.out.println("===" + HexStringConver.bytes2HexStr(BTCMD.queryCmd(1234)))
    }
}
