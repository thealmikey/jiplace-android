package com.almikey.jiplace.ui.my_places

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.chatsdk.core.session.NM
import com.almikey.jiplace.R
import com.almikey.jiplace.model.MyLocation
import com.almikey.jiplace.model.MyPlace
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.R.menu
import android.view.MenuInflater


class MyPlacesFragment : Fragment() {

    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    val myPlacesViewModel: MyPlaceViewModel by viewModel()

    lateinit var mCoordinatorLayout:CoordinatorLayout

    lateinit var mRecyclerview: RecyclerView

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
//        mRecyclerview = view?.findViewById(R.id.jiplace_recyclerview) as RecyclerView
        mCoordinatorLayout = view?.findViewById(R.id.jiplaces_container_for_recyclerview) as CoordinatorLayout
        mRecyclerview = ContextMenuRecyclerView(this.context!!)
        var params:ViewGroup.LayoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mRecyclerview.layoutParams = params
        mCoordinatorLayout.addView(mRecyclerview)
        mRecyclerview.layoutManager = LinearLayoutManager(activity as Activity)

//        var mListener =  RvMyPlaceItemListener(this.context!!,mRecyclerview,MyPlaceClickListener(this.context!!))
//
//        mRecyclerview.addOnItemTouchListener(mListener)

        var myPlace1 = MyPlace(
            22, time = Date(),
            location = MyLocation(33.toFloat(), 44.toFloat()), hint = "a message from the one who knocks"
        )

        var myPlaces:MutableList<MyPlace> = mutableListOf()
        var myPlacesAdapter = MyPlaceAdapter(this,myPlaces)

        myPlacesViewModel.myPlaces.observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(scopeProvider)
            .subscribe {
                it.forEach {
                    myPlaces.add(it)
                }
                myPlacesAdapter.notifyDataSetChanged()
            }


        mRecyclerview.adapter = myPlacesAdapter
        registerForContextMenu(mRecyclerview);
    }


    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = activity!!.menuInflater
        inflater.inflate(R.menu.jiplace_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        Log.d("context menu item","context menu item selected")
        val info = item.menuInfo as ContextMenuRecyclerView.RecyclerViewContextMenuInfo
        return when (item.itemId) {
            R.id.edit_jiplace_description -> {
                Log.d("edit","context menu")
                true
            }
            R.id.delete_jiplace -> {
                Log.d("delete","context menu")
                true
            }
            else -> super.onContextItemSelected(item)
        }

        Toast.makeText(this.context,"${info.position}",Toast.LENGTH_LONG).show()
        // handle menu item here
        return super.onContextItemSelected(item)
    }


}




//    override fun onContextItemSelected(item: MenuItem): Boolean {
//
//        when (item.getItemId()) {
//            R.id.edit_jiplace_description -> {
//
//                return true
//            }
//            R.id.delete_jiplace -> {
//
//                return true
//            }
//        }
//        return super.onContextItemSelected(item);
//    }




