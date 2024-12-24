package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU 
{
    // TODO: Define fields and methods.
    private int currentTick;
    private STATUS status;
    private List<Pose> PoseList;
    
     public GPSIMU (int currentTick)
 {
     this.currentTick=currentTick;
     this.status=STATUS.DOWN;
     this.PoseList = new CopyOnWriteArrayList <Pose>();//?????
 }
}
