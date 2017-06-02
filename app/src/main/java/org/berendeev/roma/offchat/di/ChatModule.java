package org.berendeev.roma.offchat.di;

import android.content.Context;

import org.berendeev.roma.offchat.data.ChatRepositoryImpl;
import org.berendeev.roma.offchat.data.ImageProviderImpl;
import org.berendeev.roma.offchat.data.LocationHelperImpl;
import org.berendeev.roma.offchat.data.SettingsRepositoryImpl;
import org.berendeev.roma.offchat.data.prefs.LastSeenTimeDataSource;
import org.berendeev.roma.offchat.data.sqlite.DatabaseOpenHelper;
import org.berendeev.roma.offchat.data.sqlite.MessageSqlDataSource;
import org.berendeev.roma.offchat.domain.ChatRepository;
import org.berendeev.roma.offchat.domain.ImageProvider;
import org.berendeev.roma.offchat.domain.LocationHelper;
import org.berendeev.roma.offchat.domain.SettingsRepository;

import java.text.DateFormat;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ChatModule {

    private Context context;

    public ChatModule(Context context) {
        this.context = context;
    }

    @Singleton
    @Provides
    public Context provideContext(){
        return context;
    }

    @Singleton
    @Provides
    public ImageProvider provideImageProvider(Context context){
        return new ImageProviderImpl(context);
    }

    @Singleton
    @Provides
    public DatabaseOpenHelper provideDatabaseOpenHelper(Context context){
        return new DatabaseOpenHelper(context);
    }

    @Singleton
    @Provides
    public LastSeenTimeDataSource provideLastSeenTimeDataSource(Context context){
        return new LastSeenTimeDataSource(context);
    }

    @Singleton
    @Provides
    public MessageSqlDataSource provideMessageSqlDataSource(DatabaseOpenHelper openHelper){
        return new MessageSqlDataSource(openHelper);
    }

    @Singleton
    @Provides
    public ChatRepository provideChatRepository(MessageSqlDataSource sqlDataSource, LastSeenTimeDataSource timeDataSource){
        return new ChatRepositoryImpl(sqlDataSource, timeDataSource);
    }

    @Singleton
    @Provides
    public LocationHelper provideLocationHelper(Context context){
        return new LocationHelperImpl(context);
    }

    @Singleton
    @Provides
    public SettingsRepository provideSettingsRepository(Context context){
        return new SettingsRepositoryImpl(context);
    }

    @Singleton
    @Provides
    public DateFormat provideDateFormat(Context context){
        return android.text.format.DateFormat.getTimeFormat(context);
    }
}
