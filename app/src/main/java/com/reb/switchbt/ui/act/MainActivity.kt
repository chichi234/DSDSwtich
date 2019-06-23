package com.reb.switchbt.ui.act

import android.app.Activity
import android.os.Bundle
import com.reb.switchbt.R
//import com.reb.switchbt.ui.base.BaseFragmentActivity
//import com.reb.switchbt.ui.frag.AboutFragment
//import com.reb.switchbt.ui.frag.BTFragment
//import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity() {

//    private lateinit var btFragment : BTFragment
//    private lateinit var aboutFragment:AboutFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        initFragment(savedInstanceState)
//        main_tab.setOnCheckedChangeListener  {_, id ->
//            when(id){
//                R.id.devices -> changeFragment(btFragment)
//                R.id.about -> changeFragment(aboutFragment)
//            }
//        }
//        main_tab.check(R.id.devices)
    }
//
//    private fun initFragment(savedInstanceState: Bundle?) {
//        if (savedInstanceState == null) {
//            btFragment = BTFragment()
//            aboutFragment = AboutFragment()
//        } else {
//            btFragment = supportFragmentManager.findFragmentByTag(BTFragment::class.java.simpleName) as BTFragment
//            aboutFragment = supportFragmentManager.findFragmentByTag(AboutFragment::class.java.simpleName) as AboutFragment
//        }
//    }
}
