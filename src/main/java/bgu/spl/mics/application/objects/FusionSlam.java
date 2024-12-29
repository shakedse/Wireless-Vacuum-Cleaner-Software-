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
    private LinkedList<LandMark> landMarks;
    private LinkedList<Pose> poses;
    private LinkedList<TrackedObject> trackedObjects;

    private FusionSlam(){
        landMarks = new LinkedList<LandMark>();
        poses = new LinkedList<Pose>();
        trackedObjects = new LinkedList<TrackedObject>();
    }
    private static class FusionSlamHolder {
        private static FusionSlam instance;

        private FusionSlamHolder() {
            instance = new FusionSlam();
        }

        public static FusionSlam getInstance() {
            return instance;
        }
    }
    public void addPose(Pose pose)
    {
        if(!poses.contains(pose))
            poses.add(pose);
    }

    public void addNewLandMark(TrackedObject trackedObject)
    {
        //input - a new tracked object
        LinkedList<CloudPoint> updatedCloudPoints = new LinkedList<CloudPoint>();
        for(CloudPoint p : trackedObject.getCloudPoints())//updating each cloud poin
        {
            for(Pose pose:poses)//finding the cooralte pose
            {
                if(trackedObject.getTime() == pose.getTime())
                {
                    CloudPoint newCloudPoint = localToGlobalCordinate(pose, p);
                    updatedCloudPoints.add(newCloudPoint);
                }
            }
        }
        LandMark newLandMark = new LandMark(trackedObject.getId(), trackedObject.getDescription(), updatedCloudPoints);
        landMarks.add(newLandMark);
        StatisticalFolder.getInstance().incrementNumLandmarks();//increment the number of landmarks
    }

    public void updateOldLandMark(TrackedObject trackedObject)
    {
        //input - an old tracked object
        LinkedList<CloudPoint> updatedCloudPoints = new LinkedList<CloudPoint>();
        for(CloudPoint p : trackedObject.getCloudPoints())//updating each cloud poin
        {
            for(Pose pose:poses) //finding the cooralte pose
            {
                if(trackedObject.getTime() == pose.getTime())
                {
                    float x = ((float) p.getX() + pose.getX())/2;
                    float y = ((float) p.getY() + pose.getY())/2;
                    CloudPoint newCloudPoint = new CloudPoint(x, y);
                    updatedCloudPoints.add(newCloudPoint);
                }
            }
            LandMark remove = landMarks.get(landMarks.indexOf((Object)trackedObject.getId()));//האם הקאסטינג סבבה פה?
            landMarks.remove(remove); //remove the old object
            LandMark newLandMark = new LandMark(trackedObject.getId(), trackedObject.getDescription(), updatedCloudPoints);
            landMarks.add(newLandMark); //add the updated object
        }
    }

    //converts the local coordinate of a cloud point to the global coordinate
    public CloudPoint localToGlobalCordinate(Pose pose, CloudPoint CloudPoint)
    {
        // Convert the yaw angle to radians
        float yawInRadians=0;
        if(0<=pose.getYaw() || pose.getYaw()  <= 360)//in degrees
            yawInRadians = pose.getYaw() * (float)Math.PI / 180;
        else //already in radians
            yawInRadians = pose.getYaw();

        // Compute the cosine and sine of the yaw angle
        float cosYaw = (float)Math.cos(yawInRadians);
        float sinYaw = (float)Math.sin(yawInRadians);

        //Apply the rotation and translation
        float x = cosYaw * (float)CloudPoint.getX() - sinYaw * (float)CloudPoint.getY() + pose.getX();
        float y = (float)(sinYaw * CloudPoint.getX() + cosYaw * CloudPoint.getY() + pose.getY());

        CloudPoint globalCloudPoint = new CloudPoint(x, y);
        return globalCloudPoint;
    }
    public void addTrackedObject(TrackedObject trackedObject)
    {
        if(!trackedObjects.contains(trackedObject))
            trackedObjects.add(trackedObject);
    }   

//get instance
    public FusionSlam getInstance()
    {
        return FusionSlamHolder.getInstance();
    }

//getters
    public LinkedList<LandMark> getLandMarks()
    {
        return landMarks;
    }   
    public LinkedList<Pose> getPoses()
    {
        return poses;
    }
    public LinkedList<TrackedObject> getTrackedObjects()
    {
        return trackedObjects;
    }

}
