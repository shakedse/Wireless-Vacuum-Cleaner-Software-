package bgu.spl.mics.application.objects;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
    private boolean EarlyFinish;

    private ConcurrentHashMap<String, DetectedObjectsEvent> camerasLastFrames;
    private ConcurrentHashMap<String, TrackedObjectsEvent> LiDarLastFrames;

    private FusionSlam() {
        landMarks = new LinkedList<LandMark>();
        poses = new LinkedList<Pose>();
        waitingTrackedObjects = new LinkedList<TrackedObject>();
        EarlyFinish = false;
        camerasLastFrames = new ConcurrentHashMap<>();
        LiDarLastFrames = new ConcurrentHashMap<>();
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


    public void addLastFrameCamera(String Camera, DetectedObjectsEvent lastFrame)
    {
        camerasLastFrames.putIfAbsent(Camera, lastFrame);
    }

    public void addLastFrameLidar(String Lidar, TrackedObjectsEvent lastFrame)
    {
        LiDarLastFrames.putIfAbsent(Lidar, lastFrame);
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
        System.out.println("create output");
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

    public class ErrorOutput {

        private String faultySensor;
        private Map<String, DetectedObjectsEvent> lastCamerasFrame;
        private Map<String, TrackedObjectsEvent> lastLiDarWorkerTrackersFrame;
        private List<Pose> poses;
        private SystemData statistics;
    
        // Constructor
        public ErrorOutput(String faultySensor, Map<String, DetectedObjectsEvent> lastCamerasFrame,
                           Map<String, TrackedObjectsEvent> lastLiDarWorkerTrackersFrame, List<Pose> poses,
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
            System.out.println(landMark.getID());
            Stats.addLandmark(landMark.getID(), landMark);
        }
        ErrorOutput toWrite = new ErrorOutput(crashed.getCrashedID(), camerasLastFrames, LiDarLastFrames, poses, Stats);
        try (FileWriter writer = new FileWriter("output.json")) {
            // Serialize Java objects to JSON file
            gson.toJson(toWrite, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }      

}
