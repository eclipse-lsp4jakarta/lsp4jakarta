package org.example.servlet;

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
 *
 * Note: jakarta.inject-api removed — version 2.0.1 is identical across EE 9, EE 10, and EE 11.
 */
@WebServlet("/hello")
public class SampleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/plain");
        PrintWriter writer = resp.getWriter();
        String name = req.getParameter("name");
        writer.println("Hello, " + (name != null ? name : "World") + "!");
    }
}
