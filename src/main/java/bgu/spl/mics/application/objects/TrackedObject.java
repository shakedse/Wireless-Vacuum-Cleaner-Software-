package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject 
{
    // TODO: Define fields and methods.
    //fields:
    private String id;
    private int Time;
    private String description;
    private LinkedList<CloudPoint> cloudPoints;

    public TrackedObject (String id,int time, String description, LinkedList<CloudPoint> cloudPoints)
    {
        this.id=id;
        this.description=description;
        this.Time = time;
        this.cloudPoints = cloudPoints;
    }
   // Getters
   public String getId() {
    return id;
}

public int getTime() {
    return Time;
}

public String getDescription() {
    return this.description;
}

public LinkedList<CloudPoint> getCloudPoints() {
    return cloudPoints;
}
}


