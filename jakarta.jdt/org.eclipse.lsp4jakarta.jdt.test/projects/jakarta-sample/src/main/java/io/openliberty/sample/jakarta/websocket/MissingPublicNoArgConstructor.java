package io.openliberty.sample.jakarta.websocket;

import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/path")
public class MissingPublicNoArgConstructor {

	String status;

	public MissingPublicNoArgConstructor(String status) {
		super();
		this.status = status;
	}

	
}
