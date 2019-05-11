package com.almikey.jiplace


import android.content.Context
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import io.github.inflationx.viewpump.ViewPumpContextWrapper


class MainActivity : AppCompatActivity() {

    val firebaseAuth: FirebaseAuth by lazy<FirebaseAuth> { FirebaseAuth.getInstance() }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.jiplace_chats_menu -> {
                if (firebaseAuth.uid != null) {
                    var intent = Intent(this@MainActivity, co.chatsdk.ui.main.MainDrawActivity::class.java)
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                } else {
                    return@OnNavigationItemSelectedListener true
                }
            }
            R.id.jiplace_home_menu -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.theNewHome)
                return@OnNavigationItemSelectedListener true
            }
            R.id.jiplace_myplaces_menu -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.jiplaces)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }
}
