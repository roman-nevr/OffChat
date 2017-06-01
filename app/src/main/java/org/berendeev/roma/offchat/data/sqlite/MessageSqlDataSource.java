package org.berendeev.roma.offchat.data.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.berendeev.roma.offchat.domain.model.Image;
import org.berendeev.roma.offchat.domain.model.Message;
import org.berendeev.roma.offchat.presentation.BuildConfig;

import java.util.ArrayList;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static org.berendeev.roma.offchat.data.sqlite.DatabaseOpenHelper.CHAT_TABLE;
import static org.berendeev.roma.offchat.data.sqlite.DatabaseOpenHelper.IMAGE;
import static org.berendeev.roma.offchat.data.sqlite.DatabaseOpenHelper.OWNER;
import static org.berendeev.roma.offchat.data.sqlite.DatabaseOpenHelper.TEXT;
import static org.berendeev.roma.offchat.data.sqlite.DatabaseOpenHelper.TIME;

public class MessageSqlDataSource {

    private SQLiteDatabase database;
    private ContentValues contentValues;

    public MessageSqlDataSource(DatabaseOpenHelper openHelper) {
        database = openHelper.getWritableDatabase();
        contentValues = new ContentValues();
    }

    public List<Message> getAllMessages(){
        List<Message> messages = new ArrayList<>();

        String orderBy = TIME + " ASC";
        Cursor cursor = database.query(CHAT_TABLE, null, null, null, null, null, orderBy);

        while (cursor.moveToNext()){
            messages.add(getMessageFromCursor(cursor));
        }
        cursor.close();
        return messages;
    }

    public void saveMessage(Message message) {
        fillContentValues(message);
        database.insertWithOnConflict(CHAT_TABLE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void saveAllMessages(List<Message> messages){
        for (Message message : messages) {
            saveMessage(message);
        }

    }

//    public Message getMess(String link){
//        String selection = String.format("%1s = ?", LINK);
//        String[] selectionArgs = {link};
//        Cursor cursor = null;
//        Message message;
//        cursor = database.query(CHAT_TABLE, null, selection, selectionArgs, null, null, null, null);
//        if(cursor.moveToFirst()){
//            message = getMessageFromCursor(cursor);
//        }else {
//            message = Message.EMPTY;
//        }
//        cursor.close();
//        return message;
//    }

    public void removeAll(){
        int a = database.delete(CHAT_TABLE, "1", null);
        if (BuildConfig.DEBUG){
            System.out.println(a);
        }
    }

//    public boolean isEmpty(){
//        Cursor cursor = database.query(CHAT_TABLE, null, null, null, null, null, null,
//                null);
//        boolean result = !cursor.moveToNext();
//        cursor.close();
//        return result;
//    }

    private void fillContentValues(Message message){
        contentValues.clear();
        contentValues.put(_ID, message.id());
        contentValues.put(TIME, message.time());
        contentValues.put(OWNER, message.owner().name());
        contentValues.put(TEXT, message.text());
        contentValues.put(IMAGE, message.image().source());
    }

    private Message getMessageFromCursor(Cursor cursor){

        int idIndex = cursor.getColumnIndex(_ID);
        int timeIndex = cursor.getColumnIndex(TIME);
        int ownerIndex = cursor.getColumnIndex(OWNER);
        int textIndex = cursor.getColumnIndex(TEXT);
        int imageIndex = cursor.getColumnIndex(IMAGE);
        return Message.create(
                cursor.getLong(idIndex),
                cursor.getLong(timeIndex),
                Message.Owner.valueOf(cursor.getString(ownerIndex)),
                cursor.getString(textIndex),
                Image.create(cursor.getString(imageIndex))
                );
    }


}
