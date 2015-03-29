package com.mantisclaw.italianpride15.ridesharehome;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseCrashReporting;

/**
 * Created by italianpride15 on 3/20/15.
 */

public class MainApp extends Application {

    private static final String parse_application_id = "pTCU4Li4XZE82K6QN7XSoWv3xM9ABWWsWBfe1zD6";
    private static final String parse_client_id = "fF5j0OolWrJTvzOQqGEVGEbiA4buNdYdhs3z962u";

    @Override
    public void onCreate() {
        super.onCreate();
        initParse();
    }

    private void initParse() {
        ParseCrashReporting.enable(this);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, parse_application_id, parse_client_id);
    }
}
