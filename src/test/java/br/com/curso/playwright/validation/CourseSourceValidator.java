package br.com.curso.playwright.validation;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Correção objetiva. A rubrica continua necessária para qualidade semântica. */
class CourseSourceValidator {
  static {
    StaticJavaParser.getParserConfiguration()
        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
  }
  private static final Map<String, String> TARGETS = Map.ofEntries(
      Map.entry("01", "aula01/LoginSauceDemoExercicioTest.java"),
      Map.entry("02", "aula02/LoginNegativoExercicioTest.java"),
      Map.entry("03", "aula03/LifecycleEConfiguracaoExercicioTest.java"),
      Map.entry("04", "aula04/CatalogoEOrdenacaoExercicioTest.java"),
      Map.entry("05", "aula05/CheckoutExercicioTest.java"),
      Map.entry("06", "aula06/InteracoesAvancadasExercicioTest.java"),
      Map.entry("07", "aula07/ArquiteturaSuiteExercicioTest.java"),
      Map.entry("08", "aula08/ApiEStorageStateExercicioTest.java"),
      Map.entry("09", "aula09/RedeEMockingExercicioTest.java"),
      Map.entry("10", "aula10/PortabilidadeEParalelismoExercicioTest.java"),
      Map.entry("11", "aula11/EvidenciasExercicioTest.java"),
      Map.entry("12", "aula12/ProjetoFinalExercicioTest.java"));

