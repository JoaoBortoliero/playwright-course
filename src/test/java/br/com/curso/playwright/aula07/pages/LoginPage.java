package br.com.curso.playwright.aula07.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Starter do aluno: implemente operações; mantenha assertions fora daqui. */
public final class LoginPage {
  private final Page page;
  public LoginPage(Page page) { this.page = page; }
  public void open() { throw new UnsupportedOperationException("TODO Aula 7"); }
  public void loginAs(String username, String password) {
    throw new UnsupportedOperationException("TODO Aula 7");
  }
  public Locator error() { throw new UnsupportedOperationException("TODO Aula 7"); }
}
