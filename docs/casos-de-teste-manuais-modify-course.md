# Casos de teste manuais: modificar e excluir curso (admin)

Funcionalidade testada: edição (**Save**) e exclusão (**Delete**) de um curso pelo administrador, na página `/admin`, implementadas em `AdminController.modifyCourse` (`POST /admin/modify/course/{courseInternalName}`).

Esse método tem **complexidade ciclomática 13** (contagem manual: 2 `if` de usuário/curso, 1 `&&`, `if (btnSave)`, 4 `if (!campo.isEmpty())`, `else if (btnDelete)` e 3 `for`, mais 1). Os casos abaixo cobrem, navegando pelo site, os desvios que um usuário consegue alcançar.

## Limitação conhecida da tela (ler antes de executar)

Na página `/admin` (`admin_page.html`), o `<form>` de cada curso está colocado entre o `<tr>` e os `<td>`, o que é HTML inválido. O navegador fecha esse formulário vazio, e os campos e os botões **Save** e **Delete** ficam sem formulário. Na prática, clicar em **Save** ou **Delete** só recarrega a página e nenhuma requisição é enviada.

Para conferir o defeito, no console do navegador (F12), na página `/admin`, rode `document.querySelector('button[name=btnSave]').form`. Se o resultado for `null`, o botão não pertence a nenhum formulário.

## Desvios que a tela não alcança

Estes desvios do `modifyCourse` não acontecem navegando pelo site, e por isso não têm caso manual. Eles são cobertos pelos testes unitários em `AMICOServer/src/test/java/com/example/demo/controllers/AdminControllerTest.java`:

| Desvio | Teste unitário |
|---|---|
| Curso inexistente (`course == null`) | `modifyCourse_unknownCourseIsIgnored` |
| Nenhum botão enviado | `noButtonMeansNoChange` |
| Save e Delete enviados juntos | `saveWinsWhenBothButtonsArrive` |
| Visitante ou aluno chamando o endpoint | `modifyCourse_studentsAndAnonymousCannotEditOrDelete` |

## Mapa de cobertura dos casos manuais

| Decisão no código | Casos que a cobrem |
|---|---|
| Acesso de visitante e de aluno à página de admin | CT-MOD-01, CT-MOD-02 |
| `btnSave` com os 4 campos preenchidos | CT-MOD-03 |
| `btnSave` com os 4 campos vazios | CT-MOD-04 |
| `btnSave` com só um campo preenchido (nome, descrição, idioma, tipo) | CT-MOD-05 a CT-MOD-08 |
| `btnDelete` com os 3 laços executando | CT-MOD-09 |
| `btnDelete` com os 3 laços sem iterar | CT-MOD-10 |

## Preparação do ambiente

1. Usar o **JDK 8** (o projeto é Java 1.8).
2. Na pasta `AMICOServer`, rodar `mvn spring-boot:run`.
3. Abrir http://localhost:8000 no navegador (a porta é a 8000, configurada em `application.properties`).
4. O banco é H2 em memória: ao reiniciar a aplicação, os dados voltam ao estado inicial. **Reinicie sempre que um caso pedir "dados originais"**, principalmente depois de CT-MOD-09 e CT-MOD-10, que apagam cursos.

Usuários dos dados iniciais usados aqui:

- **admin** / **pass**: administrador.
- **amico** / **pass**: aluno comum, sem permissão de admin.

Cursos usados:

- **Introduction to AI**: tem 5 skills e alunos inscritos.
- **Introduction to AI2**: usado só no CT-MOD-03. Depois do CT-MOD-03 ele passa a se chamar `Curso Editado`.
- **AI Advanced Tips2**: usado a partir do CT-MOD-04 (idioma `English`, tipo `Computer Science`, descrição `Learn all the most advanced stuff related to IA`) e na exclusão sem vínculos. Confirme antes de executar que ele não tem alunos nem matérias (veja o passo 1 do CT-MOD-10).

## Casos de teste

### CT-MOD-01: Visitante sem login tenta abrir a página de admin

- **Objetivo:** Verificar que quem não está logado não acessa a administração de cursos.
- **Pré-condições:** Aplicação recém-iniciada. Nenhum usuário logado (use uma janela anônima).
- **Resultado esperado:** O sistema não mostra a tabela de cursos e leva o visitante para a tela de login.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/admin | O sistema redireciona para http://localhost:8000/login. |
| 2 | Conferir a página exibida | A tabela de cursos com os botões Save e Delete não aparece. |

### CT-MOD-02: Aluno comum tenta abrir a página de admin

