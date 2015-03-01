package com.mantisclaw.italianpride15.ridesharehome;

import com.parse.ParseAnalytics;

import java.util.Map;

/**
 * Created by italianpride15 on 2/28/15.
 */
public class PFAnalytics {

    public static enum AnalyticsCategory {
        DEEPLINK("Deeplink"),
        LOCATION("Location"),
        USER("User"),
        SERVICES("Services"),
        STATS("Stats"),
        ALERTS("Alerts");

        private final String name;

        private AnalyticsCategory(String s) {
            name = s;
        }

        public String toString() {
            return name;
        }
    }

    public static void trackEvent(AnalyticsCategory category, Map dictionary) {
        dictionary.put("DeviceToken", "00000000-54b3-e7c7-0000-000046bffd97");
        String cat = category.toString();
        ParseAnalytics.trackEventInBackground(category.toString(), dictionary);
    }
}
