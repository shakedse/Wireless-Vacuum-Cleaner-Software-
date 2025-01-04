package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU 
{
    private int currentTick;
    private STATUS status;
    private ConcurrentHashMap<Integer, Pose> PoseList;
    private int lastTick;
    private static GPSIMU instance = new GPSIMU(0); 
    
    public GPSIMU (int currentTick)
    {
        this.currentTick=0;
        this.lastTick=0;
        this.status=STATUS.UP;
        this.PoseList = new ConcurrentHashMap<Integer, Pose>();
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
                PoseList.put(pose.getTime(), pose);
            }
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        for(Pose pose : PoseList.values())
        {
            if(pose.getTime() > lastTick)
                lastTick = pose.getTime();
            
        }
    }

    public static GPSIMU getInstance()
    {
        return instance;
    }

    public Pose getPoseAtTick (int tickNow)
    {
        if(lastTick == tickNow)//if we finished the poses
            this.status = STATUS.DOWN;
        return PoseList.get(tickNow);
    }
    
    public void statusDown()
    {
        this.status = STATUS.DOWN;
    }

    public STATUS getStatus()
    {
        return status;
    }
    
    public void setTick(int tick)
    {
        currentTick = tick;
    }
}
