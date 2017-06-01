package org.berendeev.roma.offchat;

import android.content.Context;

import org.berendeev.roma.offchat.data.sqlite.DatabaseOpenHelper;
import org.berendeev.roma.offchat.data.sqlite.MessageSqlDataSource;
import org.berendeev.roma.offchat.domain.model.Image;
import org.berendeev.roma.offchat.domain.model.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest= Config.NONE)
public class SqlTest {

    private Context context;
    private MessageSqlDataSource dataSource;
    private long id;

    @Before
    public void before(){
        context = RuntimeEnvironment.application.getApplicationContext();
        DatabaseOpenHelper openHelper = new DatabaseOpenHelper(context);
        dataSource = new MessageSqlDataSource(openHelper);
    }

    @Test
    public void save_message_test(){
        dataSource.saveMessage(getMessage("test"));
        dataSource.saveMessage(getMessage("test2"));

//        printAll();

        Assert.assertTrue(dataSource.getAllMessages().size() == 2);
        printPassed("save_message_test");
    }

    @Test
    public void get_all_after_time(){
        dataSource.saveMessage(getMessage("test"));
        dataSource.saveMessage(getMessage("test2"));

        long time = getCurrentTime();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String text_after = "test_after_1";

        dataSource.saveMessage(getMessage(text_after));

        List<Message> allAfterTime = dataSource.getAllAfterTime(time);

        Assert.assertTrue(allAfterTime.get(0).text().equals(text_after));

        printPassed("get_all_after_time");
    }

    private void printPassed(String test) {
        System.out.println("test " + test + " passed");
    }

    private Message getMessage(String text){
        return Message.create(id++, getCurrentTime(), Message.Owner.me, text, Image.create(""));
    }

    private long getCurrentTime(){
        return System.currentTimeMillis();
    }

    private void printAll(){
        List<Message> messages = dataSource.getAllMessages();
        System.out.println("All messages");
        System.out.println(messages);
    }
}
