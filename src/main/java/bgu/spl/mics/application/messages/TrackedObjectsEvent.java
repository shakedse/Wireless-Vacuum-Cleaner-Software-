package bgu.spl.mics.application.messages;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.*;

public class TrackedObjectsEvent<TrackedObject> implements Event {
    private LinkedList<TrackedObject> TrackedObjectsList;

    public TrackedObjectsEvent(LinkedList<TrackedObject> TrackedObjectsList)
    {
        this.TrackedObjectsList = TrackedObjectsList;
    }
}
