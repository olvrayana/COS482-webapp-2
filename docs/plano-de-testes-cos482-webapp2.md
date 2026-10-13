# Plano de Testes – COS482 WebApp 2 (Entregas 1 e 2)

## 1. Identificador do Plano de Teste (Test plan identifier)

**PT-COS482-WEBAPP-2-V1.0**

## 2. Introdução (Introduction)

Este documento estabelece o plano de testes para o projeto **COS482-webapp-2** da disciplina COS482 (2026.2). O objetivo é aplicar conceitos de qualidade e testes de software, de forma incremental ao longo do semestre, no código-fonte do sistema escolhido. Este plano engloba o escopo, a abordagem, os recursos e o cronograma para as atividades das Entregas 1 e 2, cobrindo testes unitários, manuais, integração, sistema, cobertura e inspeção de código.

## 3. Itens de Teste (Test items)

Os itens a serem testados consistem nos módulos e classes do repositório bifurcado (fork) do grupo: https://github.com/olvrayana/COS482-webapp-2.

- **Módulos principais:** Controllers de rotas (Moodle, Admin, SignUp, CourseInformation) e o serviço de usuários (UserService).
- **Requisitos:** A base para o comportamento esperado será a documentação do repositório original e/ou os requisitos levantados pelo grupo.

## 4. Funcionalidades a serem testadas (Features to be tested)

Conforme as exigências do trabalho:

**Entrega 1:**

- **Testes Unitários:** Pelo menos uma classe de alta complexidade por integrante. Cada classe sob teste terá **complexidade ciclomática mínima de 10** (desvios, laços, estruturas de controle).
  - _Classe 1:_ MoodleController (método `allCourses`, CC 13 medida por PMD; classe com CC total 85) - Responsável: Gabriella Lima
  - _Classe 2:_ AdminController (método `modifyCourse`, CC 13) - Responsável: Beatriz Farias
  - _Classe 3:_ SignUpController (método `registered`, CC 16) - Responsável: Rayana Oliveira
  - _Classe 4:_ UserService (método `updateUser`, CC 28) - Responsável: Thomas Cardoso
  - _Classe 5:_ CourseInformationController (método `addCourseToUser`, CC 11) - Responsável: Giulia Cadorin
- **Testes Manuais:** Pelo menos uma funcionalidade principal por integrante do grupo.

**Entrega 2:**

- **Testes Unitários (Avançados):** Ampliação dos testes usando isolamento de dependências (Mocking, ex: Mockito).
- **Testes de Integração:** Validação da comunicação entre os módulos.
- **Testes de Sistema (E2E):** Funcionalidades validadas via interface com Selenium (ou similar). Ao menos um teste E2E por integrante. Opcional: teste não funcional (desempenho/segurança).

## 5. Funcionalidades que não serão testadas (Features not to be tested)

- **Operações de CRUD simples:** Métodos que apenas salvam/recuperam informações do banco sem algoritmos não serão foco dos testes unitários da Entrega 1.

## 6. Abordagem (Approach)

A estratégia abrangerá:

1. **Testes Manuais:** Pelo menos um cenário completo modelado e executado no **TestLink** na Entrega 1. _Status: três cenários já cadastrados e executados (inscrição em curso, de Giulia, modificar curso, de Beatriz, e práticas do Moodle, de Gabriella), com exports XML em `docs/`._
2. **Testes Unitários e Mocking:** Desenvolvimento de testes na linguagem do projeto. Na Entrega 2, uso de ferramentas de Mocking para isolar dependências. _Status: 5 classes de teste implementadas (JUnit 5 + Mockito, com repositórios e services mockados) - 96 testes no total._
3. **Testes de Integração e Sistema:** Validação do fluxo de ponta a ponta na Entrega 2 usando Selenium (ou equivalente para a linguagem).
4. **Técnicas e Cobertura (Entrega 2):**
   - _Funcional:_ Testes baseados em especificação.
   - _Estrutural:_ Alcançar ao menos **80% de cobertura todas-arestas** na classe de alta complexidade de cada integrante.
   - _Baseada em Defeitos:_ Alcançar ao menos **80% de escore de mutação** nas mesmas classes.
