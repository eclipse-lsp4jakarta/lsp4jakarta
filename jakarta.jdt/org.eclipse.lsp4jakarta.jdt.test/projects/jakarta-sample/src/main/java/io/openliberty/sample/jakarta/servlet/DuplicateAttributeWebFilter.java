package io.openliberty.sample.jakarta.servlet;

import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.Filter;

@WebFilter(urlPatterns = "", value = "")
public abstract class DuplicateAttributeWebFilter implements Filter {

}
