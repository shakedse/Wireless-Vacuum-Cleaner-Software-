package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    // TODO: Define fields and methods.
    //fields:
    private String id;
    private String description;
    private List<CloudPoint> cloudPointsList;

     public LandMark (String id, String description)
 {
     this.id=id;
     this.description=description;
     this.cloudPointsList = new CopyOnWriteArrayList <CloudPoint>();//?????
 }

}
