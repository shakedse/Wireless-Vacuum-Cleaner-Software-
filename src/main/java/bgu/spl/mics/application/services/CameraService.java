package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectedObjectsEvent;
import bgu.spl.mics.application.messages.LastCameraFrameEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;


import java.util.concurrent.CountDownLatch;

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
    private final CountDownLatch latch; 
    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera, CountDownLatch latch) 
    {
        super("cameraService" + camera.getID());
        this.camera = camera;
        this.time = 0;
        this.latch = latch;
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

            DetectedObject CheckForError = camera.checkForErrors(time); // getting the current ticks event to check if the camera crashed

            if(camera.getStatus() == STATUS.ERROR || CheckForError != null) //if the camera crashed - terminate
            {
                sendEvent(new LastCameraFrameEvent(camera.getStringId(), camera.gerLastFrame()));
                camera.statusDown();
                String description = CheckForError.getDescription();
                sendBroadcast(new CrashedBroadcast("Camera" + camera.getID() + ", because of: " + description));
                terminate();
            }
            else
            {
                DetectedObjectsEvent event = camera.activateTick(time - camera.getFrequency()); //returns the events of the detected objects at the tick
                if(event != null && event.getDetectedObjects().size() > 0) //if there are detected objects
                {  
                    sendEvent(event); 
                    camera.setLastFrame(event);
                }
                
                int last = camera.getList().size()-1;
                if (camera.getList().get(last).getTime() <= time - camera.getFrequency())
                {
                    camera.statusDown();
                    System.out.println("CameraService is down at time:" + (tick.getTick()));
                    sendBroadcast(new TerminatedBroadcast(((Integer)camera.getID()).toString()));
                    terminate();
                }
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class ,(TerminatedBroadcast terminate) ->{
            if(terminate.getTerminatedID().equals("TimeService") )//if the terminated MS is timeService - terminate me too
            {
                camera.statusDown();
                sendBroadcast(new TerminatedBroadcast(((Integer)camera.getID()).toString()));//tell everyone that the camera terminated itself
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class ,(CrashedBroadcast crashed) ->{
            sendEvent(new LastCameraFrameEvent(camera.getStringId(), camera.gerLastFrame()));
            terminate();
        });

        latch.countDown();
    }
}
