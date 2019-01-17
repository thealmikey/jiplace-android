package com.almikey.jiplace

import android.app.Application
import androidx.multidex.MultiDexApplication
import co.chatsdk.core.error.ChatSDKException
import co.chatsdk.core.session.ChatSDK
import co.chatsdk.core.session.Configuration
import co.chatsdk.firebase.FirebaseNetworkAdapter
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule
import co.chatsdk.firebase.push.FirebasePushModule
import co.chatsdk.ui.manager.BaseInterfaceAdapter
import com.almikey.jiplace.di.KoinModules
import com.almikey.jiplace.repository.MyPlacesRepository
import com.almikey.jiplace.ui.my_places.MyPlacesViewModel
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import org.koin.android.ext.android.startKoin

class MainApplication: MultiDexApplication() {

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
        val builder = Configuration.Builder(context)
        builder.firebaseRootPath("prod")
        builder.firebaseDatabaseURL("https://jiplace.firebaseio.com")
        try {
            ChatSDK.initialize(builder.build(), BaseInterfaceAdapter(context), FirebaseNetworkAdapter())
        } catch (e: ChatSDKException) {
        }
        FirebaseFileStorageModule.activate()
        FirebasePushModule.activate()
        startKoin (this, listOf(KoinModules.modules))
    }



}