**Título:** Página de um curso inexistente retorna erro 500 em vez de 404

**Labels sugeridas:** `bug`

## Descrição

Ao abrir a página de um curso que não existe (por exemplo, um curso que acabou de ser excluído), o sistema mostra um erro 500 (Internal Server Error). O esperado é uma página de "curso não encontrado" (404).

## Passos para reproduzir

1. Subir a aplicação com os dados iniciais (`mvn spring-boot:run` na pasta `AMICOServer`).
2. Entrar como `admin` / `pass` e abrir http://localhost:8000/admin.
3. Excluir o curso **Introduction to AI** com o botão **Delete**.
4. Abrir http://localhost:8000/course/introduction-to-ai.

(Também deve reproduzir com qualquer nome inexistente, como http://localhost:8000/course/curso-que-nao-existe.)

## Resultado esperado

Página de erro amigável (404 / curso não encontrado).

## Resultado obtido

Erro 500.

## Causa provável

Análise do código, sem o stack trace em mãos: em `CourseInformationController.course`, o método chama `courseRepository.findByInternalName(internalName)`, que devolve `null` quando o curso não existe. Logo em seguida o código usa `course.getName()` sem verificar o `null`, o que lança `NullPointerException`. Os métodos `/course/{internalName}/subjects` e `/course/{internalName}/skills` seguem o mesmo padrão e provavelmente têm o mesmo problema.

Arquivo: `AMICOServer/src/main/java/com/example/demo/controllers/CourseInformationController.java` (método `course`, por volta das linhas 50 a 73).

## Sugestão de correção

Verificar se `course == null` e devolver a página de erro 404 (já existem templates em `templates/error/404.html` e `templates/HTML/ErrorPages/404Error.html`) ou lançar uma exceção que o Spring mapeie para 404. Aplicar a mesma verificação aos endpoints `/subjects` e `/skills`.

## Evidência

- Caso de teste manual CT-MOD-09, passo 6 (`docs/casos-de-teste-manuais-modify-course.md`).
- Capturas: `manual-test-results/modify-course/CT-MOD-09.1.gif` e `CT-MOD-09.2.gif`.
