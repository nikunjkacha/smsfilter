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

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;

public class UI extends ListActivity
{
    //private static final String TAG = "UI";

    private int mSaveBlockedMessagesCheckableItemPosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        Settings settings = new Settings(this);

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
}
