/*
Author: Jelle Geerts

Usage of the works is permitted provided that this instrument is
retained with the works, so that any entity that uses the works is
notified of this instrument.

DISCLAIMER: THE WORKS ARE WITHOUT WARRANTY.
*/

package bughunter2.smsfilter;

import bughunter2.smsfilter.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MessageViewer extends Activity
{
    //private static final String TAG = "MessageViewer";

    public static final int REQUEST_CODE_MUTATED = 0;

    public static final int RESULT_CODE_NOT_MUTATED = 0;
    public static final int RESULT_CODE_MUTATED = 1;

    public static final String MESSAGE_ID_EXTRA = C.PACKAGE_NAME + ".message_id";

    private static final long MESSAGE_ID_INITIAL = -1;
    private long mMessageID = MESSAGE_ID_INITIAL;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.message_viewer);

        Intent intent = getIntent();
        mMessageID = intent.getLongExtra(MESSAGE_ID_EXTRA, MESSAGE_ID_INITIAL);
        if (mMessageID < 0)
            throw new AssertionError();

        // User is viewing this message. Hence, remove the notification.
        Notifier.cancel(this, String.valueOf(mMessageID), Notifier.NEW_MESSAGE);

        Message message = new Settings(this).getMessage(mMessageID);

        TextView addressTextView = (TextView) findViewById(R.id.address);
        TextView receivedAtTextView = (TextView) findViewById(R.id.receivedAt);
        TextView messageTextView = (TextView) findViewById(R.id.message);

        addressTextView.setText(
            getString(R.string.from)
            + ": " + message.address);
        receivedAtTextView.setText(
            getString(R.string.received)
            + ": " + TimeFormatter.f(this, message.receivedAt, TimeFormatter.FULL_FORMAT));
        messageTextView.setText(message.message);

        setTitle(message.address);
    }

    public void onConfirmDelete(View v)
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
                        delete();
                    }
                })
            .setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void delete()
    {
        new Settings(this).deleteMessage(mMessageID);

        setResult(RESULT_CODE_MUTATED);
        finish();
    }
}
