package com.almikey.jiplace.ui.call

import android.content.Context
import android.media.RingtoneManager
import android.os.Bundle
import android.os.PersistableBundle
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.almikey.jiplace.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_audio_call.*
import org.webrtc.*
import java.nio.charset.Charset
import java.util.HashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AudioCallActivity : AppCompatActivity() {

    val executor: ExecutorService = Executors.newSingleThreadExecutor();

    val ref: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    val userId: String by lazy {
        FirebaseAuth.getInstance().uid!!
    }

    lateinit var otherUser: String
    var callReceived = false

    val peerConnectionFactory by lazy {
        //Initialize PeerConnectionFactory globals.
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(this)
            .createInitializationOptions()
        var po = PeerConnectionFactory.initialize(initializationOptions)

        var pps = PeerConnectionFactory.printStackTraces()
        val options = PeerConnectionFactory.Options()
        var peerConnectionFactoryBuilder = PeerConnectionFactory.builder()
            .setOptions(options)
        PeerConnectionFactory.builder()
            .setOptions(options)
            .createPeerConnectionFactory();

    }
//    "stun:stun.l.google.com:19302",
//    "stun:stun1.l.google.com:19302",
//    "stun:stun3.l.google.com:19302"

    var serverList =
        arrayListOf<String>(
            "stun:142.93.197.248:3478"
        )

    fun getIceServers(servers: ArrayList<String>): ArrayList<PeerConnection.IceServer> {
        var iceServers: ArrayList<PeerConnection.IceServer> = ArrayList()
        for (theUrl in servers) {
            Log.d("webrtc call", "getting ice urls $theUrl")
            var iceServerBuilder = PeerConnection.IceServer.builder(theUrl)
            iceServerBuilder.setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK)
            iceServers.add(iceServerBuilder.createIceServer())
        }
        Log.d("webrtc call", "iceserver are ${iceServers.size} in number")
        Log.d("webrtc call", "iceservers toString ${iceServers.toString()}")
        return iceServers
    }


    private var sdpConstraints: MediaConstraints = MediaConstraints()
    private var localAudioTrack: AudioTrack? = null
    lateinit var localPeer: PeerConnection

    private var gotUserMedia: Boolean = false
    private var peerIceServers: MutableList<PeerConnection.IceServer> = getIceServers(serverList)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_call)
        start()
        // Logging.enableLogToDebugOutput(Logging.Severity.LS_VERBOSE)
        runOnUiThread {
            if (gotUserMedia) {
                localPeer = createPeerConnection()!!
                val b = this.intent.extras
                otherUser = b!!.getString("other_user_to_call")
                callReceived = b.getBoolean("received")
                addStreamToLocalPeer(localPeer)
                if (!otherUser.isEmpty() && otherUser != null && !callReceived) {
//                onOfferReceived()
                    doCall()
                }
            } else {
                doAnswer()
                Log.d("webrtc call", "couldn't get user media")
            }
        }
//        startCallNotification()
        audio_call_button.setOnClickListener {
            //doCall()
        }

        audio_answer_button.setOnClickListener {
            stopCallNotification()
        }
    }

    //
    private fun start() {
        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource)
        gotUserMedia = true
    }

    private fun createPeerConnection(): PeerConnection? {
        val rtcConfig = PeerConnection.RTCConfiguration(peerIceServers)
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA
        peerConnectionFactory.printInternalStackTraces(true)
        Log.d("webrtc call", "i got to the peercreation methode")
        return peerConnectionFactory
            .createPeerConnection(
                rtcConfig, PCObserver()
            )


    }

    //
//    /**
//     * Adding the stream to the localpeer
//     */
    private fun addStreamToLocalPeer(localPeer: PeerConnection) {
        //creating local mediastream
        if (gotUserMedia) {
            val stream = peerConnectionFactory.createLocalMediaStream("102")
            stream.addTrack(localAudioTrack)
            localPeer.addStream(stream)
        } else {
            Log.d("webrtc call", "Haven't got local media stream")
        }
    }

    //
