package com.lightbend.akka.sample;

import java.io.IOException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class IotMain {

	public static void main(String[] args) throws IOException {
		ActorSystem system = ActorSystem.create("iot-system");
		
		try {
			//create top level supervisor
			ActorRef supervisor = system.actorOf(IotSupervisor.props(), "iot-supervisor");
			
			System.out.println("Press ENTER to exit the system");
			System.in.read();
			
		} finally {
			system.terminate();
		}
	}
}
