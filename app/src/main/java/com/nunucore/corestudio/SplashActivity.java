package com.nunucore.corestudio;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Aktifkan SplashScreen API
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Langsung redirect ke MainActivity
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }
}
