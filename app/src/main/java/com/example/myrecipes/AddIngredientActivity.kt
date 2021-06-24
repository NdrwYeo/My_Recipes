package com.example.myrecipes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.myrecipes.databinding.ActivityAddIngredientBinding
import com.example.myrecipes.Ingredients
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import java.util.*
import kotlinx.android.synthetic.main.recycle_row_ingredients.view.*

class AddIngredientActivity : AppCompatActivity() {

    var rc_id : String = ""
    var list = arrayListOf<Ingredients>()
    private lateinit var binding: ActivityAddIngredientBinding

    override fun onStart() {
        super.onStart()
        rc_id = intent.getStringExtra(AddNewRecipeActivity.RECIPE_KEY).toString()
        initSpinner()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddIngredientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackForAddIngredient.setOnClickListener {
            val fc = Function()
            fc.delete(rc_id)
            finish()
        }
        binding.btnAddIngredient.setOnClickListener { add() }
        binding.btnNextStep.setOnClickListener { insert() }


    }

    private fun add(){

        if(binding.eTxtIngredientNameInput.text.isEmpty())
            return

        if (binding.eTxtQuantityInput.text.isEmpty())
            return

        val ing = Ingredients()
        ing.ingredientID = UUID.randomUUID().toString()
        ing.ingredientName = binding.eTxtIngredientNameInput.text.toString()
        ing.quantity = binding.eTxtQuantityInput.text.toString().toDouble()
        ing.recipeID = rc_id
        ing.unit = binding.spinnerUnit.selectedItem.toString()

        list.add(ing)
        display()
        cleartxt()

    }

    private fun display(){
        val adapter = GroupAdapter<GroupieViewHolder>()
        if(list.count() > 0){
            list.forEach {
                adapter.add(bindataIng(it))
            }
            binding.ryrIngredientList.adapter = adapter

        }
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


    private fun initSpinner(){
        val unit = resources.getStringArray(R.array.ing_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, unit)
        binding.spinnerUnit.adapter = adapter

    }

    private fun cleartxt(){
        binding.eTxtIngredientNameInput.setText("")
        binding.eTxtQuantityInput.setText("")
    }

    private fun insert(){

        list.forEach {
            val ref = FirebaseDatabase.getInstance().getReference("/Ingredient/${it.ingredientID}")
            ref.setValue(it)
        }

        val i = Intent(this, AddStepActivity::class.java)
        i.putExtra(RECIPE_KEY, rc_id)
        finish()
        startActivity(i)

    }

    companion object{
        val RECIPE_KEY = "RECIPE_KEY"
    }


}