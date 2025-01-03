package bgu.spl.mics.application.objects;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase 
{
    private static LiDarDataBase instance = new LiDarDataBase();
    private List<StampedCloudPoints> cloudPoints;
        /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
      //".\\example_input_2\\lidar_data.json"  

    private LiDarDataBase() 
    {
        this.cloudPoints = null;
    }

    // build the data from the json file
    public void buildData(String filepath)
    {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filepath)) 
        {
            // Convert JSON File to Java Object
            this.cloudPoints = gson.fromJson(reader, new TypeToken<List<StampedCloudPoints>>(){}.getType());
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LiDarDataBase getInstance()
    {   
        return instance;
    }

    public List<StampedCloudPoints> getCloudPoints() 
    {
        return cloudPoints;
    }
}
