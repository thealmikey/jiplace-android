package com.almikey.jiplace.ui.my_places

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class UserPictureCollectionPagerAdapter(var fm:FragmentManager,var userPictures:ArrayList<String>):FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
      val fragment = UserPictureFragment()
        fragment.arguments= Bundle().apply {
            putString("user_pic",userPictures[position])
        }
        return fragment
    }

    override fun getCount(): Int {
       return userPictures.size
    }
}