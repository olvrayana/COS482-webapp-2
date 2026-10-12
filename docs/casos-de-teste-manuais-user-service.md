# Casos de teste manuais: cadastro, perfil e exclusão de usuário

Funcionalidades testadas: cadastro de conta (`/registered`), atualização de perfil (`PUT /api/users/{userInternalName}`), consulta e envio de foto de perfil (`GET/PUT /api/users/img/{userInternalName}`) e exclusão administrativa (`DELETE /api/users/{userInternalName}`), que utilizam `UserService`.

Os testes unitários estão em `AMICOServer/src/test/java/com/example/demo/controllers/UserServiceTest.java`.

> A tela HTML de edição (`/profile/{userInternalName}/updated`) grava o perfil diretamente pelo controller. Os casos de atualização abaixo usam a API REST para exercitar `UserService.updateUser`.

## Preparação do ambiente

1. Usar o **JDK 8** (o projeto é Java 1.8).
2. Na pasta `AMICOServer`, rodar `mvn spring-boot:run`.
3. Abrir http://localhost:8000 no navegador (a porta é a 8000, configurada em `application.properties`).
4. O banco H2 é em memória. Reiniciar a aplicação restaura os dados iniciais.
5. Usar um cliente REST como Postman para os casos que chamam `/api/users`. Configurar **Authorization > Basic Auth** com as credenciais indicadas em cada caso e, para atualização, enviar o corpo como JSON.

Usuários dos dados iniciais usados aqui:

- **amico** / **pass**: aluno comum.
- **admin** / **pass**: administrador.

O CT-USR-01 cria a conta descartável **heleno_manual** / **senha123**, com e-mail `heleno.manual@exemplo.com.br`. Os casos de perfil e imagem dependem dessa conta. Execute o CT-USR-12 por último para removê-la. Se interromper a sequência antes da remoção, reinicie a aplicação para limpar a conta do banco em memória.

> A imagem enviada fica salva no sistema de arquivos em `AMICOServer/files/image/users/{userID}/profile-{userID}.jpg` e não é apagada ao reiniciar a aplicação ou excluir a conta. Remova manualmente somente esse arquivo de teste após terminar.

## Casos de teste

### CT-USR-01: Cadastrar uma conta válida

- **Objetivo:** Verificar o cadastro bem-sucedido e a criação de uma conta de aluno.
- **Pré-condições:** Aplicação recém-iniciada. Os dados não contêm `heleno_manual` nem o e-mail `heleno.manual@exemplo.com.br`.
- **Resultado esperado:** Cadastro aceito e conta disponível para login.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/signup | Formulário de cadastro exibido. |
| 2 | Preencher username `heleno_manual`, e-mail `heleno.manual@exemplo.com.br`, senha `senha123` e repetir a senha | Os quatro campos ficam preenchidos. |
| 3 | Clicar em **Sign Up!** | O sistema exibe a tela de login, sem mensagem de erro. |
| 4 | Entrar em http://localhost:8000/login com `heleno_manual` / `senha123` | Login realizado; a conta foi criada como aluno. |

### CT-USR-02: Recusar senhas diferentes

- **Objetivo:** Verificar que o cadastro não é feito quando a confirmação da senha não corresponde à senha informada.
- **Pré-condições:** Aplicação rodando; usar um nome e e-mail ainda não cadastrados.
- **Resultado esperado:** O cadastro é recusado e o formulário informa que as senhas são diferentes.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/signup | Formulário de cadastro exibido. |
| 2 | Informar username `teste_senhas`, e-mail `teste.senhas@exemplo.com.br`, senha `senha123` e confirmação `senha321` | Os dados ficam preenchidos. |
| 3 | Clicar em **Sign Up!** | O formulário permanece aberto e mostra `The passwords are different.` |
| 4 | Tentar entrar com `teste_senhas` / `senha123` | O login não é realizado; a conta não foi criada. |

### CT-USR-03: Recusar username com caracteres inválidos

