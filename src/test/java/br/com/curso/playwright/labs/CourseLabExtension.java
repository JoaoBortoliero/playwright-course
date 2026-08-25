package br.com.curso.playwright.labs;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

public final class CourseLabExtension
    implements BeforeAllCallback, AfterAllCallback, ParameterResolver {
  private CourseLabServer server;
  @Override public void beforeAll(ExtensionContext context) { server = CourseLabServer.start(); }
  @Override public void afterAll(ExtensionContext context) { if (server != null) server.close(); }
  @Override public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
    return parameter.getParameter().getType() == CourseLabServer.class;
  }
  @Override public Object resolveParameter(ParameterContext parameter, ExtensionContext context) {
    return server;
  }
}
