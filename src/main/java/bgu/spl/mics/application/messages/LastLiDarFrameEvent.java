package bgu.spl.mics.application.messages;

import bgu.spl.mics.*;

public class LastLiDarFrameEvent<TrackedObjectsEvent> implements Event
{
    private String LidarWorker;
    private TrackedObjectsEvent lastFrame;
    
    public LastLiDarFrameEvent(String LidarWorker, TrackedObjectsEvent lastFrame)
    {
        this.LidarWorker = LidarWorker;
        this.lastFrame = lastFrame;
    }

    public String getName()
    {
        return LidarWorker;
    }

    public TrackedObjectsEvent getLastFrame()
    {
        return lastFrame;
    }
}