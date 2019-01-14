package com.almikey.jiplace.ui.my_places

import android.app.Activity
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.almikey.jiplace.R
import com.almikey.jiplace.model.MyLocation
import com.almikey.jiplace.model.MyPlace
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_jiplace.view.*

import kotlinx.android.synthetic.main.jiplaces_recyclerview.*
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import java.util.*


class MyPlacesFragment : Fragment() {

    lateinit var mRecyclerview:RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_places_jiplace, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        var theLayout = view?.findViewById<LinearLayout>(R.id.jiplacesRecyclerview)
//        var bgAnim = theLayout.background as AnimationDrawable
//        bgAnim.setEnterFadeDuration(2000)
//        bgAnim.setExitFadeDuration(4000)
//        bgAnim.start()
        mRecyclerview = view?.findViewById(R.id.jiplace_recyclerview) as RecyclerView
        mRecyclerview.layoutManager = LinearLayoutManager(activity as Activity)
        val groupAdapter = GroupAdapter<ViewHolder>()

        var myPlace1 = MyPlace(22, DateTime.now().toDate(),
            MyLocation(33.toDouble(),44.toDouble()),"the one who knocks")
        var myPlace2 = MyPlace(32, DateTime.now().toDate(),
            MyLocation(33.toDouble(),44.toDouble()),"Heisenberg")
        var myPlace3 = MyPlace(42, DateTime.now().toDate(),
            MyLocation(33.toDouble(),44.toDouble()),"Science Jessee")


        groupAdapter.add(MyPlaceItem(myPlace1))
        groupAdapter.add(MyPlaceItem(myPlace2))
        groupAdapter.add(MyPlaceItem(myPlace3))
        mRecyclerview.adapter = groupAdapter
    }

    class MyPlaceItem(var myPlace:MyPlace):Item() {
        override fun bind(viewHolder: ViewHolder, position: Int) {

            val formatter= DateTimeFormat.forPattern("d MMMM")
            val theDate = formatter.print(DateTime(myPlace.time))
            viewHolder.itemView.jiplace_item_date.text =theDate
            val formatter2 = DateTimeFormat.forPattern("hh:mm aa")
            val theTime = formatter2.print(DateTime(myPlace.time))
            viewHolder.itemView.jiplace_item_time.text =theTime
            viewHolder.itemView.jiplace_item_hint.text = myPlace.hint
           return
        }

        override fun getLayout(): Int {
           return R.layout.item_jiplace
        }
    }

}