//    /**
//     * This method is called when the app is initiator - We generate the offer and send it over through socket
//     * to remote peer
//     */
    private fun doCall() {
        localPeer!!.createOffer(object : SdpObserver {
            override fun onSetSuccess() {
                Log.d("offer", "set success")
            }

            override fun onCreateFailure(p0: String?) {
                Log.d("offer", "create failure")
            }

            override fun onSetFailure(p0: String?) {
                Log.d("offer", "set failure")
            }

            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                var sessionDescription = sessionDescription
                localPeer!!.setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                        Log.d("setLocaldescription", "i failed in setting description")
                        return
                    }

                    override fun onSetSuccess() {
                        Log.d("setLocaldescription", "i succeded in setting description AFTER")
                        Log.d("webrtc", "i succeded in creating local description")
                        var userWebRTCRef = ref.getReference("myplaceusers/$userId/webrtc")
                        userWebRTCRef.child("sdp").child("description")
                            .setValue(sessionDescription.toString())
                        userWebRTCRef.child("sdp").child("type")
                            .setValue(sessionDescription!!.type.canonicalForm())
                        userWebRTCRef.child("call")
                            .child("$otherUser")
                            .child("oncall")
                            .setValue(true).addOnSuccessListener {
                                Log.d("webrtc call", "on create put data to firebase")
                            }
                        onAnswerReceived()
                        return
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                        Log.d("webrtc", "i succeded in creating local description BEFORE")
//                        var userWebRTCRef = ref.getReference("myplaceusers/$userId/webrtc")
//                        userWebRTCRef.child("sdp").child("description").setValue(p0.toString())
//                        userWebRTCRef.child("sdp").child("type").setValue(p0!!.type.canonicalForm())
//                        userWebRTCRef.child("call")
//                            .child("$otherUser")
//                            .child("oncall")
//                            .setValue(true).addOnSuccessListener {
//                                Log.d("webrtc call","on create put data to firebase")
//                            }
                        //we will have the firebase functions monitoring the /call part to send a message to $otherUser
                        // about the incoming call
                        return
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.d("i failed", "setting sdp n stuff")
                        return
                    }

                }, sessionDescription)
                Log.d("onCreateSuccess", "SignallingClient emit ")
//                SignallingClientKotlin.emitMessage(sessionDescription)
                //create a firebase sdp in ${callinguser}/webrtc/
            }
        }, sdpConstraints)
    }

    //
//    /**
//     * Received remote peer's media stream. we will get the first video track and render it
//     */
    private fun gotRemoteStream(stream: MediaStream) {
        //we have remote video stream. add to the renderer.
        val audioTrack = stream.audioTracks[0]
//        localPeer.add
    }

    //
//
//    /**
//     * Received local ice candidate. Send it to remote peer through signalling for negotiation
//     */
    fun onIceCandidateReceived(iceCandidate: IceCandidate) {
        //we have received ice candidate. We can set it to the other peer.
        // SignallingClientKotlin.emitIceCandidate(iceCandidate)
        var userWebRTCRef = ref.getReference("myplaceusers/$userId/webrtc")
        val childUpdates = HashMap<String, Any?>()
        iceCandidate.apply {
            childUpdates["ice/sdp"] = sdp
            childUpdates["ice/sdpMLineIndex"] = sdpMLineIndex
            childUpdates["ice/sdpMid"] = sdpMid
            childUpdates["ice/serverUrl"] = serverUrl
        }
        userWebRTCRef.updateChildren(childUpdates)

    }


    fun onRemoteHangUp(msg: String) {
        // showToast("Remote Peer hungup")
        runOnUiThread({ this.hangup() })
    }

    //
