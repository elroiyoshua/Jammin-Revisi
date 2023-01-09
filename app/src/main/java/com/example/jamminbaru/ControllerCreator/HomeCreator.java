package com.example.jamminbaru.ControllerCreator;

import static com.example.jamminbaru.creator.LoginCreator.creator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.jamminbaru.R;

public class HomeCreator extends AppCompatActivity {
    private ImageButton profile;
    private TextView usernamedisplayTagHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        String usernameTxt = getIntent().getStringExtra("usernameTag");

        String displayUsername = creator.getPhoneTxt();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_creator);

        usernamedisplayTagHome = findViewById(R.id.usernamedisplayTagHome);
        usernamedisplayTagHome.setText(displayUsername);

        profile = (ImageButton) findViewById(R.id.profileLogo);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeCreator.this, ProfileCreator.class);
                startActivity(intent);
            }
        });


    }
}