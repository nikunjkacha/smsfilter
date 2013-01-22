/*
Author: Jelle Geerts

Usage of the works is permitted provided that this instrument is
retained with the works, so that any entity that uses the works is
notified of this instrument.

DISCLAIMER: THE WORKS ARE WITHOUT WARRANTY.
*/

package bughunter2.smsfilter;

public class Message
{
    long id;
    String address;
    long receivedAt;
    String message;

    Message(long id, String address, long receivedAt, String message)
    {
        this.id = id;
        this.address = address;
        this.receivedAt = receivedAt;
        this.message = message;
    }
}
