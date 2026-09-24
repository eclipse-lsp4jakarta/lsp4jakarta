package org.example.servlet;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Sample Servlet using:
 *  - Jakarta Servlet 5.0  (EE 9)
 *  - Jakarta Inject 2.0   (EE 9)
 */
@WebServlet("/hello")
public class SampleServlet extends HttpServlet {

    @Inject
    private GreetingService greetingService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/plain");
        PrintWriter writer = resp.getWriter();
        writer.println(greetingService.greet(req.getParameter("name")));
    }
}
