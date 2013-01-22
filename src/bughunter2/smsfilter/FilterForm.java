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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FilterForm extends Activity
{
    //private static final String TAG = "FilterForm";

    public static final int REQUEST_CODE_MUTATED = 0;

    public static final int RESULT_CODE_NOT_MUTATED = 0;
    public static final int RESULT_CODE_MUTATED = 1;

    public static final String FILTER_NAME_EXTRA = C.PACKAGE_NAME + ".filter_name";

    public static final int ADD_MODE = 1;
    public static final int EDIT_MODE = 2;

    private int mMode = ADD_MODE;

    private EditText mFilterNameEditText;
    private EditText mAddressEditText;
    private LinearLayout mContentFilterLayoutView;

    private String mOldFilterName;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.filter_form);

        mFilterNameEditText = (EditText) findViewById(R.id.filterName);
        mAddressEditText = (EditText) findViewById(R.id.address);

        // NOTE:
        // We're using a LinearLayout as the container for the content filter
        // EditText widgets on purpose, since using a ListView may be
        // problematic due to the ListView recycling mechanism.
        // To the user, however, the performance should generally be equal,
        // whether one uses a LinearLayout or a ListView, since the container
        // should generally only contain a few EditText widgets.
        mContentFilterLayoutView = (LinearLayout) findViewById(R.id.contentFilterLayout);

        Intent intent = getIntent();
        mOldFilterName = intent.getStringExtra(FILTER_NAME_EXTRA);
        String address = null;
        if (mOldFilterName != null)
        {
            mMode = EDIT_MODE;

            Settings settings = new Settings(this);

            address = settings.getFilterAddress(mOldFilterName);

            List<String> contentFilters = settings.getContentFilters(mOldFilterName);
            for (String contentFilter : contentFilters)
                addContentFilter(contentFilter);
        }

        mFilterNameEditText.setText(mOldFilterName);
        mAddressEditText.setText(address);

        mFilterNameEditText.addTextChangedListener(
            new TextWatcher()
            {
                @Override
                public void afterTextChanged(Editable s)
                {
                    // When an error was displayed, and one focuses another
                    // field, and then returns back to the field with the
                    // error, and inserts a character, it may be that the error
                    // isn't automatically cleared, because word completion
                    // hasn't yet completed (i.e., the word still has an
                    // underline). Hence, we have to clear the error manually.
                    mFilterNameEditText.setError(null);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                }
            });

        if (mMode == ADD_MODE)
            setTitle(getString(R.string.newMessageFilter));
        else if (mMode == EDIT_MODE)
            setTitle(getString(R.string.editMessageFilter));
        else
            throw new AssertionError();
    }

    public void onOK(View v)
    {
        String filterName = mFilterNameEditText.getText().toString();
        if (filterName.length() == 0)
        {
            showTextViewError(R.id.filterName, getString(R.string.thisFieldMayNotBeEmpty));
            return;
        }

        String address = mAddressEditText.getText().toString();
        if (address.length() == 0)
        {
            showTextViewError(R.id.address, getString(R.string.thisFieldMayNotBeEmpty));
            return;
        }

        Settings settings = new Settings(this);

        boolean filterNameChanged = false;
        if (mMode == EDIT_MODE)
            filterNameChanged = filterName.equals(mOldFilterName) == false;

        if (mMode == ADD_MODE
            || (mMode == EDIT_MODE
                && filterNameChanged))
        {
            if (settings.isFilterNameUsed(filterName))
            {
                showTextViewError(R.id.filterName, getString(R.string.filterNameAlreadyUsed));
                return;
            }
        }

        List<String> contentFilters = getContentFilters();

        if (mMode == EDIT_MODE)
            settings.deleteFilter(mOldFilterName);
        settings.saveFilter(new Filter(filterName, address, contentFilters));

        setResult(RESULT_CODE_MUTATED);
        finish();
    }

    public void onCancel(View v)
    {
        setResult(RESULT_CODE_NOT_MUTATED);
        finish();
    }

    public void onAddContentFilter(View v)
    {
        addContentFilter(null);
    }

    private void addContentFilter(String text)
    {
        int visibility = View.VISIBLE;
        View contentFilterView = findViewById(R.id.contentFilterView);
        if (contentFilterView.getVisibility() != visibility)
        {
            contentFilterView.setVisibility(visibility);
            Button addContentFilterButton = (Button) findViewById(R.id.addContentFilter);
            addContentFilterButton.setText(getString(R.string.addAnotherContentFilter));
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout row = (LinearLayout) inflater.inflate(R.layout.content_filter_item, null);

        final EditText editText = (EditText) row.findViewById(R.id.content_string);
        if (text != null)
            editText.setText(text);

        Button clearButton = (Button) row.findViewById(R.id.clear);
        clearButton.setOnClickListener(
            new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    editText.setText("");
                }
            });

        mContentFilterLayoutView.addView(row);
    }

    private List<String> getContentFilters()
    {
        List<String> strings = new ArrayList<String>();
        List<EditText> contentFilterWidgets = getContentFilterWidgets();
        for (EditText contentFilterWidget : contentFilterWidgets)
        {
            String s = contentFilterWidget.getText().toString();
            if (s.length() != 0)
                strings.add(s);
        }
        return strings;
    }

    private List<EditText> getContentFilterWidgets()
    {
        List<EditText> list = new ArrayList<EditText>();
        for (int i = 0; i < mContentFilterLayoutView.getChildCount(); ++i)
        {
            LinearLayout row = (LinearLayout) mContentFilterLayoutView.getChildAt(i);
            EditText editText = (EditText) row.findViewById(R.id.content_string);
            list.add(editText);
        }
        return list;
    }

    private void showTextViewError(int id, String error)
    {
        TextView textView = (TextView) findViewById(id);
        textView.setError(error);
        // Focus the field so that (A) the error message is expanded,
        // and (B) the user can fix the error.
        textView.requestFocus();
    }
}
