# Soluções de referência

Este é um projeto Maven independente. Ele não participa do build do aluno e o
script do curso nunca copia seus arquivos. Execute somente após sua tentativa:

```powershell
.\course.ps1 solution 04
```

As soluções mostram uma implementação defensável, não a única implementação
aceita. Compare primeiro: fronteiras de responsabilidade, locators, oráculos,
isolamento e teardown. Os comentários `Decisão` registram o motivo dos pontos
que normalmente geram alternativas.

Alguns gabaritos usam `ReferenceSession` apenas para evitar a repetição do
bootstrap. A implementação explícita do lifecycle continua disponível na
solução da Aula 3; o helper não é uma API que o aluno precise conhecer antes.

As Aulas 1–5 e 7/12 usam SauceDemo. As Aulas 6, 8–11 usam HTML/API locais para
serem determinísticas. Credenciais presentes são as públicas do SauceDemo.
