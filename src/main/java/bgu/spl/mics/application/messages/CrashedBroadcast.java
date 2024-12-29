package bgu.spl.mics.application.messages;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.*;

public class CrashedBroadcast implements Broadcast
{
    private String crashedID;
    
    public CrashedBroadcast(String crashedID)
    {
        this.crashedID = crashedID;
    }

    public String getCrashedID ()
    {
        return crashedID;
    }
}
