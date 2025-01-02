package bgu.spl.mics.application.messages;

import java.util.LinkedList;
import java.util.List;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.*;

public class TrackedObjectsEvent implements Event<Boolean> {
    private LinkedList<TrackedObject> TrackedObjectsList;
    private String ID;
    private int time;

    public TrackedObjectsEvent(String id, int time) {
        this.ID = id;
        this.time = time;
        this.TrackedObjectsList = new LinkedList<TrackedObject>();
    }
    public TrackedObjectsEvent(String id, int time, TrackedObject trackedObject) {
        this.ID = id;
        this.time = time;
        this.TrackedObjectsList = new LinkedList<TrackedObject>();
        this.TrackedObjectsList.add(trackedObject);
    }

    public int getTime() {
        return time;
    }

    public LinkedList<TrackedObject> getTrackedObjectsList() {
        return TrackedObjectsList;
    }

    public void addTrackedObject(TrackedObject trackedObject) {
        this.TrackedObjectsList.add(trackedObject);
    }

    public void setTrackedObjectsList(LinkedList<TrackedObject> TrackedObjectsList) {
        this.TrackedObjectsList = TrackedObjectsList;
    }
}