//    /**
//     * SignallingCallback - Called when remote peer sends offer
//     */
    fun onOfferReceived() {
        ref.getReference("myplaceusers/$otherUser/webrtc/sdp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val type = dataSnapshot.child("type").value as String
                val description = dataSnapshot.child("description").value as String

                SessionDescription(SessionDescription.Type.fromCanonicalForm(type.toLowerCase()), description)

                localPeer!!.setRemoteDescription(
                    CustomSdpObserver("localSetRemote"),
                    SessionDescription(SessionDescription.Type.OFFER, description)
                )
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        )
    }

    private fun doAnswer() {
        localPeer!!.createAnswer(
            object : SdpObserver {
                override fun onSetFailure(p0: String?) {
                    Log.d("create answer", "i failed in setting description")
                    return
                }

                override fun onSetSuccess() {
                    Log.d("create answer", "i succeded in setting description")
                    return
                }

                override fun onCreateSuccess(p0: SessionDescription?) {
                    Log.d("create answer", "i succeded in creating description")
                    localPeer!!.setLocalDescription(CustomSdpObserver("localSetLocal"), p0)
                    // SignallingClientKotlin.emitMessage(sessionDescription)
                    var userWebRTCRef = ref.getReference("myplaceusers/$userId/webrtc")
                    userWebRTCRef.child("sdp").child("description").setValue(p0.toString())
                    userWebRTCRef.child("sdp").child("type").setValue(p0!!.type.canonicalForm())
                    userWebRTCRef.child("call")
                        .child("$otherUser")
                        .child("oncall")
                        .setValue(true).addOnSuccessListener {
                            onOfferReceived()
                        }
                    return
                }

                override fun onCreateFailure(p0: String?) {
                    Log.d("create answer", "i failed in creating description")
                    return
                }
            }, MediaConstraints()
        )
    }

    //
    //s
