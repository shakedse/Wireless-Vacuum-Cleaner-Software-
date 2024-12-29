
    import static org.junit.jupiter.api.Assertions.assertTrue;
    import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.junit.jupiter.api.Assertions.assertNull;

    import org.junit.jupiter.api.Test;

    import java.util.concurrent.BlockingQueue;
    import java.util.concurrent.ConcurrentHashMap;
    import java.util.concurrent.CountDownLatch;
    import java.util.concurrent.LinkedBlockingQueue;

    import javax.sql.StatementEvent;
    import bgu.spl.mics.Broadcast;
    import bgu.spl.mics.Event;
    import bgu.spl.mics.Message;
    import bgu.spl.mics.MessageBusImpl;
    import bgu.spl.mics.MicroService;

    public class MessageBusTest 
    {
        public static class EventTestNUM1 implements Event<String>
        {
            private String message1;

            public EventTestNUM1(String message1)
            {
                this.message1 = message1; 
            }
            public String getMessage()
            {
                return message1;
            }
            public String getSenderName() 
            {
                return "EventTestNUM1";
            }
        }
        public static class EventTestNUM2 implements Event<String>
        {
            private String message2;
            public EventTestNUM2(String message2)
            {
                this.message2 = message2; 
            }
            public String getMessage()
            {
                return message2;
            }
            public String getSenderName() 
            {
                return "EventTestNUM2";
            }
        }
        public static class TerminateTest implements Event<String>
        {
            private String terminateString;
            
            public TerminateTest(String terminateString)
            {
                this.terminateString = terminateString; 
            }
            
            public String getMessage()
            {
                return terminateString;
            }
            
            public String getSenderName() 
            {
                return "Terminate";
            }
        }
        public static class BroadcastTestNUM1 implements Broadcast
        {
            private String message1;

            public BroadcastTestNUM1(String message1)
            {
                this.message1 = message1; 
            }
            public String getMessage()
            {
                return message1;
            }
            public String getSenderName() 
            {
                return "BroadCastTestNUM1";
            }
        }
        public static class BroadcastTestNUM2 implements Broadcast
        {
            private String message2;
            public BroadcastTestNUM2(String message2)
            {
                this.message2 = message2; 
            }
            public String getMessage()
            {
                return message2;
            }
            public String getSenderName() 
            {
                return "BroadCastTestNUM2";
            }
        }  
        public static class MicroServiceTestNUM1 extends MicroService
        {
            private final CountDownLatch latch;
            private int counterEvent1;
            private int counterEvent2;
            private int counterBroadcast1;
            private int counterBroadcast2;


            public MicroServiceTestNUM1(String name, CountDownLatch latch)
            {
                super(name);
                this.latch=latch;
                this.counterEvent1 = 0;
                this.counterEvent2 = 0;
                this.counterBroadcast1 = 0;
                this.counterBroadcast2 = 0;    
            }
            public int getcounterEvent1()
            {
                return counterEvent1;
            }
            public int getcounterEvent2()
            {
                return counterEvent2;
            }
            public int getcounterBroadcast1()
            {
                return counterBroadcast1;
            }
            public int getcounterBroadcast2()
            {
                return counterBroadcast2;
            }
            protected void initialize()
            {
                subscribeEvent(EventTestNUM1.class, (event)->{
                    System.out.println("Event 1 arrived for " + getName());
                    counterEvent1++;
                });
                subscribeEvent(EventTestNUM2.class, (event)->{
                    System.out.println("Event 2 arrived for " + getName());
                    counterEvent2++;
                });
                subscribeBroadcast(BroadcastTestNUM1.class, (broadcast)->{
                    System.out.println("Broadcast 1 arrived for " + getName());
                    counterBroadcast1++;
                });
                subscribeBroadcast(BroadcastTestNUM2.class, (broadcast)->{
                    System.out.println("Broadcast 2 arrived for " + getName());
                    counterBroadcast2++;
                });
                subscribeEvent(TerminateTest.class, (event)->{
                    System.out.println("terminate arrived");
                    terminate();
                });
                latch.countDown();
            }
        }


        @Test
        public void testMicroService() 
        {

            System.out.println("test has started");
        CountDownLatch latch = new CountDownLatch(5);

        //Initialize the microService
        MicroServiceTestNUM1 MicroService1 = new MicroServiceTestNUM1("MicroService1", latch);
        MicroServiceTestNUM1 MicroService2 = new MicroServiceTestNUM1("MicroService2", latch);
        MicroServiceTestNUM1 MicroService3 = new MicroServiceTestNUM1("MicroService3", latch);
        MicroServiceTestNUM1 MicroService4 = new MicroServiceTestNUM1("MicroService4", latch);
        MicroServiceTestNUM1 MicroService5 = new MicroServiceTestNUM1("MicroService5", latch);
            
        MicroServiceTestNUM1[] microServices = {MicroService1, MicroService2, MicroService3, MicroService4, MicroService5}; //array of microservices
        Class[] events = {EventTestNUM1.class,EventTestNUM2.class, TerminateTest.class}; 
        Class[] broadcasts = {BroadcastTestNUM1.class, BroadcastTestNUM2.class}; 

        //Initialize threads
        Thread t1 = new Thread(MicroService1);
        Thread t2 = new Thread(MicroService2);
        Thread t3 = new Thread(MicroService3);
        Thread t4 = new Thread(MicroService4);
        Thread t5 = new Thread(MicroService5);

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        
        //waiting for everyone to initialize
        try{
        latch.await();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        

    //getting the messageBusImpl 
            MessageBusImpl messageBusImplTest = MessageBusImpl.getInstance();


        //checking method 1 - subscribe event
            //each testing MicroService is subscribed to -EventTestNUM1.class,EventTestNUM2.class
            
                BlockingQueue<MicroService> testQueue = messageBusImplTest.getEventSubscribers().get(EventTestNUM1.class);
                if(testQueue.size()== microServices.length)
                    System.out.println("all the microServices are subscribed to EventTestNUM1");
                
                    testQueue = messageBusImplTest.getEventSubscribers().get(EventTestNUM2.class);
                if(testQueue.size()== microServices.length)
                    System.out.println("all the microServices are subscribed to EventTestNUM2");

        //checking method 2 - subscribe broadcast
            //each testing MicroService is subscribed to -BroadcastTestNUM1.class,BroadcastTestNUM2.class
            
            BlockingQueue<MicroService> testQueue2 = messageBusImplTest.getBroadcastSubscribers().get(BroadcastTestNUM1.class);
            if(testQueue.size()== microServices.length)
                System.out.println("all the microServices are subscribed to BroadcastTestNUM1");
            
                testQueue2 = messageBusImplTest.getBroadcastSubscribers().get(BroadcastTestNUM2.class);
            if(testQueue2.size()== microServices.length)
                System.out.println("all the microServices are subscribed to BroadcastTestNUM2");

        //checking method 3 - sendBroadcast
            //each time we send event there is supposed to be ++ in the counter
            
            messageBusImplTest.sendBroadcast(new BroadcastTestNUM1("broadcast1"));
            messageBusImplTest.sendBroadcast(new BroadcastTestNUM2("broadcast2"));
            int counter = 0;

            for(int i = 0; i < microServices.length; i++)
            {
                System.out.println("counter broadcast 1="+microServices[i].getcounterBroadcast1());
                if(microServices[i].getcounterBroadcast1()==1){
                    System.out.println("Broadcast 1 sent - sucess for " + microServices[i].getName());
                    counter++;
                }
                System.out.println("counter broadcast 2=" + microServices[i].getcounterBroadcast2());
                if(microServices[i].getcounterBroadcast2()==1){
                    System.out.println("Broadcast 2 sent - sucess for " + microServices[i].getName());
                    counter ++;
                }
            }
            try {
                t1.join();
                t2.join();
                t3.join();
                t4.join();
                t5.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            System.out.println("counter is: " + counter);
            assertTrue(counter == 10);
            if(counter == 10)
            System.out.println("all the microServices got the broadcast");
            
            //checking method 4 - send event

            counter = 0;
            messageBusImplTest.sendEvent(new EventTestNUM1("event1"));
            for(int i = 0; i < microServices.length; i++)
        {
            if(microServices[i].getcounterEvent1()==1){
                System.out.print("EVENT 1 sent - sucess for " + microServices[i].getName());
            }
        }
        assertTrue(counter==1);

        counter = 0;
        messageBusImplTest.sendEvent(new EventTestNUM2("event2"));
        for(int i = 0; i < microServices.length; i++)
    {
        if(microServices[i].getcounterEvent2()==1){
            System.out.print("EVENT 2 sent - sucess for " + microServices[i].getName());
        }
    }
    assertTrue(counter==1);

    //checking method 5 - register
        //checking that registering inside initialize() is working - meaning the MS is in the message queue
        int registered = 0;
        for (int i = 0; i < microServices.length; i++)
        {
            if(null!=messageBusImplTest.getMessageQueue().get(microServices[i]))
            {
                registered++;
            }
        }
        if(registered == microServices.length)
        {
            System.out.println("all the microservices are registered");
        }

        //checking method 6 - unregister TerminateTest
        
        int unregister =0;
        for (int i = 0; i < microServices.length; i++)
        {//sending the event 5 times so that each microService will recive the terimante event
            messageBusImplTest.sendEvent(new TerminateTest("terminate"));//should be now unregistered
        }
       for (MicroServiceTestNUM1 ms: microServices)
        assertNull(messageBusImplTest.getMessageQueue().get(ms));
// trying to delete after everyone was deleted
    }




    //test2
    
        //checking edge cases
        public static class MicroServiceTestNUM2 extends MicroService
{
    private final CountDownLatch latch;
    private int counterEvent1;
    private int counterEvent2;
    private int counterBroadcast1;
    private int counterBroadcast2;
    public MicroServiceTestNUM2(String name, CountDownLatch latch)
    {
        super(name);
        this.latch=latch;
        this.counterEvent1 = 0;
        this.counterEvent2 = 0;
        this.counterBroadcast1 = 0;
        this.counterBroadcast2 = 0;    
    }
    public int getcounterEvent1()
    {
        return counterEvent1;
    }
    public int getcounterEvent2()
    {
        return counterEvent2;
    }
    public int getcounterBroadcast1()
    {
        return counterBroadcast1;
    }
    public int getcounterBroadcast2()
    {
        return counterBroadcast2;
    }
    protected void initialize()
    {
        subscribeEvent(EventTestNUM1.class, (event)->{
            System.out.println("Event 1 arrived");
            counterEvent1++;
        });
        subscribeBroadcast(BroadcastTestNUM1.class, (broadcast)->{
            System.out.println("Broadcast 1 arrived");
            counterBroadcast1++;
        });
        subscribeEvent(TerminateTest.class, (event)->{
            System.out.println("terminate arrived");
            terminate();
        });
        latch.countDown();
    }
}

@Test
public void testMicroServiceEdge() 
{
    System.out.println("edge test has started");
    CountDownLatch latch = new CountDownLatch(3);
    //Initialize the microService
    MicroServiceTestNUM2 EdgeMicroService1 = new MicroServiceTestNUM2("EdgeMicroService1", latch);
    MicroServiceTestNUM2 EdgeMicroService2 = new MicroServiceTestNUM2("EdgeMicroService2", latch);
    MicroServiceTestNUM2 EdgeMicroService3 = new MicroServiceTestNUM2("EdgeMicroService3", latch);
        
    MicroServiceTestNUM2[] microServices = {EdgeMicroService1, EdgeMicroService2, EdgeMicroService3}; //array of microservices
    Class[] events = {EventTestNUM1.class,EventTestNUM2.class, TerminateTest.class}; 
    Class[] broadcasts = {BroadcastTestNUM1.class, BroadcastTestNUM2.class}; 
    
    //Initialize threads
    Thread t1 = new Thread(EdgeMicroService1);
    Thread t2 = new Thread(EdgeMicroService2);
    Thread t3 = new Thread(EdgeMicroService3);

    t1.start();
    t2.start();
    t3.start();
            
    //waiting for everyone to initialize
    try{
    latch.await();
    }
    catch (InterruptedException e)
    {
        e.printStackTrace();
    }
    
    //getting the messageBusImpl 
    MessageBusImpl messageBusImplTest = MessageBusImpl.getInstance();
    
    
    //edge case 1 - double subscription to an event
    //EdgeMicroService1 is already subscribed to EventTestNUM1 from initializinig 
    messageBusImplTest.subscribeEvent(EventTestNUM1.class, EdgeMicroService1);
    assertEquals(messageBusImplTest.getEventSubscribers().size(),1, "duplicate sub should not create duplicate entries");

    //edge case 2 - trying to sub EdgeMicroService2 to a broadcast which is not subed to
    messageBusImplTest.sendBroadcast(new BroadcastTestNUM2("broadcast 2"));

    //edge case 3 - unregister a MS and send event after
    messageBusImplTest.unregister(EdgeMicroService2);
    assertNull(messageBusImplTest.getMessageQueue().get(EdgeMicroService2), "unregistered service should not have a message queue");
    EventTestNUM2 eventAfterUnregister = new EventTestNUM2("event after unregister");
    assertNull(messageBusImplTest.sendEvent(eventAfterUnregister), "Sending an event to an unregistered service should return null");


    
    }
}

















































