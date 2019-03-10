package com.almikey.jiplace.ui.my_places.users_list

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import co.chatsdk.core.dao.Thread
import co.chatsdk.core.dao.User
import co.chatsdk.core.session.ChatSDK
import co.chatsdk.firebase.wrappers.UserWrapper
import com.almikey.jiplace.R
import com.almikey.jiplace.ui.call.AudioCallActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.jiplaces_users_inplace_user_item.view.*

class JiplaceUserItem(
    var context: Fragment,
    var theTime: Long,
    var user: User,
    var theKey: String,
    var imageUrls: ArrayList<String>
) : Item() {
    public val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(context) }
    override fun bind(viewHolder: ViewHolder, position: Int) {

        var wrapper: UserWrapper = UserWrapper.initWithEntityId(theKey);
        var userObservable = wrapper.metaOn();
        wrapper.onlineOn();
        userObservable.observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(scopeProvider)
            .subscribe {
                if (it.entityID!! != null) {

                    viewHolder.itemView.jiplaceChat.isEnabled = true
                }
            }

        //load the first image from the images in the user/profile/{time}/uuid.png into
        //the imageview
        if (!imageUrls.isEmpty()) {
            Picasso.get().load(imageUrls[0])
                .config(Bitmap.Config.RGB_565)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.myuser)
                .error(R.drawable.myuser)
                .into(viewHolder.itemView.myplace_user_pic);
            //set an onClick that shows other images associated with this jiplace for this timespan
            //will load a view pager that loads thet items in imageUrls one by one
            viewHolder.itemView.myplace_user_pic.setOnClickListener {
                var b: Bundle = Bundle();
                b.putStringArray("user_image_urls", imageUrls.toTypedArray());
                var i: Intent = Intent(context.context!!, UserImageActivity::class.java)
                i.putExtras(b);
                context.activity!!.startActivity(i)
            }
        }else{
            if (imageUrls.isEmpty()){
                Picasso.get().load(R.drawable.myuser)
                    .config(Bitmap.Config.RGB_565)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.myuser)
                    .error(R.drawable.myuser)
                    .into(viewHolder.itemView.myplace_user_pic);
            }
        }

        viewHolder.itemView.jiplaceCall.setOnClickListener {
            var b: Bundle = Bundle();
            b.putString("other_user_to_call", user.entityID);
            var i: Intent = Intent(context.context!!, AudioCallActivity::class.java)
            i.putExtras(b);
            context.activity!!.startActivity(i)
        }


        viewHolder.itemView.jiplaceChat.setOnClickListener {
            Log.d("user entity id", "${user.entityID}")
            if (FirebaseAuth.getInstance().uid!! != null && FirebaseAuth.getInstance().uid!! != user.entityID) {
                Log.d("uuid n", "is reached")
                ChatSDK.thread().createThread("${user.entityID}-$theTime", user, ChatSDK.currentUser())
                    .observeOn(AndroidSchedulers.mainThread())
                    .autoDisposable(scopeProvider)
                    .subscribe(object : Consumer<Thread> {
                        override fun accept(thread: Thread) {
                            var fbId = FirebaseAuth.getInstance().uid
                            var refChatLink: DatabaseReference = FirebaseDatabase.getInstance()
                                .getReference("myplaceusers/chat/$fbId/${user.entityID}")
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

    override fun getLayout(): Int {
        return R.layout.jiplaces_users_inplace_user_item
    }

    override fun createViewHolder(itemView: View): ViewHolder {
        return super.createViewHolder(itemView)
    }

}