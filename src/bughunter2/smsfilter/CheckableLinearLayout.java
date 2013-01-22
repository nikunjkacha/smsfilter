/*
Author: Jelle Geerts

Usage of the works is permitted provided that this instrument is
retained with the works, so that any entity that uses the works is
notified of this instrument.

DISCLAIMER: THE WORKS ARE WITHOUT WARRANTY.
*/

package bughunter2.smsfilter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CheckableLinearLayout extends LinearLayout implements Checkable
{
    private Checkable mCheckable;

    public CheckableLinearLayout(Context context)
    {
        super(context);
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public static CheckableLinearLayout build(Context context, int id, String title, String summary)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        CheckableLinearLayout v = (CheckableLinearLayout) inflater.inflate(R.layout.checkable_list_item_2, null);
        v.setId(id);
        TextView textView;
        textView = (TextView) v.findViewById(android.R.id.title);
        textView.setText(title);
        textView = (TextView) v.findViewById(android.R.id.summary);
        textView.setText(summary);
        return v;
    }

    public boolean isChecked()
    {
        return mCheckable.isChecked();
    }

    public void setChecked(boolean isChecked)
    {
        mCheckable.setChecked(isChecked);
    }

    public void toggle()
    {
        mCheckable.toggle();
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        findCheckableView(this);
    }

    private boolean findCheckableView(ViewGroup vg)
    {
        for (int i = 0; i < vg.getChildCount(); ++i)
        {
            View child = vg.getChildAt(i);
            if (child instanceof ViewGroup)
            {
                if (findCheckableView((ViewGroup) child))
                    return true;
            }
            if (child instanceof Checkable)
            {
                mCheckable = (Checkable) child;
                return true;
            }
        }
        return false;
    }
}
