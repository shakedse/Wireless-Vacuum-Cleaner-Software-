package bgu.spl.mics.application.objects;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bgu.spl.mics.application.GurionRockRunner.SystemData;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectedObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;


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

    
    private FusionSlam() {
        landMarks = new LinkedList<LandMark>();
        poses = new LinkedList<Pose>();
        waitingTrackedObjects = new LinkedList<TrackedObject>();

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
        System.out.println("new LandMark; id:" + trackedObject.getId() + "des: " + trackedObject.getDescription());
        LandMark newLandMark = new LandMark(trackedObject.getId(), trackedObject.getDescription(), updatedCloudPoints);
        landMarks.add(newLandMark);
        StatisticalFolder.getInstance().incrementNumLandmarks();// increment the number of landmarks
    }

    public void ChecksIfExist(TrackedObject trackedObject) {
        boolean found = false;
        for (LandMark landMark : landMarks) {
            if (landMark.getID().equals(trackedObject.getId())) {
                System.out.println("LandMark: " + landMark.getID());
                System.out.println("tracked: " + trackedObject.getId());
                found = true;
                break;
            }
        }
        if(found)
        {
            System.out.println("update");
            updateOldLandMark(trackedObject);
        }
            
        else
        {
            System.out.println("new");
            addNewLandMark(trackedObject);
        }
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
        System.out.println("create output");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SystemData systemData = new SystemData(StatisticalFolder.getInstance().getSystemRunTime()
                                                , StatisticalFolder.getInstance().getNumDetectedObjects()
                                                , StatisticalFolder.getInstance().getNumTrackedObjects()
                                                , StatisticalFolder.getInstance().getNumLandmarks()
                                                , new HashMap<>());
        for(LandMark landMark : getLandMarks()) {
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

    public class ErrorOutput {

        private String faultySensor;
        private Map<String, DetectedObjectsEvent> lastCamerasFrame;
        private Map<Integer, TrackedObjectsEvent> lastLiDarWorkerTrackersFrame;
        private List<Pose> poses;
        private SystemData statistics;
    
        // Constructor
        public ErrorOutput(String faultySensor, Map<String, DetectedObjectsEvent> lastCamerasFrame,
                           Map<Integer, TrackedObjectsEvent> lastLiDarWorkerTrackersFrame, List<Pose> poses,
                           SystemData statistics) {
            this.faultySensor = faultySensor;
            this.lastCamerasFrame = lastCamerasFrame;
            this.lastLiDarWorkerTrackersFrame = lastLiDarWorkerTrackersFrame;
            this.poses = poses;
            this.statistics = statistics;
        }
    }

    public void errorOutPut(CrashedBroadcast crashed)
    {
        System.out.println("create output");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SystemData Stats = new SystemData(StatisticalFolder.getInstance().getSystemRunTime()
                                        , StatisticalFolder.getInstance().getNumDetectedObjects()
                                        , StatisticalFolder.getInstance().getNumTrackedObjects()
                                        , StatisticalFolder.getInstance().getNumLandmarks()
                                        , new HashMap<>());
        for(LandMark landMark : getLandMarks()) {
            Stats.addLandmark(landMark.getID(), landMark);
        }
        ErrorOutput toWrite = new ErrorOutput(crashed.getCrashedID(), StatisticalFolder.getInstance().getCamerasLastFrames(), StatisticalFolder.getInstance().getLiDarLastFrames(), poses, Stats);
        try (FileWriter writer = new FileWriter("output.json")) {
            // Serialize Java objects to JSON file
            gson.toJson(toWrite, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }      

}
