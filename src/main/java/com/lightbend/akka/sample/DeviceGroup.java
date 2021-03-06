package com.lightbend.akka.sample;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class DeviceGroup extends AbstractActor {
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	
	final String groupId;
	
	public DeviceGroup(String groupId) {
		this.groupId = groupId;
	}
	
	public static Props props(String groupId) {
		return Props.create(DeviceGroup.class, () -> new DeviceGroup(groupId));
	}
	
	public static final class RequestDeviceList {
		final long requestId;
		
		public RequestDeviceList(long requestId) {
			this.requestId = requestId;
		}
	}
	
	public static final class ReplyDeviceList {
		final long requestId;
		final Set<String> ids;
		
		public ReplyDeviceList(long requestId, Set<String> ids) {
			this.requestId = requestId;
			this.ids = ids;
		}
	}
	
	final Map<String, ActorRef> deviceIdToActor = new HashMap<>();
	final Map<ActorRef, String> actorToDeviceId = new HashMap<>();
	
	@Override
	public void preStart() {	
		log.info("DeviceGroup {} started", groupId);
	}
	
	@Override
	public void postStop() {
		log.info("DeviceGroup {} stopped", groupId);
	}
	
	private void onTrackDevice(DeviceManager.RequestTrackDevice r) {
		if (this.groupId.equals(r.groupId)) {
			// Valid group
			ActorRef deviceActor = deviceIdToActor.get(r.deviceId);
			if (deviceActor != null) {
				deviceActor.forward(r, getContext());
			} else {
				log.info("Creating device actor for {}", r.deviceId);
				deviceActor = getContext().actorOf(Device.props(groupId, r.deviceId), "device-" + r.deviceId);
				
				//Death watch for actor
				getContext().watch(deviceActor);
				
				deviceIdToActor.put(r.deviceId, deviceActor);
				actorToDeviceId.put(deviceActor, r.deviceId);
				deviceActor.forward(r, getContext());
			}
		} else {
			log.warning(
					"Ignoring TrackDevice request for {}. This actor is responsible for {}.",
					r.groupId, this.groupId);
		}
	}
	
	private void onTerminated(Terminated t) {
		ActorRef deviceActor = t.getActor();
		String deviceId = actorToDeviceId.get(deviceActor);
		log.info("Device actor for {} has been terminated", deviceId);
		deviceIdToActor.remove(deviceId);
		actorToDeviceId.remove(deviceActor);
	}
	
	private void onDeviceList(RequestDeviceList r) {
		getSender().tell(new ReplyDeviceList(r.requestId, deviceIdToActor.keySet()), getSelf());
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(DeviceManager.RequestTrackDevice.class, this::onTrackDevice)
				.match(Terminated.class, this::onTerminated)
				.match(RequestDeviceList.class, this::onDeviceList)
				.build();
	}

}
