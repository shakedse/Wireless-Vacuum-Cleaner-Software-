package bgu.spl.mics.application.messages;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.*;

public class CrashedBroadcast implements Broadcast{
    private MicroService crashed;
    
    public CrashedBroadcast(MicroService crashed)
    {
        this.crashed = crashed;
    }
}
