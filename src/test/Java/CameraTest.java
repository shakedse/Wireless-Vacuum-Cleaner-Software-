
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
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
import bgu.spl.mics.application.services.CameraService;


//need to test - the method which prepares data before sending.
/*

public LinkedList<DetectedObject> getDetectedObjectsAtTick(int tick)
{
    LinkedList<DetectedObject> DetectedObjectsAtTick = new LinkedList<DetectedObject>();
    for(StampedDetectedObjects obj: stampDetectedObjects)
    {
        if(obj.getTime() == tick)
        {
            DetectedObjectsAtTick = obj.getList();
            break;
        }
    }
    return DetectedObjectsAtTick;
}


  //returns the event that matches the certin tick
public DetectedObjectsEvent activateTick(int tick)
{
    LinkedList<DetectedObject> detectedObjects = this.getDetectedObjectsAtTick
    if(!detectedObjects.isEmpty())
    {
 //id error??
        DetectedObjectsEvent event = new DetectedObjectsEvent(tick, detectedObject
        for(int i=0; i<detectedObjects.size(); i++)
        {
            StatisticalFolder.getInstance().incrementNumDetectedObjects();
        }
        return event;
    }   
    return null;
*/


public class CameraTest 
{
    private static class TestMicroService extends CameraService 
    {
        private final CountDownLatch latch;
        private Camera cameraTest;

        public TestMicroService(Camera cameraTest, CountDownLatch latch) 
        {
            super(cameraTest);
            this.latch = latch;
        }
    }
}
