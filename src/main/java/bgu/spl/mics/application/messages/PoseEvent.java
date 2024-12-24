package bgu.spl.mics.application.messages;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.*;

public class PoseEvent implements Event{
    private Pose currentPose;

    public PoseEvent(Pose currentPose)
    {
        this.currentPose = currentPose;
    }
}
