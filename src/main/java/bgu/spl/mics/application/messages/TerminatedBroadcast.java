package bgu.spl.mics.application.messages;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.*;

public class TerminatedBroadcast implements Broadcast {
    private String terminatedID;

    public TerminatedBroadcast(String terminatedID)
    {
        this.terminatedID = terminatedID;
    }

    public String getTerminatedID()
    {
        return terminatedID;
    }
}
