import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Event;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;

public class MessageBusTest {
    private MessageBusImpl messageBusImplTest = MessageBusImpl.getInstance();
    private MicroServiceTestNUM1[] microServices;
    private Class[] events;
    private Class[] broadcasts;
    private CountDownLatch latch;
    private MicroServiceTestNUM1 MicroService1;
    private MicroServiceTestNUM1 MicroService2;
    private MicroServiceTestNUM1 MicroService3;
    private MicroServiceTestNUM1 MicroService4;
    private MicroServiceTestNUM1 MicroService5;

    @BeforeEach
    public void setUp() {
        messageBusImplTest.getMessageQueue().clear();
        messageBusImplTest.getBroadcastSubscribers().clear();
        messageBusImplTest.getEventSubscribers().clear();

        latch = new CountDownLatch(5);

        // Initialize the microService
        MicroService1 = new MicroServiceTestNUM1("MicroService1", latch);
        MicroService2 = new MicroServiceTestNUM1("MicroService2", latch);
        MicroService3 = new MicroServiceTestNUM1("MicroService3", latch);
        MicroService4 = new MicroServiceTestNUM1("MicroService4", latch);
        MicroService5 = new MicroServiceTestNUM1("MicroService5", latch);

        microServices = new MicroServiceTestNUM1[5];
        microServices[0] = MicroService1;
        microServices[1] = MicroService2;
        microServices[2] = MicroService3;
        microServices[3] = MicroService4;
        microServices[4] = MicroService5;
        events = new Class[3];
        events[0] = EventTestNUM1.class;
        events[1] = EventTestNUM2.class;
        events[2] = TerminateTest.class;
        broadcasts = new Class[2];
        broadcasts[0] = BroadcastTestNUM1.class;
        broadcasts[1] = BroadcastTestNUM2.class;

        // Initialize threads
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

        // waiting for everyone to initialize
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class EventTestNUM1 implements Event<String> {
        private String message1;

        public EventTestNUM1(String message1) {
            this.message1 = message1;
        }

        public String getMessage() {
            return message1;
        }

        public String getSenderName() {
            return "EventTestNUM1";
        }
    }

    public static class EventTestNUM2 implements Event<String> {
        private String message2;

        public EventTestNUM2(String message2) {
            this.message2 = message2;
        }

        public String getMessage() {
            return message2;
        }

        public String getSenderName() {
            return "EventTestNUM2";
        }
    }

    public static class TerminateTest implements Event<String> {
        private String terminateString;

        public TerminateTest(String terminateString) {
            this.terminateString = terminateString;
        }

        public String getMessage() {
            return terminateString;
        }

        public String getSenderName() {
            return "Terminate";
        }
    }

    public static class BroadcastTestNUM1 implements Broadcast {
        private String message1;

        public BroadcastTestNUM1(String message1) {
            this.message1 = message1;
        }

        public String getMessage() {
            return message1;
        }

        public String getSenderName() {
            return "BroadCastTestNUM1";
        }
    }

    public static class BroadcastTestNUM2 implements Broadcast {
        private String message2;

        public BroadcastTestNUM2(String message2) {
            this.message2 = message2;
        }

        public String getMessage() {
            return message2;
        }

        public String getSenderName() {
            return "BroadCastTestNUM2";
        }
    }

    public static class MicroServiceTestNUM1 extends MicroService {
        private final CountDownLatch latch;
        private int counterEvent1;
        private int counterEvent2;
        private int counterBroadcast1;
        private int counterBroadcast2;

        public MicroServiceTestNUM1(String name, CountDownLatch latch) {
            super(name);
            this.latch = latch;
            this.counterEvent1 = 0;
            this.counterEvent2 = 0;
            this.counterBroadcast1 = 0;
            this.counterBroadcast2 = 0;
        }

        public int getcounterEvent1() {
            return counterEvent1;
        }

        public int getcounterEvent2() {
            return counterEvent2;
        }

        public int getcounterBroadcast1() {
            return counterBroadcast1;
        }

        public int getcounterBroadcast2() {
            return counterBroadcast2;
        }

        protected void initialize() {
            subscribeEvent(EventTestNUM1.class, (event) -> {
                System.out.println("Event 1 arrived for " + getName());
                counterEvent1++;
            });
            subscribeEvent(EventTestNUM2.class, (event) -> {
                System.out.println("Event 2 arrived for " + getName());
                counterEvent2++;
            });
            subscribeBroadcast(BroadcastTestNUM1.class, (broadcast) -> {
                System.out.println("Broadcast 1 arrived for " + getName());
                counterBroadcast1++;
            });
            subscribeBroadcast(BroadcastTestNUM2.class, (broadcast) -> {
                System.out.println("Broadcast 2 arrived for " + getName());
                counterBroadcast2++;
            });
            subscribeEvent(TerminateTest.class, (event) -> {
                System.out.println("terminate arrived");
                terminate();
            });
            latch.countDown();
        }
    }

    @Test
    public void testRegister() {
        System.out.println("register test - has started");
        // TEST - normal register
        // checking that registering inside initialize() is working - meaning the MS is
        // in the messageQueue
        int registered = 0;
        for (int i = 0; i < microServices.length; i++) {
            if (null != messageBusImplTest.getMessageQueue().get(microServices[i])) {
                registered++;
            }
        }
        assertEquals(registered, microServices.length);

        // TEST - registering a lot of microServices
        // checking that we can register a lot of microServices
        for (int i = 0; i < 100; i++) {
            MicroServiceTestNUM1 ms = new MicroServiceTestNUM1("MicroService" + i, latch);
            messageBusImplTest.register(ms);
        }
        assertEquals(105, messageBusImplTest.getMessageQueue().size());

        // TEST- double register
        // checking that double register is not possible
        messageBusImplTest.register(MicroService1);
        assertEquals(105, messageBusImplTest.getMessageQueue().size());

        // TEST - register after unregister
        // checking that after unregistering a microService we can register it again
        messageBusImplTest.unregister(MicroService1);
        messageBusImplTest.register(MicroService1);
        assertNotNull(messageBusImplTest.getMessageQueue().get(MicroService1));

        System.out.println("finish");

    }

    @Test
    public void testUnregister() {
        System.out.println("unregister test - has started");
        // TEST - normal unregister - terminaiting each microService
        int unregister = 0;
        for (int i = 0; i < microServices.length; i++) {// sending the event 5 times so that each microService will
                                                        // recieve the terimante event
            messageBusImplTest.unregister(microServices[i]);// should be now unregistered
        }
        for (MicroServiceTestNUM1 ms : microServices)
            assertNull(messageBusImplTest.getMessageQueue().get(ms));

        assertEquals(0, messageBusImplTest.getMessageQueue().size());

        // TEST - double unregister
        // checking that double unregister is not possible
        messageBusImplTest.register(MicroService1);
        assertEquals(1, messageBusImplTest.getMessageQueue().size());

        messageBusImplTest.unregister(MicroService1);
        assertEquals(0, messageBusImplTest.getMessageQueue().size());

        messageBusImplTest.unregister(MicroService1);
        assertEquals(0, messageBusImplTest.getMessageQueue().size());

        assertNull(messageBusImplTest.getMessageQueue().get(MicroService1));
        System.out.println("finish");

    }

    @Test
    public void testSubscribeEvent() {
        System.out.println("SubscribeEvent test - has started");
        // TEST - normal subscribe event
        // checking that each testing MicroService is subscribed to
        // -EventTestNUM1.class,EventTestNUM2.class

        BlockingQueue<MicroService> testQueue = messageBusImplTest.getEventSubscribers().get(EventTestNUM1.class);
        
        assertEquals(testQueue.size(), microServices.length);
        System.out.println("all the microServices are subscribed to EventTestNUM1");

        testQueue = messageBusImplTest.getEventSubscribers().get(EventTestNUM2.class);
        assertEquals(testQueue.size(), microServices.length);
        System.out.println("all the microServices are subscribed to EventTestNUM2");

        // TEST - double subscribe event
        // checking that double subscribe event is not possible
        // all the microServices are already subscribed to EventTestNUM1 from
        // initializing
        messageBusImplTest.subscribeEvent(EventTestNUM1.class, MicroService1);
        assertEquals(messageBusImplTest.getEventSubscribers().get(EventTestNUM1.class).size(), 5);

        // TEST - subscribe event after unregister
        // checking that after unregistering a microService we can subscribe it again
        messageBusImplTest.unregister(MicroService1);
        messageBusImplTest.subscribeEvent(EventTestNUM1.class, MicroService1);
        assertNotNull(messageBusImplTest.getEventSubscribers().get(EventTestNUM1.class));
    }

    @Test
    public void testSubscribeBroadcast() {
        System.out.println("SubscribeBroadcast test - has started");

        // TEST - normal subscribe broadcast
        // checking that each testing MicroService is subscribed to
        // -BroadcastTestNUM1.class,BroadcastTestNUM2.class
        BlockingQueue<MicroService> testQueue2 = messageBusImplTest.getBroadcastSubscribers()
                .get(BroadcastTestNUM1.class);
        assertEquals(testQueue2.size(), microServices.length);
        System.out.println("all the microServices are subscribed to BroadcastTestNUM1");

        testQueue2 = messageBusImplTest.getBroadcastSubscribers().get(BroadcastTestNUM2.class);
        assertEquals(testQueue2.size(), microServices.length);
        System.out.println("all the microServices are subscribed to BroadcastTestNUM2");

        // TEST - double subscribe broadcast
        // checking that double subscribe broadcast is not possible
        // all the microServices are already subscribed to BroadcastTestNUM1 from
        // initializing
        messageBusImplTest.subscribeBroadcast(BroadcastTestNUM1.class, MicroService1);
        assertEquals(messageBusImplTest.getBroadcastSubscribers().get(BroadcastTestNUM1.class).size(), 5);

        // TEST - subscribe event after unregister
        // checking that after unregistering a microService we can subscribe it again
        messageBusImplTest.unregister(MicroService1);
        messageBusImplTest.subscribeBroadcast(BroadcastTestNUM1.class, MicroService1);
        assertNotNull(messageBusImplTest.getBroadcastSubscribers().get(BroadcastTestNUM1.class));

    }

    @Test
    public void testSendevent() 
    {
        System.out.println("Sendevent test - has started");
        // TEST - normal send event
        // checking that each testing MicroService is subscribed to EventTestNUM1 and
        // EventTestNUM2
        int counter = 0;
        for (int i = 0; i < microServices.length; i++) 
        {
            messageBusImplTest.sendEvent(new EventTestNUM1("event1"));
        }
         try {
             Thread.sleep(50);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
        for (int i = 0; i < microServices.length; i++) 
        {
            if (microServices[i].getcounterEvent1() == 1) 
            {
                System.out.print("EVENT 1 sent - sucess for " + microServices[i].getName());
                counter++;
            }
        }
        assertEquals(counter , microServices.length);

        counter = 0;
        for (int i = 0; i < microServices.length; i++) 
        {
        messageBusImplTest.sendEvent(new EventTestNUM2("event2"));
        }

        try {
            Thread.sleep(50);
        } catch (Exception e) {
            e.printStackTrace();  
        }
        for (int i = 0; i < microServices.length; i++) 
        {
            if (microServices[i].getcounterEvent2() == 1) 
            {
                System.out.print("EVENT 2 sent - sucess for " + microServices[i].getName());
                counter++;
            }
        }
        assertEquals(counter , microServices.length);
        
        // TEST - send event to unregistered microservice
        messageBusImplTest.unregister(MicroService1);
        EventTestNUM2 event2 = new EventTestNUM2("event2");
        messageBusImplTest.sendEvent(event2);
        assertNull(messageBusImplTest.getMessageQueue().get(MicroService1));


        // TEST - Re-register MicroService1 and send event again
        messageBusImplTest.register(MicroService1);
        messageBusImplTest.sendEvent(event2);
        assertNotNull(messageBusImplTest.getMessageQueue().get(MicroService1));


         // TEST - unregister a MS and send event after
        for (int i = 0; i < microServices.length; i++) 
        {
            messageBusImplTest.unregister(microServices[i]);
        }
        for (int i = 0; i < microServices.length; i++)
        {
        assertNull(messageBusImplTest.getMessageQueue().get(MicroService2));
        }
        EventTestNUM2 eventAfterUnregister = new EventTestNUM2("event after unregister");
         assertNull(messageBusImplTest.sendEvent(eventAfterUnregister));
    }

    @Test
    public void testSendbroad() 
    {
        System.out.println("SendBroadcast test - has started");
        // TEST - normal send event
        // checking that each testing MicroService is subscribed to 
        int counter = 0;
        messageBusImplTest.sendBroadcast(new BroadcastTestNUM1("broadcast1"));
        messageBusImplTest.sendBroadcast(new BroadcastTestNUM2("broadcast2"));
        try {
            Thread.sleep(50);
        } catch (Exception e) {
            e.printStackTrace();  
        }
        for (int i = 0; i < microServices.length; i++) {
            System.out.println("counter broadcast 1=" + microServices[i].getcounterBroadcast1());
            if (microServices[i].getcounterBroadcast1() == 1) {
                System.out.println("Broadcast 1 sent - sucess for " + microServices[i].getName());
                counter++;
            }
            System.out.println("counter broadcast 2=" + microServices[i].getcounterBroadcast2());
            if (microServices[i].getcounterBroadcast2() == 1)
             {
                System.out.println("Broadcast 2 sent - sucess for " + microServices[i].getName());
                counter++;
            }
        }
        assertTrue(counter == 10);

        // TEST - send broadcast to unregistered microservice
        messageBusImplTest.unregister(MicroService1);
        BroadcastTestNUM2 broadcast2 = new BroadcastTestNUM2("broadcast2");
        assertNull(messageBusImplTest.getMessageQueue().get(MicroService1));

        // TEST - Re-register MicroService1 and send broadcast again
        messageBusImplTest.register(MicroService1);
        messageBusImplTest.sendBroadcast(broadcast2);
        assertNotNull(messageBusImplTest.getMessageQueue().get(MicroService1));

         // TEST - unregister a MS and send broadcast after
         messageBusImplTest.unregister(MicroService2);
         assertNull(messageBusImplTest.getMessageQueue().get(MicroService2));
         BroadcastTestNUM2 broadcastAfterUnregister = new BroadcastTestNUM2("broadcast after unregister");
         messageBusImplTest.sendBroadcast(broadcastAfterUnregister);
            assertNull(messageBusImplTest.getMessageQueue().get(MicroService2));

    }
}