package bgu.spl.mics;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus
 * interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for
 * unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus 
{

	private static final MessageBusImpl instance = new MessageBusImpl();
	private ConcurrentHashMap<MicroService, BlockingQueue<Message>> messageQueue;// queues for messages = events and broadcast
	private ConcurrentHashMap<Class<? extends Event<?>>, BlockingQueue<MicroService>> eventSubscribers;// map for event subscribers
	private ConcurrentHashMap<Class<? extends Broadcast>, BlockingQueue<MicroService>> broadcastSubscribers;// map for broadcasts
	private ConcurrentHashMap<Event<?>,Future<?>> EventAndFuture;
	
	private MessageBusImpl() //A private constructor
	{
        eventSubscribers = new ConcurrentHashMap<>();
		broadcastSubscribers = new ConcurrentHashMap<>();
        messageQueue = new ConcurrentHashMap<>();
        EventAndFuture = new ConcurrentHashMap<>();
    }
	//getters
	public ConcurrentHashMap<MicroService, BlockingQueue<Message>> getMessageQueue() 
	{
		return messageQueue;
	}
	public ConcurrentHashMap<Class<? extends Event<?>>, BlockingQueue<MicroService>> getEventSubscribers() 
	{
		return eventSubscribers;
	}
	public ConcurrentHashMap<Class<? extends Broadcast>, BlockingQueue<MicroService>> getBroadcastSubscribers() 
	{
		return broadcastSubscribers;
	}
	public ConcurrentHashMap<Event<?>, Future<?>> getEventAndFuture() 
	{
		return EventAndFuture;
	}
	
	//A method so that we can reach the private methods
	public static MessageBusImpl getInstance()
	{
		return instance;
	}
	// A ms entering a queue of a certin event class
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		eventSubscribers.computeIfAbsent(type, subscribersQueue -> new LinkedBlockingQueue<MicroService>()).add(m);// Adding an event to the queue if doesnt exists
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) 
	{
		broadcastSubscribers.computeIfAbsent(type, subscribersQueue -> new LinkedBlockingQueue<MicroService>()).add(m);// Adding an event to the queue if doesnt exists
	}

	@Override
	public <T> void complete(Event<T> e, T result) 
	{
		Future<T> future = (Future<T>)EventAndFuture.get(e);
		if (future != null) {
			future.resolve(result);
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) 
	{
		BlockingQueue<MicroService> curr = broadcastSubscribers.get(b.getClass());
		//synchronized (curr) //synchronized that no ms will be added to the queue?????????????????????
		//{ if it's unregister it doeant matter if i added or not?
			if (curr != null)// we found the queue
			{
				for(MicroService ms: curr)
				{
					synchronized (messageQueue){ // syncronized so that you wouldn't unregister the ms while you add the broadcast
						if (!messageQueue.get(ms).contains(b))
							messageQueue.get(ms).add(b);
					}
				}
			}
		//}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		BlockingQueue<MicroService> curr = eventSubscribers.get(e.getClass());
		if (curr == null)
			return null;
		synchronized (curr) 
		{
			if (!curr.isEmpty()) 
			{
				MicroService toMission = curr.poll();
				curr.add(toMission);// handling the round robin
				messageQueue.get(toMission).add(e);
				Future<T> ret = new Future<T>();
				EventAndFuture.putIfAbsent(e, ret);
				return ret;
			}
		}
		return null;
	}

	@Override
	public void register(MicroService m)
	// a new microservice registers to a new queue
	{
		messageQueue.putIfAbsent(m, new LinkedBlockingQueue<Message>());
	}

	@Override
	public void unregister(MicroService m) 
	{
		if(messageQueue.containsKey(m))
		{
			BlockingQueue<Message> curr = messageQueue.get(m);
			synchronized(curr)
			{
				while(!curr.isEmpty())
					{
						Message toDelete = curr.poll();
						EventAndFuture.get(toDelete).resolve(null);
						EventAndFuture.remove(toDelete);
					}
				messageQueue.remove(m);
			}

			//removing the microService from the other queues
			synchronized(eventSubscribers)
			{
				eventSubscribers.values().forEach(queue -> queue.remove(m));
			}

			synchronized(broadcastSubscribers)
			{
				broadcastSubscribers.values().forEach(queue -> queue.remove(m));
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException
	{
		if(!messageQueue.containsKey(m))
			throw new InterruptedException("This MicroService hasn't register");
		try
		{
			return messageQueue.get(m).take();
		}
		catch(InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
		return null;
	}
}
