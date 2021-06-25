package com.example.myrecipes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myrecipes.databinding.ActivityUpdateRecipeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.lang.Double
import java.util.*

class UpdateRecipeActivity : AppCompatActivity() {

    var selectUpdatePhotoUri: Uri? = null
    var imageString: String = ""
    var rc_id : String = ""
    private lateinit var binding: ActivityUpdateRecipeBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityUpdateRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackForUpdateRecipe.setOnClickListener {
            onBackPressed()
        }

        binding.imgUpdateRecipe.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        binding.btnUpdateNext.setOnClickListener {
            updateRecipe()
        }

        binding.btnForwardForUpdateRecipe.setOnClickListener {
            val id = intent.getStringExtra("RECIPE_ID")
            val name = binding.eTxtUpdateTitleInput.text.toString()
            val intent = Intent(this, UpdateIngredientActivity::class.java)
            intent.putExtra("UPDATED_RECIPE_ID", id)
            intent.putExtra("UPDATED_RECIPE_TITLE", name)
            startActivity(intent)
        }

    }

    override fun onStart() {
        super.onStart()
        rc_id = intent.getStringExtra(RecipeDetailActivity.RECIPE_ID).toString()
        init()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectUpdatePhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectUpdatePhotoUri)
            binding.imgUpdateRecipe.setImageBitmap(bitmap)
        }
    }

    private fun init() {
        val ref = FirebaseDatabase.getInstance().getReference("/Recipe").orderByChild("recipeID").equalTo(rc_id)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val rc = it.getValue(Recipe::class.java)
                    if (rc != null) {

                        Picasso.get().load(rc.recipeImage).into(binding.imgUpdateRecipe).toString()
                        binding.eTxtUpdateTitleInput.setText(rc.recipeTitle)
                        //recipe nutrition fact
                        binding.eTxtUpdateCaloriesInput.setText(rc.calories.toString())
                        binding.eTxtUpdateTotalFatInput.setText(rc.totalFat.toString())
                        binding.eTxtUpdateSaturatedFatInput.setText(rc.saturatedFat.toString())
                        binding.eTxtUpdateFibreInput.setText(rc.fibre.toString())
                        binding.eTxtUpdateProteinInput.setText(rc.protein.toString())
                        binding.eTxtUpdateCholesterolInput.setText(rc.cholesterol.toString())
                        imageString = rc.recipeImage
                        spinnerinit(rc.category)
                    }
                }
            }
        })
    }

    private fun spinnerinit(str: String){
        val category = resources.getStringArray(R.array.pick_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, category)
        binding.nutritionFactUpdateSpinner.adapter = adapter
        binding.nutritionFactUpdateSpinner.setSelection(adapter.getPosition((str)))
    }

    private fun updateRecipe(){


        if(binding.eTxtUpdateTitleInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter recipe title", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateTitleInput.requestFocus()
            return

        } else if(binding.eTxtUpdateCaloriesInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter calories amount", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateCaloriesInput.requestFocus()
            return

        } else if(binding.eTxtUpdateTotalFatInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter total fat amount", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateTotalFatInput.requestFocus()
            return

        }

        else if(binding.eTxtUpdateSaturatedFatInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter saturated fat amount", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateSaturatedFatInput.requestFocus()
            return

        }
        else if(binding.eTxtUpdateFibreInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter fibre amount", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateFibreInput.requestFocus()
            return
        }
        else if(binding.eTxtUpdateProteinInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter protein amount", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateProteinInput.requestFocus()
            return
        }
        else if(binding.eTxtUpdateCholesterolInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter cholesterol amount", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateCholesterolInput.requestFocus()
            return
        } else {
            val ref = FirebaseDatabase.getInstance().getReference("Recipe/$rc_id")
            ref.child("recipeTitle").setValue(binding.eTxtUpdateTitleInput.text.toString())
            ref.child("calories").setValue(binding.eTxtUpdateCaloriesInput.text.toString().toDouble())
            ref.child("totalFat").setValue(binding.eTxtUpdateTotalFatInput.text.toString().toDouble())
            ref.child("saturatedFat").setValue(binding.eTxtUpdateSaturatedFatInput.text.toString().toDouble())
            ref.child("fibre").setValue(binding.eTxtUpdateFibreInput.text.toString().toDouble())
            ref.child("protein").setValue(binding.eTxtUpdateProteinInput.text.toString().toDouble())
            ref.child("cholesterol").setValue(binding.eTxtUpdateCholesterolInput.text.toString().toDouble())
            ref.child("category").setValue(binding.nutritionFactUpdateSpinner.selectedItem.toString())

            uploadFoodImageToFirebaseStorage()
        }
    }

    private fun uploadFoodImageToFirebaseStorage(){

        val name = binding.eTxtUpdateTitleInput.text.toString()
        val filename = UUID.randomUUID().toString()
        val ref_rec= FirebaseDatabase.getInstance().getReference("Recipe/$rc_id")
        val ref_img = FirebaseStorage.getInstance().getReference("/RecipeImage/$filename")

        if(selectUpdatePhotoUri==null){
            ref_rec.child("recipeImage").setValue(imageString)
        }else{
            ref_img.putFile(selectUpdatePhotoUri!!).addOnSuccessListener {
                ref_img.downloadUrl.addOnSuccessListener {
                    ref_rec.child("recipeImage").setValue(it.toString())
                }
            }
        }
        Toast.makeText(baseContext, "Update successful", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, UpdateIngredientActivity::class.java)
        intent.putExtra(UPDATED_RECIPE_ID, rc_id)
        intent.putExtra(UPDATED_RECIPE_TITLE, name)
        startActivity(intent)
    }

    companion object{
        val UPDATED_RECIPE_ID = "UPDATED_RECIPE_ID"
        val UPDATED_RECIPE_TITLE = "UPDATED_RECIPE_TITLE"
    }

}