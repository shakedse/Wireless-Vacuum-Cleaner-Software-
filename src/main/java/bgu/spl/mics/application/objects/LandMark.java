package bgu.spl.mics.application.objects;

import java.util.LinkedList;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    // TODO: Define fields and methods.
    //fields:
    private String id;
    private String description;
    private LinkedList<CloudPoint> cloudPointsList;

    public LandMark (String id, String description, LinkedList<CloudPoint> cloudPointsList)
    {
        this.id=id;
        this.description=description;
        this.cloudPointsList = cloudPointsList;
    }

    public LinkedList<CloudPoint> getCloudPoints()
    {
        return cloudPointsList;
    }

    public String getID()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    public void setAvgCloudPoint(LinkedList<CloudPoint> newCloudPoints)
    {
        int updateSize = Math.min(cloudPointsList.size(), newCloudPoints.size());
        int i;
        for(i = 0; i < updateSize; i++)
        {
            cloudPointsList.get(i).setX((cloudPointsList.get(i).getX() + newCloudPoints.get(i).getX())/2);
            cloudPointsList.get(i).setY((cloudPointsList.get(i).getY() + newCloudPoints.get(i).getY())/2);
        }
        while(i < newCloudPoints.size()-1)
        {
            i++;
            CloudPoint toAdd = new CloudPoint(newCloudPoints.get(i).getX(), newCloudPoints.get(i).getY());
            cloudPointsList.add(toAdd);
        }
    }
}
