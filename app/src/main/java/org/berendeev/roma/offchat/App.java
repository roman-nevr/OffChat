package org.berendeev.roma.offchat;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import org.berendeev.roma.offchat.app.MainActivity;


public class App extends Application {

    private static boolean mainActivityStarted = false;

    @Override public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallback());
    }

    public static boolean isMainActivityStarted(){
        return mainActivityStarted;
    }

    private class MyActivityLifecycleCallback implements ActivityLifecycleCallbacks {

        @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override public void onActivityStarted(Activity activity) {
            if (activity instanceof MainActivity){
                mainActivityStarted = true;
            }
        }

        @Override public void onActivityResumed(Activity activity) {

        }

        @Override public void onActivityPaused(Activity activity) {

        }

        @Override public void onActivityStopped(Activity activity) {
            if (activity instanceof MainActivity){
                mainActivityStarted = false;
            }
        }

        @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override public void onActivityDestroyed(Activity activity) {

        }
    }
}