- **Objetivo:** Verificar que um aluno sem perfil de admin não acessa a administração de cursos.
- **Pré-condições:** Dados originais.
- **Resultado esperado:** Acesso negado (erro 403) e nenhuma tabela de cursos.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Entrar em http://localhost:8000/login com amico / pass | Login realizado. |
| 2 | Abrir http://localhost:8000/admin | O sistema mostra a página de acesso negado (403). |
| 3 | Conferir a página exibida | A tabela de cursos com os botões Save e Delete não aparece. |

### CT-MOD-03: Salvar alterando os quatro campos

- **Objetivo:** Verificar o fluxo principal de edição com todos os campos preenchidos.
- **Pré-condições:** Dados originais. Logado como admin / pass.
- **Resultado esperado:** Os quatro campos do curso são atualizados.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/admin | Tabela de cursos exibida. |
| 2 | Na linha de Introduction to AI2, trocar Nome para `Curso Editado`, Idioma para `Portuguese`, Tipo para `Teste` e Descrição para `Descricao nova` | Os campos mostram os novos valores. |
| 3 | Clicar em **Save** | A página volta para `/admin/`. |
| 4 | Recarregar a página e conferir a linha do curso | Nome `Curso Editado`, idioma `Portuguese`, tipo `Teste` e descrição `Descricao nova` continuam gravados. |

### CT-MOD-04: Salvar com os quatro campos vazios

- **Objetivo:** Verificar que campos vazios não apagam os dados do curso.
- **Pré-condições:** Dados originais. Logado como admin / pass.
- **Resultado esperado:** O curso continua com todos os valores originais.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/admin | Tabela de cursos exibida. |
| 2 | Na linha de AI Advanced Tips2, apagar Nome, Idioma, Tipo e Descrição | Os quatro campos ficam vazios. |
| 3 | Clicar em **Save** | A página volta para `/admin/`. |
| 4 | Recarregar a página e conferir a linha do curso | Nome, idioma, tipo e descrição continuam com os valores originais. |

### CT-MOD-05: Salvar alterando só o nome

- **Objetivo:** Verificar que só o campo preenchido é alterado.
- **Pré-condições:** Dados originais. Logado como admin / pass.
- **Resultado esperado:** Somente o nome muda; idioma, tipo e descrição ficam como estavam.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/admin | Tabela de cursos exibida. |
| 2 | Na linha de AI Advanced Tips2, trocar Nome para `Nome Parcial` e apagar Idioma, Tipo e Descrição | Só o nome tem valor. |
| 3 | Clicar em **Save** | A página volta para `/admin/`. |
| 4 | Recarregar a página e conferir a linha do curso | O nome é `Nome Parcial`; idioma `English`, tipo `Computer Science` e a descrição `Learn all the most advanced stuff related to IA` continuam iguais. |

### CT-MOD-06: Salvar alterando só a descrição

- **Objetivo:** Verificar o ramo da descrição preenchida sozinha.
- **Pré-condições:** Dados originais. Logado como admin / pass.
- **Resultado esperado:** Somente a descrição muda.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/admin | Tabela de cursos exibida. |
| 2 | Na linha de AI Advanced Tips2, apagar Nome, Idioma e Tipo e trocar a Descrição para `So a descricao` | Só a descrição tem valor. |
| 3 | Clicar em **Save** | A página volta para `/admin/`. |
| 4 | Recarregar a página e conferir a linha do curso | Nome, idioma e tipo continuam originais; a descrição é `So a descricao`. |

### CT-MOD-07: Salvar alterando só o idioma

- **Objetivo:** Verificar o ramo do idioma preenchido sozinho.
- **Pré-condições:** Dados originais. Logado como admin / pass.
- **Resultado esperado:** Somente o idioma muda.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/admin | Tabela de cursos exibida. |
| 2 | Na linha de AI Advanced Tips2, apagar Nome, Tipo e Descrição e trocar o Idioma para `Frances` | Só o idioma tem valor. |
| 3 | Clicar em **Save** | A página volta para `/admin/`. |
| 4 | Recarregar a página e conferir a linha do curso | Nome, tipo e descrição continuam originais; o idioma é `Frances`. |

### CT-MOD-08: Salvar alterando só o tipo

- **Objetivo:** Verificar o ramo do tipo preenchido sozinho.
- **Pré-condições:** Dados originais. Logado como admin / pass.
- **Resultado esperado:** Somente o tipo muda.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/admin | Tabela de cursos exibida. |
| 2 | Na linha de AI Advanced Tips2, apagar Nome, Idioma e Descrição e trocar o Tipo para `Arte` | Só o tipo tem valor. |
| 3 | Clicar em **Save** | A página volta para `/admin/`. |
| 4 | Recarregar a página e conferir a linha do curso | Nome, idioma e descrição continuam originais; o tipo é `Arte`. |

### CT-MOD-09: Excluir um curso com skills e alunos inscritos

