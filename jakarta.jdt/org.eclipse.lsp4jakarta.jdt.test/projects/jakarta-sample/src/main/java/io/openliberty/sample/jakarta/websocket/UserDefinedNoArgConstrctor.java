package io.openliberty.sample.jakarta.websocket;

import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/path")
public class UserDefinedNoArgConstrctor {

	public UserDefinedNoArgConstrctor() {
		super();
	}
	
}