5. **Qualidade e Inspeção (Entrega 2):** Avaliação de atributos ISO/IEC 25010 e uso de SonarQube para inspeção de código (resolvendo problemas de pelo menos uma classe por integrante).
6. **Gestão de Defeitos:** Bugs encontrados serão geridos no **GitHub Issues**. _Status: defeitos do MoodleController reportados (issues #13, #14, #16) e do CourseInformationController (issue #10): 2 NPEs reproduzidos em execução com HTTP 500, 1 fragilidade latente (upload anônimo), nota sem validação de faixa, entrega vazia e count() usado como ID (reproduzido com EntityNotFoundException). Demais defeitos observados documentados no relatório de execução dos casos manuais._

## 7. Critérios de aprovação/reprovação do item (Item pass/fail criteria)

- 100% dos testes devem executar sem falhas na esteira CI/ambiente de testes.
- Classes selecionadas para teste unitário devem comprovar complexidade ciclomática >= 10.
- Critérios de cobertura da Entrega 2 (80% arestas, 80% mutação) devem ser atingidos nas classes foco.
- Links de artefatos presentes no `README.md` do repositório e colaboração individual verificável no GitHub/Google Docs.

## 8. Critérios de suspensão e requisitos de retomada (Suspension criteria and resumption requirements)

- **Suspensão:** Falhas críticas que impeçam o build da aplicação ou a indisponibilidade do ambiente de testes (ex: SonarQube offline).
- **Retomada:** Os testes continuam após os commits corretivos na branch main ou master.

## 9. Entregáveis de Teste (Test deliverables)

- **Entrega 1:** Plano de Teste, Código com Testes Unitários iniciais, Casos de Teste Manuais (TestLink/Planilhas) e Issues no GitHub.
- **Entrega 2:** Testes Unitários aprimorados (Mocks), Testes de Integração, Testes de Sistema (Selenium), Relatórios de Cobertura e Mutação, Relatório ISO/IEC 25010 e Relatórios de Inspeção (SonarQube - antes e depois).
  _(Todos linkados no README.md)_

## 10. Tarefas de Teste (Testing tasks)

1. **T1:** Configurar repositório fork, medir complexidade ciclomática e escolher classes.
2. **T1:** Implementar testes unitários básicos e executar testes manuais no TestLink.
3. **T2:** Ampliar testes unitários com Mocking e criar testes de integração.
4. **T2:** Configurar Selenium e implementar testes E2E.
5. **T2:** Executar análise de cobertura de arestas e análise de mutação.
6. **T2:** Configurar e rodar inspeção de código no SonarQube, aplicar correções nas classes foco.
7. **T1/T2:** Registar todos os bugs no GitHub Issues.

## 11. Necessidades Ambientais (Environmental needs)

- Ambiente de desenvolvimento local configurado para a stack do webapp-2.
- **Ferramentas:** Framework unitário (ex: JUnit), TestLink, GitHub (Issues e branches), Selenium, Ferramenta de Cobertura (ex: JaCoCo), Ferramenta de Mutação (ex: PIT) e SonarQube.

## 12. Responsabilidades (Responsibilities)

| Integrante      | Atribuições e Responsabilidades                                                                                                                                                       |
| :-------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Gabriella Lima  | Teste unitário da classe MoodleController; Teste manual da funcionalidade "submissão e correção de práticas" (TestLink: `docs/testlink-praticas-moodle.xml`); 1 Teste E2E (Selenium). |
| Beatriz Farias  | Teste unitário da classe AdminController; Teste manual da funcionalidade "modificar curso" (TestLink: `docs/testlink-modify-course.xml`); 1 Teste E2E (Selenium).                     |
| Rayana Oliveira | Teste unitário da classe SignUpController; Teste manual funcionalidade Z; 1 Teste E2E (Selenium).                                                                                     |
| Thomas Cardoso  | Teste unitário da classe UserService; Teste manual funcionalidade W; 1 Teste E2E (Selenium).                                                                                          |
| Giulia Cadorin  | Teste unitário da classe CourseInformationController; Teste manual da funcionalidade "inscrição em curso" (TestLink: `docs/testlink-inscricao-curso.xml`); 1 Teste E2E (Selenium).    |

## 13. Necessidades de equipe e treinamento (Staffing and training needs)

Ter domínio do ecossistema (JUnit, Mockito, JaCoCo, PIT e SonarQube). Caso não tenha, a equipe assume a responsabilidade por encontrar, configurar e validar as ferramentas equivalentes para análise estática, mocking, cobertura, mutação e inspeção (SonarQube) para a linguagem adotada, conforme as regras da disciplina.

## 14. Cronograma (Schedule)

- **Entrega 1:** Até 12/10/2026, segunda-feira, 23h59.
- **Apresentação da Entrega 1:** 15/10/2026.
- **Entrega 2:** Até 06/12/2026, domingo, 23h59.
- **Apresentação Final:** 08/12/2026, 10/12/2026 ou 15/12/2026.

## 15. Riscos e Contingências (Risks and contingencies)

| Risco                                                                                                              | Contingência                                                                                                          |
| :----------------------------------------------------------------------------------------------------------------- | :-------------------------------------------------------------------------------------------------------------------- |
| Dificuldade de configurar framework de mutação.                                                                    | Procurar documentação de ferramentas compatíveis cedo no ciclo (ex: PIT para Java) e pedir apoio antecipado.          |
| Um membro da equipe não realizar os commits necessários, afetando seu AP e o trabalho.                             | Gestão de tarefas transparente e comunicação via grupo; o histórico do GitHub e Docs isolará as contribuições.        |
| O método `updateUser` da classe UserService tem complexidade 28, o que dificultará a criação de testes e mutantes. | Isolar os comportamentos desse método e tentar cobrir os fluxos principais (Happy Paths) logo no início da Entrega 1. |

## 16. Aprovações (Approvals)

- Professor (Validação prévia de complexidade/escopo).
- Membros da equipe.
