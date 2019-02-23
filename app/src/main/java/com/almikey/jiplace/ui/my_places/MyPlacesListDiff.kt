package com.almikey.jiplace.ui.my_places

import androidx.recyclerview.widget.DiffUtil
import com.almikey.jiplace.model.MyPlace

class MyPlacesListDiff (private val oldList: List<MyPlace>, private val newList: List<MyPlace>): DiffUtil.Callback(){
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].uuidString == newList[newItemPosition].uuidString
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }
//hey
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].hint == newList[newItemPosition].hint
    }
}