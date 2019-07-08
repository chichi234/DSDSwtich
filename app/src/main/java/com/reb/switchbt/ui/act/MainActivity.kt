package com.reb.switchbt.ui.act

import android.os.Bundle
import com.clj.fastble.BleManager
import com.reb.switchbt.R
import com.reb.switchbt.ui.base.BaseFragmentActivity
import com.reb.switchbt.ui.frag.AboutFragment
import com.reb.switchbt.ui.frag.BTFragment
import com.reb.switchbt.util.DebugLog
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseFragmentActivity() {

    private lateinit var btFragment : BTFragment
    private lateinit var aboutFragment:AboutFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initFragment(savedInstanceState)
        main_tab.setOnCheckedChangeListener  {_, id ->
            when(id){
                R.id.devices -> changeFragment(btFragment)
                R.id.about -> changeFragment(aboutFragment)
            }
        }
        main_tab.check(R.id.devices)
        changeFragment(btFragment)
    }

    private fun initFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            btFragment = BTFragment()
            aboutFragment = AboutFragment()
        } else {
            btFragment = supportFragmentManager.findFragmentByTag(BTFragment::class.java.simpleName) as BTFragment
            aboutFragment = supportFragmentManager.findFragmentByTag(AboutFragment::class.java.simpleName) as AboutFragment
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DebugLog.i("onDestroy")
        BleManager.getInstance().disconnectAllDevice()

    }
}
