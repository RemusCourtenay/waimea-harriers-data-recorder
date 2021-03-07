package com.example.waimeaHarriersDataInput;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainMenuActivity  extends Activity {

    // GUI STUFF
    private Resources appResources;

    private TextView mainHeaderTextView;
    private Button goToSignInButton;
    private Button goToInputDataButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(0);
        this.appResources = getResources();


//        this.mainHeaderTextView = findViewById(R.id.main_menu_header_text_view);
//        this.goToSignInButton = findViewById(R.id.main_menu_go_to_login_activity_button);
//        this.goToInputDataButton = findViewById(R.id.main_menu_go_to_input_data_activity_button);


    }




    /*---- BUTTON - ON CLICK METHODS ----*/

    public void goToButtonClicked(View goToButtonAsView) {
        Class<?> nextActivity;
        int buttonViewId = goToButtonAsView.getId();

        if (buttonViewId == goToSignInButton.getId()) {
            nextActivity = LoginActivity.class;
        } else if (buttonViewId == goToInputDataButton.getId()) {
            nextActivity = InputDataActivity.class;
        } else {
            throw new RuntimeException("GoTo button not yet setup in goToButtonClicked method");
        }
        Intent goToNextActivityIntent = new Intent(this, nextActivity);
        startActivity(goToNextActivityIntent);

    }

}
