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
        doorCoordinates.add(thirdCloudPoint);
        doorCoordinates.add(forthCloudPoint);
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
     * pose 1 = 0.8726 sin = 0.766 cos = 0.6428
     * pose 2 = 0.5236 sin = 0.5 cos = 0.866
     * 
     * chair's first LandMark:
     * at pose 1: Local x = 1, y = 2
     *                Global x = (0.6428*1) - (0.766*2) + 1 = 0.1108
     *                       y = (0.766*1) + (0.6428*2) + 4 = 6.0516
     * at pose 2: Local x = 2, y = 3
     *                Global x = (0.866*2) - (0.5*3) + 0 = 0.232
     *                       y = (0.5*2) + (0.866*3) + 5 = 8.589
     * avg = x = 0.1714, y = 3.4412
     * 
     * chair's second LandMark:
     * at pose 1: Local x = 1.1, y = 2.1
     *                Global x = (0.6428*1.1) - (0.766*2.1) + 1 = 0.09849
     *                       y = (0.766*1.1) + (0.6428*2.1) + 4 = 6.19248
     * at pose 2: Local x = 2.2, y = 4.2
     *                Global x = (0.866*2.2) - (0.5*4.2) + 0 = -0.1948
     *                       y = (0.5*2.2) + (0.866*4.2) + 5 = 9.7372
     * avg = x = -0.04815, y = 7.96484
     * 
     * door's first LandMark:
     * at pose 2: Local x = 5, y = 2
     *                Global x = (0.866*5) - (0.5*2) + 0 = 3.33
     *                       y = (0.5*5) + (0.866*2) + 5 = 9.232
     * 
     * door's second LandMark:
     * at pose 2: Local x = 6, y = 3.8
     *                Global x = (0.866*6) - (0.5*3.8) + 0 = 3.296
     *                       y = (0.5*6) + (0.866*3.8) + 5 = 11.2908
     * 
     */
    public void testLocalToGlobalCoordinate() {
        // Arrange

        CloudPoint ChairCloudPoint1 = fusionSlam.getLandMarks().get(0).getCloudPoints().get(0);
        CloudPoint ChairCloudPoint2 = fusionSlam.getLandMarks().get(0).getCloudPoints().get(1);
        CloudPoint DoorCloudPoint1 = fusionSlam.getLandMarks().get(1).getCloudPoints().get(0);
        CloudPoint DoorCloudPoint2 = fusionSlam.getLandMarks().get(1).getCloudPoints().get(1);

        double expectedChairX1 = 0.1714;
        double expectedChairY1 = 7.3248; 
        double expectedChairX2 = -0.04815;
        double expectedChairY2 = 7.96484; 

        double expectedDoorX1 = 3.33;
        double expectedDoorY1 = 9.232;
        double expectedDoorX2 = 3.296;
        double expectedDoorY2 = 11.2908;  

        // Assert
        assertEquals(expectedChairX1, ChairCloudPoint1.getX(), 0.001, "the X cord are wrong");
        assertEquals(expectedChairY1, ChairCloudPoint1.getY(), 0.001, "the Y cord are wrong");
        assertEquals(expectedChairX2, ChairCloudPoint2.getX(), 0.001, "the X cord are wrong");
        assertEquals(expectedChairY2, ChairCloudPoint2.getY(), 0.001, "the Y cord are wrong");

        assertEquals(expectedDoorX1, DoorCloudPoint1.getX(), 0.001, "the X cord are wrong");
        assertEquals(expectedDoorY1, DoorCloudPoint1.getY(), 0.001, "the Y cord are wrong");
        assertEquals(expectedDoorX2, DoorCloudPoint2.getX(), 0.001, "the X cord are wrong");
        assertEquals(expectedDoorY2, DoorCloudPoint2.getY(), 0.001, "the Y cord are wrong");

    }
}
