package br.com.curso.playwright.aula07.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class CartPage {
  private final Page page;
  public CartPage(Page page) { this.page = page; }
  public Locator items() { throw new UnsupportedOperationException("TODO Aula 7"); }
  public void removeProduct(String name) { throw new UnsupportedOperationException("TODO Aula 7"); }
  public void startCheckout() { throw new UnsupportedOperationException("TODO Aula 7"); }
}
