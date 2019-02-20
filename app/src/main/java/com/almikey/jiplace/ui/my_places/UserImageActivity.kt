package com.almikey.jiplace.ui.my_places

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.almikey.jiplace.R



class UserImageActivity : AppCompatActivity() {

    private lateinit var mUserPictureCollectionPagerAdapter: UserPictureCollectionPagerAdapter
    private lateinit var mViewPager: ViewPager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_image)

        // ViewPager and its adapters use support library
        // fragments, so use supportFragmentManager.
        val b = this.intent.extras
        val mArray = b!!.getStringArray("user_image_urls")
        mUserPictureCollectionPagerAdapter = UserPictureCollectionPagerAdapter(supportFragmentManager,
            mArray.toCollection(ArrayList()))
        mViewPager = findViewById(R.id.user_image_pager)
        mViewPager.adapter = mUserPictureCollectionPagerAdapter
    }
}
