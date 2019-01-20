package com.almikey.jiplace.ui.my_places

import android.app.Activity
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.chatsdk.core.session.NM
import com.almikey.jiplace.R
import com.almikey.jiplace.model.MyLocation
import com.almikey.jiplace.model.MyPlace
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.item_jiplace.view.*

import kotlinx.android.synthetic.main.jiplaces_recyclerview.*
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class MyPlacesFragment : Fragment() {

    val myPlacesViewModel: MyPlacesViewModel by viewModel()

    lateinit var mRecyclerview:RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NM.auth().authenticateWithCachedToken()
        return
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.jiplaces_recyclerview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerview = view?.findViewById(R.id.jiplace_recyclerview) as RecyclerView
        mRecyclerview.layoutManager = LinearLayoutManager(activity as Activity)
        val groupAdapter = GroupAdapter<ViewHolder>()

        var myPlace1 = MyPlace(22, time=Date(),
           location =  MyLocation(33.toFloat(),44.toFloat()),hint = "a message from the one who knocks")


        myPlacesViewModel.myPlaces.observeOn(AndroidSchedulers.mainThread()).subscribe {
            it.forEach {
               groupAdapter.add(MyPlaceItem(this,it))
            }
        }

        groupAdapter.add(MyPlaceItem(this,myPlace1))
        mRecyclerview.adapter = groupAdapter
    }

    class MyPlaceItem(var context:Fragment, var myPlace:MyPlace):Item() {

        override fun createViewHolder(itemView: View): ViewHolder {

            return super.createViewHolder(itemView)
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {

            viewHolder.itemView.setOnClickListener {
                var bundle = bundleOf("latitude" to myPlace.location.latitude.toDouble()
                    ,"longitude" to myPlace.location.longitude.toDouble(),"theTime" to myPlace.time.time)
                NavHostFragment.findNavController(context).navigate(R.id.myPlacesUserFragment, bundle)
            }


            val formatter= DateTimeFormat.forPattern("d MMMM YYYY")
            val theDate = formatter.print(DateTime(myPlace.time))
            viewHolder.itemView.jiplace_item_date.text =theDate
            val formatter2 = DateTimeFormat.forPattern("hh:mm aa")
            val theTime = formatter2.print(DateTime(myPlace.time))
            viewHolder.itemView.jiplace_item_time.text =theTime
            viewHolder.itemView.jiplace_item_hint.text = myPlace.hint
            viewHolder.itemView.theLat.text = myPlace.location.latitude.toString()
            viewHolder.itemView.theLon.text = myPlace.location.longitude.toString()
           return
        }

        override fun getLayout(): Int {
           return R.layout.item_jiplace
        }
    }



}
