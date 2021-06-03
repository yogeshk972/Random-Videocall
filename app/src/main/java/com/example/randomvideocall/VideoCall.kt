package com.example.randomvideocall

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration

@Suppress("DEPRECATION")
class VideoCall : AppCompatActivity() {

    private val PERMISSION_REQ_ID = 22
    lateinit var user1 : String
    lateinit var user2 : String
    lateinit var mRef : DatabaseReference
    lateinit var mRtcEngine: RtcEngine
    lateinit var channelName: String
    private var isMuted = false

    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val  mRtcEventHandler : IRtcEngineEventHandler = object: IRtcEngineEventHandler() {

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            Log.e("qwe", "sac")
            runOnUiThread {
                setupRemoteVideo(uid)
            }
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {

            super.onJoinChannelSuccess(channel, uid, elapsed)
            runOnUiThread {

                Log.e("qwe", "join succ")

            }
        }

        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
            super.onFirstRemoteVideoDecoded(uid, width, height, elapsed)
            Log.e("qwe", "vcxz")
            runOnUiThread {
                setupRemoteVideo(uid)
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            runOnUiThread {
                finishCalling()
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

        val endCall = findViewById<MaterialButton>(R.id.hangupButton)
        endCall.setOnClickListener{
            finishCalling()
        }

        val switchCamera = findViewById<ImageView>(R.id.switchCamera)
        switchCamera.setOnClickListener {
            mRtcEngine.switchCamera()
        }

        val muteUnmute = findViewById<ImageView>(R.id.muteUnmute)
        muteUnmute.setOnClickListener {
            isMuted = !isMuted
            mRtcEngine.muteLocalAudioStream(isMuted);
            val res: Int = if (isMuted) R.drawable.btn_mute else R.drawable.btn_unmute
            muteUnmute.setImageResource(res)
        }

    }

    private fun setupLocalVideo() {
        Log.e("qwe", "local server")
        runOnUiThread {

            mRtcEngine.enableVideo()
            mRtcEngine!!.setVideoEncoderConfiguration(
                VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
                )
            )

            val localVideo = findViewById<FrameLayout>(R.id.localVideo)
            val surfaceView = RtcEngine.CreateRendererView(baseContext)
            surfaceView.setZOrderMediaOverlay(true)
            localVideo.addView(surfaceView)
            mRtcEngine.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))

        }
    }

    fun setupRemoteVideo(uid: Int){
        Log.e("qwe", "remote server")

        runOnUiThread {
            val surfaceView = RtcEngine.CreateRendererView(applicationContext)

            mRtcEngine.setupRemoteVideo(
                VideoCanvas(
                    surfaceView,
                    VideoCanvas.RENDER_MODE_HIDDEN,
                    uid
                )
            )

            surfaceView.tag = uid

            val remoteVideo = findViewById<FrameLayout>(R.id.remoteVideo)
            remoteVideo.addView(surfaceView)

        }
    }

    private fun getExtras(){
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

    private fun initEngineAndJoinChannel(){
        try {
            mRtcEngine = RtcEngine.create(
                getBaseContext(),
                getString(R.string.appId),
                mRtcEventHandler
            );
        }catch (e: Exception){
            Log.e(TAG, "${e.printStackTrace()}")
        }

        joinChannel()
        setupLocalVideo()

        joinFriend(user2)
    }

    private fun joinChannel(){
        val token = "006dc040b1b9c374e3c8ae2fab6d8c4d802IAB6ctmM4iAFrDegiEBhcEth30ntjBvllS6gtUno6cspJWLMzZAAAAAAEAD7cHweRVi5YAEAAQBEWLlg"
        mRtcEngine.joinChannel(token, "test-channel", "Extra Optional Data", 0);
    }

    private fun joinFriend(strangerId: String) {
        channelName = strangerId
        finishCalling()
        startCalling()
    }

    private fun finishCalling() {
        mRtcEngine.leaveChannel()
        startActivity( Intent(this, MainActivity::class.java) )
    }

    private fun startCalling() {
        joinChannel()
        setupLocalVideo()
    }

}