  @Test void deveAtenderAoContratoObjetivoDaAula() throws Exception {
    String module = System.getProperty("course.module");
    assertTrue(TARGETS.containsKey(module), "Use -Dcourse.module=01..12");
    Path source = Path.of("src/test/java/br/com/curso/playwright").resolve(TARGETS.get(module));
    assertTrue(Files.exists(source), "Arquivo esperado não encontrado: " + source);
    String text = Files.readString(source);
    CompilationUnit unit = StaticJavaParser.parse(source);

    Set<String> annotations = unit.findAll(AnnotationExpr.class).stream()
        .map(a -> a.getName().getIdentifier()).collect(java.util.stream.Collectors.toSet());
    Set<String> calls = unit.findAll(MethodCallExpr.class).stream()
        .map(MethodCallExpr::getNameAsString).collect(java.util.stream.Collectors.toSet());

    assertFalse(annotations.contains("Disabled"),
        "Remova @Disabled somente quando a implementação estiver completa.");
    assertFalse(calls.contains("fail"), "Remova os fail() usados como marcador de TODO.");
    assertFalse(text.contains("Thread.sleep") || calls.contains("waitForTimeout"),
        "Esperas fixas são proibidas: aguarde ação, evento ou estado observável.");
    assertFalse(text.matches("(?s).*setForce\\s*\\(\\s*true\\s*\\).*"),
        "force=true ignora acionabilidade; corrija o estado/locator.");
    assertFalse(text.contains("xpath=") || text.contains("//html") || text.contains("/html/body"),
        "XPath estrutural é frágil; use semântica, test id ou escopo de domínio.");
    assertTrue(calls.stream().anyMatch(name -> name.startsWith("assert") || name.startsWith("has")
            || name.startsWith("isVisible") || name.startsWith("contains")),
        "O exercício precisa de assertions; ações sem oráculo não constituem teste.");

    int number = Integer.parseInt(module);
    if (number == 1) {
      assertTrue(calls.containsAll(Set.of(
              "create", "newContext", "newPage", "navigate", "getByRole", "getByTestId")),
          "Aula 1: construa o lifecycle e use role/test ID ensinados antes do exercício.");
      assertTrue(calls.contains("hasURL") && calls.contains("hasText"),
          "Aula 1: valide navegação e estado visível com assertions web-first.");
    }
    if (number == 2) {
      assertTrue(calls.containsAll(Set.of("getByRole", "getByTestId", "filter")),
          "Aula 2: pratique role, test ID e composição por filter no desafio.");
      assertTrue(calls.contains("hasText"),
          "Aula 2: mensagens e badge devem usar assertion web-first.");
    }
    if (number == 3) {
      assertTrue(annotations.containsAll(Set.of(
              "TestInstance", "BeforeAll", "BeforeEach", "AfterEach", "AfterAll")),
          "Aula 3: implemente os quatro hooks e lifecycle PER_CLASS explicados.");
      assertTrue(calls.contains("newContext") && calls.contains("newPage"),
          "Aula 3: cada teste precisa receber contexto e página novos.");
      assertTrue(text.contains("TestConfig") && text.contains("BrowserFactory"),
          "Aula 3: URL/browser/headless devem vir da configuração fornecida.");
    }
    if (number == 4) {
      assertTrue(usesType(unit, "BigDecimal"),
          "Aula 4: converta os textos de preço para BigDecimal.");
      assertTrue(text.contains("ParameterizedTest"), "Aula 4: inclua um teste parametrizado legível.");
      assertTrue(calls.contains("selectOption") && calls.contains("allTextContents")
              && (calls.contains("sort") || calls.contains("sorted")),
          "Aula 4: selecione a ordenação, capture a coleção e ordene somente uma cópia.");
    }
    if (number == 5) {
      assertTrue(usesType(unit, "BigDecimal"),
          "Aula 5: recalcule subtotal, imposto e total com BigDecimal.");
      assertTrue(annotations.contains("ParameterizedTest"),
          "Aula 5: represente as validações obrigatórias com parametrização legível.");
      assertTrue(calls.contains("add"),
          "Aula 5: prove numericamente que subtotal + imposto = total.");
    }
    if (number == 6) {
      assertTrue(text.contains("waitForDownload") && text.contains("waitForPopup"),
          "Aula 6: observe download e popup antes das ações.");
      assertTrue(text.contains("frameLocator") && text.contains("setInputFiles"),
          "Aula 6: pratique iframe e upload pela API do Playwright.");
    }
    if (number == 7) assertTrue(text.contains("PlaywrightTest"),
        "Aula 7: use a extensão com @PlaywrightTest.");
    if (number == 8) {
      assertTrue(usesType(unit, "APIRequestContext"),
          "Aula 8: use o cliente HTTP APIRequestContext.");
      assertTrue(calls.containsAll(Set.of("get", "post", "status", "dispose", "storageState")),
          "Aula 8: pratique GET/POST/status, ownership das respostas e storageState.");
    }
    if (number == 9) assertTrue(text.contains("waitForResponse") && text.contains("route"),
        "Aula 9: observe uma integração real e mantenha mock separado.");
    if (number == 10) assertTrue(text.contains("smoke") && text.contains("setViewportSize"),
        "Aula 10: marque a smoke e configure o viewport no contexto.");
    if (number == 11) {
      assertTrue(text.contains("PlaywrightTest"),
          "Aula 11: use a extensão responsável pela política automática de evidências.");
      assertTrue(calls.contains("step") && calls.contains("addAttachment"),
          "Aula 11: organize o relatório com steps de negócio e anexo sem segredo.");
    }
    if (number == 12) {
      assertTrue(text.contains("smoke") && text.contains("regression")
              && text.contains("negative"), "Aula 12: classifique a suíte final.");
      Path jenkinsfile = Path.of("Jenkinsfile");
      assertTrue(Files.exists(jenkinsfile),
          "Aula 12: mantenha a pipeline como código no Jenkinsfile.");
      String pipeline = Files.readString(jenkinsfile);
      assertTrue(pipeline.contains("triggers") && pipeline.contains("cron("),
          "Aula 12: configure e explique a execução periódica com triggers/cron.");
      assertTrue(pipeline.contains("disableConcurrentBuilds"),
          "Aula 12: impeça sobreposição das execuções agendadas.");
    }
  }

  private static boolean usesType(CompilationUnit unit, String simpleName) {
    return unit.findAll(ClassOrInterfaceType.class).stream()
        .anyMatch(type -> type.getNameAsString().equals(simpleName));
  }
}
