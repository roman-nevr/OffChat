package org.berendeev.roma.offchat.presentation;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.facebook.stetho.Stetho;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import org.berendeev.roma.offchat.di.ChatComponent;
import org.berendeev.roma.offchat.di.ChatModule;
import org.berendeev.roma.offchat.di.DaggerChatComponent;


public class App extends Application {

    private static boolean mainActivityStarted = false;
    private static ChatComponent chatComponent;

    @Override public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallback());
        initDi();
        initPicasso();
        if (BuildConfig.DEBUG){
            initStetho();
        }
    }

    private void initDi() {
        chatComponent = DaggerChatComponent.builder().chatModule(new ChatModule(this)).build();
    }

    public static ChatComponent getChatComponent() {
        return chatComponent;
    }

    public static boolean isMainActivityStarted(){
        return mainActivityStarted;
    }

    private void initPicasso(){
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, getCacheMaxSize()));
        Picasso built = builder.build();
        if (BuildConfig.DEBUG){
            built.setIndicatorsEnabled(true);
            built.setLoggingEnabled(true);
        }
        Picasso.setSingletonInstance(built);
    }

    private int getCacheMaxSize(){
        return 10 * 1024 * 1024;
    }

    private void initStetho(){
        if(!BuildConfig.DEBUG){
            return;
        }
        // Create an InitializerBuilder
        Stetho.InitializerBuilder initializerBuilder =
                Stetho.newInitializerBuilder(this);

        // Enable Chrome DevTools
        initializerBuilder.enableWebKitInspector(
                Stetho.defaultInspectorModulesProvider(this)
        );

        // Enable command line interface
        initializerBuilder.enableDumpapp(
                Stetho.defaultDumperPluginsProvider(this)
        );

        // Use the InitializerBuilder to generate an Initializer
        Stetho.Initializer initializer = initializerBuilder.build();

        // Initialize Stetho with the Initializer
        Stetho.initialize(initializer);
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
