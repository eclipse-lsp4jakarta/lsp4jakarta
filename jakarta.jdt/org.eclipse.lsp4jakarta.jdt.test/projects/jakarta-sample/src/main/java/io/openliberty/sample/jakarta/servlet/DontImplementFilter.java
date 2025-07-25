package io.openliberty.sample.jakarta.servlet;

import jakarta.servlet.annotation.WebFilter;

@WebFilter(urlPatterns = { "/filter" })
public class DontImplementFilter {

}