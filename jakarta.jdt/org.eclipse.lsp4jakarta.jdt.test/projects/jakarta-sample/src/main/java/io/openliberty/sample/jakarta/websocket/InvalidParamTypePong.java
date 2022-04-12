package io.openliberty.sample.jakarta.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;

import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.OnOpen;
import jakarta.websocket.PongMessage;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.Session;

@ServerEndpoint(value = "/demo/{test}/var/{abcd}")
public class InvalidParamTypePong {
    private static Session session; 

    @OnOpen
    public void OnOpen(Session session) throws IOException {
        System.out.println("Websocket opened: " + session.getId().toString());
    }
    
    @OnMessage
    public void OnMessage(PongMessage pong, int invalid) throws IOException {
        System.out.println("Websocket opened: " + session.getId().toString());
    }
    
    @OnClose
    public void OnClose(Session session) throws IOException {
        System.out.println("WebSocket closed for " + session.getId());
    }
}