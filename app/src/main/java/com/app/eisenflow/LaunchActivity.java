package com.app.eisenflow;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.app.eisenflow.utils.Utils;

/**
 * LaunchActivity is an entry point for the app.
 * It shows the app's onboarding tutorial or
 * the main activity.
 *
 * Created on 12/18/17.
 */

public class LaunchActivity extends AppCompatActivity {
    public static final String PREF_FIRST_TIME_USER = "FirstTimeUser";
    private static final int SPLASH_TIME = 3000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashcreen_one);

        if (Utils.isFirstTimeUser(this)) {
            new BackgroundTask(this).execute();
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private static class BackgroundTask extends AsyncTask {
        private Activity activity;
        private Intent intent;

        public BackgroundTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            intent = new Intent(activity, TutorialActivity.class);
        }

        @Override
        protected Object doInBackground(Object[] params) {
            /*  Use this method to load background
            * data that your app needs. */
            try {
                Thread.sleep(SPLASH_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
//            Pass your loaded data here using Intent
            activity.startActivity(intent);
            activity.finish();
        }
    }
}
