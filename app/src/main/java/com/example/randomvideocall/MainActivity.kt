package com.example.randomvideocall

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

  val TAG = "asd"

  class MainActivity : AppCompatActivity() {
      private var currentUser: FirebaseUser? = null
      private lateinit var auth: FirebaseAuth
      private lateinit var mref : DatabaseReference
      private var userList = mutableListOf<String?>()

      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          setContentView(R.layout.activity_main)


          val btn = findViewById<Button>(R.id.callBtn)
          val progressBar = findViewById<ProgressBar>(R.id.progressBar)

          btn.setOnClickListener{

              auth = FirebaseAuth.getInstance()
              mref = FirebaseDatabase.getInstance().reference.child("users")

              btn.isEnabled = false
              progressBar.visibility = View.VISIBLE

              auth.signInAnonymously().addOnCompleteListener { task ->
                  if (task.isSuccessful) {
                      Toast.makeText(this, "Signed in Anonymously", Toast.LENGTH_SHORT).show()

                      addCurrentUser()

                      val timer = object : CountDownTimer(15*1000, 1000) {

                          override fun onTick(millisUntilFinished: Long) {
                              Log.e(TAG,"time rem: ${millisUntilFinished / 1000}")
                              getAllUsers()

                              if( userList.size > 1 ){
                                  startVideoCalling()
                              }

                              Log.e(TAG,"list : ${ userList }")
                          }

                          override fun onFinish() {

                              if( userList.size > 0 ){
                                  startVideoCalling()
                              }

                              btn.isEnabled = true
                              progressBar.visibility= View.INVISIBLE

                              deleteUser()
                          }
                      }

                      timer.start()

                  } else {
                      Toast.makeText(this, "try again after some time", Toast.LENGTH_LONG).show()

                      btn.isEnabled = true
                      progressBar.visibility= View.INVISIBLE

                      deleteUser()
                  }
              }

          }
      }

      fun addCurrentUser(){
          currentUser= auth.currentUser
          if( currentUser != null ) {
              mref.push()
              mref.child( currentUser!!.uid ).setValue("")
          }
      }

      fun deleteUser() {
          if (currentUser != null) {
              mref.child(currentUser!!.uid).removeValue()
          }
          currentUser?.delete()
      }

      fun getAllUsers() {

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                for( user in snapshot.children ){
                    userList.add(user.key )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG,"error : ${error}")
            }
        }
          mref.addValueEventListener(listener)
    }

    fun startVideoCalling(){
        if( !userList.contains(currentUser!!.uid) ){
            return
        }

        userList.remove( currentUser!!.uid )
        val user1= currentUser!!.uid
        val user2= userList.random()!!
        Log.e("asd","calling starts, users : $user1 , $user2")

        mref.child(user1).removeValue()
        mref.child(user2).removeValue()

        val intent = Intent(this, VideoCall::class.java).apply {
            putExtra("user1",user1)
            putExtra("user2",user2)
        }
        startActivity(intent)
    }

}