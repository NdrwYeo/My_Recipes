package com.example.myrecipes

import android.R.layout.simple_spinner_dropdown_item
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.myrecipes.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.recycle_row_item.view.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    val list = arrayListOf<Recipe>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkUserAccountSignIn()
        init()
        spinnerinit()

        binding.spinnerCategory.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapter: AdapterView<*>, v: View?, i: Int, lng: Long) {
                filter()
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        binding.imgAddNewRecipe.setOnClickListener {
            val i = Intent(this,AddNewRecipeActivity::class.java)
            startActivity(i)
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val i = Intent(this, LoginActivity::class.java)
            finish()
            startActivity(i)
        }

    }


    private fun init(){
        val adapter = GroupAdapter<GroupieViewHolder>()
        val ref = FirebaseDatabase.getInstance().getReference("/Recipe")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val rep = it.getValue(Recipe::class.java)
                    if (rep != null) {
                        adapter.add(bindata(rep))
                        list.add(rep)
                    }
                }
                binding.ryrRecipeList.adapter = adapter
            }
        })
    }

    private class bindata(val recipe: Recipe): Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.ryrTitle.text = recipe.recipeTitle
            Picasso.get().load(recipe.recipeImage).into(viewHolder.itemView.ryrImage)

            viewHolder.itemView.ryrRoot.setOnClickListener {
                val intent = Intent(it.context, RecipeDetailActivity::class.java)
                intent.putExtra(RECIPE_KEY, recipe.recipeID)
                (it.context as Activity).startActivity(intent)

            }

        }

        override fun getLayout(): Int {
            return R.layout.recycle_row_item
        }
    }

    companion object{
        val RECIPE_KEY = "RECIPE_KEY"
    }

    private fun spinnerinit(){
        val category = resources.getStringArray(R.array.all_array)
        val adapter = ArrayAdapter(this, simple_spinner_dropdown_item, category)
        binding.spinnerCategory.adapter = adapter
    }

    private fun filter(){
        val search = binding.spinnerCategory.selectedItem.toString()
        val adapter = GroupAdapter<GroupieViewHolder>()

        list.forEach {
            if(!(search.equals("All"))){
                if(it.category.toUpperCase().contains(search.toUpperCase())){
                    adapter.add(bindata(it))
                }
            }else{
                adapter.add(bindata(it))
            }
        }
        binding.ryrRecipeList.adapter = adapter

    }

    private fun checkUserAccountSignIn(){
        if (FirebaseAuth.getInstance().uid.isNullOrEmpty()){
            MainActivity.launchIntentClearTask(this)
            FirebaseAuth.getInstance().signOut()
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }



}
