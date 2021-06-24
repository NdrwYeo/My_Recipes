package com.example.myrecipes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.widget.Toast
import com.example.myrecipes.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    var currentDate = Calendar.getInstance().time
    var user = User()
    var selectedPhotoUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            signUpUser()
        }

        binding.imgUploadPic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }

        binding.btnBackForRegister.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            binding.imgUploadPic.setImageBitmap(bitmap)
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
    private fun signUpUser(){

        if(binding.eTxtUsernameInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter your username", Toast.LENGTH_SHORT).show()
            binding.eTxtUsernameInput.requestFocus()
            return
        }

        if(binding.eTxtEmailInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter your email address", Toast.LENGTH_SHORT).show()
            binding.eTxtEmailInput.requestFocus()
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(binding.eTxtEmailInput.text.toString()).matches()){
            Toast.makeText(this,"Please enter valid email address", Toast.LENGTH_SHORT).show()
            binding.eTxtEmailInput.requestFocus()
            return
        }

        if(binding.eTxtPasswordInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter your password", Toast.LENGTH_SHORT).show()
            binding.eTxtPasswordInput.requestFocus()
            return
        }

        if(binding.eTxtConfirmPasswordInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter confirm password", Toast.LENGTH_SHORT).show()
            binding.eTxtConfirmPasswordInput.requestFocus()
            return
        }

        if(selectedPhotoUri == null){
            Toast.makeText(this,"Please select your profile pic", Toast.LENGTH_SHORT).show()
            return
        }

        val email = binding.eTxtEmailInput.text.toString()
        val password = binding.eTxtPasswordInput.text.toString()
        val confirmPass = binding.eTxtConfirmPasswordInput.text.toString()
        if(confirmPass == password) {
            if (isValidPassword(password)) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (!it.isSuccessful) {
                            Toast.makeText(baseContext, "Register failed.", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            user.id = FirebaseAuth.getInstance().uid.toString()
                            uploadImageToFirebaseStorage()

                            FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                                ?.addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Toast.makeText(
                                            baseContext, "Email verification sent.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        startActivity(Intent(this, LoginActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            baseContext,
                                            "Register failed.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                        }
                    }
            } else {
                Toast.makeText(
                    baseContext,
                    "Password does not fulfill the criteria",
                    Toast.LENGTH_SHORT
                ).show()
                binding.eTxtPasswordInput.requestFocus()
                return
            }
        }else{
            Toast.makeText(
                baseContext,
                "Password mismatch",
                Toast.LENGTH_SHORT
            ).show()
            binding.eTxtConfirmPasswordInput.requestFocus()
            return
        }
    }

    private fun uploadImageToFirebaseStorage(){

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/ProfileImages/$filename")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                saveUserToDatabase(it.toString())
            }

        }
    }

    private fun saveUserToDatabase(profileImageUrl: String){
        val ref = FirebaseDatabase.getInstance().getReference("/Users/${user.id}")
        user.email = binding.eTxtEmailInput.text.toString()
        user.username = binding.eTxtUsernameInput.text.toString()
        user.profileImageUrl = profileImageUrl
        user.registerDate = currentDate
        ref.setValue(user)
    }

    private fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=\\S+$).{8,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(password)
        return matcher.matches()
    }
}