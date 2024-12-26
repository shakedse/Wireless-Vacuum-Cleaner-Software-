package bgu.spl.mics.application.objects;
import java.util.LinkedList;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam 
{
    
    // Singleton instance holder
    //fields:
    private LandMark[] landMarks;
    private LinkedList<Pose> poses;
    private FusionSlam FusionSlam = new FusionSlam();

    private FusionSlam(){
        landMarks = new LandMark[10];
        poses = new LinkedList<Pose>();
    }

    public FusionSlam getInstance()
    {
        return FusionSlam;
    }

    public void addPose(Pose toAdd)
    {
        poses.add(toAdd);
    }
    private static class FusionSlamHolder
    {
        // TODO: Implement singleton instance logic.
        
    }
}
