package bgu.spl.mics.application.objects;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {


    List<StampedCloudPoints> cloudPoints;
    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        // TODO: Implement this
        return null;
    }
}
