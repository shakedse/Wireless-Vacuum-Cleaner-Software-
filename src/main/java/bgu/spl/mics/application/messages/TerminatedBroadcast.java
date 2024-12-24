package bgu.spl.mics.application.messages;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.*;

public class TerminatedBroadcast implements Broadcast {
    private MicroService terminated;

    public TerminatedBroadcast(MicroService terminated)
    {
        this.terminated = terminated;
    }
}
