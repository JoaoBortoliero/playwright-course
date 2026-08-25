package br.com.curso.playwright.validation;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

class ArchitectureValidator {
  @Test void pagesEComponentsNaoDevemDependerDeJUnit() {
    JavaClasses classes = new ClassFileImporter().importPackages("br.com.curso.playwright");
    ArchRuleDefinition.noClasses()
        .that().resideInAnyPackage("..pages..", "..components..")
        .should().dependOnClassesThat().resideInAnyPackage("org.junit..")
        .because("Page Objects executam ações; o teste decide os oráculos")
        .check(classes);
  }

  @Test void paginasNaoDevemDependerDeClassesDeTeste() {
    JavaClasses classes = new ClassFileImporter().importPackages("br.com.curso.playwright");
    ArchRuleDefinition.noClasses().that().resideInAnyPackage("..pages..")
        .should().dependOnClassesThat().haveSimpleNameEndingWith("Test")
        .check(classes);
  }
}
