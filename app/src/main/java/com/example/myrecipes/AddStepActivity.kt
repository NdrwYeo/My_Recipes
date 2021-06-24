package com.example.myrecipes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myrecipes.databinding.ActivityAddStepBinding
import com.example.myrecipes.databinding.RecycleRowPreparationBinding
import kotlinx.android.synthetic.main.recycle_row_preparation.view.*
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import java.util.*

class AddStepActivity : AppCompatActivity() {


    private lateinit var binding: ActivityAddStepBinding
    private lateinit var binding2: RecycleRowPreparationBinding
    var rc_id : String = ""
    var list = arrayListOf<Steps>()
    var counter = 1


    override fun onStart() {
        super.onStart()
        rc_id = intent.getStringExtra(AddIngredientActivity.RECIPE_KEY).toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStepBinding.inflate(layoutInflater)
        binding2 = RecycleRowPreparationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackForAddStep.setOnClickListener {
            val fc = Function()
            fc.delete(rc_id)
            finish()
        }
        binding.btnAddStep.setOnClickListener { add() }

        binding.btnFinish.setOnClickListener { insert() }

    }

    private fun add(){
        val step = Steps()

        step.stepID = UUID.randomUUID().toString()
        step.stepNo = counter
        step.desc = binding.eTxtDescriptionInput.text.toString()
        step.recipeID = rc_id

        list.add(step)
        display()
        clear()

    }

    private fun display(){
        val adapter = GroupAdapter<GroupieViewHolder>()
        if(list.count() > 0){
            list.forEach {
                adapter.add(bindataStep(it))
            }
            binding.ryrStepList.adapter = adapter

        }
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

    private fun clear(){
        binding.eTxtDescriptionInput.setText("")
        binding.eTxtStepInput.setText(counter.toString())
        counter += 1
    }

    private fun insert(){

        list.forEach {
            val ref = FirebaseDatabase.getInstance().getReference("/Steps/${it.stepID}")
            ref.setValue(it)
        }
    }

}