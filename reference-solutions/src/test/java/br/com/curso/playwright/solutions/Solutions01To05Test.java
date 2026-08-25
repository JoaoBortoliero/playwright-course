package br.com.curso.playwright.solutions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Solutions01To05Test {
  @Test @Tag("solution-01")
  void aula01PrimeiroLogin() {
    try (ReferenceSession session = new ReferenceSession()) {
      session.login("standard_user");
      assertThat(session.page).hasURL(java.util.regex.Pattern.compile(".*/inventory.html"));
      assertThat(session.page.getByTestId("title")).hasText("Products");
    }
  }

  @Test @Tag("solution-02")
  void aula02ErroELocatorComposto() {
    try (ReferenceSession session = new ReferenceSession()) {
      session.login("locked_out_user");
      assertThat(session.page.getByTestId("error"))
          .hasText("Epic sadface: Sorry, this user has been locked out.");
    }
    try (ReferenceSession session = new ReferenceSession()) {
      session.login("standard_user");
      Locator backpack = session.page.getByTestId("inventory-item")
          .filter(new Locator.FilterOptions().setHasText("Sauce Labs Backpack"));
      backpack.getByRole(AriaRole.BUTTON,
          new Locator.GetByRoleOptions().setName("Add to cart")).click();
      assertThat(session.page.getByTestId("shopping-cart-badge")).hasText("1");
    }
  }

  @ParameterizedTest @CsvSource({"az,A", "za,Z"}) @Tag("solution-04")
  void aula04Ordenacao(String option, String direction) {
    try (ReferenceSession session = new ReferenceSession()) {
      session.login("standard_user");
      session.page.getByTestId("product-sort-container").selectOption(option);
      List<String> names = session.page.getByTestId("inventory-item-name").allTextContents();
      List<String> expected = new ArrayList<>(names);
      expected.sort(direction.equals("A") ? Comparator.naturalOrder() : Comparator.reverseOrder());
      assertEquals(expected, names); // Decisão: a cópia preserva a evidência exibida.

      session.page.getByTestId("product-sort-container").selectOption("lohi");
      List<BigDecimal> prices = session.page.getByTestId("inventory-item-price")
          .allTextContents().stream().map(Solutions01To05Test::money).toList();
      List<BigDecimal> sorted = new ArrayList<>(prices);
      sorted.sort(Comparator.naturalOrder());
      assertEquals(sorted, prices);
    }
  }

  @Test @Tag("solution-04")
  void aula04CatalogoCompletoEPrecoCrescente() {
    try (ReferenceSession session = new ReferenceSession()) {
      session.login("standard_user");
      Locator names = session.page.getByTestId("inventory-item-name");
      assertThat(names).hasCount(6);
      assertEquals(Set.of(
              "Sauce Labs Backpack", "Sauce Labs Bike Light", "Sauce Labs Bolt T-Shirt",
              "Sauce Labs Fleece Jacket", "Sauce Labs Onesie",
              "Test.allTheThings() T-Shirt (Red)"),
          Set.copyOf(names.allTextContents()));

      session.page.getByTestId("product-sort-container").selectOption("lohi");
      List<BigDecimal> displayed = session.page.getByTestId("inventory-item-price")
          .allTextContents().stream().map(Solutions01To05Test::money).toList();
      List<BigDecimal> expected = new ArrayList<>(displayed);
      expected.sort(Comparator.naturalOrder());
      assertEquals(expected, displayed);
    }
  }

  @ParameterizedTest(name = "login de {0}")
  @MethodSource("loginCases")
  @Tag("solution-04")
  void aula04LoginOrientadoADados(
      String username, String expectedTestId, String expectedText) {
    try (ReferenceSession session = new ReferenceSession()) {
      session.login(username);
      assertThat(session.page.getByTestId(expectedTestId)).hasText(expectedText);
    }
  }

  static Stream<Arguments> loginCases() {
    return Stream.of(
        Arguments.of("standard_user", "title", "Products"),
        Arguments.of("locked_out_user", "error",
            "Epic sadface: Sorry, this user has been locked out."));
  }

  @Test @Tag("solution-05")
  void aula05CompraCompletaComTotais() {
    try (ReferenceSession session = new ReferenceSession()) {
      session.login("standard_user");
      add(session.page, "Sauce Labs Backpack");
      add(session.page, "Sauce Labs Bike Light");
      assertThat(session.page.getByTestId("shopping-cart-badge")).hasText("2");
      session.page.getByTestId("shopping-cart-link").click();
      assertThat(session.page.getByTestId("inventory-item")).hasCount(2);
      session.page.getByTestId("checkout").click();
      session.page.getByTestId("firstName").fill("Ada");
      session.page.getByTestId("lastName").fill("Lovelace");
      session.page.getByTestId("postalCode").fill("01000-000");
      session.page.getByTestId("continue").click();

      BigDecimal subtotal = money(session.page.getByTestId("subtotal-label").textContent());
      BigDecimal tax = money(session.page.getByTestId("tax-label").textContent());
      BigDecimal total = money(session.page.getByTestId("total-label").textContent());
      assertEquals(0, subtotal.add(tax).compareTo(total));
      assertTrue(subtotal.signum() > 0);
      session.page.getByTestId("finish").click();
      assertThat(session.page.getByTestId("complete-header"))
          .hasText("Thank you for your order!");
    }
  }

  @ParameterizedTest(name = "checkout {0}/{1}/{2}")
  @MethodSource("requiredCustomerFields")
  @Tag("solution-05")
  void aula05CamposObrigatorios(
      String firstName, String lastName, String postalCode, String error) {
    try (ReferenceSession session = new ReferenceSession()) {
      session.login("standard_user");
      add(session.page, "Sauce Labs Backpack");
      session.page.getByTestId("shopping-cart-link").click();
      session.page.getByTestId("checkout").click();
      session.page.getByTestId("firstName").fill(firstName);
      session.page.getByTestId("lastName").fill(lastName);
      session.page.getByTestId("postalCode").fill(postalCode);
      session.page.getByTestId("continue").click();
      assertThat(session.page.getByTestId("error")).containsText(error);
    }
  }

  static Stream<Arguments> requiredCustomerFields() {
    return Stream.of(
        Arguments.of("", "Lovelace", "01000-000", "First Name is required"),
        Arguments.of("Ada", "", "01000-000", "Last Name is required"),
        Arguments.of("Ada", "Lovelace", "", "Postal Code is required"));
  }

  private static void add(Page page, String product) {
    Locator item = page.getByTestId("inventory-item")
        .filter(new Locator.FilterOptions().setHasText(product));
    item.getByRole(AriaRole.BUTTON,
        new Locator.GetByRoleOptions().setName("Add to cart")).click();
  }

  private static BigDecimal money(String text) {
    return new BigDecimal(text.replaceAll("[^0-9.]", ""));
  }
}
