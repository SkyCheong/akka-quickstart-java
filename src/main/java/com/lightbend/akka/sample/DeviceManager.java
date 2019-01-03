package com.lightbend.akka.sample;

import akka.actor.AbstractActor;

public class DeviceManager extends AbstractActor {

	public static final class RequestTrackDevice {
		public final long requestId;
		public final String groupId;
		public final String deviceId;
		
		public RequestTrackDevice(long requestId, String groupId, String deviceId) {
			this.requestId = requestId;
			this.groupId = groupId;
			this.deviceId = deviceId;
		}
	}
	
	public static final class DeviceRegistered {
		public final long requestId;
		
		public DeviceRegistered(long requestId) {
			this.requestId = requestId;
		}
	}
	
	@Override
	public Receive createReceive() {
		// TODO Auto-generated method stub
		return null;
	}

}
