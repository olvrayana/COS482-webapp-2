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

**Não foram realizadas alterações no arquivo `pom.xml`, no código de produção ou nos testes.**

O relatório foi gerado em:

```text
AMICOServer/target/site/jacoco/index.html
```

## 3. Execução dos testes

[Seção pendente de preenchimento e revisão pelo grupo.]

Nesta seção, registrar os dados da execução: quantidade de testes do projeto original, quantidade executada, aprovações, falhas, erros e testes ignorados. A linha `Tests run` apresentada pelo Maven deve ser utilizada como evidência da execução, junto às informações necessárias para interpretar o resultado.

## 4. Cobertura geral

A tabela apresenta os contadores totais obtidos no relatório XML do JaCoCo, correspondentes à medição do commit original.

| Contador | Coberto | Total | Percentual |
|---|---:|---:|---:|
| Instructions | 0 | 11.956 | 0% |
| Branches | 0 | 1.116 | 0% |
| Lines | 0 | 2.349 | 0% |
| Cxty — complexidade | 0 | 917 | 0% |
| Methods | 0 | 359 | 0% |
| Classes | 0 | 34 | 0% |

Os contadores representam:

- **Instructions:** instruções do bytecode Java exercitadas.
- **Branches:** saídas de decisões exercitadas, como os caminhos verdadeiro e falso de um `if`.
- **Lines:** linhas de código exercitadas.
- **Cxty:** complexidade ciclomática contabilizada pelo JaCoCo.
- **Methods:** métodos exercitados.
- **Classes:** classes exercitadas.

Os percentuais descrevem a cobertura registrada nessa execução. A interpretação do resultado deve ser apresentada em conjunto com a seção de execução dos testes.

**[Inserir captura da página inicial do relatório HTML, mostrando a linha Total.]**

## 5. Classes de alta complexidade

A tabela deve conter uma linha por integrante, associando cada pessoa à classe assumida na Entrega 1. As classes devem apresentar complexidade ciclomática de pelo menos 10.

| Integrante | Classe | Cxty | Linhas cobertas | Branches cobertos |
|---|---|---:|---|---|
| [Nome do integrante] | `MoodleController` | 85 | 0 de 194 (0%) | 0 de 154 (0%) |
| [Nome do integrante] | [Classe assumida] | [preencher] | x de y (z%) | x de y (z%) |
| [Nome do integrante] | [Classe assumida] | [preencher] | x de y (z%) | x de y (z%) |
| [Adicionar os demais integrantes] | | | | |

**[Confirmar as associações entre integrantes e classes e conferir todos os valores no relatório.]**

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
