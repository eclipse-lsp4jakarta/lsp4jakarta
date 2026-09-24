package org.example.servlet;

/**
 * Simple service injected into the servlet.
 */
public class GreetingService {

    public String greet(String name) {
        return "Hello, " + (name != null ? name : "World") + "!";
    }
}
