# Casos de teste manuais: cadastro de novo utilizador (signup)

Funcionalidade testada: cadastro de uma nova conta de utilizador na aplicação web, acessível em `/signup`, implementada em `SignUpController.registered` (`POST /registered`).

Esse método tem **complexidade ciclomática 16** (contagem manual: verificações de campos vazios para `username`, `password` e `userMail`, verificação de igualdade de senhas, validações de tamanho mínimo e máximo de senha, tamanho mínimo e máximo de `username`, regex de caracteres válidos no `username`, validação de formato de `userMail`, e verificações de duplicidade no repositório, mais 1). Os casos abaixo cobrem, navegando pela interface do site, os fluxos válidos e os desvios de validação alcançáveis pelo utilizador.

## Limitação conhecida do ecrã (ler antes de executar)

Nos navegadores modernos, os campos de formulário HTML podem conter atributos nativos de validação do HTML5 (como `required`, `type="email"` ou `minlength`/`maxlength`). Em alguns navegadores, essas travas de frontend podem interceptar o envio antes que a requisição chegue ao controlador Spring no backend.

Para garantir que as validações do backend em `SignUpController.registered` sejam acionadas e testadas:
1. Caso o navegador impeça o envio de um campo em branco via pop-up nativo, abra o console do navegador (F12).
2. Na aba **Elements**, localize a tag `<form action="/registered" ...>`.
3. Adicione o atributo `novalidate` na tag (ex.: `<form action="/registered" method="post" novalidate>`) ou remova temporariamente o atributo `required` do campo que deseja testar.

## Desvios que o ecrã não alcança

Estes desvios do `registered` dificilmente ocorrem navegando pelo site (como o envio de parâmetros estritamente `null` via HTTP), e por isso não têm caso manual. Eles são cobertos pelos testes unitários em `AMICOServer/src/test/java/com/example/demo/controllers/SignUpControllerTest.java`:

| Desvio | Teste unitário |
|---|---|
| Inicialização do formulário GET com `wrongData = false` | `signup_initializesModelAndReturnsSignupView` |
| Tratamento defensivo de parâmetros nulos em serviços mockados | `registered_rejectsEmptyUsername`, `registered_rejectsEmptyPassword` |
| Isolamento estrito de injeção e ciclo de vida de repositório | `SignUpControllerTest.setUp` |

## Mapa de cobertura dos casos manuais

| Decisão no código | Casos que a cobrem |
|---|---|
| Cadastro com sucesso (Happy Path) | CT-SIGN-01 |
| Rejeição de `username` vazio (`""`) | CT-SIGN-02 |
| Rejeição de `password` vazia (`""`) | CT-SIGN-03 |
| Rejeição de `userMail` vazio (`""`) | CT-SIGN-04 |
| Senhas divergentes (`pass1 != pass2`) | CT-SIGN-05 |
| Senha abaixo do tamanho mínimo (limite inferior $\le 7$) | CT-SIGN-06 |
| Senha acima do tamanho máximo (limite superior $\ge 15$) | CT-SIGN-07 |
| `username` abaixo do tamanho mínimo (limite inferior $\le 4$) | CT-SIGN-08 |
| `username` acima do tamanho máximo (limite superior $\ge 16$) | CT-SIGN-09 |
| `username` com caracteres proibidos | CT-SIGN-10 |
| Formato de `userMail` inválido (sem `@`) | CT-SIGN-11 |
| `username` já cadastrado no banco de dados | CT-SIGN-12 |
| `userMail` já cadastrado no banco de dados | CT-SIGN-13 |

## Preparação do ambiente

