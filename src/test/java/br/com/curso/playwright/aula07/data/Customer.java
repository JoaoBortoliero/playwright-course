package br.com.curso.playwright.aula07.data;

public record Customer(String firstName, String lastName, String postalCode) {
    public Customer {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName obrigatorio");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("lastName obrigatorio");
        }
        if (postalCode == null || postalCode.isBlank()) {
            throw new IllegalArgumentException("postalCode obrigatorio");
        }
    }
}
