package com.example.myrecipes

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.myrecipes.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    var isExist : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            doLogin()
        }

        binding.txtForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.btnBackForLogin.setOnClickListener {
            onBackPressed()
        }
    }

    private fun exit(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
        FirebaseAuth.getInstance().signOut()
        this.finish()
    }
    override fun onBackPressed() {
        exit(this)
    }

    private fun doLogin(){
        if(binding.eTxtEmailInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter email address", Toast.LENGTH_SHORT).show()
            binding.eTxtEmailInput.requestFocus()
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(binding.eTxtEmailInput.text.toString()).matches()){
            Toast.makeText(this,"Please enter valid email address", Toast.LENGTH_SHORT).show()
            binding.eTxtEmailInput.requestFocus()
            return
        }

        if(binding.eTxtPasswordInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter password", Toast.LENGTH_SHORT).show()
            binding.eTxtPasswordInput.requestFocus()
            return
        }

        auth.signInWithEmailAndPassword(binding.eTxtEmailInput.text.toString(), binding.eTxtPasswordInput.text.toString()).addOnCompleteListener {
            if(!it.isSuccessful){
                Toast.makeText(this,"Invalid email or password", Toast.LENGTH_SHORT).show()
            }else{
                if(FirebaseAuth.getInstance().currentUser != null){
                    if (FirebaseAuth.getInstance().currentUser?.isEmailVerified!!){
                        val ref = FirebaseDatabase.getInstance().getReference("/Users").orderByChild("id").equalTo(FirebaseAuth.getInstance().currentUser?.uid)
                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                snapshot.children.forEach {
                                    val user = it.getValue(User::class.java)
                                    if (user != null) {
                                        isExist = true
                                        enterDashboardActivity()
                                    }
                                }
                                if (isExist == false){
                                    FirebaseAuth.getInstance().signOut()
                                    Toast.makeText(baseContext, "Invalid email address.",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                    }
                    else{
                        Toast.makeText(baseContext, "Please verify your email address.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(baseContext, "Login failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun enterDashboardActivity(){
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}