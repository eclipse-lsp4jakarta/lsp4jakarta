package io.openliberty.sample.jakarta.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;

import java.io.IOException;

import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/path")
public class DuplicateAnnotationTest {

	@OnOpen
	public void onOpen(Session session) throws IOException {
		// TODO
	}

	@OnMessage
	public void onMessage(Session session, String message) throws IOException {
		// TODO
	}

	@OnClose
	public void onClose(Session session) throws IOException {
		// TODO
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		// TODO
	}
	
	@OnOpen
	public void onOpen2(Session session) throws IOException {
		// TODO
	}
	
	@OnClose
	public void onClose2(Session session) throws IOException {
		// TODO
	}
	
	@OnError
	public void onError2(Session session, Throwable throwable) {
		// TODO
	}
}
