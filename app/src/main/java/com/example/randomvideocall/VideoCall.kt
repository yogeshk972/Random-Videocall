package com.example.randomvideocall

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas


class User(id: String, aid: String){
    var userId: String= id
    var agoraUid: String = aid
}

@Suppress("DEPRECATION")
class VideoCall : AppCompatActivity() {

    val PERMISSION_REQ_ID = 22
    lateinit var user1 : String
    lateinit var user2 : String
    lateinit var mRef : DatabaseReference
    lateinit var mRtcEngine: RtcEngine
    lateinit var channelName: String


    val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val  mRtcEventHandler : IRtcEngineEventHandler = object: IRtcEngineEventHandler() {

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
            runOnUiThread {

//                SurfaceView localView = mUidsList.remove(0);
//                mUidsList.put(uid, localView);
                mRef.child(user1).setValue( User(user1, uid as String) );

            }
        }

        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
            super.onFirstRemoteVideoDecoded(uid, width, height, elapsed)
            runOnUiThread {

                val mRemoteView = RtcEngine.CreateRendererView(applicationContext)

                mRemoteView.setZOrderOnTop(true)
                mRemoteView.setZOrderMediaOverlay(true)
                mRtcEngine.setupRemoteVideo(
                    VideoCanvas(
                        mRemoteView,
                        VideoCanvas.RENDER_MODE_HIDDEN,
                        uid
                    )
                )

            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            runOnUiThread {
                /*
                showToast("User: " + uid + " left the room.");
                onRemoteUserLeft(uid);
             */
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.call_screen)

        mRef = FirebaseDatabase.getInstance().reference.child("users")

        getExtras()

        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
            checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
            checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            initEngineAndJoinChannel();
        }

    }

    private fun setupLocalVideo() {
        runOnUiThread {
            mRtcEngine.enableVideo()
            mRtcEngine.enableInEarMonitoring(true)
            mRtcEngine.setInEarMonitoringVolume(80)
            val surfaceView = RtcEngine.CreateRendererView(baseContext)
            mRtcEngine.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
            surfaceView.setZOrderOnTop(false)
            surfaceView.setZOrderMediaOverlay(false)
        }
    }


    fun getExtras(){
        user1 = intent.extras!!.getString("user1")?:""
        user2 = intent.extras!!.getString("user2")?:""

        channelName= user1
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQ_ID -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    initEngineAndJoinChannel()
                }
            }
        }
    }


    fun initEngineAndJoinChannel(){
        try {
            mRtcEngine = RtcEngine.create(
                getBaseContext(),
                getString(R.string.appId),
                mRtcEventHandler
            );
        }catch (e: Exception){
            Log.e(TAG, "${e.printStackTrace()}")
        }

        setupLocalVideo()
        joinChannel()

        joinFriend(user2)
    }

    fun joinChannel(){
        mRtcEngine.joinChannel(null, channelName, "Extra Optional Data", 0);
    }

    fun joinFriend(strangerId: String) {
        channelName = strangerId
        finishCalling()
        startCalling()
    }

    fun finishCalling() {
//        leaveChannel()
        mRtcEngine.leaveChannel()
    }

    fun startCalling() {
        setupLocalVideo()
        joinChannel()
    }
}