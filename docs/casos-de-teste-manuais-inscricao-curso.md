# Casos de teste manuais: inscrição em curso

Funcionalidade testada: inscrição de um usuário em um curso (botão **Add Course**, endereço `/course/{curso}/add`), implementada em `CourseInformationController.addCourseToUser`.

Os testes unitários dessa mesma classe estão em `AMICOServer/src/test/java/com/example/demo/controllers/CourseInformationControllerTest.java`.

## Preparação do ambiente

1. Usar o **JDK 8** (o projeto é Java 1.8).
2. Na pasta `AMICOServer`, rodar `mvn spring-boot:run`.
3. Abrir http://localhost:8000 no navegador (a porta é a 8000, configurada em `application.properties`).
4. O banco é H2 em memória: ao reiniciar a aplicação, os dados voltam ao estado inicial. Reinicie sempre que um caso pedir "dados originais".

Usuários dos dados iniciais usados aqui: **amico** / **pass** (aluno inscrito nos 6 primeiros cursos; AI Advanced Tips e Cortar con tijeras estão marcados como concluídos).

## Casos de teste

### CT-INSC-01: Visitante sem login tenta se inscrever em um curso

- **Objetivo:** Verificar que um visitante não logado não consegue se inscrever e recebe o aviso de login.
- **Pré-condições:** Aplicação rodando. Nenhum usuário logado (use uma janela anônima).
- **Resultado esperado:** Alerta de login obrigatório e nenhuma inscrição criada.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/course/introduction-to-ai2 | A página do curso Introduction to AI2 é exibida, com o botão "Add Course". |
| 2 | Clicar em "Add Course" | O sistema volta para a página do curso e mostra um alerta azul com o texto "To register for a course it is necessary to be logged into the system. Press AMICOURSES to return to the main screen...". |

### CT-INSC-02: Inscrição com sucesso em um curso novo

- **Objetivo:** Verificar o fluxo principal: um aluno logado se inscreve em um curso em que ainda não está.
- **Pré-condições:** Aplicação recém-iniciada (dados originais). Usuário amico / senha pass é aluno e ainda não está no curso Introduction to AI2.
- **Resultado esperado:** Redirecionamento para /profile/amico e curso Introduction to AI2 listado nos cursos do aluno.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/login e entrar com usuário amico e senha pass | Login realizado e página inicial exibida. |
| 2 | Abrir http://localhost:8000/course/introduction-to-ai2 | Página do curso exibida. |
| 3 | Clicar em "Add Course" | O sistema redireciona para o perfil do usuário (/profile/amico). |
| 4 | Procurar a lista de cursos do perfil | O curso Introduction to AI2 aparece na lista de cursos do usuário. |

### CT-INSC-03: Tentar se inscrever de novo logo depois de inscrito

- **Objetivo:** Verificar que a mesma inscrição não é feita duas vezes.
- **Pré-condições:** Executar antes o CT-INSC-02 (amico já inscrito em Introduction to AI2).
- **Resultado esperado:** Mensagem de já inscrito e nenhuma duplicata na lista de cursos.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Com amico logado, abrir http://localhost:8000/course/introduction-to-ai2 | Página do curso exibida. |
| 2 | Clicar em "Add Course" | A página do curso é exibida com o alerta "You are already registered in this course.". |
| 3 | Abrir o perfil do usuário e contar quantas vezes o curso aparece | O curso aparece uma única vez na lista. |

### CT-INSC-04: Tentar se inscrever em curso em que o aluno já está inscrito

- **Objetivo:** Verificar o aviso para um curso em andamento em que o aluno já está inscrito.
- **Pré-condições:** Aplicação recém-iniciada. amico já está inscrito em Matematicas Para Gatos (curso não concluído).
- **Resultado esperado:** Mensagem de já inscrito; nada é alterado no perfil.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Entrar como amico / pass em http://localhost:8000/login | Login realizado. |
| 2 | Abrir http://localhost:8000/course/matematicas-para-gatos | Página do curso exibida. |
| 3 | Clicar em "Add Course" | Alerta "You are already registered in this course.". |

