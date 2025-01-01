package bgu.spl.mics.application;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.mics.*;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import bgu.spl.mics.application.messages.*;

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

        Gson gson = new Gson();
        try (FileReader reader = new FileReader("C:\\Users\\ron01\\.vscode\\SPL2\\SPL2\\example input\\configuration_file.json")) 
        {
            // Convert JSON File to Java Object
            Type configFileType = new TypeToken<configurationFileType>(){}.getType();
            configurationFileType DataBases = gson.fromJson(reader, configFileType);

            //giving eahc camera its data
            for(Camera camera : DataBases.getCameras().getCamerasConfigurations()) {
                camera.buildData("C:\\Users\\ron01\\.vscode\\SPL2\\SPL2\\example input\\" + DataBases.getCameras().getCameraDatasPath().substring(2));  
            }

            // giving the LiDar its data
            LiDarDataBase.getInstance().buildData("C:\\Users\\ron01\\.vscode\\SPL2\\SPL2\\example input\\" + DataBases.getLiDarWorkers().getLidarsDataPath().substring(2));
            
            // giving the GPSIMU its data
            GPSIMU.getInstance().buildData("C:\\Users\\ron01\\.vscode\\SPL2\\SPL2\\example input\\" + DataBases.getPoseJsonFile().substring(2));

            // testing camera data
            for (Camera camera : DataBases.getCameras().getCamerasConfigurations()) {
                System.out.println("ID: " + camera.getID() + ", Frequency: " + camera.getFrequency());
            }

            // testing GPS data
            for (int i = 1; i < 3 ; i++) {
                System.out.print(GPSIMU.getInstance().getPoseAtTick(i).getX());
                System.out.print("     ");
                System.out.print(GPSIMU.getInstance().getPoseAtTick(i).getY());
                System.out.print("     ");
                System.out.print(GPSIMU.getInstance().getPoseAtTick(i).getYaw());
                System.out.println("     ");
                System.out.print(GPSIMU.getInstance().getPoseAtTick(i).getTime());
                System.out.println("     ");
            }

            // testing LiDar data
            for (int i = 0; i < 1 ; i++) {
                System.out.print(LiDarDataBase.getInstance().getCloudPoints().get(i).getID());
                System.out.print("     ");
                System.out.print(LiDarDataBase.getInstance().getCloudPoints().get(i).getTime());
                System.out.print("     "); // not getting the right time!!!
                System.out.print(LiDarDataBase.getInstance().getCloudPoints().get(i).getCloudPoints().toString());
                System.out.println("     ");
            }


            int numberOfThreads = 0;

            Thread FusionSlamThread = new Thread(new FusionSlamService(FusionSlam.getInstance()));
            FusionSlamThread.start();
            numberOfThreads++;

            System.out.println("Number of Threads: " + numberOfThreads);
            while(numberOfThreads > MessageBusImpl.getInstance().getNumberOfMS()) {
                try 
                {
                    Thread.sleep(5);
                    System.out.println("Waiting for all threads to start");
                } 
                catch (InterruptedException e) 
                {
                    e.printStackTrace();
                }
            }

            Thread posethThread = new Thread(new PoseService(GPSIMU.getInstance()));
            posethThread.start();
            numberOfThreads++;

            System.out.println("Number of Threads: " + numberOfThreads);
            while(numberOfThreads > MessageBusImpl.getInstance().getNumberOfMS()) {
                try 
                {
                    Thread.sleep(5);
                    System.out.println("Waiting for all threads to start");
                } 
                catch (InterruptedException e) 
                {
                    e.printStackTrace();
                }
            }

            for(Camera camera : DataBases.getCameras().getCamerasConfigurations()) {
                Thread cameraThread = new Thread(new CameraService(camera, new CountDownLatch(1)));
                cameraThread.start();
                numberOfThreads++;
            }

            System.out.println("Number of Threads: " + numberOfThreads);
            while(numberOfThreads > MessageBusImpl.getInstance().getNumberOfMS()) {
                try 
                {
                    Thread.sleep(5);
                    System.out.println("Waiting for all threads to start");
                } 
                catch (InterruptedException e) 
                {
                    e.printStackTrace();
                }
            }

            for(LiDarWorkerTracker LiDarWorker : DataBases.getLiDarWorkers().getLidarConfigurations()) {
                Thread LiDarWorkerThread = new Thread(new LiDarService(LiDarWorker, new CountDownLatch(1)));
                LiDarWorkerThread.start();
                numberOfThreads++;
            }


            System.out.println("Number of Threads: " + numberOfThreads);
            while(numberOfThreads > MessageBusImpl.getInstance().getNumberOfMS()) {
                try 
                {
                    Thread.sleep(5);
                    System.out.println("Waiting for all threads to start");
                } 
                catch (InterruptedException e) 
                {
                    e.printStackTrace();
                }
            }

            TimeService timeService = new TimeService(DataBases.getTickTime(), DataBases.getDuration());
            Thread timeServiceThread = new Thread(timeService);
            timeServiceThread.start();

            System.out.println("Number of Threads: " + numberOfThreads);
            while(timeService.isFinished() == false) {
                try 
                {
                    Thread.sleep(5);
                    System.out.println("Waiting for all threads to start");
                } 
                catch (InterruptedException e) 
                {
                    e.printStackTrace();
                }
            }

            System.out.println("number Of Threads: " + MessageBusImpl.getInstance().getNumberOfMS());
            for(MicroService microService : MessageBusImpl.getInstance().getMessageQueue().keySet()) {
                System.out.println("number Of Threads: " + microService.getName());
            }
            // TODO: Initialize system components and services.
            // TODO: Start the simulation.
    
            SystemData systemData = new SystemData(StatisticalFolder.getInstance().getSystemRunTime()
                                                , StatisticalFolder.getInstance().getNumDetectedObjects()
                                                , StatisticalFolder.getInstance().getNumTrackedObjects()
                                                , StatisticalFolder.getInstance().getNumLandmarks()
                                                , new HashMap<>());
            for(LandMark landMark : FusionSlam.getInstance().getLandMarks()) {
                System.out.println(landMark.getID());
                systemData.addLandmark(landMark.getID(), landMark);
            }
    
            Gson gsonW = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter("output.json")) {
            // Serialize Java objects to JSON file
                gson.toJson(systemData, writer);
            }
    
            catch (IOException e) {
                e.printStackTrace();
            }
            
        } 
        catch (IOException e) {
            e.printStackTrace();
        
        }
    }
}
