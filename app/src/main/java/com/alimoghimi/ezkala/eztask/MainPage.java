package com.alimoghimi.ezkala.eztask;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

public class MainPage extends AppCompatActivity {

    int progressCounter = 0;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);



        new Thread(new Runnable() {
            @Override
            public void run() {
                while(progressCounter < 100)
                {
                    progressCounter++;
                    android.os.SystemClock.sleep(50);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progressCounter);

                            if(progressCounter == 100)
                            {
                                Intent SignIn = new Intent(getApplicationContext(),SignIn.class);
                                startActivity(SignIn);
                            }

                        }
                    });

                }

            }
        }).start();

    }
}
