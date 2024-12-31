package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.util.LinkedList;

import com.google.gson.Gson;

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
    private static GPSIMU instance = new GPSIMU(0); 
    
    public GPSIMU (int currentTick)
    {
        this.currentTick=0;
        this.status=STATUS.DOWN;
        this.PoseList = new LinkedList<Pose>();
    }

    // build the data from the json file
    public void buildData(String path)
    {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(path)) 
        {
            Pose[] poses = gson.fromJson(reader, Pose[].class);
            for (Pose pose : poses) 
            {
                PoseList.add(pose);
            }
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    public static GPSIMU getInstance()
    {
        return instance;
    }

    public Pose getPoseAtTick (int tickNow)
    {
        if(PoseList.size() == tickNow)//if we finished the poses
            this.status = STATUS.DOWN;
        return PoseList.get(tickNow);
    }
    
    public void setTick(int tick)
    {
        currentTick = tick;
    }
}
