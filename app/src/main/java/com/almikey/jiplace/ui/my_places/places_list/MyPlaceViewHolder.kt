package com.almikey.jiplace.ui.my_places.places_list

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.almikey.jiplace.R
import com.almikey.jiplace.model.MyPlace
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.koin.standalone.KoinComponent
import android.widget.Button
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File


class MyPlaceViewHolder(var context: Fragment, var view: View) : KoinComponent
    , RecyclerView.ViewHolder(view) {

    var b = view.setOnLongClickListener(object : View.OnLongClickListener {
        override fun onLongClick(v: View): Boolean {
            view.showContextMenu()
            return true
        }
    });

    var itemHint = view.findViewById<TextView>(R.id.jiplace_item_hint)
    var itemTime = view.findViewById<TextView>(R.id.jiplace_item_time)
    var itemDate = view.findViewById<TextView>(R.id.jiplace_item_date)
    var itemLatitude = view.findViewById<TextView>(R.id.jiplace_item_latitude)
    var itemLongitude = view.findViewById<TextView>(R.id.jiplace_item_longitude)
    var itemPic = view.findViewById<Button>(R.id.jiplace_item_photo)
    var itemPicLoaded = view.findViewById<CircleImageView>(R.id.jiplace_item_photo_circle)

    fun bindTo(myPlace: MyPlace) {
        val formatter = DateTimeFormat.forPattern("d MMMM YYYY")
        val theDate = formatter.print(DateTime(myPlace.time))
        itemDate.text = theDate
        val formatter2 = DateTimeFormat.forPattern("hh:mm aa")
        val theTime = formatter2.print(DateTime(myPlace.time))
        itemTime.text = theTime
        itemHint.text = myPlace.hint
        itemLatitude.text = myPlace.location.latitude.toString()
        itemLongitude.text = myPlace.location.longitude.toString()

        if (!myPlace.profile.localPicUrl.trim().isEmpty() && myPlace.profile.localPicUrl.trim().isEmpty()) {
            // val picture = BitmapFactory.decodeStream(context.activity!!.contentResolver.openInputStream(Uri.parse(myPlace.profile.localPicUrl)))

            Log.d("image path", "file://" + myPlace.profile.localPicUrl)
            var cb = Picasso.get().load(File(myPlace.profile.localPicUrl))
                .error(R.drawable.myplace_image_loading)
                .placeholder(R.drawable.myplace_image_loading)
                .config(Bitmap.Config.RGB_565)
                .fit()
                .centerCrop().into(itemPicLoaded);
            itemPic.alpha = 0f
        } else if (!myPlace.profile.localPicUrl.trim().isEmpty() && !myPlace.profile.firebasePicUrl.trim().isEmpty()) {
            // val picture = BitmapFactory.decodeStream(context.activity!!.contentResolver.openInputStream(Uri.parse(myPlace.profile.localPicUrl)))

            Log.d("firebase url", myPlace.profile.firebasePicUrl)
            var cb = Picasso.get().load(myPlace.profile.firebasePicUrl)
                .placeholder(R.drawable.myplace_image_loading)
                .config(Bitmap.Config.RGB_565)
                .fit()
                .centerCrop().into(itemPicLoaded);
            itemPic.alpha = 0f
        }

    }

}