1. **Adaptação do Ambiente (Java 21):** Embora o projeto original seja projetado para JDK 8, a execução e os testes foram adaptados para rodar em versões recentes do Java (Java 21). Devido à incompatibilidade entre as bibliotecas legadas (Spring Boot 1.5 / CGLIB / Mockito) e o sistema de módulos moderno do Java, foi aplicada a diretiva `--add-opens java.base/java.lang=ALL-UNNAMED` nos seguintes pontos:
   - Nos argumentos de VM (VM Arguments) da execução do **JUnit** no Eclipse.
   - Nos argumentos de VM (VM Arguments) da execução do **Maven Build** diretamente no Eclipse, permitindo rodar os comandos sem a necessidade de modificar o arquivo `pom.xml`.
   - Nos argumentos de VM da classe principal da **Aplicação** (`AMICOServerApplication`) no Eclipse.
2. Na pasta `AMICOServer`, rodar `mvn spring-boot:run` (ou executar a aplicação pelo Eclipse com a diretiva configurada).
3. Abrir http://localhost:8000 no navegador.
4. O banco é H2 em memória: ao reiniciar a aplicação, os dados voltam ao estado inicial. **Reinicie sempre que um caso pedir "dados originais"**, especialmente após o CT-SIGN-01.

Utilizadores dos dados iniciais usados aqui para testes de duplicidade:
- **admin** (email: `admin@mail.com`)
- **amico** (email: `amico@mail.com`)

## Casos de teste

### CT-SIGN-01: Cadastro de utilizador com dados válidos

- **Objetivo:** Verificar que um utilizador com dados válidos e inéditos é cadastrado com sucesso.
- **Pré-condições:** Aplicação recém-iniciada.
- **Resultado esperado:** O sistema grava o utilizador e redireciona para `/login`.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Acessar http://localhost:8000/signup | Formulário de cadastro exibido sem erros. |
| 2 | Preencher `username` com `novousuario1`, email com `novousuario1@gmail.com` e senhas com `senha1234` | Campos preenchidos. |
| 3 | Clicar em **Sign up** / **Inscrever-se!** | O sistema redireciona para http://localhost:8000/login. |

### CT-SIGN-02: Cadastro com nome de utilizador vazio

- **Objetivo:** Verificar a rejeição de cadastro quando o `username` está em branco.
- **Pré-condições:** Dados originais. `novalidate` ativado no HTML.
- **Resultado esperado:** Erro `"The username can't be empty"`.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Acessar http://localhost:8000/signup | Formulário exibido. |
| 2 | Deixar `username` vazio e preencher os restantes campos | Campos prontos para envio. |
| 3 | Clicar em **Sign up** | Permanece em `/signup` exibindo `"The username can't be empty"`. |

### CT-SIGN-03: Cadastro com senha vazia

- **Objetivo:** Verificar a rejeição de cadastro quando a `password` está em branco.
- **Pré-condições:** Dados originais. `novalidate` ativado no HTML.
- **Resultado esperado:** Erro `"The password can't be empty"`.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Acessar http://localhost:8000/signup | Formulário exibido. |
| 2 | Deixar os campos de senha vazios e preencher os restantes | Campos prontos para envio. |
| 3 | Clicar em **Sign up** | Permanece em `/signup` exibindo `"The password can't be empty"`. |

### CT-SIGN-04: Cadastro com email vazio

- **Objetivo:** Verificar a rejeição de cadastro quando o `userMail` está em branco.
- **Pré-condições:** Dados originais. `novalidate` ativado no HTML.
- **Resultado esperado:** Erro `"The userMail can't be empty"`.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Acessar http://localhost:8000/signup | Formulário exibido. |
| 2 | Deixar o campo de e-mail vazio e preencher os restantes | Campos prontos para envio. |
| 3 | Clicar em **Sign up** | Permanece em `/signup` exibindo `"The userMail can't be empty"`. |

### CT-SIGN-05: Confirmação de senha divergente

- **Objetivo:** Verificar a validação de igualdade entre as senhas inseridas.
- **Pré-condições:** Dados originais.
- **Resultado esperado:** Erro `"The passwords are different."`.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Acessar http://localhost:8000/signup | Formulário exibido. |
| 2 | Preencher Senha com `senha123` e Repetir Senha com `outra456` | Senhas distintas. |
| 3 | Clicar em **Sign up** | Permanece em `/signup` exibindo `"The passwords are different."`. |