- **Objetivo:** Verificar a exclusão de um curso com vínculos, quando os três laços de limpeza executam.
- **Pré-condições:** Dados originais. Introduction to AI tem 5 skills e alunos inscritos (amico está inscrito).
- **Resultado esperado:** O curso some da lista; o perfil dos alunos não mostra mais o curso; nenhuma página de erro.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Entrar como amico / pass, abrir `/profile/amico` e anotar que Introduction to AI aparece nos cursos | Curso listado. |
| 2 | Sair, entrar como admin / pass e abrir http://localhost:8000/admin | Introduction to AI aparece na tabela. |
| 3 | Na linha de Introduction to AI, clicar em **Delete** | A página volta para `/admin/` sem erro. |
| 4 | Recarregar a página e conferir a tabela | Introduction to AI não aparece mais. |
| 5 | Entrar como amico / pass e abrir o perfil | Introduction to AI não aparece mais nos cursos do aluno. |
| 6 | Abrir http://localhost:8000/course/introduction-to-ai | Página de curso não encontrado (ou erro 404 amigável). |

### CT-MOD-10: Excluir um curso sem nenhum vínculo

- **Objetivo:** Verificar a exclusão quando as listas de matérias, alunos e skills estão vazias (os laços não iteram).
- **Pré-condições:** Dados originais. Logado como admin / pass.
- **Resultado esperado:** O curso é removido sem erro.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/course/ai-advanced-tips2 e confirmar que o curso não tem matérias nem alunos; se tiver, usar outro curso sem vínculos, por exemplo um criado em **Create Course** na própria página `/admin` | Curso sem vínculos confirmado. |
| 2 | Abrir http://localhost:8000/admin | Tabela de cursos exibida. |
| 3 | Na linha desse curso, clicar em **Delete** | A página volta para `/admin/` sem erro. |
| 4 | Recarregar a página e conferir a tabela | O curso não aparece mais. |

## Registro de execução

Preencher ao executar. Em caso de falha, anexar print e abrir uma issue no GitHub.

| Caso | Data | Executor | Resultado obtido | Status (Passou/Falhou) | Evidência (gif) | Issue |
|---|---|---|---|---|---|---|
| CT-MOD-01 | 11/10/2026 | Beatriz Farias | Visitante redirecionado para o login; tabela de cursos não exibida. | Passou | [CT-MOD-01.gif](../manual-test-results/modify-course/CT-MOD-01.gif) | |
| CT-MOD-02 | 11/10/2026 | Beatriz Farias | Aluno recebeu acesso negado; tabela de cursos não exibida. | Passou | [CT-MOD-02.gif](../manual-test-results/modify-course/CT-MOD-02.gif) | |
| CT-MOD-03 | 11/10/2026 | Beatriz Farias | Os quatro campos foram atualizados e continuaram gravados após recarregar. | Passou | [CT-MOD-03.gif](../manual-test-results/modify-course/CT-MOD-03.gif) | |
| CT-MOD-04 | 11/10/2026 | Beatriz Farias | Os valores originais foram mantidos após salvar com os campos vazios. | Passou | [CT-MOD-04.gif](../manual-test-results/modify-course/CT-MOD-04.gif) | |
| CT-MOD-05 | 11/10/2026 | Beatriz Farias | Apenas o nome foi alterado. | Passou | [CT-MOD-05.gif](../manual-test-results/modify-course/CT-MOD-05.gif) | |
| CT-MOD-06 | 11/10/2026 | Beatriz Farias | Apenas a descrição foi alterada. | Passou | [CT-MOD-06.gif](../manual-test-results/modify-course/CT-MOD-06.gif) | |
| CT-MOD-07 | 11/10/2026 | Beatriz Farias | Apenas o idioma foi alterado. | Passou | [CT-MOD-07.gif](../manual-test-results/modify-course/CT-MOD-07.gif) | |
| CT-MOD-08 | 11/10/2026 | Beatriz Farias | Apenas o tipo foi alterado. | Passou | [CT-MOD-08.gif](../manual-test-results/modify-course/CT-MOD-08.gif) | |
| CT-MOD-09 | 11/10/2026 | Beatriz Farias | Passos 1 a 5 conforme o esperado: o curso saiu da tabela e do perfil do aluno. No passo 6, abrir /course/introduction-to-ai devolveu erro 500 em vez de 404 amigável. | Falhou | [CT-MOD-09.1.gif](../manual-test-results/modify-course/CT-MOD-09.1.gif), [CT-MOD-09.2.gif](../manual-test-results/modify-course/CT-MOD-09.2.gif) | [issue-course-not-found-500.md](issue-course-not-found-500.md) (abrir no GitHub) |
| CT-MOD-10 | 11/10/2026 | Beatriz Farias | O curso foi removido e não aparece mais na tabela. | Passou | [CT-MOD-10.gif](../manual-test-results/modify-course/CT-MOD-10.gif) | |
