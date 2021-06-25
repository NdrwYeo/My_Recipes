package com.example.myrecipes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.myrecipes.databinding.ActivityForgotPasswordBinding

import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackForForgotPassword.setOnClickListener {
            onBackPressed()
        }

        binding.btnResetPassword.setOnClickListener {
            resetPassword()
        }
    }

    private fun resetPassword(){
        val email = binding.eTxtResetEmailInput.text.toString().trim()
        if(email.isEmpty()){
            binding.eTxtResetEmailInput.error = "Please enter your email"
            binding.eTxtResetEmailInput.requestFocus()
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.eTxtResetEmailInput.error = "Email input is invalid"
            binding.eTxtResetEmailInput.requestFocus()
            return
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(this,"Email sent", Toast.LENGTH_SHORT).show()
                Intent(this,LoginActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }else{
                Toast.makeText(this,"${it.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}