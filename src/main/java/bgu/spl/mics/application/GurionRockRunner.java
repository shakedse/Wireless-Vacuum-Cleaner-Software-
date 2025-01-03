package bgu.spl.mics.application;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.mics.*;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */

    public class configurationFileType {
        private CamerasConfig Cameras;
        private LiDarConfig LiDarWorkers;
        private String poseJsonFile;
        private int TickTime;
        private int Duration;
    
        public configurationFileType(CamerasConfig Cameras, LiDarConfig LiDarWorkers, String poseJsonFile, int TickTime, int Duration) {
            this.Cameras = Cameras;
            this.LiDarWorkers = LiDarWorkers;
            this.poseJsonFile = poseJsonFile;
            this.TickTime = TickTime;
            this.Duration = Duration;
        }
    
        public CamerasConfig getCameras() {
            return Cameras;
        }
    
        public LiDarConfig getLiDarWorkers() {
            return LiDarWorkers;
        }
    
        public String getPoseJsonFile() {
            return poseJsonFile;
        }
    
        public int getTickTime() {
            return TickTime;
        }
    
        public int getDuration() {
            return Duration;
        }
    }
    
    class CamerasConfig {
        private List<Camera> CamerasConfigurations;
        private String camera_datas_path;

        public List<Camera> getCamerasConfigurations() {
            return CamerasConfigurations;
        }
    
        public String getCameraDatasPath() {
            return camera_datas_path;
        }
    }
    
    class LiDarConfig {
        private List<LiDarWorkerTracker> LidarConfigurations;
        private String lidars_data_path;
    
        public List<LiDarWorkerTracker> getLidarConfigurations() {
            return LidarConfigurations;
        }
    
        public String getLidarsDataPath() {
            return lidars_data_path;
        }
    }
    
    public static class SystemData {
        private int systemRuntime;
        private int numDetectedObjects;
        private int numTrackedObjects;
        private int numLandmarks;
        private Map<String, LandMark> landMarks;

        // Getters and setters (or public fields for simplicity)
        public SystemData(AtomicInteger systemRuntime, AtomicInteger numDetectedObjects, AtomicInteger numTrackedObjects, AtomicInteger numLandmarks, Map<String, LandMark> landMarks) {
            this.systemRuntime = systemRuntime.get();
            this.numDetectedObjects = numDetectedObjects.get();
            this.numTrackedObjects = numTrackedObjects.get();
            this.numLandmarks = numLandmarks.get();
            this.landMarks = landMarks;
        }

        public void addLandmark(String key, LandMark landmark) {
            this.landMarks.put(key, landmark);
        }
    }
    
    
    public static void main(String[] args) 
    {        
        // TODO: Parse configuration file.

        if (args.length == 0) {
            System.err.println("Please provide the path to the configuration file as a command-line argument.");
            return;
        }
    
        // getting the path to the configuration file
        String configFilePath = args[0];

        // reading the configuration file
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(configFilePath + "\\configuration_file.json")) 
        {
            // Convert JSON File to Java Object
            Type configFileType = new TypeToken<configurationFileType>(){}.getType();
            configurationFileType DataBases = gson.fromJson(reader, configFileType);

            //giving each camera its data
            for(Camera camera : DataBases.getCameras().getCamerasConfigurations()) {
                camera.buildData(configFilePath + DataBases.getCameras().getCameraDatasPath());  
            }

            // giving the LiDar its data
            LiDarDataBase.getInstance().buildData(configFilePath + DataBases.getLiDarWorkers().getLidarsDataPath());
            
            // giving the GPSIMU its data
            GPSIMU.getInstance().buildData(configFilePath + DataBases.getPoseJsonFile());

            int numberOfThreads = 0;

            // making sure the data is built before starting the threads
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // starting the threads
            Thread posethThread = new Thread(new PoseService(GPSIMU.getInstance()));
            posethThread.start();
            numberOfThreads++;

            for(LiDarWorkerTracker LiDarWorker : DataBases.getLiDarWorkers().getLidarConfigurations()) {
                Thread LiDarWorkerThread = new Thread(new LiDarService(LiDarWorker, new CountDownLatch(1)));
                LiDarWorkerThread.start();
                numberOfThreads++;
            }

            Thread FusionSlamThread = new Thread(new FusionSlamService(FusionSlam.getInstance(), new CountDownLatch(1)));
            FusionSlamThread.start();
            numberOfThreads++;

            for(Camera camera : DataBases.getCameras().getCamerasConfigurations()) {
                Thread cameraThread = new Thread(new CameraService(camera, new CountDownLatch(1)));
                cameraThread.start();
                numberOfThreads++;
            }
            
            // making sure all the threads started before starting the time service
            while(numberOfThreads > MessageBusImpl.getInstance().getNumberOfMS()) {
                try 
                {
                    Thread.sleep(5);
                    System.out.println("Waiting for all threads to start, started: " + MessageBusImpl.getInstance().getNumberOfMS() + " out of " + numberOfThreads);
                } 
                catch (InterruptedException e) 
                {
                    e.printStackTrace();
                }
            }

            TimeService timeService = new TimeService(DataBases.getTickTime(), DataBases.getDuration());
            Thread timeServiceThread = new Thread(timeService);
            timeServiceThread.start();
        } 
        catch (IOException e) {
            e.printStackTrace();
        
        }
    }
}
