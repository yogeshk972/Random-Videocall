  package com.example.randomvideocall

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
      private lateinit var reference : DatabaseReference
      private var userList = mutableListOf<String?>()

      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          setContentView(R.layout.activity_main)


          val btn = findViewById<Button>(R.id.callBtn)
          val progressBar = findViewById<ProgressBar>(R.id.progressBar)

          btn.setOnClickListener{

              auth = FirebaseAuth.getInstance()
              reference = FirebaseDatabase.getInstance().getReference().child("users")

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
                                  startVideoChatting()
                              }

                              Log.e(TAG,"list : ${ userList }")
                          }

                          override fun onFinish() {

                              if( userList.size > 1 ){
                                  startVideoChatting()
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
              reference.child( currentUser!!.uid ).setValue("")
          }
      }

      fun deleteUser() {
          if (currentUser != null) {
              reference.child(currentUser!!.uid).removeValue()
          }
          currentUser?.delete()
      }

      fun getAllUsers() {

        Log.e(TAG,"xx $reference")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                Log.e(TAG,"snap : $snapshot")
                for( user in snapshot.children ){
                    Log.e(TAG,"he : $user")
                    userList.add(user.key )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG,"error : ${error}")
            }
        }
          reference.addValueEventListener(listener)
    }

    fun startVideoChatting(){
        userList.remove( currentUser!!.uid )
        val user1= currentUser!!.uid
        val user2= userList.random()
        Log.e("asd","$user1 , $user2")
    }

}