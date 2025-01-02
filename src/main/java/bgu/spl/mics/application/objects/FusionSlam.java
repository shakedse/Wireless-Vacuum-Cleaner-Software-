package bgu.spl.mics.application.objects;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bgu.spl.mics.application.GurionRockRunner.SystemData;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping
 * (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update
 * a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam
 * exists.
 */
public class FusionSlam {

    // Singleton instance holder
    // fields:
    private LinkedList<LandMark> landMarks;
    private LinkedList<Pose> poses;
    private LinkedList<TrackedObject> waitingTrackedObjects;
    private boolean EarlyFinish;

    private FusionSlam() {
        landMarks = new LinkedList<LandMark>();
        poses = new LinkedList<Pose>();
        waitingTrackedObjects = new LinkedList<TrackedObject>();
        EarlyFinish = false;
    }

    public void setEarlyFinish() {
        this.EarlyFinish = true;
    }

    public boolean getEarlyFinish() {
        return this.EarlyFinish;
    }
    public LinkedList<TrackedObject> getWaitingObjects() {
        return waitingTrackedObjects;
    }

    private static class FusionSlamHolder {
        private static FusionSlam instance = new FusionSlam();

        private FusionSlamHolder() {
        }

        public static FusionSlam getInstance() {
            return instance;
        }
    }

    public void addPose(Pose pose) {
        if (!poses.contains(pose))
            poses.add(pose);
    }

    public void addNewLandMark(TrackedObject trackedObject) 
    {
        // input - a new tracked object
        LinkedList<CloudPoint> updatedCloudPoints = new LinkedList<CloudPoint>();
        for (Pose pose : poses)// finding the cooralte pose
        {
            if (trackedObject.getTime() == pose.getTime()) // if the pose is found
            {
                for (CloudPoint p : trackedObject.getCloudPoints())// updating each cloud point
                {
                    CloudPoint newCloudPoint = localToGlobalCordinate(pose, p);
                    updatedCloudPoints.add(newCloudPoint);
                }
            }
        }
        LandMark newLandMark = new LandMark(trackedObject.getId(), trackedObject.getDescription(), updatedCloudPoints);
        landMarks.add(newLandMark);
        System.out.println("new Landmark added:" + newLandMark.getID());
        StatisticalFolder.getInstance().incrementNumLandmarks();// increment the number of landmarks
    }

    public void ChecksIfExist(TrackedObject trackedObject) {
        boolean found = false;
        for (LandMark landMark : landMarks) {
            if (landMark.getID().equals(trackedObject.getId())) {
                found = true;
                break;
            }
        }
        if(found)
            updateOldLandMark(trackedObject);
        else
            addNewLandMark(trackedObject);
    }

    public void updateOldLandMark(TrackedObject trackedObject) {
        // input - an old tracked object
        LinkedList<CloudPoint> updatedCloudPoints = new LinkedList<CloudPoint>();
        for (Pose pose : poses)// finding the cooralte pose
        {
            if (trackedObject.getTime() == pose.getTime()) {
                for (CloudPoint p : trackedObject.getCloudPoints())// updating each cloud point
                {
                    CloudPoint newCloudPoint = localToGlobalCordinate(pose, p);
                    updatedCloudPoints.add(newCloudPoint);
                }
            }
        }
        for (LandMark landMark : landMarks) {
            if (landMark.getID().equals(trackedObject.getId())) {
                landMark.setAvgCloudPoint(updatedCloudPoints);
            }
        }


        /*
         * for(CloudPoint p : trackedObject.getCloudPoints()) //finding the cooralte
         * pose
         * {
         * double x = (p.getX() + cloudPoint.getX())/2;
         * double y = (p.getY() + cloudPoint.getY())/2;
         * CloudPoint newCloudPoint = new CloudPoint(x, y);
         * updatedCloudPoints.add(newCloudPoint);
         * }
         * LandMark remove =
         * landMarks.get(landMarks.indexOf((Object)trackedObject.getId()));//האם
         * הקאסטינג סבבה פה?
         * landMarks.remove(remove); //remove the old object
         * LandMark newLandMark = new LandMark(trackedObject.getId(),
         * trackedObject.getDescription(), updatedCloudPoints);
         * landMarks.add(newLandMark); //add the updated object
         */
    }

    // converts the local coordinate of a cloud point to the global coordinate
    public CloudPoint localToGlobalCordinate(Pose pose, CloudPoint CloudPoint) {
        // Convert the yaw angle to radians
        float yawInRadians = 0;
        if (0 <= pose.getYaw() || pose.getYaw() <= 360)// in degrees
            yawInRadians = pose.getYaw() * (float) Math.PI / 180;
        else // already in radians
            yawInRadians = pose.getYaw();

        // Compute the cosine and sine of the yaw angle
        float cosYaw = (float) Math.cos(yawInRadians);
        float sinYaw = (float) Math.sin(yawInRadians);

        // Apply the rotation and translation
        float x = cosYaw * (float) CloudPoint.getX() - sinYaw * (float) CloudPoint.getY() + pose.getX();
        float y = (float) (sinYaw * CloudPoint.getX() + cosYaw * CloudPoint.getY() + pose.getY());

        CloudPoint globalCloudPoint = new CloudPoint(x, y);
        return globalCloudPoint;
    }


    // get instance
    public static FusionSlam getInstance() {
        return FusionSlamHolder.getInstance();
    }

    // getters
    public LinkedList<LandMark> getLandMarks() {
        return landMarks;
    }

    public LinkedList<Pose> getPoses() {
        return poses;
    }


    public void buildOutput()
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SystemData systemData = new SystemData(StatisticalFolder.getInstance().getSystemRunTime()
                                                , StatisticalFolder.getInstance().getNumDetectedObjects()
                                                , StatisticalFolder.getInstance().getNumTrackedObjects()
                                                , StatisticalFolder.getInstance().getNumLandmarks()
                                                , new HashMap<>());
            for(LandMark landMark : getLandMarks()) {
                System.out.println(landMark.getID());
                systemData.addLandmark(landMark.getID(), landMark);
            }
            try (FileWriter writer = new FileWriter("output.json")) {
            // Serialize Java objects to JSON file
                gson.toJson(systemData, writer);
            }
    
            catch (IOException e) {
                e.printStackTrace();
            }
    }

}
