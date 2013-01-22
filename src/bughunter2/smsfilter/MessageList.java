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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MessageList extends Activity
{
    //private static final String TAG = "Messages";

    private MessageListArrayAdapter mAdapter;

    private ListView mListView;

    private BroadcastReceiver mReceiver;

    private class MessageListArrayAdapter extends ArrayAdapter<Message>
    {
        public MessageListArrayAdapter(Context context, List<Message> messages)
        {
            super(context, 0, messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder = null;
            View v = convertView;

            if (v == null)
            {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.message_list_item, null);
                holder = new ViewHolder();
                holder.addressTextView = (TextView) v.findViewById(R.id.address);
                holder.messageTextView = (TextView) v.findViewById(android.R.id.message);
                holder.receivedAtTextView = (TextView) v.findViewById(R.id.receivedAt);
                v.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) v.getTag();
            }

            Message message = getItem(position);
            holder.addressTextView.setText(message.address);
            holder.messageTextView.setText(message.message);
            holder.receivedAtTextView.setText(
                TimeFormatter.f(
                    MessageList.this, message.receivedAt, TimeFormatter.SHORT_FORMAT));

            return v;
        }

        private class ViewHolder
        {
            TextView addressTextView;
            TextView messageTextView;
            TextView receivedAtTextView;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.messages));
        setContentView(R.layout.message_list);

        mListView = (ListView) findViewById(R.id.messageList);

        List<Message> messages = new ArrayList<Message>();
        mAdapter = new MessageListArrayAdapter(this, messages);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(
            new OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    Message message = mAdapter.getItem(position);
                    startViewerActivity(message.id);
                }
            });

        registerForContextMenu(mListView);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Messages may have been removed via the notification, hence we simply
        // always refresh the message list.
        refreshList();

        mReceiver =
            new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    refreshList();
                }
            };
        registerReceiver(mReceiver, new IntentFilter(Settings.ACTION_NEW_MESSAGE));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (!(v instanceof ListView))
            throw new AssertionError();
        menu.setHeaderTitle(R.string.messageOptions);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.message_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int adapterPosition = info.position;
        final long messageID = mAdapter.getItem(adapterPosition).id;
        int menuItemID = item.getItemId();
        if (menuItemID == R.id.view)
        {
            startViewerActivity(messageID);
            return true;
        }
        else if (menuItemID == R.id.delete)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.delete)
                .setMessage(R.string.messageWillBeDeleted)
                .setPositiveButton(
                    R.string.delete,
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            new Settings(MessageList.this).deleteMessage(messageID);
                            mAdapter.remove(mAdapter.getItem(adapterPosition));
                            mAdapter.notifyDataSetChanged();
                        }
                    })
                .setNegativeButton(android.R.string.cancel, null);
            builder.show();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == MessageViewer.REQUEST_CODE_MUTATED)
        {
            if (resultCode == MessageViewer.RESULT_CODE_MUTATED)
                refreshList();
        }
    }

    private void startViewerActivity(long messageID)
    {
        Intent intent = new Intent(this, MessageViewer.class);
        intent.putExtra(MessageViewer.MESSAGE_ID_EXTRA, messageID);
        startActivityForResult(intent, MessageViewer.REQUEST_CODE_MUTATED);
    }

    private void refreshList()
    {
        Settings settings = new Settings(this);
        List<Message> messages = settings.getMessages();

        mAdapter.clear();
        for (Message message : messages)
            mAdapter.add(message);
        mAdapter.notifyDataSetChanged();
    }

    public void onConfirmDeleteAll(View v)
    {
        if (mAdapter.getCount() == 0)
        {
            Toast.makeText(this, R.string.noMessagesToDelete, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.delete)
            .setMessage(R.string.allMessagesWillBeDeleted)
            .setPositiveButton(
                R.string.delete,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        deleteAll();
                    }
                })
            .setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void deleteAll()
    {
        if (mAdapter.getCount() == 0)
        {
            // Condition should've been handled by confirmDeleteAll().
            throw new AssertionError();
        }

        Settings settings = new Settings(this);
        for (int i = 0; i < mAdapter.getCount(); ++i)
        {
            Message message = mAdapter.getItem(i);
            settings.deleteMessage(message.id);
        }
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }
}
