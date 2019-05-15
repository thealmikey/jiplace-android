package com.almikey.jiplace.ui.my_places.places_list

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import co.chatsdk.core.session.NM
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.almikey.jiplace.R
import com.almikey.jiplace.database.dao.MyPlaceUserSharedDao
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.model.MyPlaceProfilePic
import com.almikey.jiplace.service.ServerSyncService.MyPlaceServerSyncServiceImpl
import com.almikey.jiplace.util.FilePickerUtil
import com.almikey.jiplace.util.ThreadCleanUp.deleteThreadsFromOtherSide
import com.almikey.jiplace.worker.UploadMyPlaceImageWorker
import com.google.firebase.auth.FirebaseAuth
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject


class MyPlacesFragment : Fragment(), KoinComponent {

    val myPlaceUserSharedDao: MyPlaceUserSharedDao by inject()
    val myPlacesServerServerSyncServiceImpl: MyPlaceServerSyncServiceImpl by inject()

    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    val myPlacesViewModel: MyPlaceViewModel by viewModel()
    var myPlaces: MutableList<MyPlace> = mutableListOf()

    lateinit var mCoordinatorLayout: CoordinatorLayout

    lateinit var mRecyclerview: RecyclerView
    lateinit var myPlacesAdapter: MyPlaceAdapter

    val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NM.auth().authenticateWithCachedToken()
        return
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        deleteThreadsFromOtherSide(scopeProvider)

