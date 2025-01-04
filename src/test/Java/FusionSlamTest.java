import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import javax.sql.StatementEvent;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Event;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DetectedObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.*;

/*
 * 
 * public void addNewLandMark(TrackedObject trackedObject) 
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

 */


public class FusionSlamTest {
    // we have 2 functions:
    // addNewLandMark which adds    
}
