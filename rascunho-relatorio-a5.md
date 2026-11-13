# Rascunho do relatório A5 — Cobertura com JaCoCo

Rascunho para o grupo revisar e reescrever. Os campos entre colchetes precisam ser preenchidos ou conferidos pelos integrantes.

## 1. Identificação

**Grupo:** [confirmar número do grupo]  
**Integrantes:** [confirmar nomes completos com o grupo]  
**Sistema:** AMICOurses  
**Repositório original indicado no README:** https://github.com/analpa1997/AMICOurses  
**Fork utilizado pelo grupo:** https://github.com/olvrayana/COS482-webapp-2  
**Commit medido:** `eb0648a6189e192230f1bb195c4a0144289c449a`  
**Data do commit:** 09/05/2019.

Para identificar a versão original do sistema, inicialmente executamos o comando indicado no enunciado:

```powershell
git log --oneline
```

Como não conseguimos identificar o ponto de início das contribuições do grupo apenas pelas mensagens dos commits, consultamos também o histórico de commits no GitHub. Localizamos onde começaram as contribuições dos integrantes e identificamos o commit `eb0648a` como o último da linha principal anterior às alterações do grupo.

Em seguida, criamos uma branch a partir desse commit para realizar a medição:

```powershell
git switch -c a5-linha-de-base eb0648a
```

## 2. Como foi medido

A medição foi realizada no Windows, utilizando:

| Ferramenta | Versão |
|---|---|
| JDK | Eclipse Temurin — OpenJDK 1.8.0_504 |
| Maven | 3.5.2, executado pelo Maven Wrapper |
| JaCoCo | 0.8.12 |

O JaCoCo é uma ferramenta de cobertura para Java. Ele registra quais partes do código foram exercitadas durante a execução e gera relatórios com contadores de instruções, desvios, linhas, complexidade, métodos e classes.

Com a branch `a5-linha-de-base` selecionada, acessamos a pasta do projeto Java a partir da raiz do repositório:

```powershell
cd .\AMICOServer
```

Depois, executamos:

```powershell
.\mvnw.cmd clean org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent test org.jacoco:jacoco-maven-plugin:0.8.12:report "-Dmaven.test.failure.ignore=true"
```

Utilizamos `.\mvnw.cmd` em vez de `mvn` porque o comando `mvn` não estava disponível no terminal. O Maven Wrapper já fazia parte do projeto e permitiu executar a versão de Maven definida por ele.

A etapa `clean` remove os resultados anteriores; `prepare-agent` prepara a coleta de cobertura; `test` solicita a execução dos testes; e `report` gera o relatório. A opção `maven.test.failure.ignore=true` permite continuar a geração do relatório mesmo quando ocorrem falhas de testes.

Na medição inicial, não foram realizadas alterações no arquivo `pom.xml`, no código de produção ou nos testes.

Após orientação do professor para tentar corrigir a execução, ajustamos apenas a configuração do Maven Surefire no `pom.xml`. A configuração herdada selecionava classes terminadas em `Test` ou `Tests`, enquanto as classes originais com métodos de teste começam com `Test`. Acrescentamos o padrão `**/Test*.java`, mantendo os padrões anteriores:

```xml
<configuration>
    <includes>
        <include>**/Test*.java</include>
        <include>**/*Test.java</include>
        <include>**/*Tests.java</include>
    </includes>
</configuration>
```

Essa alteração permite descobrir `TestE2EFront`, `TestAPI` e `TestAPIRestTemplate`, sem renomear arquivos ou modificar o código de produção e os testes. A medição final utilizou o mesmo comando com `clean` apresentado acima.

Após a correção, realizamos tentativas de execução e identificamos que o teste HTTP dependia do backend disponível na porta 8000. Iniciamos o backend separadamente com `.\mvnw.cmd spring-boot:run`, em outro terminal dentro de `AMICOServer`, e repetimos a medição completa. Os resultados apresentados neste relatório correspondem à execução final de 13/11/2026, cujo XML do JaCoCo foi gerado às 15h35min41s, no horário de Brasília. Os três XMLs do Surefire foram gerados entre 15h35min17s e 15h35min35s, na mesma execução.

Mantivemos as versões originais das dependências de navegador. O ambiente apresentou incompatibilidade entre o ChromeDriver selecionado, que suporta Chrome 114, e o Chrome instalado, versão 157. Não alteramos a lógica dos testes para contornar esse erro.

O comando do JaCoCo acompanha o processo Java dos testes. O teste `TestAPI` carrega o Spring nesse processo; os testes HTTP e de navegador acessam serviços externos, cuja execução não é automaticamente coletada por esse agente. Essa limitação deve acompanhar a interpretação da cobertura.

O relatório foi gerado em:

```text
AMICOServer/target/site/jacoco/index.html
```

## 3. Execução dos testes

