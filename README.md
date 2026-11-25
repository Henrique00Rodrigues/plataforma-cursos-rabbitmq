# Plataforma de Cursos — RabbitMQ

**Trabalho final de Sistemas Distribuídos e Cloud Computing — Tema 04**

**Desenvolvedor:** Henrique Rodrigues

## 1. Sobre o projeto

Este projeto apresenta uma plataforma de cursos simulada, desenvolvida com Java e Spring Boot, que utiliza o RabbitMQ para realizar a comunicação assíncrona entre aplicações independentes.

O sistema representa acontecimentos acadêmicos, como conclusões e abandonos de cursos, envio de avaliações e revisão de avaliações.

Esses acontecimentos são representados por eventos JSON, publicados por uma aplicação produtora e encaminhados pelo RabbitMQ para diferentes consumidores, responsáveis por processar as informações e registrar seus resultados no PostgreSQL.

O principal objetivo é demonstrar conceitos de **arquitetura orientada a eventos, mensageria assíncrona, roteamento, concorrência, idempotência e recuperação de consumidores**.

## 2. Tecnologias utilizadas

- Java 21
- Spring Boot 4.1.2
- Maven (projeto multimódulo)
- RabbitMQ
- PostgreSQL
- Spring AMQP
- Spring Data JPA e Spring JDBC
- API REST

## 3. Arquitetura do sistema

O projeto utiliza uma arquitetura orientada a eventos, organizada em sete módulos Maven.

| Módulo                   | Responsabilidade                                                              |
| ------------------------ | ----------------------------------------------------------------------------- |
| `shared`                 | Define os contratos e tipos de eventos compartilhados.                        |
| `producer`               | Recebe requisições HTTP, registra os eventos e publica mensagens no RabbitMQ. |
| `certification-consumer` | Registra os certificados dos cursos concluídos.                               |
| `communication-consumer` | Registra avisos acadêmicos.                                                   |
| `pedagogical-consumer`   | Registra o histórico de avaliações enviadas e revisadas.                      |
| `audit-consumer`         | Mantém o histórico dos eventos recebidos.                                     |
| `indicators-consumer`    | Atualiza os indicadores de conclusão de cursos.                               |

O consumidor de Indicadores permite executar duas instâncias simultâneas, identificadas como A e B, que compartilham a mesma fila e dividem o processamento das mensagens.

## 4. Eventos e roteamento

O sistema possui quatro tipos de eventos:

| Evento              | Routing key          |
| ------------------- | -------------------- |
| `CursoConcluido`    | `curso.concluido`    |
| `CursoAbandonado`   | `curso.abandonado`   |
| `AvaliacaoEnviada`  | `avaliacao.enviada`  |
| `AvaliacaoRevisada` | `avaliacao.revisada` |

A exchange utilizada é `eventos.cursos`, do tipo `topic`.

### Matriz de roteamento

| Evento            | Certificação | Comunicação | Pedagógico | Auditoria | Indicadores |
| ----------------- | ------------ | ----------- | ---------- | --------- | ----------- |
| CursoConcluido    | Sim          | Sim         | Não        | Sim       | Sim         |
| CursoAbandonado   | Não          | Sim         | Não        | Sim       | Não         |
| AvaliacaoEnviada  | Não          | Não         | Sim        | Sim       | Não         |
| AvaliacaoRevisada | Não          | Sim         | Sim        | Sim       | Não         |

O roteamento é realizado por meio das binding keys configuradas no RabbitMQ, incluindo `avaliacao.#` para o acompanhamento pedagógico e `#` para a auditoria.

## 5. Contrato JSON

Todos os eventos seguem um contrato padronizado, contendo identificador único, tipo, horário de ocorrência e dados específicos.

Exemplo de evento `CursoConcluido`:

```json
{
  "eventoId": "94848307-ea96-4fbe-870e-7beb6e4fdf6c",
  "tipo": "CursoConcluido",
  "ocorridoEm": "2026-11-24T14:30:00-03:00",
  "dados": {
    "alunoId": 101,
    "cursoId": 25,
    "cargaHoraria": 40
  }
}
```

O identificador e o horário apresentados são ilustrativos. Os valores reais são gerados pela aplicação produtora.

## 6. Pré-requisitos

Para executar o projeto localmente, são necessários:

- JDK 21 instalado.
- PostgreSQL em execução na porta `5432`.
- RabbitMQ em execução na porta `5672`.
- Plugin de gerenciamento do RabbitMQ habilitado.
- PowerShell, para executar os comandos apresentados.

O painel do RabbitMQ pode ser acessado em:

http://localhost:15672

A configuração local do projeto utiliza o usuário `guest` do RabbitMQ, disponível para conexões locais.

## 7. Configuração do PostgreSQL

Crie um banco chamado `plataforma_cursos` e um usuário de aplicação chamado `plataforma_cursos_app`, com permissão para criar e acessar as tabelas.

Em uma instalação nova, os seguintes comandos podem ser executados com um usuário administrador do PostgreSQL:

```sql
CREATE ROLE plataforma_cursos_app
LOGIN PASSWORD 'DEFINA_UMA_SENHA_LOCAL';

CREATE DATABASE plataforma_cursos
OWNER plataforma_cursos_app;
```

