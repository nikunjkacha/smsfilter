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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class UI extends ListActivity
{
    private static final String TAG = "UI";

    private int mSaveBlockedMessagesCheckableItemPosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        Settings settings = new Settings(this);

        if (android.os.Build.VERSION.SDK_INT > 18) // android.os.Build.VERSION_CODES.JELLY_BEAN_MR2
            showAppMayNotWorkWarning();

        List<View> views = new ArrayList<View>();

        views.add(SimpleListItem2.build(
            this,
            R.id.filters,
            getString(R.string.messageFilters),
            getString(R.string.viewAndEditMessageFilters)));

        views.add(SimpleListItem2.build(
            this,
            R.id.messages,
            getString(R.string.messages),
            getString(R.string.viewBlockedMessages)));

        CheckableLinearLayout checkableLinearLayout =
                CheckableLinearLayout.build(
                this,
                R.id.saveBlockedMessages,
                getString(R.string.saveMessages),
                getString(R.string.saveMessagesAndShowNotifications));
        mSaveBlockedMessagesCheckableItemPosition = views.size();
        views.add(checkableLinearLayout);

        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ViewsAdapter adapter = new ViewsAdapter(views);
        setListAdapter(adapter);

        if (settings.saveMessages())
            listView.setItemChecked(mSaveBlockedMessagesCheckableItemPosition, true);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        int itemID = v.getId();
        if (itemID == R.id.filters)
        {
            Intent intent = new Intent(this, FilterList.class);
            startActivity(intent);
        }
        else if (itemID == R.id.messages)
        {
            Intent intent = new Intent(this, MessageList.class);
            startActivity(intent);
        }
        else if (itemID == R.id.saveBlockedMessages)
        {
            SparseBooleanArray checkedItemPositions = l.getCheckedItemPositions();
            boolean isChecked = checkedItemPositions.get(mSaveBlockedMessagesCheckableItemPosition);
            new Settings(this).setSaveMessages(isChecked);
        }
    }
    
    /* The add-on doesn't work properly on Android version 4.4 (and possibly
     * won't work on newer versions either).
     * Since Android 4.4, calling abortBroadcast() in our SMSReceiver.java no
     * longer has the desired effect (namely preventing the default SMS
     * application from receiving an SMS message that we wanted to block).
     * See the following links for more information:
     *     https://code.google.com/p/android/issues/detail?id=61684
     *     https://android-developers.blogspot.com/2013/10/getting-your-sms-apps-ready-for-kitkat.html
     */
    private void showAppMayNotWorkWarning()
    {
        SpannableString message = new SpannableString(getString(R.string.appMayNotWork));
        Linkify.addLinks(message, Linkify.WEB_URLS);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.sorry)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();

        TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
        if (messageView == null)
            Log.d(TAG, "Can't make link clickable. messageView == null");
        else
            messageView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
