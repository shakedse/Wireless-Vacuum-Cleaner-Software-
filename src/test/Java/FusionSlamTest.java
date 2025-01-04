import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import bgu.spl.mics.application.objects.*;


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



 import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

public class FusionSlamTest {

    // we have 2 functions:
    // addNewLandMark which adds the landsMarks from the TrackedObjects
    // this method is using the localToGlobalCordinate to convert th tracked objects cloud points to global coordinate

    private FusionSlam fusionSlam;

    @BeforeEach
    public void setUp() {
        fusionSlam = FusionSlam.getInstance();
    }

    @Test
    public void testAddNewLandMark() {
        // Arrange
        LinkedList<CloudPoint> chairCoordinates = new LinkedList<CloudPoint>();
        CloudPoint firstCloudPoint = new CloudPoint(1.0,2.0);
        CloudPoint secondCloudPoint = new CloudPoint(1.1,2.1);
        chairCoordinates.add(firstCloudPoint);
        chairCoordinates.add(secondCloudPoint);
        TrackedObject trackedObject = new TrackedObject("chair", 1, "Test Object: chair", chairCoordinates);
        Pose pose = new Pose(1, 4, 50, 1);
        fusionSlam.addPose(pose);

        // Act
        fusionSlam.addNewLandMark(trackedObject);

        // Assert
        assertEquals(1, fusionSlam.getLandMarks().size(), "Expected one landmark to be added.");
        LandMark addedLandmark = fusionSlam.getLandMarks().get(0);
        assertEquals(trackedObject.getId(), addedLandmark.getID());
        assertEquals(trackedObject.getDescription(), addedLandmark.getDescription());
    }

    @Test
    public void testChecksIfExistUpdatesExistingLandmark() {
        // Arrange
        LinkedList<CloudPoint> chairCoordinates = new LinkedList<CloudPoint>();
        CloudPoint firstCloudPoint = new CloudPoint(2.0,3.0);
        CloudPoint secondCloudPoint = new CloudPoint(2.2,4.2);
        chairCoordinates.add(firstCloudPoint);
        chairCoordinates.add(secondCloudPoint);
        TrackedObject trackedObjectChair = new TrackedObject("chair", 2, "Test Object: chair", chairCoordinates);

        LinkedList<CloudPoint> doorCoordinates = new LinkedList<CloudPoint>();
        CloudPoint thirdCloudPoint = new CloudPoint(5.0,2.0);
        CloudPoint forthCloudPoint = new CloudPoint(6.0,3.8);
        chairCoordinates.add(thirdCloudPoint);
        chairCoordinates.add(forthCloudPoint);
        TrackedObject trackedObjectDoor = new TrackedObject("door", 2, "Test Object: door", doorCoordinates);

        Pose pose = new Pose(0, 5, 30, 2);
        fusionSlam.addPose(pose);

        // Act
        fusionSlam.ChecksIfExist(trackedObjectChair); // should only update the cordinates
        fusionSlam.ChecksIfExist(trackedObjectDoor); // should add new LandMark

        // Assert
        assertEquals(2, fusionSlam.getLandMarks().size(), "Expected one add new landmarks, not two.");
        LandMark addedLandmark = fusionSlam.getLandMarks().get(0);
        assertEquals(trackedObjectChair.getId(), addedLandmark.getID());
        assertEquals(trackedObjectChair.getDescription(), addedLandmark.getDescription());

        addedLandmark = fusionSlam.getLandMarks().get(1);
        assertEquals(trackedObjectDoor.getId(), addedLandmark.getID());
        assertEquals(trackedObjectDoor.getDescription(), addedLandmark.getDescription());
    }

    @Test
    /*
     * Pose at time 1 = 1, 4, 50
     * Pose at time 2 = 0, 5, 30
     * 
     * angle in rad,sin,cos: 
     * pose 1 = 0.8726 sin = 0.766 cos = 0.649
     * pose 2 = 0.5236 sin = 0.5 cos = 0.866
     * 
     * chair's first LandMark:
     * at pose 1: Local x = 1, y = 2
     *                Global x = (0.649*1) - (0.766*2) + 1 = 0.117
     *                       y = (0.766*1) - (0.649*2) + 4 = 3.468
     *
     * at pose 2: Local x = 2, y = 3
     *                Global x = (0.866*2) - (0.5*3) + 0 = 0.232
     *                       y = (0.5*2) - (0.866*3) + 5 = 3.402
     * 
     * avg = x = 0.2045, y = 3.435
     * 
     */
    public void testLocalToGlobalCoordinate() {
        // Arrange

        CloudPoint ChairCloudPoint1 = fusionSlam.getLandMarks().get(0).getCloudPoints().get(0);
        CloudPoint DoorCloudPoint1 = fusionSlam.getLandMarks().get(0).getCloudPoints().get(0);
        
        double expectedChairX = 0.2045;
        double expectedChiarY = 3.435; 


        // Assert
        assertEquals(expectedChairX, ChairCloudPoint1.getX());
        assertEquals(expectedChiarY, ChairCloudPoint1.getY());

    }
}
