package bgu.spl.mics.application.objects;

import java.util.List;
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
    private LinkedBlockingQueue<Pose> PoseList;
    
    public GPSIMU (int currentTick)
    {
        this.currentTick=0;
        this.status=STATUS.DOWN;
        this.PoseList = new LinkedBlockingQueue<Pose>();//?????
    }

    public Pose getPoseAtTick()
    {
        for(Pose p: PoseList)
        {
            if(p.getTime() == currentTick)
            {
                return p;
            }
        }
        return null;
    }
    
    public void setTick(int tick)
    {
        currentTick = tick;
    }


}
