package com.example.myrecipes

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.example.myrecipes.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLoginActivity.setOnClickListener {
            enterLoginActivity()
        }

        binding.btnRegisterActivity.setOnClickListener {
            enterRegisterActivity()
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    private fun enterLoginActivity(){
        FirebaseAuth.getInstance().signOut()
        intent = Intent(this, LoginActivity::class.java )
        startActivity(intent)
    }

    private fun enterRegisterActivity(){
        FirebaseAuth.getInstance().signOut()
        intent = Intent(this, RegisterActivity::class.java )
        startActivity(intent)
    }

    companion object{
        fun launchIntent(context: Context){
            val intent = Intent(context,MainActivity::class.java)
            context.startActivity(intent)
        }

        fun launchIntentClearTask(context: DashboardActivity){
            val intent = Intent(context,MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }
}