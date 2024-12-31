package bgu.spl.mics.application.messages;

import java.util.LinkedList;
import java.util.List;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.*;

public class TrackedObjectsEvent<TrackedObject> implements Event<TrackedObject> {
    private LinkedList<TrackedObject> TrackedObjectsList;

    public TrackedObjectsEvent(LinkedList<TrackedObject> TrackedObjectsList)
    {
        this.TrackedObjectsList = TrackedObjectsList;
    }
    public TrackedObjectsEvent()
    {
        this.TrackedObjectsList = new LinkedList<TrackedObject>() ;
    }
    public LinkedList<TrackedObject> getTrackedObjectsList()
    {
        return TrackedObjectsList;
    }
    public void addTrackedObject (TrackedObject trackedObject)
    {
        this.TrackedObjectsList.add(trackedObject);
    }
}
