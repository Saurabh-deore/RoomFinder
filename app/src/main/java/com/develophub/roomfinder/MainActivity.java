package com.develophub.roomfinder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views
        ImageView logo = findViewById(R.id.logoImage);
        TextView appName = findViewById(R.id.appName);
        TextView tagline=findViewById(R.id.tagline);

        // Load animations
        Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.scale_fade_in);
        Animation textAnim = AnimationUtils.loadAnimation(this, R.anim.slide_fade_in);
        Animation texttag=AnimationUtils.loadAnimation(this,R.anim.slide_fade_in);
        // Start animations
        logo.startAnimation(logoAnim);
        appName.startAnimation(textAnim);
        tagline.startAnimation(texttag);

        // Delay and move to next screen
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        }, 4000);
    }
}
