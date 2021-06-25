package com.example.myrecipes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import com.example.myrecipes.databinding.ActivityNewRecipeBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddNewRecipeActivity : AppCompatActivity() {

    var recipe = Recipe()
    var selectedPhotoUri: Uri? = null
    var uuid = UUID.randomUUID().toString()

    private lateinit var binding: ActivityNewRecipeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        spinnerinit()
        binding.btnBackForAddRecipe.setOnClickListener {
            val fc = Function()
            fc.delete(uuid)
            finish() }

        binding.imgAddRecipe.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }

        binding.btnAddNext.setOnClickListener {
            if(selectedPhotoUri != null){
                savePhoto()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            binding.imgAddRecipe.setImageBitmap(bitmap)
        }

    }

    private fun savePhoto(){
        val recipeImgID = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/RecipeImages/$recipeImgID")
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                insert(it.toString())
            }

        }
    }

    private fun insert(url : String){
        recipe.recipeID = uuid
        recipe.recipeImage = url
        recipe.recipeTitle = binding.eTxtAddTitleInput.text.toString()
        recipe.calories = binding.eTxtAddCaloriesInput.text.toString().toDouble()
        recipe.totalFat = binding.eTxtAddTotalFatInput.text.toString().toDouble()
        recipe.saturatedFat = binding.eTxtAddSaturatedFatInput.text.toString().toDouble()
        recipe.fibre = binding.eTxtAddFibreInput.text.toString().toDouble()
        recipe.protein = binding.eTxtAddProteinInput.text.toString().toDouble()
        recipe.cholesterol = binding.eTxtAddCholesterolInput.text.toString().toDouble()
        recipe.category = binding.nutritionFactAddSpinner.selectedItem.toString()

        val ref = FirebaseDatabase.getInstance().getReference("/Recipe/$uuid")
        ref.setValue(recipe)

        val i = Intent(this, AddIngredientActivity::class.java)
        i.putExtra(RECIPE_KEY,uuid)
        finish()
        startActivity(i)

    }

    companion object{
        val RECIPE_KEY = "RECIPE_KEY"
    }

    private fun spinnerinit(){
        val cat = resources.getStringArray(R.array.pick_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cat)
        binding.nutritionFactAddSpinner.adapter = adapter
    }

}