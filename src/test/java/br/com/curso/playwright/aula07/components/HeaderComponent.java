package br.com.curso.playwright.aula07.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class HeaderComponent {
  private final Page page;
  public HeaderComponent(Page page) { this.page = page; }
  public Locator cartBadge() { throw new UnsupportedOperationException("TODO Aula 7"); }
  public void openCart() { throw new UnsupportedOperationException("TODO Aula 7"); }
}