- **Objetivo:** Verificar que o cadastro aceita somente letras, números, hífen e sublinhado no username.
- **Pré-condições:** Aplicação rodando.
- **Resultado esperado:** Username com espaço é recusado e uma mensagem de validação aparece.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/signup | Formulário de cadastro exibido. |
| 2 | Informar username `heleno manual`, e-mail `heleno.invalido@exemplo.com.br` e uma senha válida igual nos dois campos | Os dados ficam preenchidos. |
| 3 | Clicar em **Sign Up!** | O cadastro é recusado; aparece a mensagem de que o username só pode conter letras, números, `-` ou `_`. |

### CT-USR-04: Recusar e-mail malformado

- **Objetivo:** Verificar a validação do formato do e-mail no cadastro.
- **Pré-condições:** Aplicação rodando.
- **Resultado esperado:** O cadastro é recusado e o formulário informa que o e-mail não é válido.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/signup | Formulário de cadastro exibido. |
| 2 | Informar username `teste_email`, e-mail `email-sem-arroba`, senha `senha123` e repetir a senha | Os dados ficam preenchidos. |
| 3 | Clicar em **Sign Up!** | O formulário permanece aberto e mostra `The email address is not correct.` |

### CT-USR-05: Recusar username já cadastrado

- **Objetivo:** Verificar que não é possível criar outra conta com o username `heleno_manual`.
- **Pré-condições:** CT-USR-01 concluído.
- **Resultado esperado:** O cadastro é recusado por duplicidade de username.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/signup | Formulário de cadastro exibido. |
| 2 | Informar username `heleno_manual`, e-mail `outro.email@exemplo.com.br` e senha válida igual nos dois campos | Os dados ficam preenchidos. |
| 3 | Clicar em **Sign Up!** | O cadastro é recusado e aparece `There is another user with that username.` |

### CT-USR-06: Recusar e-mail já cadastrado

- **Objetivo:** Verificar que um endereço de e-mail não pode ser associado a duas contas.
- **Pré-condições:** CT-USR-01 concluído.
- **Resultado esperado:** O cadastro é recusado por duplicidade de e-mail.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Abrir http://localhost:8000/signup | Formulário de cadastro exibido. |
| 2 | Informar username `outro_usuario`, e-mail `heleno.manual@exemplo.com.br` e senha válida igual nos dois campos | Os dados ficam preenchidos. |
| 3 | Clicar em **Sign Up!** | O cadastro é recusado e aparece `There is another user with that email address.` |

### CT-USR-07: Atualizar dados válidos do próprio perfil pela API

- **Objetivo:** Verificar o fluxo de atualização do `UserService` para o usuário autenticado.
- **Pré-condições:** CT-USR-01 concluído. No Postman, configurar Basic Auth como `heleno_manual` / `senha123`.
- **Resultado esperado:** Resposta HTTP 200 e os campos válidos ficam gravados no perfil.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Enviar `GET http://localhost:8000/api/users/myProfile` | Resposta HTTP 200 com os dados do perfil; anotar o valor de `userID`. |
| 2 | Enviar `PUT http://localhost:8000/api/users/heleno_manual` com `Content-Type: application/json` e corpo `{"userID": ID_ANOTADO, "userFirstName": "Heleno", "userLastName": "Professor", "userMail": "heleno.atualizado@exemplo.com.br", "userAddress": "Rua das Flores, 45", "city": "Montes Claros", "country": "Brasil", "phoneNumber": "389999988", "interests": "Futebol e Tigrinho"}` | Resposta HTTP 200; a resposta contém os valores enviados. Substituir `ID_ANOTADO` pelo número obtido no passo anterior. |
| 3 | Repetir o `GET /api/users/myProfile` | Os dados atualizados continuam gravados. |

### CT-USR-08: Ignorar campos de perfil fora dos limites

- **Objetivo:** Verificar que campos inválidos não substituem os valores atualmente gravados.
- **Pré-condições:** CT-USR-07 concluído. Manter a autenticação Basic Auth como `heleno_manual` / `senha123`.
- **Resultado esperado:** Resposta HTTP 200; os campos rejeitados preservam seus valores anteriores.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Enviar `PUT http://localhost:8000/api/users/heleno_manual` com `Content-Type: application/json` e corpo `{"userID": ID_ANOTADO, "userFirstName": "Al", "userMail": "email-invalido", "userAddress": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", "city": "xxxxxxxxxxxxxxxxxxxx", "phoneNumber": "12345678", "interests": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}` | Resposta HTTP 200; os valores inválidos não são aplicados. |
| 2 | Repetir `GET http://localhost:8000/api/users/myProfile` | Nome, e-mail, endereço, cidade, telefone e interesses permanecem iguais aos valores válidos do CT-USR-07. |

