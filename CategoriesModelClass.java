package com.superquiz.easyquiz.triviastar;


import androidx.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

 

//This is the POJO for Firebase RealTime database
@IgnoreExtraProperties
@Keep
public class CategoriesModelClass {
    public String i, n;

    public CategoriesModelClass(String i, String n) {
        this.i = i;
        this.n = n;
    }

    public CategoriesModelClass() {
    }

    public String getImage() {
        return i;
    }

    public void setImage(String i) {
        this.i = i;
    }

    public String getName() {
        return n;
    }

    public void setName(String n) {
        this.n = n;
    }

}

