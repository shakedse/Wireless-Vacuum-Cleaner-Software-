package bgu.spl.mics.application.objects;

import java.util.concurrent.CopyOnWriteArrayList;

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
    private String descripton;
    private CloudPoint[] cloudPoints;

    public TrackedObject (String id,int time, String descripton)
    {
        this.id=id;
        this.descripton=descripton;
        this.Time = time;
        this.cloudPoints = new CloudPoint[10];// איזה גודל המערך צריך להיות?
    }
}


