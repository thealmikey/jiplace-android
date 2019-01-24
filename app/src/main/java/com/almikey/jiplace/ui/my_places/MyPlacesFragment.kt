package com.almikey.jiplace.ui.my_places

import android.app.Activity
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
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
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.item_jiplace.view.*

import kotlinx.android.synthetic.main.jiplaces_recyclerview.*
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import android.view.ContextMenu
import android.view.View.OnCreateContextMenuListener
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.almikey.jiplace.repository.MyPlacesRepository
import com.almikey.jiplace.worker.HintPickerWorker
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject


class MyPlacesFragment : Fragment() {

    class MyPlacesListDiff (private val oldList: List<MyPlace>, private val newList: List<MyPlace>):DiffUtil.Callback(){
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].uuidString == newList[newItemPosition].uuidString
        }

        override fun getOldListSize(): Int {
           return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].hint == newList[newItemPosition].hint
        }
    }




    val groupAdapter by lazy {
        GroupAdapter<ViewHolder>()
    }
    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    val myPlacesViewModel: MyPlacesViewModel by viewModel()

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
        mRecyclerview = view?.findViewById(R.id.jiplace_recyclerview) as RecyclerView
        mRecyclerview.layoutManager = LinearLayoutManager(activity as Activity)

        var myPlace1 = MyPlace(
            22, time = Date(),
            location = MyLocation(33.toFloat(), 44.toFloat()), hint = "a message from the one who knocks"
        )


        myPlacesViewModel.myPlaces.observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(scopeProvider)
            .subscribe {
                it.forEach {
                    groupAdapter.add(MyPlaceItem(this, it))
                }
            }

        groupAdapter.add(MyPlaceItem(this, myPlace1))
        mRecyclerview.adapter = groupAdapter
        registerForContextMenu(mRecyclerview)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = this.activity!!.menuInflater
        Log.d("the view","${v!!.jiplace_item_hint.text}")
        inflater.inflate(R.menu.jiplace_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {

        when (item.getItemId()) {
            R.id.edit_jiplace_description -> {

                return true
            }
            R.id.delete_jiplace -> {

                return true
            }
        }
        return super.onContextItemSelected(item);
    }



    class MyPlaceViewHolder(var context: Fragment, var view: View) : com.xwray.groupie.ViewHolder(view){

        class RecyclerViewContextMenuInfo(var thePosition:Int,var theId:Int):ContextMenu.ContextMenuInfo

    }

    class MyPlaceItem(var context: Fragment, var myPlace: MyPlace) : Item<MyPlaceViewHolder>(), KoinComponent {
        val myPlacesRepo: MyPlacesRepository by inject()
        fun getHintAfterJiplaceOther(theUuid: String,tv:TextView){
            lateinit var theHintStr: String
            var dialog = MaterialDialog(context.context!!).show {
                customView(R.layout.jiplace_description_hint)
            }
            val customView = dialog.getCustomView()
            var theText = customView?.findViewById<EditText>(R.id.jiplaceDescription)
            theText?.text.toString()

            dialog.negativeButton {
               Log.d("negative on modal","don't change nothing")
            }
            dialog.positiveButton {
                theHintStr = theText?.text.toString()
                var thePlace = myPlacesRepo.findByUuid(theUuid).subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io()).take(1).subscribe {
                        var newPlace = it.copy(hint = theHintStr)
                        myPlacesRepo.update(newPlace)
                    }
                tv.text = theHintStr
            }
        }

        override fun createViewHolder(itemView: View): MyPlaceViewHolder {
            super.createViewHolder(itemView)
            return MyPlaceViewHolder(context, itemView)
        }


        override fun bind(viewHolder: MyPlaceViewHolder, position: Int) {
//            context.registerForContextMenu(viewHolder.itemView)
            viewHolder.itemView.setOnLongClickListener {
                Log.d("pressed long","i been long pressed")
//                it.showContextMenu()
               var tv: TextView = it.jiplace_item_hint

                var mPopUpMenuListener:PopupMenu.OnMenuItemClickListener = object:PopupMenu.OnMenuItemClickListener{
                    override fun onMenuItemClick(item: MenuItem?): Boolean {
                        when (item!!.getItemId()) {
                            R.id.edit_jiplace_description ->{
                                  Log.d("this hint",myPlace.hint)
                            getHintAfterJiplaceOther(myPlace.uuidString,tv)

                                return true
                            }
                            R.id.delete_jiplace ->{

                                val formatter = DateTimeFormat.forPattern("d MMMM YYYY")
                                val theDate = formatter.print(DateTime(myPlace.time))
                                Log.d("this date",theDate)
                                return true
                            }

                        }
                        return false
                    }

                }
                var thePopupMenu = PopupMenu(context.context!!, viewHolder.itemView)
                thePopupMenu.setOnMenuItemClickListener(mPopUpMenuListener)
                thePopupMenu.inflate(R.menu.jiplace_context_menu)
                thePopupMenu.show()
                return@setOnLongClickListener true
                }



            viewHolder.itemView.setOnClickListener {
                var bundle = bundleOf(
                    "latitude" to myPlace.location.latitude.toDouble()
                    , "longitude" to myPlace.location.longitude.toDouble(), "theTime" to myPlace.time.time
                )
                NavHostFragment.findNavController(context).navigate(R.id.myPlacesUserFragment, bundle)
            }


            val formatter = DateTimeFormat.forPattern("d MMMM YYYY")
            val theDate = formatter.print(DateTime(myPlace.time))
            viewHolder.itemView.jiplace_item_date.text = theDate
            val formatter2 = DateTimeFormat.forPattern("hh:mm aa")
            val theTime = formatter2.print(DateTime(myPlace.time))
            viewHolder.itemView.jiplace_item_time.text = theTime
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




