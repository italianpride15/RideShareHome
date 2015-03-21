package com.mantisclaw.italianpride15.ridesharehome;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseCrashReporting;

/**
 * Created by italianpride15 on 3/20/15.
 */

public class MainApp extends Application {

    private static final String parse_application_id = "Z6WVHg75MPlkEmPYDB8Oa5CED9Wukm7UyGi1w79s";
    private static final String parse_client_id = "s5OF0qYXOgRelbps5ySIOBLeoG4GnrgL6BXprtQQ";

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
