package com.newe.rangrang


import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.widget.Toast
import com.newe.rangrang.fragment.FlashFragment
import com.newe.rangrang.fragment.ScreenFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomBar.setOnTabSelectListener{tabId ->
            when(tabId){
                R.id.tab_flash -> replaceFragment(FlashFragment())
                R.id.tab_screen -> replaceFragment(ScreenFragment())
                else -> Toast.makeText(this,"出错啦",Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container,fragment)
                .commit()
    }
}
