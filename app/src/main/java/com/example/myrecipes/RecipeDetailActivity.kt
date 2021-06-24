package com.example.myrecipes

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.myrecipes.DashboardActivity.Companion.RECIPE_KEY
import com.example.myrecipes.databinding.ActivityDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.recycle_row_ingredients.view.*
import kotlinx.android.synthetic.main.recycle_row_preparation.view.*

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    var rc_id : String = ""


    override fun onStart() {
        super.onStart()

        rc_id = intent.getStringExtra(DashboardActivity.RECIPE_KEY).toString()

        init()
        bindIngredient()
        bindSteps()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackForRecipeDetail.setOnClickListener {
            finish()
        }

        binding.btnUpdateRecipe.setOnClickListener{
            val intent = Intent(this, UpdateRecipeActivity::class.java)
            intent.putExtra("RecipeId", rc_id)
            startActivity(intent)
        }

        binding.btnDeleteRecipe.setOnClickListener{
            val intent = Intent(this, DashboardActivity::class.java)
            val builder = AlertDialog.Builder(this@RecipeDetailActivity)
            builder.setTitle("Delete Recipe")
            builder.setMessage("Do you want to delete this recipe?")
            builder.setPositiveButton("Yes"){ dialog, which ->
                Toast.makeText(applicationContext, "Recipe is deleted", Toast.LENGTH_SHORT).show()
                deleteRecipe(rc_id)
                startActivity(intent)
            }
            builder.setNegativeButton("No"){ dialog, which ->
                Toast.makeText(
                    applicationContext,
                    "Recipe will not be deleted",
                    Toast.LENGTH_SHORT
                ).show()

            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }


    }

    private fun init() {

        //recipe title & image
        val ref2 = FirebaseDatabase.getInstance().getReference("/Recipe").orderByChild("recipeID").equalTo(rc_id)
        ref2.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val rc = it.getValue(Recipe::class.java)
                    if (rc != null) {
                        Picasso.get().load(rc.recipeImage).into(binding.recipeDetailImage)
                        binding.recipeDetailTitle.text = rc.recipeTitle


                        //recipe nutrition fact
                        binding.caloriesNumberText.text = rc.calories.toString()
                        binding.totalFatNumberText.text = rc.totalFat.toString()
                        binding.saturatedFatNumberText.text = rc.saturatedFat.toString()
                        binding.fibreNumberText.text = rc.fibre.toString()
                        binding.proteinNumberText.text = rc.protein.toString()
                        binding.cholesterolNumberText.text = rc.cholesterol.toString()

                    }
                }
            }
        })
    }

    private fun bindIngredient() {
        val ref = FirebaseDatabase.getInstance().getReference("/Ingredient").orderByChild("recipeID").equalTo(rc_id)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach {
                    val ing = it.getValue(Ingredients::class.java)
                    if (ing != null) {
                        adapter.add(bindataIng(ing))
                    }
                }
                binding.ryrIngredient.adapter = adapter
            }
        })
    }

    private class bindataIng(val ingredients: Ingredients): Item<GroupieViewHolder>() {
        override fun bind(viewHolder1: GroupieViewHolder, position: Int) {
            viewHolder1.itemView.ing_qty.text = ingredients.quantity.toString()
            viewHolder1.itemView.ing_name.text = ingredients.ingredientName
            viewHolder1.itemView.ing_desc.text = ingredients.unit
        }

        override fun getLayout(): Int {
            return R.layout.recycle_row_ingredients
        }
    }

    private fun bindSteps() {
        val list = arrayListOf<Steps>()

        val ref = FirebaseDatabase.getInstance().getReference("/Steps").orderByChild("recipeID").equalTo(rc_id)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach {
                    val stp = it.getValue(Steps::class.java)
                    if (stp != null) {
                        list.add(stp)
                        if(list.count() == snapshot.children.count()){
                            list.sortBy { it.stepNo }
                            list.forEach {
                                adapter.add(bindataStep(it))
                            }
                        }
                    }
                }
                binding.ryrSteps.adapter = adapter
            }
        })
    }


    private class bindataStep(val steps: Steps): Item<GroupieViewHolder>() {
        override fun bind(viewHolder2: GroupieViewHolder, position: Int) {
            viewHolder2.itemView.counterText.text = steps.stepNo.toString()
            viewHolder2.itemView.prepareText.text = steps.desc
        }
        override fun getLayout(): Int {
            return R.layout.recycle_row_preparation
        }
    }

    private fun deleteRecipe(id: String){

        val refRecipe = FirebaseDatabase.getInstance().getReference("Recipe").child(id)
        refRecipe.removeValue()

        val refImage = FirebaseDatabase.getInstance().getReference("/Recipe").orderByChild("recipeID").equalTo(id)
        refImage.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val rc = it.getValue(Recipe::class.java)
                    if (rc != null) {
                        val imageUrl = rc.recipeImage
                        val storageReference: StorageReference =
                            FirebaseStorage.getInstance().reference.child("RecipeImage").child(imageUrl)
                        storageReference.delete()
                    }
                }
            }
        })

        val refIngs = FirebaseDatabase.getInstance().getReference("/Ingredient").orderByChild("recipeID").equalTo(id)
        refIngs.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    it.ref.removeValue()
                }
            }
        })


        val refStep = FirebaseDatabase.getInstance().getReference("Steps").orderByChild("recipeID").equalTo(id)
        refStep.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    it.ref.removeValue()
                }
            }
        })

        val refFav = FirebaseDatabase.getInstance().getReference("Favourite").orderByChild("recipeID").equalTo(id)
        refFav.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    it.ref.removeValue()
                }
            }
        })

        val refHis = FirebaseDatabase.getInstance().getReference("History").orderByChild("recipeID").equalTo(id)
        refHis.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    it.ref.removeValue()
                }
            }
        })

    }





}