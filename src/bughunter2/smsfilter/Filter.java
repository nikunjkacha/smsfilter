/*
Author: Jelle Geerts

Usage of the works is permitted provided that this instrument is
retained with the works, so that any entity that uses the works is
notified of this instrument.

DISCLAIMER: THE WORKS ARE WITHOUT WARRANTY.
*/

package bughunter2.smsfilter;

import java.util.List;

public class Filter
{
    String name;
    String address;
    List<String> contentFilters;

    public Filter(String name, String address, List<String> contentFilters)
    {
        this.name = name;
        this.address = address;
        this.contentFilters = contentFilters;
    }
}
