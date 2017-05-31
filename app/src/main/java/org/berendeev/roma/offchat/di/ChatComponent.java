package org.berendeev.roma.offchat.di;

import org.berendeev.roma.offchat.domain.ChatRepository;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;

@Singleton
@Component(modules = ChatModule.class)
public interface ChatComponent {
    ChatRepository chatRepository();
}
