package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectedObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;

import java.util.LinkedList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectedObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService 
{
    private final Camera camera;
    private int time;
    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) 
    {
        super("cameraService" + camera.getID());
        this.camera = camera;
        this.time = 0;
        // TODO Implement this
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectedObjectsEvents.
     */
    @Override
    protected void initialize() {
        // TODO Implement this

        subscribeBroadcast(TickBroadcast.class ,(TickBroadcast tick) ->{
            time = tick.getTick(); 
            DetectedObjectsEvent event = camera.activateTick(time - camera.getFrequency());//returns the events of the detected objects at the tick
        if(event != null)
        {  
            for(Object obj : event.getDetectedObjects()) //handling error object
            {
                DetectedObject decObj = (DetectedObject) obj;
                if(decObj.getID().equals("ERROR"))//if the detected object id is ERROR
                {
                    sendBroadcast(new CrashedBroadcast("Camera"));//send a broadcast that the camera crashed
                    terminate();
                }
            }
            sendEvent(event); //returns Future???
            for(int i=0; i<event.getDetectedObjects().size(); i++)
            {
                StatisticalFolder.getInstance().incrementNumDetectedObjects();//increment the number of detected objects in the statistical folder each time we detect an object
            }
        }
        });

        subscribeBroadcast(TerminatedBroadcast.class ,(TerminatedBroadcast terminate) ->{
            if(terminate.getTerminatedID().equals("TimeService") )//if the terminated MS is timeService - terminate me too
            {
                sendBroadcast(new TerminatedBroadcast(((Integer)camera.getID()).toString()));//tell everyone that the camera terminated itself
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class ,(CrashedBroadcast crashed) ->{
            terminate();
        });
        
    }
}
