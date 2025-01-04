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
	public int getNumberOfMS() 
	{
		return messageQueue.size();
	}
	
	//A method so that we can reach the private methods
	public static MessageBusImpl getInstance()
	{
		return instance;
	}
	// A ms entering a queue of a certin event class
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) 
	{/*
		@pre: type != null, m != null
		@post: if the event type is not in the eventSubscribers map, add it
		@post: add the microservice to the event queue
		@param: type - the event type
		@param: m - the microservice to add to the queue
		@return: void
		*/
		
		eventSubscribers.computeIfAbsent(type, subscribersQueue -> new LinkedBlockingQueue<MicroService>());
		if (!eventSubscribers.get(type).contains(m)) 
		{
            eventSubscribers.get(type).add(m);
        }
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) 
	{
		/*
		 * @pre: type != null, m != null
		 * @post: if the broadcast type is not in the broadcastSubscribers map, add it
		 * @post: add the microservice to the broadcast queue
		 * @param: type - the broadcast type
		 * @param: m - the microservice to add to the queue
		 * @return: void
		 */
		broadcastSubscribers.computeIfAbsent(type, subscribersQueue -> new LinkedBlockingQueue<MicroService>());
		if (!broadcastSubscribers.get(type).contains(m)) 
		{
            broadcastSubscribers.get(type).add(m);
        }
	}

	@Override
	public <T> void complete(Event<T> e, T result) 
	{
		/*
		@pre: e != null, result != null
		@post: resolve the future of the event
		@param: e - the event to resolve
		@param: result - the result to resolve the event with
		@return: void
		*/
		Future<T> future = (Future<T>)EventAndFuture.get(e);
		if (future != null) {
			future.resolve(result);
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) 
	{
		/*
		 * @pre: b != null
		 * @post: add the broadcast to the queues of the microservices that are subscribed to it
		 * @param: b - the broadcast to send
		 * @return: void
		 */
		synchronized (messageQueue){
			BlockingQueue<MicroService> curr = broadcastSubscribers.get(b.getClass());
			synchronized (broadcastSubscribers) //synchronized that no ms will be added to the queue
			{ //if it's unregister it doeant matter if i added or not?
				if (curr != null)// we found the queue
				{
					for(MicroService ms: curr)
					{
					// syncronized so that you wouldn't unregister the ms while you add the broadcast
						if(messageQueue.get(ms) != null)
							if (!messageQueue.get(ms).contains(b))
								messageQueue.get(ms).add(b);
					}
				}
			}
		}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) 
	{
		/*
		 * @pre: e != null
		 * @post: add the event to the queue of the microservice that is subscribed to it
		 * @param: e - the event to send
		 * @return: Future<T> - the future of the event
		 */
		BlockingQueue<MicroService> curr = eventSubscribers.get(e.getClass());
		if (curr == null)
			return null;
		synchronized (curr) 
		{
			if (!curr.isEmpty()) 
			{
				Future<T> ret = new Future<T>();
				EventAndFuture.putIfAbsent(e, ret);
				MicroService toMission = curr.poll();
				messageQueue.get(toMission).add(e);
				curr.add(toMission);// handling the round robin
				return ret;
			}
		}
		return null;
	}

	@Override
	public void register(MicroService m)
	// a new microservice registers to a new queue
	{/*
		@pre: m != null
		@post: add the microservice to the messageQueue
		@param: m - the microservice to register
		@return: void
		*/
		messageQueue.putIfAbsent(m, new LinkedBlockingQueue<Message>());
	}

	@Override
	public void unregister(MicroService m) 
	{
		/*
		 * @pre: m != null
		 * @post: remove the microservice from the messageQueue
		 * @post: remove the microservice from the eventSubscribers
		 * @post: remove the microservice from the broadcastSubscribers
		 * @param: m - the microservice to unregister
		 * @return: void
		 */
		if(messageQueue.containsKey(m))
		{
			BlockingQueue<Message> curr = messageQueue.get(m);
			synchronized(messageQueue)
			{
				while(!curr.isEmpty())
					{
						Message toDelete = curr.poll();
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
		/*
		 * @pre: m != null
		 * @post: if the microservice hasn't register, throw an exception
		 * @param: m - the microservice to get the message from
		 * @return: Message - the message that the microservice got
		 */
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
