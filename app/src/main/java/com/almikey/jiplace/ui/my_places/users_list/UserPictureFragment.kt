package com.almikey.jiplace.ui.my_places.users_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.almikey.jiplace.R
import com.squareup.picasso.Picasso

class UserPictureFragment:Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View= inflater.inflate(R.layout.fragment_user_profile_viewpage, container, false)
        arguments?.takeIf { it.containsKey("user_pic") }?.apply {
            val imageView: ImageView = rootView.findViewById(R.id.user_profile_image)
            var imageUrl= getString("user_pic")
            Picasso.get().load(imageUrl)
                .placeholder(R.drawable.myplace_image_loading)
                .fit()
                .centerCrop().into(imageView);

        }
        return rootView
    }
}