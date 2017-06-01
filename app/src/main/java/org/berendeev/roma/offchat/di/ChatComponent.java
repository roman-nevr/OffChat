package org.berendeev.roma.offchat.di;

import org.berendeev.roma.offchat.domain.ChatRepository;
import org.berendeev.roma.offchat.domain.ImageProvider;
import org.berendeev.roma.offchat.presentation.MainActivity;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;

@Singleton
@Component(modules = ChatModule.class)
public interface ChatComponent {
    ChatRepository chatRepository();

    ImageProvider imageProvider();

    void inject(MainActivity activity);
}
