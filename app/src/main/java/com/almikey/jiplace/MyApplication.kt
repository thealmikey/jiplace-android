package com.almikey.jiplace

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import co.chatsdk.core.error.ChatSDKException
import co.chatsdk.core.session.ChatSDK
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule
import co.chatsdk.firebase.push.FirebasePushModule
import co.chatsdk.ui.manager.BaseInterfaceAdapter
import com.almikey.jiplace.di.KoinModules
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import org.koin.android.ext.android.startKoin
import androidx.work.WorkManager
import co.chatsdk.core.interfaces.InterfaceAdapter
import co.chatsdk.core.session.NM
import co.chatsdk.firebase.FirebaseNetworkAdapter
import com.google.android.libraries.places.api.Places


class MainApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        Places.initialize(applicationContext,"AIzaSyCbkci-b47seazRUhZsFRLJVhvOSoxMiSQ")

        WorkManager.initialize(
            this,
            androidx.work.Configuration.Builder()
                .setMinimumLoggingLevel(Log.VERBOSE)
                .setMaxSchedulerLimit(50)
                .build()
        )

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
        val builder = co.chatsdk.core.session.Configuration.Builder(context)
        builder.firebaseRootPath("prod")
        builder.firebaseDatabaseURL("https://jiplace.firebaseio.com")
        builder.setClientPushEnabled(true)

        try {
            ChatSDK.initialize(builder.build(), FirebaseNetworkAdapter(), BaseInterfaceAdapter(context))
        } catch (e: ChatSDKException) {
        }
        FirebaseFileStorageModule.activate()
        FirebasePushModule.activate()

        startKoin(this, listOf(KoinModules.modules))
    }


}