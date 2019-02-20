package com.almikey.jiplace.di

import androidx.room.Room
import com.almikey.jiplace.database.MyPlacesRoomDatabase
import com.almikey.jiplace.database.dao.MyPlaceUserSharedDao
import com.almikey.jiplace.database.dao.OtherUserDao
import com.almikey.jiplace.repository.MyPlacesRepository
import com.almikey.jiplace.ui.my_places.places_list.MyPlaceViewModel
import com.almikey.jiplace.util.CurrentLocationRx
import com.almikey.myplace.service.MyPlacesDao
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module


class KoinModules {
    companion object {
        val modules = module {
            single<MyPlacesRoomDatabase>("roomdb") {
                Room.databaseBuilder(androidApplication(), MyPlacesRoomDatabase::class.java, "myplaces-db")
                    .build()
            }
            single<MyPlacesDao>{
                get<MyPlacesRoomDatabase>().myPlacesDao()
            }
            single<MyPlaceUserSharedDao>{
                get<MyPlacesRoomDatabase>().myPlaceUserSharedDao()
            }
            single<OtherUserDao>{
                get<MyPlacesRoomDatabase>().otherUserDao()
            }
            // single instance of HelloRepository
            factory<MyPlacesRepository> {
                MyPlacesRepository(androidApplication(),get())
            }
            // MyViewModel ViewModel
            viewModel { MyPlaceViewModel(get()) }

            factory<CurrentLocationRx>{
                CurrentLocationRx(androidApplication())
            }

        }

        val roomTestModule = module {

            single<MyPlacesRoomDatabase> {
                // In-Memory database config
                Room.inMemoryDatabaseBuilder(androidApplication(), MyPlacesRoomDatabase::class.java)
                    .allowMainThreadQueries()
                    .build()
            }
            single<MyPlacesDao>{
                get<MyPlacesRoomDatabase>().myPlacesDao()
            }

        }
    }
}