Execute as duas instruções separadamente, fora de uma transação explícita.

Antes de iniciar cada aplicação, configure a variável de ambiente no terminal PowerShell correspondente:

```powershell
$senha = Read-Host "Senha do PostgreSQL" -AsSecureString

$env:PLATAFORMA_CURSOS_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $senha).Password
```

A configuração dos módulos utiliza essa variável para autenticar no PostgreSQL. Não é necessário inserir a senha diretamente no código-fonte.

As tabelas são inicializadas por meio dos respectivos arquivos `schema.sql`.

## 8. Compilação e execução

Clone o repositório:

```powershell
git clone https://github.com/Henrique00Rodrigues/plataforma-cursos-rabbitmq.git

cd plataforma-cursos-rabbitmq
```

Compile os módulos usando o Maven Wrapper:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Certifique-se de que PostgreSQL e RabbitMQ estejam executando.

Em terminais PowerShell separados, configure a variável de ambiente do banco e inicie as aplicações.

**Producer:**

```powershell
java -jar .\producer\target\producer-0.0.1-SNAPSHOT.jar
```

**Certificação:**

```powershell
java -jar .\certification-consumer\target\certification-consumer-0.0.1-SNAPSHOT.jar
```

**Comunicação:**

```powershell
java -jar .\communication-consumer\target\communication-consumer-0.0.1-SNAPSHOT.jar
```

**Pedagógico:**

```powershell
java -jar .\pedagogical-consumer\target\pedagogical-consumer-0.0.1-SNAPSHOT.jar
```

**Auditoria:**

```powershell
java -jar .\audit-consumer\target\audit-consumer-0.0.1-SNAPSHOT.jar
```

**Indicadores — Instância A:**

```powershell
java -jar .\indicators-consumer\target\indicators-consumer-0.0.1-SNAPSHOT.jar --indicadores.instancia=A
```

**Indicadores — Instância B:**

```powershell
java -jar .\indicators-consumer\target\indicators-consumer-0.0.1-SNAPSHOT.jar --indicadores.instancia=B
```

O módulo `shared` é uma biblioteca compartilhada e não precisa ser executado separadamente.

A configuração da exchange, das filas e dos bindings é declarada no código da aplicação produtora.

## 9. Endpoints da API

A aplicação produtora disponibiliza os seguintes endpoints:

| Método | Endpoint                             | Operação                       |
| ------ | ------------------------------------ | ------------------------------ |
| POST   | `/api/eventos/curso-concluido`       | Registrar conclusão de curso   |
| POST   | `/api/eventos/curso-abandonado`      | Registrar abandono de curso    |
| POST   | `/api/eventos/avaliacao-enviada`     | Registrar envio de avaliação   |
| POST   | `/api/eventos/avaliacao-revisada`    | Registrar revisão de avaliação |
| POST   | `/api/eventos/{eventoId}/republicar` | Republicar evento existente    |

### Exemplo de publicação

No PowerShell:

```powershell
$corpo = @{
    alunoId = 101
    cursoId = 25
    cargaHoraria = 40
} | ConvertTo-Json

$evento = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/eventos/curso-concluido" `
    -ContentType "application/json" `
    -Body $corpo

$evento | ConvertTo-Json -Depth 5
```

## 10. Testes implementados

O projeto permite demonstrar os seguintes comportamentos:

**Roteamento dos eventos:** publicação dos quatro eventos e verificação dos consumidores que recebem cada mensagem.

**Consumidores concorrentes:** execução das instâncias A e B do consumidor de Indicadores, compartilhando `fila.indicadores`.

**Interrupção e recuperação:** desligamento do consumidor de Comunicação durante a publicação de `AvaliacaoRevisada`. A mensagem permanece pendente e é processada após a reinicialização.

**Idempotência:** republicação de `CursoConcluido` com o mesmo UUID, verificando que não ocorre a emissão de um segundo certificado nem um incremento duplicado do indicador.

**Confirmação de processamento:** os consumidores utilizam ACK manual após a conclusão bem-sucedida do processamento.

Os comandos, consultas SQL, resultados esperados e explicações desses testes estão descritos na documentação técnica.

## 11. Documentação completa

A documentação técnica inclui a descrição detalhada da arquitetura, diagramas, decisões de implementação, configuração do RabbitMQ, matriz de roteamento, contrato JSON e procedimentos para reproduzir os testes.

**[Acessar documentação técnica em Word](DOCUMENTACAO/Documentacao_Tecnica_Plataforma_de_Cursos.docx)**

## 12. Considerações finais

O projeto demonstra a utilização de mensageria assíncrona para distribuir acontecimentos acadêmicos entre aplicações com responsabilidades independentes.

A combinação de RabbitMQ, Spring Boot e PostgreSQL permite observar o roteamento das mensagens, o processamento concorrente, a recuperação após interrupções e a prevenção de efeitos duplicados.

O sistema foi desenvolvido para fins acadêmicos, com foco na demonstração dos conceitos de Sistemas Distribuídos e Cloud Computing.