### CT-USR-09: Exibir a imagem padrão do perfil

- **Objetivo:** Verificar que a API retorna a imagem padrão quando a conta ainda não tem foto própria.
- **Pré-condições:** CT-USR-01 concluído e CT-USR-10 ainda não executado; a conta não recebeu uma foto.
- **Resultado esperado:** Resposta HTTP 200 com conteúdo de imagem JPEG.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Com Basic Auth `heleno_manual` / `senha123`, enviar `GET http://localhost:8000/api/users/img/heleno_manual` | Resposta HTTP 200; o corpo é uma imagem JPEG padrão, não uma página de erro. |

### CT-USR-10: Enviar e consultar foto de perfil

- **Objetivo:** Verificar que uma imagem enviada é gravada e servida pela API.
- **Pré-condições:** CT-USR-01 concluído. Ter um arquivo JPEG pequeno disponível e manter Basic Auth `heleno_manual` / `senha123`.
- **Resultado esperado:** Envio aceito, nome da imagem atualizado e imagem acessível pela rota de consulta.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Enviar `PUT http://localhost:8000/api/users/img/heleno_manual`; no Postman, escolher **Body > form-data**, adicionar a chave `profileImage` do tipo **File** e selecionar o JPEG | Resposta HTTP 200 e JSON do usuário com `urlProfileImage` igual a `profile-{userID}.jpg`. |
| 2 | Enviar `GET http://localhost:8000/api/users/img/heleno_manual` | Resposta HTTP 200 com a imagem enviada; o tipo de conteúdo é `image/jpeg`. |

### CT-USR-11: Impedir que um aluno exclua uma conta

- **Objetivo:** Verificar que a exclusão de usuários é restrita a administradores.
- **Pré-condições:** CT-USR-01 concluído. Configurar Basic Auth como `amico` / `pass`.
- **Resultado esperado:** A solicitação é negada e a conta de teste permanece disponível.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Enviar `DELETE http://localhost:8000/api/users/heleno_manual` | Resposta HTTP 403 (acesso negado para usuário sem papel de administrador). |
| 2 | Enviar `GET http://localhost:8000/api/users/heleno_manual` | Resposta HTTP 200 com JSON da conta `heleno_manual`, confirmando que a tentativa de exclusão não a removeu. |

### CT-USR-12: Administrador exclui a conta de teste

- **Objetivo:** Verificar a exclusão administrativa de uma conta sem cursos ou entregas associadas.
- **Pré-condições:** Concluir os casos anteriores. Configurar Basic Auth como `admin` / `pass`.
- **Resultado esperado:** Resposta HTTP 200 e conta removida.

| Passo | Ação | Resultado esperado do passo |
|---|---|---|
| 1 | Enviar `DELETE http://localhost:8000/api/users/heleno_manual` | Resposta HTTP 200 com os dados da conta removida. |
| 2 | Configurar Basic Auth como `heleno_manual` / `senha123` e enviar `GET http://localhost:8000/api/users/myProfile` | A autenticação falha com HTTP 401, pois a conta já não existe. |

## Registro de execução

Preencher ao executar. Em caso de falha, anexar evidência e abrir uma issue no GitHub.

| Caso | Data | Executor | Resultado obtido | Status (Passou/Falhou) | Evidência | Issue |
|---|---|---|---|---|---|---|
| CT-USR-01 |  |  |  |  |  |  |
| CT-USR-02 |  |  |  |  |  |  |
| CT-USR-03 |  |  |  |  |  |  |
| CT-USR-04 |  |  |  |  |  |  |
| CT-USR-05 |  |  |  |  |  |  |
| CT-USR-06 |  |  |  |  |  |  |
| CT-USR-07 |  |  |  |  |  |  |
| CT-USR-08 |  |  |  |  |  |  |
| CT-USR-09 |  |  |  |  |  |  |
| CT-USR-10 |  |  |  |  |  |  |
| CT-USR-11 |  |  |  |  |  |  |
| CT-USR-12 |  |  |  |  |  |  |
