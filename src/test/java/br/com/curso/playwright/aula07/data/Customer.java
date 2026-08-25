package br.com.curso.playwright.aula07.data;

public record Customer(String firstName, String lastName, String postalCode) {
  public Customer {
    if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("firstName obrigatório");
    if (lastName == null || lastName.isBlank()) throw new IllegalArgumentException("lastName obrigatório");
    if (postalCode == null || postalCode.isBlank()) throw new IllegalArgumentException("postalCode obrigatório");
  }
}
