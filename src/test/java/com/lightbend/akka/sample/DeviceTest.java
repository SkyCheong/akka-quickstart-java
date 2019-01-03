package com.lightbend.akka.sample;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;

public class DeviceTest {

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
    public void testReplyToRegistrationRequests() {
    	TestKit probe = new TestKit(system);
    	ActorRef deviceActor = system.actorOf(Device.props("group", "device"));
    	
    	deviceActor.tell(new DeviceManager.RequestTrackDevice(1L, "group", "device"), probe.getRef());
    	probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
    	assertEquals(deviceActor, probe.getLastSender());
    }
    
    @Test
    public void testIgnoreWrongRegistrationRequests() {
    	TestKit probe = new TestKit(system);
    	ActorRef deviceActor = system.actorOf(Device.props("group", "device"));
    	
    	deviceActor.tell(new DeviceManager.RequestTrackDevice(1L, "wrongGroup", "device"), probe.getRef());
    	probe.expectNoMessage();
    	
    	deviceActor.tell(new DeviceManager.RequestTrackDevice(1L, "group", "wrongDevice"), probe.getRef());
    	probe.expectNoMessage();
    	
    }
	
	@Test
	public void testReplyWithEmptyReadingIfNoTemperatureIsKnown() {
		TestKit probe = new TestKit(system);
		ActorRef deviceActor = system.actorOf(Device.props("group", "device"));
		deviceActor.tell(new Device.ReadTemperature(42L), probe.getRef());
		Device.RespondTemperature response = probe.expectMsgClass(Device.RespondTemperature.class);
		assertEquals(42L, response.requestId);
		assertEquals(Optional.empty(), response.value);
		
	}
	
	@Test
	public void testReplyWithLatestTemperatureReading() {
		TestKit probe = new TestKit(system);
		ActorRef deviceActor = system.actorOf(Device.props("group", "device"));
		
		deviceActor.tell(new Device.RecordTemperature(1L, 26.8), probe.getRef());
		assertEquals(1L, probe.expectMsgClass(Device.TemperatureRecorded.class).requestId);
		
		deviceActor.tell(new Device.ReadTemperature(2L), probe.getRef());
		Device.RespondTemperature response1 = probe.expectMsgClass(Device.RespondTemperature.class);
		assertEquals(2L, response1.requestId);
		assertEquals(Optional.of(26.8), response1.value);
		
		deviceActor.tell(new Device.RecordTemperature(3L, 15.0), probe.getRef());
		assertEquals(3L, probe.expectMsgClass(Device.TemperatureRecorded.class).requestId);
		
		deviceActor.tell(new Device.ReadTemperature(4L), probe.getRef());
		Device.RespondTemperature response2 = probe.expectMsgClass(Device.RespondTemperature.class);
		assertEquals(4L, response2.requestId);
		assertEquals(Optional.of(15.0), response2.value);
	}
}