### CT-SIGN-06: Senha abaixo do tamanho mínimo

- **Objetivo:** Verificar a rejeição de senha com $\le 7$ caracteres.
- **Pré-condições:** Dados originais.
- **Resultado esperado:** Erro `"The password is too short."`.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Acessar http://localhost:8000/signup | Formulário exibido. |
| 2 | Preencher senhas com `12345` (5 caracteres) | Campos preenchidos. |
| 3 | Clicar em **Sign up** | Permanece em `/signup` exibindo `"The password is too short."`. |

### CT-SIGN-07: Senha acima do tamanho máximo

- **Objetivo:** Verificar a rejeição de senha com $\ge 15$ caracteres.
- **Pré-condições:** Dados originais.
- **Resultado esperado:** Erro `"The password is too long."`.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Acessar http://localhost:8000/signup | Formulário exibido. |
| 2 | Preencher senhas com `senhamuitolonga123` (18 caracteres) | Campos preenchidos. |
| 3 | Clicar em **Sign up** | Permanece em `/signup` exibindo `"The password is too long."`. |

### CT-SIGN-08: Nome de utilizador abaixo do tamanho mínimo

- **Objetivo:** Verificar a rejeição de username com $\le 4$ caracteres.
- **Pré-condições:** Dados originais.
- **Resultado esperado:** Erro `"The username is too short."`.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Acessar http://localhost:8000/signup | Formulário exibido. |
| 2 | Preencher `username` com `abc` (3 caracteres) | Campos preenchidos. |
| 3 | Clicar em **Sign up** | Permanece em `/signup` exibindo `"The username is too short."`. |

### CT-SIGN-09: Nome de utilizador acima do tamanho máximo

- **Objetivo:** Verificar a rejeição de username com $\ge 16$ caracteres.
- **Pré-condições:** Dados originais.
- **Resultado esperado:** Erro `"The username is too long."`.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Acessar http://localhost:8000/signup | Formulário exibido. |
| 2 | Preencher `username` com `usuarioexcessivamentelongo` | Campos preenchidos. |
| 3 | Clicar em **Sign up** | Permanece em `/signup` exibindo `"The username is too long."`. |

### CT-SIGN-10: Nome de utilizador com caracteres proibidos

- **Objetivo:** Verificar se apenas letras, números, `-` ou `_` são aceites.
- **Pré-condições:** Dados originais.
- **Resultado esperado:** Erro `"The username only can contains letters, numbers, - or _."`.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Acessar http://localhost:8000/signup | Formulário exibido. |
| 2 | Preencher `username` com `user@name!` | Caracteres especiais inseridos. |
| 3 | Clicar em **Sign up** | Permanece em `/signup` exibindo `"The username only can contains letters, numbers, - or _."`. |

### CT-SIGN-11: Formato de e-mail inválido

- **Objetivo:** Verificar a validação de padrão sintático de endereço de email.
- **Pré-condições:** Dados originais.
- **Resultado esperado:** Erro `"The email address is not correct."`.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Acessar http://localhost:8000/signup | Formulário exibido. |
| 2 | Preencher e-mail com `email-sem-arroba-nem-dominio` | Formato incorreto. |
| 3 | Clicar em **Sign up** | Permanece em `/signup` exibindo `"The email address is not correct."`. |

### CT-SIGN-12: Nome de utilizador já cadastrado

- **Objetivo:** Verificar a unicidade do `username` no banco de dados.
- **Pré-condições:** Dados originais.
- **Resultado esperado:** Erro `"There is another user with that username."`.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Acessar http://localhost:8000/signup | Formulário exibido. |
| 2 | Preencher `username` com `amico` (já existente no banco) | Campos preenchidos. |
| 3 | Clicar em **Sign up** | Permanece em `/signup` exibindo `"There is another user with that username."`. |

### CT-SIGN-13: E-mail já cadastrado