A execução inicial, anterior à correção dos filtros, registrou zero testes executados. O código original possui cinco métodos anotados com `@Test`.

Na execução completa final de 13/11/2026, às 15h35, os arquivos XML do Surefire registraram:

| Resultado | Quantidade |
|---|---:|
| Testes executados | 5 |
| Aprovados | 2 |
| Falhas de asserção (`Failures`) | 0 |
| Erros de execução (`Errors`) | 3 |
| Ignorados (`Skipped`) | 0 |

Os testes `TestAPI.checkShowProfile` e `TestAPIRestTemplate.checkShowProfile` passaram. Os três testes de `TestE2EFront` (`checkCreateCourse`, `checkDownload` e `checkShowProfile`) apresentaram `SessionNotCreatedException` ao iniciar o navegador, devido à incompatibilidade do ChromeDriver. Portanto, não conseguiram chegar às verificações funcionais esperadas. Esses erros de ambiente não foram classificados como falhas de asserção nem como testes ignorados.

**[Incluir ou preservar uma captura da saída do terminal com o resumo da execução final. As contagens acima foram conferidas nos três XMLs do Surefire.]**

## 4. Cobertura geral

A tabela apresenta os contadores totais obtidos no XML do JaCoCo da execução completa final de 13/11/2026, gerado às 15h35min41s, após a correção dos filtros. Os valores permaneceram iguais aos da tentativa anterior, embora o teste HTTP tenha passado, pois a execução do backend externo não foi instrumentada pelo agente dos testes.

| Contador | Coberto | Total | Percentual |
|---|---:|---:|---:|
| Instructions | 2.981 | 11.956 | 24,93% |
| Branches | 23 | 1.116 | 2,06% |
| Lines | 442 | 2.349 | 18,82% |
| Cxty — complexidade | 98 | 917 | 10,69% |
| Methods | 88 | 359 | 24,51% |
| Classes | 33 | 34 | 97,06% |

Os contadores representam:

- **Instructions:** instruções do bytecode Java exercitadas.
- **Branches:** saídas de decisões exercitadas, como os caminhos verdadeiro e falso de um `if`.
- **Lines:** linhas de código exercitadas.
- **Cxty:** complexidade ciclomática contabilizada pelo JaCoCo.
- **Methods:** métodos exercitados.
- **Classes:** classes exercitadas.

Os percentuais descrevem a cobertura registrada nessa execução. A interpretação do resultado deve ser apresentada em conjunto com a seção de execução dos testes.

Uma classe ser contabilizada como coberta não significa que todos os seus comportamentos foram verificados. A inicialização do contexto Spring também executa código e contribui para os contadores.

**[Inserir captura da página inicial do relatório HTML, mostrando a linha Total.]**

## 5. Classes de alta complexidade

A tabela deve conter uma linha por integrante, associando cada pessoa à classe assumida na Entrega 1. As classes devem apresentar complexidade ciclomática de pelo menos 10.

| Integrante | Classe | Cxty | Linhas cobertas | Branches cobertos |
|---|---|---:|---|---|
| [Nome do integrante] | `MoodleController` | 85 | 1 de 194 (0,52%) | 0 de 154 (0%) |
| [Nome do integrante] | [Classe assumida] | [preencher] | x de y (z%) | x de y (z%) |
| [Nome do integrante] | [Classe assumida] | [preencher] | x de y (z%) | x de y (z%) |
| [Adicionar os demais integrantes] | | | | |

**[Confirmar as associações entre integrantes e classes e conferir todos os valores no relatório.]**

Os valores de `MoodleController` acima foram conferidos no XML da execução final das 15h35. Com 0 de 154 branches cobertos, faltam 124 branches para atingir pelo menos 80%.

## 6. Análise por integrante

**[Cada integrante do grupo escreve o próprio parágrafo e inclui a captura do relatório da sua classe.]**

O parágrafo deve apresentar:

1. Qual decisão do código foi selecionada.
2. Qual saída dessa decisão não foi exercitada.
3. Qual entrada permitiria exercitar essa saída.
4. Qual seria o resultado esperado, com base na especificação ou documentação disponível.
5. Quantos branches adicionais precisam ser cobertos para a classe atingir pelo menos 80%.

**[Inserir a captura do trecho do relatório que apresenta o desvio amarelo ou vermelho.]**

## Declaração de uso de IA

Utilizamos o ChatGPT, por meio do Codex, para auxiliar na organização das ideias e na estruturação de um rascunho do relatório. A assistência também apoiou a execução do comando de medição e a leitura dos contadores do JaCoCo.

**Modelo utilizado:** [preencher conforme identificação disponível na sessão].  
**Prompt utilizado:** [inserir o pedido enviado para organizar o rascunho].  
**Conferência pelo grupo:** [descrever como os integrantes verificaram o commit, os comandos e os números no relatório gerado].

O grupo deverá revisar o rascunho, conferir os dados e escrever a versão final e as análises individuais.