//    /**
//     * SignallingCallback - Called when remote peer sends answer to your offer
//     */
//
    fun onAnswerReceived() {
        ref.getReference("myplaceusers/$otherUser/webrtc/sdp")
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val type = dataSnapshot.child("type").value as String
                val description = dataSnapshot.child("sdp").value as String

                SessionDescription(SessionDescription.Type.fromCanonicalForm(type.toLowerCase()), description)

                localPeer!!.setRemoteDescription(
                    CustomSdpObserver("localSetRemote"),
                    SessionDescription(
                        SessionDescription.Type.fromCanonicalForm(type.toLowerCase()),
                        description
                    )
                )
                ref.getReference("myplaceusers/$otherUser/webrtc/ice")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            // Get Post object and use the values to update the UI

                            val sdp = dataSnapshot.child("sdp").value as String
                            val sdpMLineIndex = dataSnapshot.child("sdpMLineIndex").value as Int
                            val sdpMid = dataSnapshot.child("sdpMid").value as String
                            val serverUrl = dataSnapshot.child("serverUrl").value as String

                           val ice = IceCandidate(sdpMid,sdpMLineIndex,sdp)

                            localPeer!!.addIceCandidate(ice)
                            doAnswer()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Getting Post failed, log a message
                            Log.w("", "loadPost:onCancelled", databaseError.toException())
                            // ...
                        }
                    })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("", "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })
    }

    private fun hangup() {
        try {
            localPeer!!.close()
            //turn off on firebase
            // SignallingClientKotlin.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onDestroy() {
        // SignallingClientKotlin.close()
        //send something to firebase
        super.onDestroy()
    }

    open class CustomSdpObserver(var theObserver: String) : SdpObserver {
        override open fun onSetFailure(p0: String?) {
            Log.d("webrtc call", "$theObserver i failed in setting description")
            return
        }

        override open fun onSetSuccess() {
            Log.d("$theObserver", "$theObserver i succeded in setting description")
            return
        }

        override open fun onCreateSuccess(p0: SessionDescription?) {
            Log.d("$theObserver", "$theObserver i succeded in creating description")
            return
        }

        override open fun onCreateFailure(p0: String?) {
            Log.d("$theObserver", "$theObserver i failed in creating description")
            return
        }
    }


    private inner class PCObserver : PeerConnection.Observer {
        override fun onIceCandidate(candidate: IceCandidate) {
            executor.execute {
                var userWebRTCRef = ref.getReference("myplaceusers/$userId/webrtc")
                val childUpdates = HashMap<String, Any?>()
                candidate.apply {
                    childUpdates["ice/sdp"] = sdp
                    childUpdates["ice/sdpMLineIndex"] = sdpMLineIndex
                    childUpdates["ice/sdpMid"] = sdpMid
                    childUpdates["ice/serverUrl"] = serverUrl
                }
                userWebRTCRef.updateChildren(childUpdates)
            }
        }


        override fun onIceCandidatesRemoved(candidates: Array<IceCandidate>) {
            Log.d("ice", "ice candidates removed")
        }

        override fun onSignalingChange(newState: PeerConnection.SignalingState) {
            Log.d("FragmentActivity.TAG", "SignalingState: $newState")
        }

        override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {
            executor.execute {
                Log.d("FragmentActivity.TAG", "IceConnectionState: $newState")
                if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                    Log.d("wut", "events.onIceConnected()")
                } else if (newState == PeerConnection.IceConnectionState.DISCONNECTED) {
                    Log.d("wut", "events.onIceDisconnected()")
                } else if (newState == PeerConnection.IceConnectionState.FAILED) {
                    Log.d("reportError(", "ICE connection failed.")
                }
            }
        }

        override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
            executor.execute {
                Log.d("FragmentActivity.TAG", "PeerConnectionState: " + newState!!)
                if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                    Log.d("wut", " events.onConnected()")

                } else if (newState == PeerConnection.PeerConnectionState.DISCONNECTED) {
                    Log.d("wut", "     events.onDisconnected()")
                } else if (newState == PeerConnection.PeerConnectionState.FAILED) {
                    Log.d("wut", " reportError(DTLS connection failed.")
                }
            }
        }

        override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) {
            Log.d("FragmentActivity.TAG", "IceGatheringState: $newState")
        }

        override fun onIceConnectionReceivingChange(receiving: Boolean) {
            Log.d("FragmentActivity.TAG", "IceConnectionReceiving changed to $receiving")
        }

        override fun onAddStream(stream: MediaStream) {
            gotRemoteStream(stream)
        }

        override fun onRemoveStream(stream: MediaStream) {}

        override fun onDataChannel(dc: DataChannel) {
            Log.d("FragmentActivity.TAG", "New Data channel " + dc.label())

//            if (!dataChannelEnabled)
//                return

            dc.registerObserver(object : DataChannel.Observer {
                override fun onBufferedAmountChange(previousAmount: Long) {
                    Log.d(
                        "FragmentActivity.TAG",
                        "Data channel buffered amount changed: " + dc.label() + ": " + dc.state()
                    )
                }

                override fun onStateChange() {
                    Log.d("FragmentActivity.TAG", "Data channel state changed: " + dc.label() + ": " + dc.state())
                }

                override fun onMessage(buffer: DataChannel.Buffer) {
                    if (buffer.binary) {
                        Log.d("FragmentActivity.TAG", "Received binary msg over $dc")
                        return
                    }
                    val data = buffer.data
                    val bytes = ByteArray(data.capacity())
                    data.get(bytes)
                    val strData = String(bytes, Charset.forName("UTF-8"))
                    Log.d("FragmentActivity.TAG", "Got msg: $strData over $dc")
                }
            })
        }

        override fun onRenegotiationNeeded() {
            // No need to do anything; AppRTC follows a pre-agreed-upon
            // signaling/negotiation protocol.
        }

        override fun onAddTrack(receiver: RtpReceiver, mediaStreams: Array<MediaStream>) {}
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    fun startCallNotification() {
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val ringtone = RingtoneManager.getRingtone(this, notification)
        ringtone.play()
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationCycle = longArrayOf(0, 1000, 1000)
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(vibrationCycle, 1)
        }
    }
    fun stopCallNotification() {
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val ringtone = RingtoneManager.getRingtone(this, notification)
        ringtone.stop()
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel()
    }

}