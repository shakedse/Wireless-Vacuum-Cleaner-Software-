package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */
    private int tickTime;
    private int duration;

    public TimeService(int TickTime, int Duration) {
        super("The TimeService");
        // TODO Implement this
        this.tickTime = TickTime;
        this.duration = Duration;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() 
    {
        int tickNum = 0;
        // TODO Implement this
        while (tickNum<duration) 
        {
            sendBroadcast(new TickBroadcast (tickNum)); 
            try
            {
                Thread.sleep(tickTime);
                tickNum++;
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
        terminate();
    }
}
