package com.lightbend.akka.sample;

import java.util.HashMap;
import java.util.Map;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
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
	
	final Map<String, ActorRef> deviceIdToActor = new HashMap<>();
	
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
				deviceIdToActor.put(r.deviceId, deviceActor);
				deviceActor.forward(r, getContext());
			}
		} else {
			log.warning(
					"Ignoring TrackDevice request for {}. This actor is responsible for {}.",
					r.groupId, this.groupId);
		}
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(DeviceManager.RequestTrackDevice.class, this::onTrackDevice)
				.build();
	}

}