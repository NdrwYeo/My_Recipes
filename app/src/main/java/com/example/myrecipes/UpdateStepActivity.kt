package com.example.myrecipes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.myrecipes.databinding.ActivityUpdateStepBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.recycle_update_step.view.*
import java.lang.Double
import java.util.*

class UpdateStepActivity : AppCompatActivity() {
    var adapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var binding: ActivityUpdateStepBinding
    var stepSelectedId : String = ""
    var rc_id : String = ""
    var rc_name : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateStepBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackForUpdateStep.setOnClickListener {
            onBackPressed()
        }

        binding.btnAddUpdateStep.setOnClickListener {
            addStep()
            init()
        }

        binding.btnUpdateStep.setOnClickListener {
            modifyStep()
            init()
        }

        binding.btnDeleteUpdateStep.setOnClickListener {
            removeStep()
        }

        binding.btnDoneForUpdateStep.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onStart() {
        super.onStart()
        rc_id = intent.getStringExtra(UpdateIngredientActivity.UPDATED_RECIPE_ID).toString()
        rc_name = intent.getStringExtra(UpdateIngredientActivity.UPDATED_RECIPE_TITLE).toString()
        binding.txtUpdateCurrentTitle.setText(rc_name)
        init()
    }

    private fun init(){
        val ref =  FirebaseDatabase.getInstance().getReference("/Steps").orderByChild("recipeID").equalTo(rc_id)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            val list = arrayListOf<Steps>()
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach{
                    val stp = it.getValue(Steps::class.java)
                    if(stp != null){
                        list.add(stp)
                        if (list.count() == snapshot.children.count()) {
                            list.sortBy { it.stepNo }
                            list.forEach {
                                adapter.add(stepData(it))
                            }
                        }

                    }
                }
                adapter.setOnItemClickListener { item, view ->
                    val stpValue = item as stepData
                    stepSelectedId = stpValue.step.stepID
                    binding.eTxtUpdateStepNoInput.setText(stpValue.step.stepNo.toString())
                    binding.eTxtUpdateDescriptionInput.setText(stpValue.step.desc)
                }
                binding.ryrUpdateStepList.adapter = adapter
            }
        })
    }


    class stepData(val step : Steps): Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.update_step_number.text = step.stepNo.toString()
            viewHolder.itemView.update_step_description.text = step.desc
        }
        override fun getLayout(): Int {
            return R.layout.recycle_update_step
        }
    }

    private fun addStep(){
        val stepID = UUID.randomUUID().toString()

        if(binding.eTxtUpdateStepNoInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter step number", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateStepNoInput.requestFocus()
            return
        }
        else if(binding.eTxtUpdateDescriptionInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please enter step description", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateDescriptionInput.requestFocus()
            return
        } else {
            val ref = FirebaseDatabase.getInstance().getReference("/Steps/$stepID")
            var step = Steps()
            step.stepID = stepID
            step.stepNo = binding.eTxtUpdateStepNoInput.text.toString().toInt()
            step.desc = binding.eTxtUpdateDescriptionInput.text.toString()
            step.recipeID = rc_id.toString()

            ref.setValue(step)
            Toast.makeText(baseContext, "Added New Step Successful", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateDescriptionInput.setText("")
            binding.eTxtUpdateStepNoInput.setText("")
        }


    }

    private fun modifyStep(){

        if(binding.eTxtUpdateStepNoInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please select step from the list to update", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateStepNoInput.clearFocus()
            return
        }
        else if(binding.eTxtUpdateDescriptionInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please select step from the list to update", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateDescriptionInput.clearFocus()
            return
        } else{
            val ref = FirebaseDatabase.getInstance().getReference("Steps/$stepSelectedId")
            ref.child("stepNo").setValue(binding.eTxtUpdateStepNoInput.text.toString().toInt())
            ref.child("desc").setValue(binding.eTxtUpdateDescriptionInput.text.toString())
            Toast.makeText(baseContext, "Step Update Successful", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateDescriptionInput.setText("")
            binding.eTxtUpdateStepNoInput.setText("")

        }
    }

    private fun removeStep(){
        if(binding.eTxtUpdateStepNoInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please select step from the list to delete", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateStepNoInput.clearFocus()
            return
        }
        else if(binding.eTxtUpdateDescriptionInput.text.toString().isEmpty()){
            Toast.makeText(this,"Please select step from the list to delete", Toast.LENGTH_SHORT).show()
            binding.eTxtUpdateDescriptionInput.clearFocus()
            return
        } else {
            val builder = AlertDialog.Builder(this@UpdateStepActivity)
            builder.setTitle("Delete Step")
            builder.setMessage("Are you want to delete this step?")
            builder.setPositiveButton("Yes") { dialog, which ->
                Toast.makeText(applicationContext, "Step deleted", Toast.LENGTH_SHORT).show()
                val ref = FirebaseDatabase.getInstance().getReference("/Steps").child(stepSelectedId)
                ref.removeValue()
                init()
                binding.eTxtUpdateStepNoInput.setText("")
                binding.eTxtUpdateDescriptionInput.setText("")
            }
            builder.setNegativeButton("No") { dialog, which ->
                binding.eTxtUpdateStepNoInput.setText("")
                binding.eTxtUpdateDescriptionInput.setText("")
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }


}