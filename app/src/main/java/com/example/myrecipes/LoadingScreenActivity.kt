package com.example.myrecipes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoadingScreenActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_screen)
        auth = FirebaseAuth.getInstance()

        Handler().postDelayed({
            if (auth.currentUser != null && auth.currentUser!!.isEmailVerified) {
                val ref = FirebaseDatabase.getInstance().getReference("/Users").orderByChild("id").equalTo(
                    FirebaseAuth.getInstance().currentUser?.uid)
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach {
                            val user = it.getValue(User::class.java)
                            if (user != null) {
                                enterDashboardActivity()
                            }
                        }
                    }
                })
            }
            else{
                enterMainActivity()
            }
        },1000)
    }

    private fun enterMainActivity(){
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun enterDashboardActivity(){
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}

