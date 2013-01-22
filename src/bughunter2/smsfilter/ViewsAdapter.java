/*
Author: Jelle Geerts

Usage of the works is permitted provided that this instrument is
retained with the works, so that any entity that uses the works is
notified of this instrument.

DISCLAIMER: THE WORKS ARE WITHOUT WARRANTY.
*/

package bughunter2.smsfilter;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ViewsAdapter extends BaseAdapter
{
    private List<View> mViews;

    ViewsAdapter(List<View> views)
    {
        super();
        mViews = views;
    }

    @Override
    public int getCount()
    {
        return mViews.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mViews.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return mViews.get(position);
    }

    @Override
    public int getViewTypeCount()
    {
        return getCount();
    }
}
