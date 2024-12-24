package bgu.spl.mics.application.objects;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
    private String id;
    private int Time;
    private List<CloudPoint> cloudPoints;

    public StampedCloudPoints (String id,int Time)
    {
        this.id=id;
        this.Time=Time;
        this.cloudPoints= new CopyOnWriteArrayList<CloudPoint> () ;
    }

}
