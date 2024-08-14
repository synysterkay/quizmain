package com.superquiz.easyquiz.triviastar;


import androidx.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Leenah on 18/01/2020.
 * dworarena@gmail.com
 */
//This is the POJO for Firebase RealTime database in order to retrieve videos and titles
@IgnoreExtraProperties
@Keep
public class LeaderBoardModelClass {
    public String name,image;
    public int score;


    public LeaderBoardModelClass(String name, String image, int score) {
        this.name = name;
        this.image = image;
        this.score = score;
    }

    public LeaderBoardModelClass() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

}

