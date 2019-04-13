package com.almikey.jiplace.ui.my_places.users_list

import co.chatsdk.core.dao.User

class MyPlaceUser(
    var theHint:String,
    var theTime: Long,
    var user: User,
    var theKey: String,
    var imageUrls: ArrayList<String>
)