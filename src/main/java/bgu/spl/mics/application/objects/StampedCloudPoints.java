package bgu.spl.mics.application.objects;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.LinkedList;
import java.util.List;
/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
    private String id;
    private int Time;
    private List<List<Double>> cloudPoints;

    public StampedCloudPoints (String id,int Time)
    {
        this.id=id;
        this.Time=Time;
        this.cloudPoints = new LinkedList<List<Double>>();
        System.out.println(this.Time +  "hiiiiiiiiiiiiiiiiiiiiiiiiii");
    }
    public String getID()
    {
        return id;
    }
    public int getTime()
    {
        System.out.println("hi");
        return Time;
    }
    public List<List<Double>> getCloudPoints()
    {
        return cloudPoints;
    }

}
