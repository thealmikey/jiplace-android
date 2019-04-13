package com.almikey.jiplace.ui.my_places.users_list

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import co.chatsdk.core.dao.Thread
import co.chatsdk.core.session.ChatSDK
import co.chatsdk.firebase.wrappers.UserWrapper
import com.almikey.jiplace.R
import com.almikey.jiplace.ui.call.AudioCallActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.uber.autodispose.autoDisposable
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.jiplaces_users_inplace_user_item.view.*
import org.koin.standalone.KoinComponent

class MyPlaceUserViewHolder(var context: Fragment, var view: View) : KoinComponent
    , RecyclerView.ViewHolder(view) {

    var userItemHint = view.findViewById<TextView>(R.id.jiplace_item_hint)
    var userItemChat = view.findViewById<TextView>(R.id.jiplaceChat)
    var userItemCall = view.findViewById<TextView>(R.id.jiplaceCall)
    var userItemPic = view.findViewById<ImageView>(R.id.myplace_user_pic)


    fun bindTo(myPlaceUser: MyPlaceUser){

       userItemHint.text = myPlaceUser.theHint

        var wrapper: UserWrapper = UserWrapper.initWithEntityId(myPlaceUser.theKey);
        var userObservable = wrapper.metaOn();
        wrapper.onlineOn();
        userObservable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.entityID!! != null) {

                    userItemChat.isEnabled = true
                }
            }

        //load the first image from the images in the user/profile/{time}/uuid.png into
        //the imageview
        if (!myPlaceUser.imageUrls.isEmpty()) {
            Picasso.get().load(myPlaceUser.imageUrls[0])
                .config(Bitmap.Config.RGB_565)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.myuser)
                .error(R.drawable.myuser)
                .into(userItemPic);
            //set an onClick that shows other images associated with this jiplace for this timespan
            //will load a view pager that loads thet items in imageUrls one by one
            userItemPic.setOnClickListener {
                var b: Bundle = Bundle();
                b.putStringArray("user_image_urls", myPlaceUser.imageUrls.toTypedArray());
                var i: Intent = Intent(context.context!!, UserImageActivity::class.java)
                i.putExtras(b);
                context.activity!!.startActivity(i)
            }
        }else{
            if (myPlaceUser.imageUrls.isEmpty()){
                Picasso.get().load(R.drawable.myuser)
                    .config(Bitmap.Config.RGB_565)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.myuser)
                    .error(R.drawable.myuser)
                    .into(userItemPic);
            }
        }

        userItemCall.setOnClickListener {
            var b: Bundle = Bundle();
            b.putString("other_user_to_call", myPlaceUser.user.entityID);
            var i: Intent = Intent(context.context!!, AudioCallActivity::class.java)
            i.putExtras(b);
            context.activity!!.startActivity(i)
        }


        userItemChat.setOnClickListener {
            Log.d("user entity id", "${myPlaceUser.user.entityID}")
            if (FirebaseAuth.getInstance().uid!! != null && FirebaseAuth.getInstance().uid!! != myPlaceUser.user.entityID) {
                Log.d("uuid n", "is reached")
                ChatSDK.thread().createThread("${myPlaceUser.user.entityID}-${myPlaceUser.theTime}", myPlaceUser.user, ChatSDK.currentUser())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Consumer<Thread> {
                        override fun accept(thread: Thread) {
                            var fbId = FirebaseAuth.getInstance().uid
                            var refChatLink: DatabaseReference = FirebaseDatabase.getInstance()
                                .getReference("myplaceusers/chat/$fbId/${myPlaceUser.user.entityID}")
                            refChatLink.setValue(true)
                            ChatSDK.ui()
                                .startChatActivityForID(context.activity!!.applicationContext, thread.entityID);
                        }
                    }, object : Consumer<Throwable> {
                        override fun accept(throwable: Throwable) {
                            // Handle error
                        }
                    });
            }
        }
        //findNavController(context).navigate(R.id.myPlacesUserFragment)
    }

}