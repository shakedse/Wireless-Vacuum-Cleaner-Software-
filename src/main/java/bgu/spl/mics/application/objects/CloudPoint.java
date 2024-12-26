package bgu.spl.mics.application.objects;

import java.util.LinkedList;

/**
 * CloudPoint represents a specific point in a 3D space as detected by the LiDAR.
 * These points are used to generate a point cloud representing objects in the environment.
 */
public class CloudPoint 
{

    // TODO: Define fields and methods.
    //FIELDS:

    private double x;
    private double y;
    private LinkedList<Double> cloudPoints;

    public CloudPoint(double x, double y, LinkedList<Double> cloudPoints)
    {
        this.x=x;
        this.y=y;
        this.cloudPoints = cloudPoints;
    }
}
