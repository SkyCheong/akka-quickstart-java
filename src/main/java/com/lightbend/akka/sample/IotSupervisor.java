package com.lightbend.akka.sample;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class IotSupervisor extends AbstractActor {
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	static Props props() {
		return Props.create(IotSupervisor.class, IotSupervisor::new);
	}
	
	@Override
	public void preStart( ) {
		log.info("IoT Application Started");
	}
	
	@Override
	public void postStop() {
		log.info("IoT Application Stopped");
	}

	@Override
	public Receive createReceive() {
		//No message to handle
		return receiveBuilder().build();
	}

}