- **Objetivo:** Verificar a unicidade do `userMail` no banco de dados.
- **Pré-condições:** Dados originais.
- **Resultado esperado:** Erro `"There is another user with that email address."`.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Acessar http://localhost:8000/signup | Formulário exibido. |
| 2 | Preencher e-mail com `admin@mail.com` (já existente no banco) | Campos preenchidos. |
| 3 | Clicar em **Sign up** | Permanece em `/signup` exibindo `"There is another user with that email address."`. |

## Registo de execução

Preencher ao executar. Em caso de falha, anexar print e abrir uma issue no GitHub.

| Caso | Data | Executor | Resultado obtido | Status (Passou/Falhou) | Evidência (gif) | Issue |
|---|---|---|---|---|---|---|
| CT-SIGN-01 | 12/10/2026 | Rayana Oliveira | Utilizador cadastrado com sucesso e redirecionado para o login. | Passou | [CT-SIGN-01.gif](../manual-test-results/signup/CT-SIGN-01.gif) | |
| CT-SIGN-02 | 12/10/2026 | Rayana Oliveira | Mensagem "The username can't be empty" exibida na página. | Passou | [CT-SIGN-02.gif](../manual-test-results/signup/CT-SIGN-02.gif) | |
| CT-SIGN-03 | 12/10/2026 | Rayana Oliveira | Mensagem "The password can't be empty" exibida na página. | Passou | [CT-SIGN-03.gif](../manual-test-results/signup/CT-SIGN-03.gif) | |
| CT-SIGN-04 | 12/10/2026 | Rayana Oliveira | Mensagem "The userMail can't be empty" exibida na página. | Passou | [CT-SIGN-04.gif](../manual-test-results/signup/CT-SIGN-04.gif) | |
| CT-SIGN-05 | 12/10/2026 | Rayana Oliveira | Mensagem "The passwords are different." exibida na página. | Passou | [CT-SIGN-05.gif](../manual-test-results/signup/CT-SIGN-05.gif) | |
| CT-SIGN-06 | 12/10/2026 | Rayana Oliveira | Mensagem "The password is too short." exibida na página. | Passou | [CT-SIGN-06.gif](../manual-test-results/signup/CT-SIGN-06.gif) | |
| CT-SIGN-07 | 12/10/2026 | Rayana Oliveira | Mensagem "The password is too long." exibida na página. | Passou | [CT-SIGN-07.gif](../manual-test-results/signup/CT-SIGN-07.gif) | |
| CT-SIGN-08 | 12/10/2026 | Rayana Oliveira | Mensagem "The username is too short." exibida na página. | Passou | [CT-SIGN-08.gif](../manual-test-results/signup/CT-SIGN-08.gif) | |
| CT-SIGN-09 | 12/10/2026 | Rayana Oliveira | Mensagem "The username is too long." exibida na página. | Passou | [CT-SIGN-09.gif](../manual-test-results/signup/CT-SIGN-09.gif) | |
| CT-SIGN-10 | 12/10/2026 | Rayana Oliveira | Mensagem "The username only can contains letters..." exibida. | Passou | [CT-SIGN-10.gif](../manual-test-results/signup/CT-SIGN-10.gif) | |
| CT-SIGN-11 | 12/10/2026 | Rayana Oliveira | Mensagem "The email address is not correct." exibida na página. | Passou | [CT-SIGN-11.gif](../manual-test-results/signup/CT-SIGN-11.gif) | |
| CT-SIGN-12 | 12/10/2026 | Rayana Oliveira | Mensagem "There is another user with that username." exibida na página. | Passou | [CT-SIGN-12.gif](../manual-test-results/signup/CT-SIGN-12.gif) | |
| CT-SIGN-13 | 12/10/2026 | Rayana Oliveira | Mensagem "There is another user with that email address." exibida na página. | Passou | [CT-SIGN-13.gif](../manual-test-results/signup/CT-SIGN-13.gif) | |
