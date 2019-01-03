package com.lightbend.akka.sample;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;

public class DeviceGroupTest {

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
    public void testRegisterDeviceActor() {
    	TestKit probe = new TestKit(system);
    	ActorRef groupActor = system.actorOf(DeviceGroup.props("group"));
    	
    	groupActor.tell(new DeviceManager.RequestTrackDevice(1L, "group", "device1"), probe.getRef());
    	probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
    	ActorRef deviceActor1 = probe.getLastSender();
    	
    	groupActor.tell(new DeviceManager.RequestTrackDevice(2L, "group", "device2"), probe.getRef());
    	probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
    	ActorRef deviceActor2 = probe.getLastSender();
    	
    	// Check that device actors are working
    	deviceActor1.tell(new Device.RecordTemperature(3L, 1.0), probe.getRef());
    	assertEquals(3L, probe.expectMsgClass(Device.TemperatureRecorded.class).requestId);
    	deviceActor2.tell(new Device.RecordTemperature(4L, 2.0), probe.getRef());
    	assertEquals(4L, probe.expectMsgClass(Device.TemperatureRecorded.class).requestId);
    }
    
    @Test
    public void testIgnoreRequestsForWrongGroupId() {
    	TestKit probe = new TestKit(system);
    	ActorRef groupActor = system.actorOf(DeviceGroup.props("group"));
    	
    	groupActor.tell(new DeviceManager.RequestTrackDevice(1L, "wrongGroup", "device"), probe.getRef());
    	probe.expectNoMessage();
    }
    
    @Test
    public void testReturnSameActorForSameDeviceId() {
    	TestKit probe = new TestKit(system);
    	ActorRef groupActor = system.actorOf(DeviceGroup.props("group"));
    	
    	groupActor.tell(new DeviceManager.RequestTrackDevice(1L, "group", "device1"), probe.getRef());
    	probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
    	ActorRef deviceActor1 = probe.getLastSender();
    	
    	groupActor.tell(new DeviceManager.RequestTrackDevice(2L, "group", "device1"), probe.getRef());
    	probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
    	ActorRef deviceActor2 = probe.getLastSender();
    	
    	assertEquals(deviceActor1, deviceActor2);
    }
}
