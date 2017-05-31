package org.berendeev.roma.offchat.di;

import android.content.Context;

import org.berendeev.roma.offchat.data.ChatRepositoryImpl;
import org.berendeev.roma.offchat.data.ImageProviderImpl;
import org.berendeev.roma.offchat.domain.ChatRepository;
import org.berendeev.roma.offchat.domain.ImageProvider;

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
    public ImageProvider provideImageProvider(Context context){
        return new ImageProviderImpl(context);
    }

    @Singleton
    @Provides
    public ChatRepository provideChatRepository(){
        return new ChatRepositoryImpl();
    }
}
