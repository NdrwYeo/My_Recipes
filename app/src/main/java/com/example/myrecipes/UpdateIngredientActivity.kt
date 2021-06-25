package com.example.myrecipes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.myrecipes.databinding.ActivityUpdateIngredientBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_add_ingredient.*
import kotlinx.android.synthetic.main.recycle_update_ing.view.*
import java.lang.Double
import java.util.*


class UpdateIngredientActivity : AppCompatActivity() {
    var adapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var binding: ActivityUpdateIngredientBinding
    var ingSelectedId : String = ""
    var rc_id : String = ""
    var rc_name : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateIngredientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        spinnerInit()

        binding.btnBackForUpdateIngredient.setOnClickListener {
            onBackPressed()
        }

        binding.btnForwardForUpdateIngredient.setOnClickListener {
            val intent = Intent (this, UpdateStepActivity::class.java)
            intent.putExtra(UPDATED_RECIPE_ID, rc_id)
            intent.putExtra(UPDATED_RECIPE_TITLE, rc_name)
            startActivity(intent)
        }

        binding.btnAddOnUpdateIngredient.setOnClickListener {
            addIngredient()
            init()
        }

        binding.btnUpdateIngredient.setOnClickListener {
            modifyIngredient()
            init()
        }

        binding.btnRemoveUpdateIngredient.setOnClickListener {
            removeIngredient()
        }

    }

    override fun onStart() {
        super.onStart()
        rc_id = intent.getStringExtra(UpdateRecipeActivity.UPDATED_RECIPE_ID).toString()
        rc_name = intent.getStringExtra(UpdateRecipeActivity.UPDATED_RECIPE_TITLE).toString()
        binding.txtCurrentRecipeTitle.setText(rc_name)
        init()

    }

    private fun init(){
        adapter.clear()
        val ref =  FirebaseDatabase.getInstance().getReference("/Ingredient").orderByChild("recipeID").equalTo(rc_id)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    val ing = it.getValue(Ingredients::class.java)
                    if(ing !=null){
                        adapter.add(ingData(ing))
                    }
                }
                adapter.setOnItemClickListener { item, view ->
                    val ingValue = item as ingData
                    ingSelectedId = ingValue.ing.ingredientID
                    binding.eTxtUpdateIngredientName.setText(ingValue.ing.ingredientName)
                    binding.eTxtUpdateIngredientQuantity.setText(ingValue.ing.quantity.toString())
                    spinnerOnChanged(ingValue.ing.unit)
                }
                binding.ryrUpdateIngredientList.adapter = adapter
            }
        })
    }

    private fun spinnerInit(){
        val ingredient = resources.getStringArray(R.array.ing_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ingredient)
        binding.spinnerUpdateIngredientUnit.adapter = adapter
    }

    private fun spinnerOnChanged(str: String){
        val ingredient = resources.getStringArray(R.array.ing_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ingredient)
        binding.spinnerUpdateIngredientUnit.adapter = adapter
        binding.spinnerUpdateIngredientUnit.setSelection(adapter.getPosition((str)))
    }

    class ingData(val ing : Ingredients): Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.updated_ing_qty.text = ing.quantity.toString()
            viewHolder.itemView.updated_ing_unit.text = ing.unit
            viewHolder.itemView.updated_ing_name.text = ing.ingredientName
        }
        override fun getLayout(): Int {
            return R.layout.recycle_update_ing
        }
    }

    private fun addIngredient(){

        val ingNewID = UUID.randomUUID().toString()
        if(binding.eTxtUpdateIngredientName.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter ingredient name", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateIngredientName.requestFocus()
            return
        }
        else if(binding.eTxtUpdateIngredientQuantity.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter ingredients quantity", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateIngredientQuantity.requestFocus()
            return
        } else{
            val ref = FirebaseDatabase.getInstance().getReference("/Ingredient/$ingNewID")
            var ingredient = Ingredients()
            ingredient.ingredientID = ingNewID
            ingredient.ingredientName = binding.eTxtUpdateIngredientName.text.toString()
            ingredient.quantity = binding.eTxtUpdateIngredientQuantity.text.toString().toDouble()
            ingredient.unit = binding.spinnerUpdateIngredientUnit.selectedItem.toString()
            ingredient.recipeID = rc_id.toString()

            ref.setValue(ingredient)
            Toast.makeText(baseContext, "Added New Ingredient Successful", Toast.LENGTH_SHORT).show()
            clearFocus()
        }
    }

    private fun modifyIngredient(){

        if(binding.eTxtUpdateIngredientName.text.toString().isEmpty()){
            Toast.makeText(this,"Please select ingredient from the list to update", Toast.LENGTH_SHORT).show()
            clearFocus()
            return
        }
        else if(binding.eTxtUpdateIngredientQuantity.text.toString().isEmpty()){
            Toast.makeText(this,"Please select ingredient from the list to update", Toast.LENGTH_SHORT).show()
            clearFocus()
            return
        } else{
            val ref = FirebaseDatabase.getInstance().getReference("Ingredient/$ingSelectedId")
            ref.child("ingredientName").setValue(binding.eTxtUpdateIngredientName.text.toString())
            ref.child("quantity").setValue(binding.eTxtUpdateIngredientQuantity.text.toString().toDouble())
            ref.child("unit").setValue(binding.spinnerUpdateIngredientUnit.selectedItem.toString())
            Toast.makeText(baseContext, "Ingredient Update Successful", Toast.LENGTH_SHORT).show()
            clearFocus()
        }
    }

    private fun removeIngredient(){
        if(binding.eTxtUpdateIngredientName.text.toString().isEmpty()){
            Toast.makeText(this,"Please select ingredient from the list to delete", Toast.LENGTH_SHORT).show()
            clearFocus()
            return
        }
        else if(binding.eTxtUpdateIngredientQuantity.text.toString().isEmpty()){
            Toast.makeText(this,"Please select ingredient from the list to delete", Toast.LENGTH_SHORT).show()
            clearFocus()
            return
        } else {
            val builder = AlertDialog.Builder(this@UpdateIngredientActivity)
            builder.setTitle("Delete Ingredient")
            builder.setMessage("Are you want to delete this ingredient?")
            builder.setPositiveButton("Yes") { dialog, which ->
                Toast.makeText(applicationContext, "Ingredient deleted", Toast.LENGTH_SHORT).show()
                val ref = FirebaseDatabase.getInstance().getReference("/Ingredient").child(ingSelectedId)
                ref.removeValue()
                init()
                clearFocus()
            }
            builder.setNegativeButton("No") { dialog, which ->
                clearFocus()
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

    }

    companion object{
        val UPDATED_RECIPE_ID = "UPDATED_RECIPE_ID"
        val UPDATED_RECIPE_TITLE = "UPDATED_RECIPE_TITLE"
    }

    private fun clearFocus(){
        binding.eTxtUpdateIngredientName.clearFocus()
        binding.eTxtUpdateIngredientQuantity.clearFocus()
        binding.eTxtUpdateIngredientName.setText("")
        binding.eTxtUpdateIngredientQuantity.setText("")
    }



}