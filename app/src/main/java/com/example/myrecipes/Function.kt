package com.example.myrecipes

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Function {

    fun delete(id : String){
        val ref1 = FirebaseDatabase.getInstance().getReference("/Recipe/$id")
        ref1.removeValue()

        val ref2 = FirebaseDatabase.getInstance().getReference("/Ingredient").orderByChild("recipeID").equalTo(id)
        ref2.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    it.ref.removeValue()
                }
            }
        })

        val ref3 = FirebaseDatabase.getInstance().getReference("/Steps").orderByChild("recipeID").equalTo(id)
        ref3.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    it.ref.removeValue()
                }

            }
        })

    }


}