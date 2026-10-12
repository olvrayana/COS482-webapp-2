# Evidencias das reproducoes (curl) - MoodleController

Registro textual das saidas dos comandos curl usados nas reproducoes.
Cada bloco mostra o comando e a saida capturada.

## CT-07 passo 3: download de submissao alheia (practiceID 3, de student-5)

Comando (logado como amico):

```
curl.exe -b cookies_amico.txt -i "http://localhost:8000/moodle/download/studyItem/introduction-to-ai/history-of-ai/13/3" -s -o baixado_13_3.bin -w "13/3: %{http_code} %{size_download} bytes"
```

Saida:

```
13/3: 200 0 bytes
```

Resultado: HTTP 200 com corpo de 0 bytes. A submissao de outro aluno nao e
entregue, mas a resposta nao indica erro (sem 403/404). Evidencia binaria em
`download-alheio-13-3-200-vazio.bin`.

Observacao: a URL `/13/4` (usada na primeira tentativa) nao e submissao
alheia - o practiceID 4 e a submissao "Not Presented" do proprio amico,
criada quando ele visitou a pagina. O download dela retornou 200 com 140835
bytes (arquivo fisico practice-4.txt, JPEG herdado de dados antigos no disco).

## CT-11: submissao com arquivo vazio e download do link

Comando (logado como amico, arquivo vazio com 2 bytes de CRLF):

```
curl.exe -b cookies_amico.txt -X POST "http://localhost:8000/moodle/practice/modify/introduction-to-ai/history-of-ai/13/4" -F "newName=Entrega sem arquivo" -F "itemFile=@vazio.txt"
```

Saida: HTTP 302 (redirect para a propria disciplina).

Estado apos o POST (trecho do HTML da pagina):

```html
<span class="col-md-2"
  >File :
  <a
    class="item-content col-md-2"
    href="/moodle/download/studyItem/introduction-to-ai/history-of-ai/13/4"
    >Entrega sem arquivo</a
  ></span
>
```

A pratica aparece como entregue, com link de download.

Download do link:

```
curl.exe -b cookies_amico.txt "http://localhost:8000/moodle/download/studyItem/introduction-to-ai/history-of-ai/13/4" -s -o baixado_ct11b.bin -w "status: %{http_code} | bytes: %{size_download}"
```

Saida:

```
status: 200 | bytes: 2
```

O link baixa o arquivo gravado (2 bytes, o CRLF do arquivo "vazio"). Com um
arquivo de 0 bytes reais, o upload e pulado (isEmpty) e o link aponta para o
arquivo fisico antigo da pasta (herdado de dados anteriores). Nos dois casos
a pratica fica marcada como entregue. Evidencia binaria em
`download-entrega-vazia-13-4.bin`.

## Issue 8: count() como ID apos delecao de pratica

Cenario: 4 praticas no banco (IDs 1-4). O professor apaga a Practice 2
(studyItem 13), o que remove as praticas 3 e 4. Depois, um aluno sem
submissao (student-0) visita a pagina.

Comando da delecao (logado como amicoTeacher):

```
curl.exe -b cookies_teacher.txt -X POST "http://localhost:8000/moodle/studyItem/modify/introduction-to-ai/history-of-ai/13" -d "newName=x" -d "newType=pdf" -d "btnDelete=delete"
```

Saida: HTTP 302.

Visita do aluno sem submissao (logado como student-0):

```
curl.exe -b cookies_student0.txt "http://localhost:8000/moodle/introduction-to-ai/history-of-ai"
```

Saida (corpo da resposta, HTTP 500):

```json
{
  "timestamp": 1791765089523,
  "status": 500,
  "error": "Internal Server Error",
  "exception": "org.springframework.orm.jpa.JpaObjectRetrievalFailureException",
  "message": "Unable to find com.example.demo.practices.Practices with id 3; nested exception is javax.persistence.EntityNotFoundException: Unable to find com.example.demo.practices.Practices with id 3",
  "path": "/moodle/introduction-to-ai/history-of-ai"
}
```

