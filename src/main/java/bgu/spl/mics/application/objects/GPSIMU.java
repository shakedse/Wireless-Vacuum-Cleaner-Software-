package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU 
{
    // TODO: Define fields and methods.
    private int currentTick;
    private STATUS status;
    private LinkedList<Pose> PoseList;
    
    public GPSIMU (int currentTick)
    {
        this.currentTick=0;
        this.status=STATUS.DOWN;
        this.PoseList = new LinkedList<Pose>();
    }

    public Pose getPoseAtTick (int tickNow)
    {
        if(PoseList.size() == tickNow)//if we finished the poses
            this.status = STATUS.DOWN;

    return PoseList.get(tickNow-1);
    }
    
    public void setTick(int tick)
    {
        currentTick = tick;
    }


}
