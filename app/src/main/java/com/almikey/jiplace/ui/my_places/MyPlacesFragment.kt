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
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder

import kotlinx.android.synthetic.main.jiplaces_recyclerview.*

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
        groupAdapter.add(Aplace())
        groupAdapter.add(Aplace())
        groupAdapter.add(Aplace())
        groupAdapter.add(Aplace())
        groupAdapter.add(Aplace())
        groupAdapter.add(Aplace())
        mRecyclerview.adapter = groupAdapter
    }

    class Aplace():Item() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
           return
        }

        override fun getLayout(): Int {
           return R.layout.item_jiplace
        }
    }

}
