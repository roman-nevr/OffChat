package org.berendeev.roma.offchat.di;

import org.berendeev.roma.offchat.domain.ChatRepository;
import org.berendeev.roma.offchat.domain.ImageProvider;
import org.berendeev.roma.offchat.domain.SettingsRepository;
import org.berendeev.roma.offchat.presentation.MainActivity;
import org.berendeev.roma.offchat.presentation.adapter.ChatAdapter;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;

@Singleton
@Component(modules = ChatModule.class)
public interface ChatComponent {
    ChatRepository chatRepository();

    ImageProvider imageProvider();

    SettingsRepository settingsRepository();

    void inject(MainActivity activity);

    void inject(ChatAdapter adapter);
}
