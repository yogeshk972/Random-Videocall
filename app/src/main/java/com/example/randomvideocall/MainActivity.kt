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


  class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private var userList = mutableListOf<FirebaseUser?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val btn = findViewById<Button>(R.id.callBtn)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)


        btn.setOnClickListener{

            auth = FirebaseAuth.getInstance()

            btn.isEnabled = false
            progressBar.visibility = View.VISIBLE

            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Signed in Anonymously", Toast.LENGTH_SHORT).show()

                    currentUser= auth.currentUser

                    val timer = object : CountDownTimer(15*1000, 1000) {

                        override fun onTick(millisUntilFinished: Long) {
                            Log.e("asd","time rem: ${millisUntilFinished / 1000}")
                            getAllUsers()
                            Log.e("asd","list : ${ userList }")
                        }

                        override fun onFinish() {
                            Toast.makeText( applicationContext,"finish",Toast.LENGTH_LONG).show()

                            Log.e("asd"," current user : ${currentUser?.uid}")

                            btn.isEnabled = true
                            progressBar.visibility= View.INVISIBLE
                            currentUser?.delete()
                        }
                    }

                    timer.start()

                } else {
                    Toast.makeText(this, "try again after some time", Toast.LENGTH_LONG).show()

                    btn.isEnabled = true
                    progressBar.visibility= View.INVISIBLE
                    currentUser?.delete()
                }
            }



        }
    }

    fun getAllUsers() {
        val ref : DatabaseReference = FirebaseDatabase.getInstance().getReference().child("users")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                Log.e("asd","snap : $snapshot")
                for( user in snapshot.children ){
                    Log.e("asd","he : $user")
                    userList.add(user.getValue(FirebaseUser::class.java))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("error","error : ${error}")
            }
        }

        ref.addValueEventListener(listener)

    }


}