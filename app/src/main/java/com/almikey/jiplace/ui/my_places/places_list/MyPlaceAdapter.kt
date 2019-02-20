package com.almikey.jiplace.ui.my_places.places_list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.almikey.jiplace.R
import com.almikey.jiplace.model.MyPlace
import kotlinx.android.synthetic.main.item_jiplace.view.*

class MyPlaceAdapter(var context: Fragment, var myplaces: List<MyPlace>) :
    RecyclerView.Adapter<MyPlaceViewHolder>() {

    var thePosition: Int = -1

    lateinit var uuidForSelectedImage: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPlaceViewHolder {
        var view: View = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_jiplace, parent, false);
        return MyPlaceViewHolder(context, view)
    }

    override fun getItemCount(): Int {
        return myplaces.size
    }

    override fun onViewRecycled(holder: MyPlaceViewHolder) {
        holder.view.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    override fun onBindViewHolder(holder: MyPlaceViewHolder, position: Int) {

        holder.view.setOnClickListener {
            var bundle = bundleOf(
                "latitude" to myplaces[position].location.latitude.toDouble()
                , "longitude" to myplaces[position].location.longitude.toDouble(),
                "theTime" to myplaces[position].time.time,
                "theUUID" to myplaces[position].uuidString
            )
            NavHostFragment.findNavController(context).navigate(R.id.myPlacesUserFragment, bundle)
        }

        holder.view.jiplace_item_photo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            var theBundle = Bundle()
            theBundle.apply {
                putString("theUuid", myplaces[position].uuidString)
            }
            uuidForSelectedImage = myplaces[position].uuidString
            thePosition = position
            context.startActivityForResult(intent, 0, theBundle)
        }

        holder.bindTo(myplaces[position])
    }
}