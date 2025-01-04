
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
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;

public class CameraTest {
    private LinkedList<DetectedObject> detectedObjectsList1 = new LinkedList<DetectedObject>();
    private LinkedList<DetectedObject> detectedObjectsList2 = new LinkedList<DetectedObject>();
    private LinkedList<DetectedObject> detectedObjectsList3 = new LinkedList<DetectedObject>();
    private LinkedList<DetectedObject> detectedObjectsList4 = new LinkedList<DetectedObject>();
    private LinkedList<DetectedObject> detectedObjectsList5 = new LinkedList<DetectedObject>();
    private LinkedList<DetectedObject> detectedObjectsList6 = new LinkedList<DetectedObject>();
    private LinkedList<DetectedObject> detectedObjectsList7 = new LinkedList<DetectedObject>();
    private LinkedList<DetectedObject> detectedObjectsList8 = new LinkedList<DetectedObject>();

    @Test
    public void Test1() {
        // the detected objects
        DetectedObject detectedObject1 = new DetectedObject("1", "person");
        DetectedObject detectedObject2 = new DetectedObject("2", "bottle");
        DetectedObject detectedObject3 = new DetectedObject("3", "dog");
        DetectedObject detectedObject4 = new DetectedObject("4", "chair");
        DetectedObject detectedObject5 = new DetectedObject("5", "mirror");
        DetectedObject detectedObject6 = new DetectedObject("6", "picture");

        // the lists of detected objects:
        // list1:
        detectedObjectsList1.add(detectedObject1);
        detectedObjectsList1.add(detectedObject2);
        detectedObjectsList1.add(detectedObject3);

        // list2:
        detectedObjectsList2.add(detectedObject2);
        detectedObjectsList2.add(detectedObject3);
        detectedObjectsList2.add(detectedObject4);

        // list3:
        detectedObjectsList3.add(detectedObject3);
        detectedObjectsList3.add(detectedObject4);
        detectedObjectsList3.add(detectedObject5);
        detectedObjectsList3.add(detectedObject6);

        // list4:
        detectedObjectsList4.add(detectedObject1);
        detectedObjectsList4.add(detectedObject2);
        detectedObjectsList4.add(detectedObject3);
        detectedObjectsList4.add(detectedObject4);
        detectedObjectsList4.add(detectedObject5);
        detectedObjectsList4.add(detectedObject6);

        // the stamped detected objects:
        // stamped detected objects 1:
        StampedDetectedObjects stampedDetectedObjects1 = new StampedDetectedObjects(1, detectedObjectsList1);

        // stamped detected objects 2:
        StampedDetectedObjects stampedDetectedObjects2 = new StampedDetectedObjects(2, detectedObjectsList2);

        // stamped detected objects 3:
        StampedDetectedObjects stampedDetectedObjects3 = new StampedDetectedObjects(3, detectedObjectsList3);

        // stamped detected objects 4:
        StampedDetectedObjects stampedDetectedObjects4 = new StampedDetectedObjects(4, detectedObjectsList4);

        // checking the method:
        System.out.println("CameraTest.Test1 started");

        LinkedList<StampedDetectedObjects> stampDetectedObjects = new LinkedList<StampedDetectedObjects>();
        stampDetectedObjects.add(stampedDetectedObjects1);
        stampDetectedObjects.add(stampedDetectedObjects2);
        stampDetectedObjects.add(stampedDetectedObjects3);
        stampDetectedObjects.add(stampedDetectedObjects4);

        for (int i = 0; i < stampDetectedObjects.size(); i++) {
            System.out.println("stampDetectedObjects.get(i).getTick() = " + stampDetectedObjects.get(i).getTime());
            for (int j = 0; j < stampDetectedObjects.get(i).getList().size(); j++) {
                System.out.println("stampDetectedObjects.get(i).getDetectedObjects().get(j).getID() = "
                        + stampDetectedObjects.get(i).getList().get(j).getID());
                System.out.println("stampDetectedObjects.get(i).getDetectedObjects().get(j).getDescription() = "
                        + stampDetectedObjects.get(i).getList().get(j).getDescription());
            }
            System.out.println();
        }

        // setting the camera:
        Camera camera1 = new Camera(1, 1, "1", stampDetectedObjects);
        int StatisticalFolderCounter = 0;

        for (int i = 1; i <= camera1.getList().size(); i++) {
            // creating an event for each tick and checking if the detected objects are
            // correct:
            DetectedObjectsEvent event = camera1.activateTick(i);

            // checking if the detected objects are correct:
            LinkedList<DetectedObject> detectedObjects = camera1.getDetectedObjectsAtTick(i);

            if (detectedObjects.size() >= 0) {
                // Making sure the detected objects are the same size:
                assertEquals(detectedObjects.size(), event.getDetectedObjects().size());

                // going over the detected objects and checking if they are the same:
                for (int j = 0; j < detectedObjects.size(); j++) {
                    // checking if the id is same:
                    assertEquals(detectedObjects.get(j).getID(), event.getDetectedObjects().get(j).getID());

                    // checking "ERROR":
                    if ((detectedObjects.get(j).getID()).equals("ERROR")) {
                        assertTrue(camera1.getStatus() == STATUS.ERROR);
                        assertEquals(event.getDetectedObjects().size(), 1);
                        assertEquals("ERROR", event.getDetectedObjects().get(j).getDescription());
                    }

                    // checking if the description is same:
                    assertEquals(detectedObjects.get(j).getDescription(),
                            event.getDetectedObjects().get(j).getDescription());
                }

                // checking the statistical folder:
                int currentNumDetectedObjects = StatisticalFolder.getInstance().getNumDetectedObjects().get();
                StatisticalFolderCounter += stampDetectedObjects.get(i - 1).getList().size();
                assertEquals(StatisticalFolderCounter, currentNumDetectedObjects);
            } else {
                assertNull(event);
            }
        }

        System.out.println("CameraTest.Test1 ended");
    }