### CT-INSC-05: Tentar se inscrever em curso que o aluno já concluiu

- **Objetivo:** Verificar o aviso para um curso concluído.
- **Pré-condições:** Aplicação recém-iniciada. amico está inscrito em AI Advanced Tips, que está marcado como concluído nos dados iniciais.
- **Resultado esperado:** Mensagem de curso já concluído (e não a de já inscrito).

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Entrar como amico / pass em http://localhost:8000/login | Login realizado. |
| 2 | Abrir http://localhost:8000/course/ai-advanced-tips | Página do curso exibida. |
| 3 | Clicar em "Add Course" | Alerta "You have already completed this course.". |

### CT-INSC-06: A mensagem de erro aparece só uma vez

- **Objetivo:** Verificar que o alerta some quando a página é recarregada.
- **Pré-condições:** Executar antes o CT-INSC-05 (alerta de curso concluído na tela).
- **Resultado esperado:** O alerta não volta depois de recarregar.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Com o alerta na tela, recarregar a página (F5) | A página do curso é exibida sem o alerta. |

### CT-INSC-07: Tentar se inscrever em um curso que não existe

- **Objetivo:** Verificar o comportamento do sistema com um endereço de curso inválido.
- **Pré-condições:** Aplicação rodando. amico logado.
- **Resultado esperado:** Mensagem de curso não encontrado ou página 404. Falha esperada se o sistema mostrar a página de erro 500 (Whitelabel Error Page).

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Entrar como amico / pass em http://localhost:8000/login | Login realizado. |
| 2 | Abrir http://localhost:8000/course/curso-que-nao-existe/add | O sistema mostra uma página de erro amigável (por exemplo, curso não encontrado ou 404). |

### CT-INSC-08: A mensagem de um usuário não pode aparecer para outro

- **Objetivo:** Verificar se o aviso de erro fica guardado no servidor e aparece para quem não o causou.
- **Pré-condições:** Aplicação rodando. Terminal do PowerShell aberto. amico logado no navegador.
- **Resultado esperado:** Nenhum alerta para o amico. Falha esperada se aparecer o aviso "To register for a course it is necessary to be logged...".

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | No PowerShell, rodar: curl.exe -s -o NUL http://localhost:8000/course/introduction-to-ai/add (simula um visitante sem login que clicou em Add Course, sem abrir a página de volta) | O comando termina sem erro. |
| 2 | No navegador, com amico logado, abrir http://localhost:8000/course/introduction-to-ai | A página do curso é exibida SEM nenhum alerta, porque o amico não clicou em nada. |

### CT-INSC-09: Datas de início e fim exibidas na página do curso

- **Objetivo:** Verificar o formato das datas mostradas na página de informações do curso.
- **Pré-condições:** Aplicação rodando. O curso Introduction to AI tem início em 31/01/2019 e fim em 30/07/2019 nos dados iniciais.
- **Resultado esperado:** Datas 31-01-2019 e 30-07-2019. Falha esperada se o mês aparecer como 00 (31-00-2019 e 30-00-2019).

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/course/introduction-to-ai | Página do curso exibida. |
| 2 | Ler as datas de início e de fim na tabela de datas do curso | Início 31-01-2019 e fim 30-07-2019 (formato dia-mês-ano). |

## Registro de execução

Preencher ao executar. Em caso de falha, anexar print e abrir uma issue no GitHub.

| Caso | Data | Executor | Resultado obtido | Status (Passou/Falhou) | Evidência (print) | Issue |
|---|---|---|---|---|---|---|
| CT-INSC-01 |  |  |  |  |  |  |
| CT-INSC-02 |  |  |  |  |  |  |
| CT-INSC-03 |  |  |  |  |  |  |
| CT-INSC-04 |  |  |  |  |  |  |
| CT-INSC-05 |  |  |  |  |  |  |
| CT-INSC-06 |  |  |  |  |  |  |
| CT-INSC-07 |  |  |  |  |  |  |
| CT-INSC-08 |  |  |  |  |  |  |
| CT-INSC-09 |  |  |  |  |  |  |
