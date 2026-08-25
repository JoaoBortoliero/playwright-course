package br.com.curso.playwright.aula07.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class InventoryPage {
  private final Page page;
  public InventoryPage(Page page) { this.page = page; }
  public void addProduct(String name) { throw new UnsupportedOperationException("TODO Aula 7"); }
  public Locator title() { throw new UnsupportedOperationException("TODO Aula 7"); }
}