Resultado: a pagina do aluno quebra com 500 (EntityNotFoundException). O
`getOne(count())` buscou um ID que nao existe mais apos as delecoes. Evidencia
em `erro-500-entitynotfound-student0.json`.

Nota: o efeito real e pior do que o previsto na analise (a pagina inteira do
aluno fica inacessivel, nao apenas a submissao errada).

## CT-08: nota negativa (-5)

Comando (logado como amicoTeacher):

```
curl.exe -b cookies_teacher.txt -X POST "http://localhost:8000/moodle/practice/update-calification/introduction-to-ai/history-of-ai/12/1" -d "newCalification=-5" -o NUL -s -w "CT-08 HTTP: %{http_code}"
```

Saida:

```
CT-08 HTTP: 302
```

Verificacao do estado apos o POST (trecho do HTML da pagina):

```html
<input
  class="form-control"
  type="number"
  name="newCalification"
  value="-5.0"
  min="0"
  max="10"
  step="any"
/>
```

A nota -5.0 foi gravada e exibida, apesar do min="0" do formulario.

## CT-09: nota acima do maximo (1000)

Comando (logado como amicoTeacher):

```
curl.exe -b cookies_teacher.txt -X POST "http://localhost:8000/moodle/practice/update-calification/introduction-to-ai/history-of-ai/12/1" -d "newCalification=1000" -o NUL -s -w "CT-09 HTTP: %{http_code}"
```

Saida:

```
CT-09 HTTP: 302
```

Verificacao do estado apos o POST (trecho do HTML da pagina):

```html
<input
  class="form-control"
  type="number"
  name="newCalification"
  value="1000.0"
  min="0"
  max="10"
  step="any"
/>
```

A nota 1000.0 foi gravada e exibida.

## CT-10: nota com texto (abc)

Comando (logado como amicoTeacher):

```
curl.exe -b cookies_teacher.txt -X POST "http://localhost:8000/moodle/practice/update-calification/introduction-to-ai/history-of-ai/12/1" -d "newCalification=abc" -o NUL -s -w "CT-10 HTTP: %{http_code}"
```

Saida:

```
CT-10 HTTP: 400
```

A requisicao foi rejeitada (falha de conversao String para Double), com a
pagina de erro padrao do Spring, sem mensagem amigavel.

## CT-12: reenvio apos correcao

Comando (logado como amico; a submissao 12/1 estava corrigida):

```
curl.exe -b cookies_amico.txt -X POST "http://localhost:8000/moodle/practice/modify/introduction-to-ai/history-of-ai/12/1" -F "newName=Alteracao apos correcao" -F "itemFile=@reenvio.txt"
```

Saida:

```
CT-12 HTTP: 302
```

Verificacao do estado apos o POST (trecho do HTML da pagina):

```html
<span class="col-md-2"
  >File :
  <a
    class="item-content col-md-2"
    href="/moodle/download/studyItem/introduction-to-ai/history-of-ai/12/1"
    >Alteracao apos correcao</a
  ></span
>
<label class="ml-1 col-md-3">Name: Alteracao apos correcao</label>
```

A submissao corrigida foi alterada: nome e arquivo substituidos, mantendo a
nota antiga.

## CT-13: rename vazio com arquivo

Comando (logado como amico):

```
curl.exe -b cookies_amico.txt -X POST "http://localhost:8000/moodle/practice/modify/introduction-to-ai/history-of-ai/13/4" -F "newName=" -F "itemFile=@reenvio.txt" -o NUL -s -w "CT-13 HTTP: %{http_code}"
```

Saida:

```
CT-13 HTTP: 302
```

Verificacao do estado apos o POST: nenhum dado alterado (a submissao
continuou com o nome anterior). O `if (!newName.isEmpty())` protege este
caminho.
