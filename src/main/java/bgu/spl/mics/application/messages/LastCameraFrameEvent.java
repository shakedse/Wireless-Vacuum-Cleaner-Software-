package bgu.spl.mics.application.messages;

import bgu.spl.mics.*;

public class LastCameraFrameEvent<DetectedObjectsEvent> implements Event
{
    private String CameraId;
    private DetectedObjectsEvent lastFrame;
    
    public LastCameraFrameEvent(String CameraId, DetectedObjectsEvent lastFrame)
    {
        this.CameraId = CameraId;
        this.lastFrame = lastFrame;
    }

    public String getName()
    {
        return CameraId;
    }

    public DetectedObjectsEvent getLastFrame()
    {
        return lastFrame;
    }
}