package com.almikey.jiplace

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.RecyclerView

interface ClickListener {
    fun onClick(view: View, adapterPosition: Int)
}


class RvMyPlaceItemListener(var context: Context, var mRecyclerView: RecyclerView, var mClickListener: ClickListener) :
    RecyclerView.OnItemTouchListener {

    var mSimpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            Log.d("single tap", "single tap confirmed")
            return false
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            Log.d("single tap", "single tap")
            return false
        }

        override fun onLongPress(e: MotionEvent?) {
            Log.d("RV long", "long pressed")
            val child: View = mRecyclerView.findChildViewUnder(e!!.getX(), e!!.getY())!!
            val childPosition: Int = mRecyclerView.getChildPosition(child);
            mClickListener.onClick(child, mRecyclerView.getChildAdapterPosition(child));
            return
        }
    }

    var mGestureDetector = GestureDetectorCompat(context, mSimpleOnGestureListener)

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        Log.d("RV ontouchevent", "called")
        return
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        var theState = mGestureDetector.onTouchEvent(e)
        Log.d("I am ", "consumed $theState")
        if (theState) {
            return false
        }
        return true
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        return
    }

}

class MyPlaceClickListener(var context: Context) : ClickListener {
    override fun onClick(view: View, adapterPosition: Int) {
        Toast.makeText(context, "$adapterPosition", Toast.LENGTH_LONG).show()
    }
}

////            context.registerForContextMenu(viewHolder.itemView)
//viewHolder.itemView.setOnLongClickListener {
//    Log.d("pressed long","i been long pressed")
////                it.showContextMenu()
//    var tv: TextView = it.jiplace_item_hint
//
//    var mPopUpMenuListener:PopupMenu.OnMenuItemClickListener = object:PopupMenu.OnMenuItemClickListener{
//        override fun onMenuItemClick(item: MenuItem?): Boolean {
//            when (item!!.getItemId()) {
//                R.id.edit_jiplace_description ->{
//                    Log.d("this hint",myPlace.hint)
//                    getHintAfterJiplaceOther(myPlace.uuidString,tv)
//
//                    return true
//                }
//                R.id.delete_jiplace ->{
//
//                    val formatter = DateTimeFormat.forPattern("d MMMM YYYY")
//                    val theDate = formatter.print(DateTime(myPlace.time))
//                    Log.d("this date",theDate)
//                    return true
//                }
//
//            }
//            return false
//        }
//
//    }
//    var thePopupMenu = PopupMenu(context.context!!, viewHolder.itemView)
//    thePopupMenu.setOnMenuItemClickListener(mPopUpMenuListener)
//    thePopupMenu.inflate(R.menu.jiplace_context_menu)
//    thePopupMenu.show()
//    return@setOnLongClickListener true
//}
