/*
Author: Jelle Geerts

Usage of the works is permitted provided that this instrument is
retained with the works, so that any entity that uses the works is
notified of this instrument.

DISCLAIMER: THE WORKS ARE WITHOUT WARRANTY.
*/

package bughunter2.smsfilter;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Settings extends SQLiteOpenHelper
{
    private static final String TAG = "Settings";

    public static final String ACTION_NEW_MESSAGE = C.PACKAGE_NAME + ".new_message";

    public static final String ANY_ADDRESS = "#ANY#";

    private static final int DATABASE_VERSION = 1;

    private static final String SETTING_SAVE_MESSAGES   = "save_messages";

    private static final String DATABASE_NAME           = C.PACKAGE_NAME + ".db";

    private static final String SETTINGS_TABLE          = "settings";
    private static final String KEY_ID                  = "id";
    private static final String KEY_KEY                 = "key";
    private static final String KEY_VALUE               = "value";

    private static final String FILTERS_TABLE           = "filters";
    private static final String KEY_NAME                = "name";
    private static final String KEY_ADDRESS             = "address";

    private static final String CONTENT_STRINGS_TABLE   = "content_strings";
    private static final String KEY_FILTER_ID           = "filter_" + KEY_ID;

    private static final String MESSAGES_TABLE          = "messages";
    private static final String KEY_RECEIVED_AT         = "received_at";

    private static final String FILTERS_ORDER_BY        = KEY_NAME + " ASC";
    private static final String MESSAGES_ORDER_BY       = KEY_RECEIVED_AT + " DESC";

    private Context mContext;

    public Settings(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String sql =
            "CREATE TABLE " + SETTINGS_TABLE + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"
            + ", " + KEY_KEY + " TEXT NOT NULL UNIQUE"
            + ", " + KEY_VALUE + " TEXT)";
        Log.d(TAG, "SQL:\n" + sql);
        db.execSQL(sql);

        sql =
            "CREATE TABLE " + FILTERS_TABLE + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"
            + ", " + KEY_NAME + " TEXT NOT NULL UNIQUE"
            + ", " + KEY_ADDRESS + " TEXT NOT NULL)";
        Log.d(TAG, "SQL:\n" + sql);
        db.execSQL(sql);

        sql =
            "CREATE TABLE " + CONTENT_STRINGS_TABLE + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"
            + ", " + KEY_FILTER_ID + " INTEGER REFERENCES " + FILTERS_TABLE + "(" + KEY_ID + ") ON DELETE CASCADE"
            + ", " + KEY_VALUE + " TEXT NOT NULL)";
        Log.d(TAG, "SQL:\n" + sql);
        db.execSQL(sql);

        sql =
            "CREATE TABLE " + MESSAGES_TABLE + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"
                    + ", " + KEY_ADDRESS + " TEXT NOT NULL"
            + ", " + KEY_RECEIVED_AT + " INTEGER NOT NULL"
            + ", " + KEY_VALUE + " TEXT)";
        Log.d(TAG, "SQL:\n" + sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }

    @Override
    public SQLiteDatabase getWritableDatabase()
    {
        SQLiteDatabase db = super.getWritableDatabase();
        if (!db.isReadOnly())
            db.execSQL("PRAGMA foreign_keys = ON");
        return db;
    }

    public String getSetting(String key, String defaultValue)
    {
        String value = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
            SETTINGS_TABLE,
            new String[] { KEY_VALUE },
            KEY_KEY + "=?",
            new String[] { key },
            null, null, null);
        while (cursor.moveToNext())
        {
            if (value != null)
            {
                // There must be only one setting with this key.
                throw new AssertionError();
            }
            value = cursor.getString(0);
        }
        cursor.close();
        db.close();
        if (value == null)
            value = defaultValue;
        return value;
    }

    public void setSetting(String key, String value)
    {
        if (key.length() == 0)
            throw new AssertionError();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_KEY, key);
        values.put(KEY_VALUE, value);
        long rowID = db.replaceOrThrow(SETTINGS_TABLE, null, values);
        db.close();
        if (rowID < 0)
            throw new AssertionError();
    }

    public boolean saveMessages()
    {
        final boolean defaultValue = true;
        return Boolean.parseBoolean(getSetting(SETTING_SAVE_MESSAGES, String.valueOf(defaultValue)));
    }

    public void setSaveMessages(boolean value)
    {
        setSetting(SETTING_SAVE_MESSAGES, Boolean.toString(value));
    }

    public boolean isFilterNameUsed(String name)
    {
        return findFilterByName(name) != null;
    }

    public Filter findFilterByName(String name)
    {
        Filter filter = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
            FILTERS_TABLE,
            new String[] { KEY_NAME, KEY_ADDRESS },
            KEY_NAME + "=?",
            new String[] { name },
            null, null, null);
        while (cursor.moveToNext())
        {
            if (filter != null)
            {
                // There must be only one filter with this name.
                throw new AssertionError();
            }
            String address = cursor.getString(1);
            List<String> contentFilters = getContentFilters(name);
            filter = new Filter(name, address, contentFilters);
        }
        cursor.close();
        db.close();
        return filter;
    }

    // Find filters that match the given address.
    // The search will _not_ return filters that match _any_ address.
    public List<Filter> findFiltersByAddress(String address)
    {
        List<Filter> filters = new ArrayList<Filter>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
            FILTERS_TABLE,
            new String[] { KEY_NAME, KEY_ADDRESS },
            KEY_ADDRESS + "=?",
            new String[] { address },
            null, null,
            FILTERS_ORDER_BY);
        while (cursor.moveToNext())
        {
            String name = cursor.getString(0);
            List<String> contentFilters = getContentFilters(name);
            filters.add(new Filter(name, address, contentFilters));
        }
        cursor.close();
        db.close();
        return filters;
    }

    // Find filters that match the given address.
    // The search will _also_ return filters that match _any_ address.
    public List<Filter> findFiltersForAddress(String address)
    {
        List<Filter> list = findFiltersByAddress(address);
        list.addAll(getWildcardFilters());
        return list;
    }

    public Filter getFilterByName(String name)
    {
        Filter filter = findFilterByName(name);
        if (filter == null)
            throw new AssertionError();
        return filter;
    }

    // Get filters that are meant to match _any_ address.
    public List<Filter> getWildcardFilters()
    {
        return findFiltersByAddress(ANY_ADDRESS);
    }

    public List<Filter> getFilters()
    {
        List<Filter> filters = new ArrayList<Filter>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
            FILTERS_TABLE,
            new String[] { KEY_NAME, KEY_ADDRESS },
            null, null, null, null,
            FILTERS_ORDER_BY);
        while (cursor.moveToNext())
        {
            String name = cursor.getString(0);
            String address = cursor.getString(1);
            List<String> contentFilters = getContentFilters(name);
            filters.add(new Filter(name, address, contentFilters));
        }
        cursor.close();
        db.close();
        return filters;
    }

    public List<String> getContentFilters(String filterName)
    {
        List<String> contentFilters = new ArrayList<String>();
        SQLiteDatabase db = getReadableDatabase();
        String sql =
            "SELECT s." + KEY_VALUE + " FROM " + FILTERS_TABLE + " f"
            + " JOIN " + CONTENT_STRINGS_TABLE + " s"
            + "  ON f." + KEY_ID + "=s." + KEY_FILTER_ID
            + " WHERE f." + KEY_NAME + "=?";
        Cursor cursor = db.rawQuery(sql, new String[] { filterName });
        while (cursor.moveToNext())
        {
            String value = cursor.getString(0);
            contentFilters.add(value);
        }
        cursor.close();
        db.close();
        return contentFilters;
    }

    public String getFilterAddress(String name)
    {
        Filter filter = getFilterByName(name);
        return filter.address;
    }

    public void saveFilter(Filter filter)
    {
        SQLiteDatabase db = getWritableDatabase();
        try
        {
            if (filter.name.length() == 0)
                throw new AssertionError();
            if (filter.address.length() == 0)
                throw new AssertionError();

            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(KEY_NAME, filter.name);
            values.put(KEY_ADDRESS, filter.address);
            long filter_id = db.replaceOrThrow(FILTERS_TABLE, null, values);
            if (filter_id < 0)
                throw new AssertionError();

            for (String s : filter.contentFilters)
            {
                if (s.length() == 0)
                    throw new AssertionError();
                values.clear();
                values.put(KEY_FILTER_ID, filter_id);
                values.put(KEY_VALUE, s);
                db.replaceOrThrow(CONTENT_STRINGS_TABLE, null, values);
            }

            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
            db.close();
        }
    }

    public void deleteFilter(String name)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(
            FILTERS_TABLE,
            KEY_NAME + "=?",
            new String[] { name });
        db.close();
    }

    public Message getMessage(long id)
    {
        Message message = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
            MESSAGES_TABLE,
            new String[] { KEY_ID, KEY_ADDRESS, KEY_RECEIVED_AT, KEY_VALUE },
            KEY_ID + "=?",
            new String[] { String.valueOf(id) },
            null, null, null);
        while (cursor.moveToNext())
        {
            if (message != null)
            {
                // There must be only one message with this identifier.
                throw new AssertionError();
            }
            message = getMessageFromCursor(cursor);
        }
        cursor.close();
        db.close();
        if (message == null)
            throw new AssertionError();
        return message;
    }

    public List<Message> getMessages()
    {
        List<Message> messages = new ArrayList<Message>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
            MESSAGES_TABLE,
            new String[] { KEY_ID, KEY_ADDRESS, KEY_RECEIVED_AT, KEY_VALUE },
            null, null, null, null,
            MESSAGES_ORDER_BY);
        while (cursor.moveToNext())
            messages.add(getMessageFromCursor(cursor));
        cursor.close();
        db.close();
        return messages;
    }

    public long saveMessage(String address, long receivedAt, String message)
    {
        if (address.length() == 0)
            throw new AssertionError();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ADDRESS, address);
        values.put(KEY_RECEIVED_AT, receivedAt);
        values.put(KEY_VALUE, message);
        long messageID = db.replaceOrThrow(MESSAGES_TABLE, null, values);
        db.close();
        if (messageID < 0)
            throw new AssertionError();
        Intent intent = new Intent(ACTION_NEW_MESSAGE);
        mContext.sendBroadcast(intent);
        showMessageNotification(messageID);
        return messageID;
    }

    public void deleteMessage(long id)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(
            MESSAGES_TABLE,
            KEY_ID + "=?",
            new String[] { String.valueOf(id) });
        db.close();

        // If there was any notification for this message, it is no longer
        // relevant. Hence, remove it.
        Notifier.cancel(mContext, String.valueOf(id), Notifier.NEW_MESSAGE);
    }

    private Message getMessageFromCursor(Cursor cursor)
    {
        long id = cursor.getLong(0);
        String address = cursor.getString(1);
        long receivedAt = cursor.getLong(2);
        String value = cursor.getString(3);
        return new Message(id, address, receivedAt, value);
    }

    private void showMessageNotification(long messageID)
    {
        Notification notification = Notifier.build(
                R.drawable.ic_stat_alert,
                mContext.getString(R.string.smsFilterBlockedAMessage));
        Intent messageViewerIntent = new Intent(mContext, MessageViewer.class);
        messageViewerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        messageViewerIntent.putExtra(MessageViewer.MESSAGE_ID_EXTRA, messageID);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, MessageViewer.REQUEST_CODE_MUTATED, messageViewerIntent, PendingIntent.FLAG_ONE_SHOT);
        notification.setLatestEventInfo(
            mContext,
            mContext.getString(R.string.smsFilterBlockedAMessage),
            mContext.getString(R.string.clickToViewBlockedMessage),
            pendingIntent);
        Notifier.notify(mContext, String.valueOf(messageID), Notifier.NEW_MESSAGE, notification);
    }
}
