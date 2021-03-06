package com.almikey.jiplace.di

import androidx.room.Room
import com.almikey.jiplace.database.MyPlacesRoomDatabase
import com.almikey.jiplace.database.dao.MyPlaceUserSharedDao
import com.almikey.jiplace.database.dao.OtherUserDao
import com.almikey.jiplace.repository.MyPlacesRepositoryImpl
import com.almikey.jiplace.service.LocalStorageService.MyPlaceLocalService
import com.almikey.jiplace.service.LocalStorageService.MyPlaceLocalServiceRoomImpl
import com.almikey.jiplace.service.ServerSyncService.MyPlaceFirebaseSyncService
import com.almikey.jiplace.service.ServerSyncService.MyPlaceServerSyncService
import com.almikey.jiplace.service.ServerSyncService.MyPlaceServerSyncServiceImpl
import com.almikey.jiplace.ui.my_places.places_list.MyPlaceViewModel
import com.almikey.jiplace.util.CurrentLocationRx
import com.almikey.myplace.service.MyPlacesDao
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import org.koin.experimental.builder.factory


class KoinModules {
    companion object {
        val modules = module(override = true) {
            single<MyPlacesRoomDatabase>("roomdb") {
                Room.databaseBuilder(androidApplication(), MyPlacesRoomDatabase::class.java, "myplaces-db")
                    .build()
            }
            single<MyPlacesDao> {
                get<MyPlacesRoomDatabase>().myPlacesDao()
            }
            single<MyPlaceUserSharedDao> {
                get<MyPlacesRoomDatabase>().myPlaceUserSharedDao()
            }
            single<OtherUserDao> {
                get<MyPlacesRoomDatabase>().otherUserDao()
            }
            single<MyPlaceLocalService> {
                MyPlaceLocalServiceRoomImpl(get())
            }
            // single instance of HelloRepository
            single<MyPlacesRepositoryImpl> {
                MyPlacesRepositoryImpl(get(), get())
            }
            // MyViewModel ViewModel
            viewModel { MyPlaceViewModel(get()) }

            factory<CurrentLocationRx> {
                CurrentLocationRx(androidApplication())
            }

            single<MyPlaceServerSyncService> {
                MyPlaceFirebaseSyncService(get())
            }

            single<MyPlaceServerSyncServiceImpl> {
                MyPlaceServerSyncServiceImpl(get())
            }

        }

        //
        val roomTestModule = module(override = true) {

            single<MyPlacesRoomDatabase>("test_roomdb"){
                // In-Memory database config
                Room.inMemoryDatabaseBuilder(androidApplication(), MyPlacesRoomDatabase::class.java)
                    .allowMainThreadQueries()
                    .build()
            }
            single<MyPlacesDao>("test_myPlacesDao"){
                get<MyPlacesRoomDatabase>(name="test_roomdb").myPlacesDao()
            }
            single<MyPlaceUserSharedDao>("test_myPlaceUserSharedDao") {
                get<MyPlacesRoomDatabase>(name="test_roomdb").myPlaceUserSharedDao()
            }
            single<OtherUserDao>("test_otherUserDao"){
                get<MyPlacesRoomDatabase>().otherUserDao()
            }
            single<MyPlaceLocalService>("test_myPlaceLocalService"){
                MyPlaceLocalServiceRoomImpl(get(name="test_myPlacesDao"))
            }
            // single instance of HelloRepository
            single<MyPlacesRepositoryImpl>("test_myPlaceRepositoryImpl"){
                MyPlacesRepositoryImpl(get(name = "test_myPlaceLocalService"), get(name = "test_myPlaceUserSharedDao"))
            }

        }
    }
}