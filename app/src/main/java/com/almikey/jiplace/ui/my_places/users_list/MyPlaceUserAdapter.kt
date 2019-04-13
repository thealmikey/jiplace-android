package com.almikey.jiplace.ui.my_places.users_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.almikey.jiplace.R
import com.almikey.jiplace.model.MyPlace

class MyPlaceUserAdapter(var context: Fragment, var myPlacesUser: List<MyPlaceUser>) :
    RecyclerView.Adapter<MyPlaceUserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPlaceUserViewHolder {
        var view: View = LayoutInflater.from(parent.getContext()).inflate(R.layout.jiplaces_users_inplace_user_item, parent, false);
        return MyPlaceUserViewHolder(context, view)
    }

    override fun getItemCount(): Int {
        return myPlacesUser.size
    }

    override fun onBindViewHolder(holder: MyPlaceUserViewHolder, position: Int) {
        holder.bindTo(myPlacesUser[position])
    }

}