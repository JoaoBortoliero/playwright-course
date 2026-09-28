# Solucoes de referencia

Este e um projeto Maven independente. Ele nao participa do build do aluno e seus arquivos nao sao copiados para o projeto principal.

Execute uma solucao somente depois da sua tentativa:

```powershell
.\course.ps1 solution 04
```

As solucoes mostram uma implementacao defensavel, nao a unica implementacao aceita. Compare responsabilidades, locators, oraculos, isolamento e teardown.

Alguns gabaritos usam `ReferenceSession` para evitar repeticao do bootstrap. A solucao da Aula 3 preserva o lifecycle explicito para fins didaticos.

Todas as solucoes com interface grafica utilizam exclusivamente o Microsoft Edge instalado na maquina. A execucao e visivel por padrao e pode ser feita sem interface com:

```powershell
.\course.ps1 solution 04 -Headless
```

As Aulas 1 a 5, 7 e 12 usam SauceDemo. As Aulas 6 e 8 a 11 usam HTML e API locais para manter os cenarios deterministicos. As credenciais presentes sao as credenciais publicas exibidas pelo SauceDemo.
