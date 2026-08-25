package br.com.curso.playwright.aula07.pages;

import br.com.curso.playwright.aula07.data.Customer;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class CheckoutPage {
  private final Page page;
  public CheckoutPage(Page page) { this.page = page; }
  public void identify(Customer customer) { throw new UnsupportedOperationException("TODO Aula 7"); }
  public Locator total() { throw new UnsupportedOperationException("TODO Aula 7"); }
  public void finish() { throw new UnsupportedOperationException("TODO Aula 7"); }
  public Locator confirmation() { throw new UnsupportedOperationException("TODO Aula 7"); }
}
