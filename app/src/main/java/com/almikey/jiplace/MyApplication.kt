package com.almikey.jiplace

import androidx.multidex.MultiDexApplication
import co.chatsdk.core.error.ChatSDKException
import co.chatsdk.core.session.ChatSDK
import co.chatsdk.core.session.Configuration
import co.chatsdk.firebase.FirebaseNetworkAdapter
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule
import co.chatsdk.firebase.push.FirebasePushModule
import co.chatsdk.ui.manager.BaseInterfaceAdapter
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import co.chatsdk.core.dao.DaoSession



class MainApplication: MultiDexApplication() {

    lateinit var daoSession: DaoSession


    override fun onCreate() {
        super.onCreate()
        ViewPump.init(
            ViewPump.builder()
                .addInterceptor(
                    CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                            .setDefaultFontPath("fonts/Roboto-Light.ttf")
                            .setFontAttrId(R.attr.fontPath)
                            .build()
                    )
                )
                .build()
        )

        val context = applicationContext

// Create a new configuration
        val builder = Configuration.Builder(context)

// Perform any configuration steps (optional)
        builder.firebaseRootPath("prod")

// Initialize the Chat SDK
        builder.firebaseDatabaseURL("https://jiplace.firebaseio.com")

        try {
            ChatSDK.initialize(builder.build(), BaseInterfaceAdapter(context), FirebaseNetworkAdapter())
        } catch (e: ChatSDKException) {
        }


// File storage is needed for profile image upload and image messages
        FirebaseFileStorageModule.activate()
        FirebasePushModule.activate()

    }



}