    @Test
    public void Test2() {
        // checking the method with an event size of 0 and a detected object "ERROR":
        System.out.println("CameraTest.Test2 started");
        DetectedObject detectedObject1 = new DetectedObject("1", "person");
        DetectedObject detectedObject2 = new DetectedObject("2", "bottle");
        DetectedObject detectedObject3 = new DetectedObject("3", "dog");
        DetectedObject detectedObject4 = new DetectedObject("4", "chair");
        DetectedObject detectedObject5 = new DetectedObject("5", "mirror");
        DetectedObject detectedObject6 = new DetectedObject("6", "picture");
        DetectedObject detectedObject7 = new DetectedObject("6", "picture");
        DetectedObject detectedObject8 = new DetectedObject("7", "phone");
        DetectedObject detectedObject9 = new DetectedObject("8", "curtain");
        DetectedObject detectedObject10 = new DetectedObject("ERROR", "shoes");

        // the lists of detected objects:
        // list1:
        detectedObjectsList5.add(detectedObject1);
        detectedObjectsList5.add(detectedObject2);
        detectedObjectsList5.add(detectedObject3);
        detectedObjectsList5.add(detectedObject8);

        // list2:an empty list

        // list3: list with "ERROR"
        detectedObjectsList6.add(detectedObject1);
        detectedObjectsList6.add(detectedObject2);
        detectedObjectsList6.add(detectedObject3);
        detectedObjectsList6.add(detectedObject7);
        detectedObjectsList6.add(detectedObject10);

        // list4:
        detectedObjectsList8.add(detectedObject1);
        detectedObjectsList8.add(detectedObject2);
        detectedObjectsList8.add(detectedObject3);
        detectedObjectsList8.add(detectedObject4);
        detectedObjectsList8.add(detectedObject5);
        detectedObjectsList8.add(detectedObject6);
        detectedObjectsList8.add(detectedObject7);
        detectedObjectsList8.add(detectedObject8);
        detectedObjectsList8.add(detectedObject9);

        // the stamped detected objects:
        // stamped detected objects 1:
        StampedDetectedObjects stampedDetectedObjects5 = new StampedDetectedObjects(1, detectedObjectsList5);

        // stamped detected objects 2:
        StampedDetectedObjects stampedDetectedObjects6 = new StampedDetectedObjects(3, detectedObjectsList6);

        // stamped detected objects 3: skipping 1 time tick
        StampedDetectedObjects stampedDetectedObjects7 = new StampedDetectedObjects(4, detectedObjectsList7);

        // stamped detected objects 4:
        StampedDetectedObjects stampedDetectedObjects8 = new StampedDetectedObjects(5, detectedObjectsList8);

        // checking the method:
        System.out.println("CameraTest.Test2 started");
        LinkedList<StampedDetectedObjects> stampDetectedObjects2 = new LinkedList<StampedDetectedObjects>();
        stampDetectedObjects2.add(stampedDetectedObjects5);
        stampDetectedObjects2.add(stampedDetectedObjects6);
        stampDetectedObjects2.add(stampedDetectedObjects7);
        stampDetectedObjects2.add(stampedDetectedObjects8);

        // setting the camera:
        Camera camera2 = new Camera(2, 2, "2", stampDetectedObjects2);
        int StatisticalFolderCounter2 = 0;

        for (int i = 1; i <= camera2.getList().size(); i++) {
            // creating an event for each tick and checking if the detected objects are
            // correct:
            DetectedObjectsEvent event = camera2.activateTick(i);
            // checking if the detected objects are correct:
            LinkedList<DetectedObject> detectedObjects = camera2.getDetectedObjectsAtTick(i);
            if (detectedObjects.size() >= 0) {
                // checking "ERROR":
                if (event!=null) 
                {     
                if ((event.getDetectedObjects().get(0).getID()).equals("ERROR")) {
                    assertTrue(camera2.getStatus() == STATUS.ERROR);
                    assertEquals(event.getDetectedObjects().size(), 1);
                    assertEquals("ERROR", event.getDetectedObjects().get(0).getID());
                    boolean foundERRORinList = false;
                    for (int j = 0; j < detectedObjects.size(); j++) {
                        if ((detectedObjects.get(j).getID()).equals("ERROR")) {
                            foundERRORinList = true;
                        }
                    }
                    assertTrue(foundERRORinList);
                    break;
                }

                // Making sure the detected objects are the same size:
                else {
                    assertEquals(detectedObjects.size(), event.getDetectedObjects().size());

                    // going over the detected objects and checking if they are the same:
                    for (int j = 0; j < detectedObjects.size(); j++) {
                        // checking if the id is same:
                        assertEquals(detectedObjects.get(j).getID(), event.getDetectedObjects().get(j).getID());

                        // checking if the description is same:
                        assertEquals(detectedObjects.get(j).getDescription(),
                                event.getDetectedObjects().get(j).getDescription());
                    }
                }
            
            //checking the statistical folder:
            int currentNumDetectedObjects = StatisticalFolder.getInstance().getNumDetectedObjects().get();
            StatisticalFolderCounter2 += stampDetectedObjects2.get(i-1).getList().size();
           assertEquals(StatisticalFolderCounter2, currentNumDetectedObjects);
            }
        }
            else 
            {
                assertNull(event);
            }
        }
        System.out.println("CameraTest.Test2 ended");
    }
}
