package com.example.myrecipes

import java.util.*

class Recipe {


    var recipeID: String = ""
    var recipeTitle: String = ""
    var recipeImage: String = ""
    var calories: Double = 0.00
    var totalFat: Double = 0.00
    var saturatedFat: Double = 0.00
    var fibre: Double = 0.00
    var protein: Double = 0.00
    var cholesterol: Double = 0.00
    var uploadDate: Date = Calendar.getInstance().time
    var category: String = ""
}