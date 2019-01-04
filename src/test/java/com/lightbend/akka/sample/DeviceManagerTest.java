package com.lightbend.akka.sample;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.testkit.javadsl.TestKit;

public class DeviceManagerTest {

	static ActorSystem system;
	
    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }
    
    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }
    
    @Test
    public void testRegisterDeviceGroup() {
    	TestKit probe = new TestKit(system);
    	ActorRef deviceManagerActor = system.actorOf(DeviceManager.props());
    	deviceManagerActor.tell(new DeviceManager.RequestTrackDevice("group1", "device1"), probe.getRef());
    	DeviceManager.DeviceRegistered a = probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
    	
    }
    
    
}
