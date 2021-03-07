package com.example.waimeaHarriersDataInput;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public abstract class SecondaryActivity extends Activity {

    protected final int layoutViewID;

    protected Button goToMainMenuButton;
    protected Resources activityResources;

    public SecondaryActivity(int layoutViewID) {
        this.layoutViewID = layoutViewID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutViewID);
        this.activityResources = getResources();

        // this.goToMainMenuButton = findViewById(R.id.secondary_go_to_main_menu_button);
    }


    public void goToButtonOnClick(View goToButtonAsView) {
        Class<?> nextActivity;
        int buttonViewId = goToButtonAsView.getId();

        if (buttonViewId == goToMainMenuButton.getId()) {
            nextActivity = MainMenuActivity.class;
        } else {
            throw new RuntimeException("Goto button not yet setup in goToButtonOnClick method");
        }

        Intent goToNextActivityIntent = new Intent(this, nextActivity);
        startActivity(goToNextActivityIntent);

    }



}