        Log.d(
            "external media", Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ).absolutePath
        )
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.jiplaces_recyclerview, container, false)
    }


    fun editMyPlace(myPlace: MyPlace) {

    }

    @SuppressLint("AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mCoordinatorLayout = view?.findViewById(R.id.jiplaces_container_for_recyclerview) as CoordinatorLayout
        mRecyclerview = ContextMenuRecyclerView(this.context!!)
        var params: ViewGroup.LayoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mRecyclerview.layoutParams = params
        mCoordinatorLayout.addView(mRecyclerview)
        mRecyclerview.layoutManager = LinearLayoutManager(activity as Activity)


        myPlacesAdapter = MyPlaceAdapter(this, myPlaces)

        myPlacesViewModel.myPlaces.observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(scopeProvider)
            .subscribe {
                myPlaces.clear()
                it.forEach {
                    if (it.deletedStatus == "false") {
                        myPlaces.add(it)
                    }

                }
                myPlacesAdapter.notifyDataSetChanged()
            }


        mRecyclerview.adapter = myPlacesAdapter
        registerForContextMenu(mRecyclerview);
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = activity!!.menuInflater
        inflater.inflate(R.menu.jiplace_context_menu, menu)
    }

    @SuppressLint("AutoDispose")
    override fun onContextItemSelected(item: MenuItem): Boolean {
        Log.d("context menu item", "context menu item selected")
        val info = item.menuInfo as ContextMenuRecyclerView.RecyclerViewContextMenuInfo
        return when (item.itemId) {
            R.id.edit_jiplace_description -> {
                Log.d("edit", "context menu")
                editMyPlaceHint(myPlaces[info.position].uuidString, info.position)
                Toast.makeText(this.context, "edit ${info.position}", Toast.LENGTH_LONG).show()
                true
            }
            R.id.delete_jiplace -> {
                deletePlaceFromDatabase(info)
                return true
            }
            else -> super.onContextItemSelected(item)
        }
        // handle menu item here
        return super.onContextItemSelected(item)
    }


    @SuppressLint("AutoDispose")
    fun deletePlaceFromDatabase(info: ContextMenuRecyclerView.RecyclerViewContextMenuInfo) {
        Log.d("delete", "context menu")
        myPlacesViewModel.findByUuid(myPlaces[info.position].uuidString)
            .subscribeOn(Schedulers.io()).take(1).observeOn(Schedulers.io()).subscribe { thePlace ->
                /**
                 *      we set a flag in the database instead of deleting the place someone
                 *      this helps when we synchronize with firebase to also delete the places
                 *      there. We should eventually delete the places once we're sure the delete has occurred
                 *      on firebase
                 */
                var newPlace = thePlace.copy(deletedStatus = "pending")
                myPlacesViewModel.update(newPlace).subscribe({
                    Log.d("MyPlacesFragment","was able to update myplace status to pending")
                    myPlacesViewModel.deleteOnDatabaseAfterServerDelete(
                        newPlace,
                        myPlacesServerServerSyncServiceImpl.deleteMyPlaceOnServer(newPlace)
                    )
                },{
                    Log.d("MyPlacesFragment","unable to update myplace status to pending")
                })

            }
    }

    /**
     *       For every User who Jiplaces with us and starts a chat,we create a JOIN column
     *       called OtherUser that involves MyPlace table and OtherUser,
     *       if a user shares multiple Jiplaces with us
     *       e.g 12:30 ,23rd Feb at Ihub and another one like 12:00, 1st Jan Uhuru park
     *       they have 2 entries in the OtherUser table
     *       if we delete one Jiplace the entries reduce by 1,
     *       We check the number of entries in the OtherUser,
     *       if it's greater than 0 we don't delete the message threads we share
     *       but if we delete all shared Jiplaces, the number of entries reduces to 0
     *       i.e. the Ihub one and the Uhuru park one
     *       we delete their thread.
     *
     *       As users have to share Jiplaces, i.e. if you have 2 Jiplaces
     *       that include userA, then userA can never have more or less of Jiplaces that have us
     *       thus if you delete the Jiplaces on
     *       your side and the thread disappears, it's also triggered on the other side
     *       this is done by setting flags on firebase indicating that the user should delete
     *       their Jiplace as well. This happens automatically on startup
     */


    fun editMyPlaceHint(theUuid: String, position: Int) {
        lateinit var theHintStr: String
        var dialog = MaterialDialog(activity as Activity).show {
            customView(R.layout.jiplace_description_hint)
        }
        val customView = dialog.getCustomView()
        var theText = customView?.findViewById<EditText>(R.id.jiplaceDescription)
        theText!!.setText(myPlaces[position].hint)
        theText?.text.toString()

        dialog.positiveButton {
            theHintStr = theText?.text.toString()
            savePlaceWithHint(theUuid, position, theHintStr)
        }
    }


    @SuppressLint("AutoDispose")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mRecyclerview.adapter!!.notifyDataSetChanged()
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {

                var uri = FilePickerUtil.getPath(context!!, data.getData())
                var imageDataLocation = data.getData().toString()

                Log.d("local uri", "the local uri is $uri")
                var theUuid = myPlacesAdapter.uuidForSelectedImage
                var thePosition = myPlacesAdapter.thePosition
                updatePicInDatabase(uri!!, theUuid, imageDataLocation)
                var thePlaces = myPlaces.toMutableList()
                thePlaces[thePosition] =
                    thePlaces[thePosition].copy(profile = MyPlaceProfilePic(localPicUrl = "placeholder"))
                myPlacesAdapter.myplaces = thePlaces
                mRecyclerview.adapter!!.notifyDataSetChanged()

            } else {
                var thePosition = myPlacesAdapter.thePosition
                var thePlaces = myPlaces.toMutableList()
                thePlaces[thePosition] = thePlaces[thePosition].copy(profile = MyPlaceProfilePic(localPicUrl = ""))
                myPlacesAdapter.myplaces = thePlaces
                mRecyclerview.adapter = MyPlaceAdapter(this, thePlaces)
                mRecyclerview.adapter!!.notifyDataSetChanged()
            }
        }
        mRecyclerview.adapter!!.notifyDataSetChanged()

    }

    @SuppressLint("AutoDispose")
    fun updatePicInDatabase(uri: String, theUuid: String, imageDataLocation: String) {
        myPlacesViewModel.findByUuid(theUuid).take(1)
            .observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).subscribe {
                var newPlace = it.copy(profile = MyPlaceProfilePic(localPicUrl = uri!!))
                //change item in db and change the adapter
                Completable.fromAction {
                    myPlacesViewModel.update(newPlace).subscribe()
                }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    myPlacesViewModel.myPlaces.observeOn(AndroidSchedulers.mainThread())
                        .autoDisposable(scopeProvider)
                        .subscribe {
                            myPlaces.clear()
                            it.forEach {
                                if (it.deletedStatus == "false") {
                                    myPlaces.add(it)
                                }

                            }
                            myPlacesAdapter.notifyDataSetChanged()
                        }
                    Log.d("adapter notify", "after loading adapter notify")
                }
                uploadPicToFirebase(it, imageDataLocation)
            }
    }

    fun uploadPicToFirebase(it: MyPlace, imageDataLocation: String) {
        var constraint: Constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        var uploadMyPlaceImageWorker =
            OneTimeWorkRequestBuilder<UploadMyPlaceImageWorker>().addTag("upload pic firebase").setInputData(
                Data.Builder()
                    .putString("UuidKey", it.uuidString)
                    .putAll(
                        mapOf(
                            "fifteenMinGroupUp" to it.timeRoundUp,
                            "fifteenMinGroupDown" to it.timeRoundDown,
                            "localPicUri" to imageDataLocation
                        )
                    )
                    .build()
            )
                .setConstraints(constraint).build()
        WorkManager.getInstance().enqueue(uploadMyPlaceImageWorker)
    }

    fun savePlaceWithHint(placeUuid: String, placePosition: Int, hintText: String) {
        CompletableFromAction {
            var thePlace = myPlacesViewModel.findByUuid(placeUuid)
                .subscribeOn(Schedulers.io()).blockingFirst()
            var newPlace = thePlace.copy(hint = hintText)

            @SuppressLint("AutoDispose")
            var b = CompletableFromAction {
                myPlacesViewModel.update(newPlace).subscribe()
                var thePlaces = myPlaces.toMutableList()
                thePlaces[placePosition] = newPlace
                myPlacesAdapter.myplaces = thePlaces
            }
                .subscribeOn(Schedulers.io()).subscribe()
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .autoDisposable(scopeProvider)
            .subscribe {

                myPlacesAdapter.notifyItemChanged(placePosition)
                Log.d("jiplace other", "n putting a location in jiplace other")
            }
    }

}