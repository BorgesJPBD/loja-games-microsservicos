# Loja de Games - Microsserviços

Projeto final da disciplina de **APIs e Microsserviços** (TADS, IFMS), Prof. Fernando Cesar Balbino.

Aplicação de comércio eletrônico de jogos, organizada em **3 microsserviços** com **Java, Spring Boot e RabbitMQ**.

**Integrantes:** Guilherme Alves, João Pedro Borges, Matheus Costa

> 📄 **Documentação da aplicação** (domínio, propósito, diagrama de arquitetura, descrição dos microsserviços e fluxo de dados): [docs/DOCUMENTACAO.md](docs/DOCUMENTACAO.md)

---

## Sumário

1. [Visão geral](#1-visão-geral)
2. [Pré-requisitos](#2-pré-requisitos)
3. [Instalação e execução](#3-instalação-e-execução)
4. [Configuração](#4-configuração)
5. [Como testar](#5-como-testar)
6. [Referência das APIs](#6-referência-das-apis)
7. [Estrutura do repositório](#7-estrutura-do-repositório)
8. [Problemas comuns](#8-problemas-comuns)

---

## 1. Visão geral

| Serviço | Porta | Responsabilidade | Banco (H2) |
| --- | --- | --- | --- |
| `produto-service` | 8081 | Catálogo de jogos e controle de estoque | `produto_db` |
| `pedido-service` | 8082 | Criação e acompanhamento de pedidos | `pedido_db` |
| `notificacao-service` | 8083 | Avisos ao cliente sobre o resultado do pedido | `notificacao_db` |

- O **cliente** fala com cada serviço por **HTTP** (síncrono).
- Os **serviços** conversam entre si por **RabbitMQ** (assíncrono), pela exchange `loja.eventos`.
- Cada serviço tem o **próprio banco H2** em arquivo. Nenhum serviço acessa o banco do outro.

**Fluxo principal:** o `pedido-service` salva o pedido como `PENDENTE` e publica `pedido.criado` → o `produto-service` confere e baixa o estoque, calcula o valor total e publica `estoque.resultado` → o `pedido-service` muda o pedido para `CONFIRMADO` ou `RECUSADO`, e o `notificacao-service` grava o aviso ao cliente.

O diagrama e a explicação completa estão na [documentação](docs/DOCUMENTACAO.md).

## 2. Pré-requisitos

- **Git**
- **JDK 25** (versão definida em `java.version` nos `pom.xml`). Confira com `java -version`.
- **Docker** com Docker Compose, em execução, para o RabbitMQ.
- Para testar a API: **Postman**, Insomnia ou `curl`.

Não é preciso instalar o Maven nem um banco de dados:
- cada serviço traz o **Maven Wrapper** (`mvnw` / `mvnw.cmd`);
- o **H2** roda embutido em cada serviço e cria o banco sozinho na pasta `data/` do serviço.

## 3. Instalação e execução

### 3.1. Clonar o repositório

```bash
git clone https://github.com/BorgesJPBD/loja-games-microsservicos.git
cd loja-games-microsservicos
```

### 3.2. Opção rápida no Windows: `iniciar.cmd`

Na raiz do projeto:

```bash
.\iniciar.cmd
```

O script sobe o RabbitMQ no Docker, espera 10 segundos e abre **3 janelas**, uma para cada serviço. Cada serviço está pronto quando a janela mostra `Started ...Application`. Na primeira execução o Maven baixa as dependências, o que leva alguns minutos.

### 3.3. Opção manual (qualquer sistema)

**1) Subir o RabbitMQ**, na raiz do projeto:

```bash
docker compose up -d
```

Confira com `docker ps`: deve aparecer o container `lojagames-rabbitmq`.

**2) Subir os 3 serviços**, cada um no próprio terminal, a partir da raiz:

Windows (PowerShell):

```powershell
# Terminal 1
cd produto-service
.\mvnw.cmd spring-boot:run

# Terminal 2
cd pedido-service
.\mvnw.cmd spring-boot:run

# Terminal 3
cd notificacao-service
.\mvnw.cmd spring-boot:run
```

Linux / macOS / Git Bash:

```bash
# Terminal 1
cd produto-service && ./mvnw spring-boot:run

# Terminal 2
cd pedido-service && ./mvnw spring-boot:run

# Terminal 3
cd notificacao-service && ./mvnw spring-boot:run
```

> Se aparecer `Permission denied` no Linux/macOS, rode `chmod +x mvnw` dentro da pasta do serviço.

### 3.4. Conferir a mensageria

Painel do RabbitMQ: http://localhost:15672 (usuário `admin`, senha `admin`).

- aba **Exchanges**: deve existir `loja.eventos`;
- aba **Queues and Streams**: devem existir `produto.pedido-criado`, `pedido.estoque-resultado` e `notificacao.estoque-resultado`.

As filas e os bindings são criados pelos próprios serviços quando eles ligam.

### 3.5. Encerrar

- Pare cada serviço com `Ctrl + C` (ou feche a janela).
- Pare o RabbitMQ com `docker compose down`.

## 4. Configuração

As configurações ficam em `src/main/resources/application.properties` de cada serviço.

| Item | Valor |
| --- | --- |
| Portas | 8081 (produto), 8082 (pedido), 8083 (notificação) |
| RabbitMQ | `localhost:5672`, usuário `admin`, senha `admin` (definidos no `docker-compose.yml`) |
| Banco | H2 em arquivo: `jdbc:h2:file:./data/<servico>_db`, usuário `sa`, senha vazia |
| Console do H2 | `http://localhost:<porta>/h2-console` |
| Tabelas | criadas pelo Hibernate (`spring.jpa.hibernate.ddl-auto=update`) |
| Tentativas do listener | 3 no total, com 1 s de intervalo; depois a mensagem é descartada |

**Mensageria:**

| Item | Nome |
| --- | --- |
| Exchange (topic) | `loja.eventos` |
| Routing key publicada pelo `pedido-service` | `pedido.criado` |
| Routing key publicada pelo `produto-service` | `estoque.resultado` |
| Fila do `produto-service` | `produto.pedido-criado` |
| Fila do `pedido-service` | `pedido.estoque-resultado` |
| Fila do `notificacao-service` | `notificacao.estoque-resultado` |

As mensagens trafegam em JSON.

**Carga inicial:** quando o banco do `produto-service` está vazio, ele cadastra 8 jogos automaticamente:

| id | Jogo | Plataforma | Preço | Estoque |
| --- | --- | --- | --- | --- |
| 1 | Elden Ring | PC | 229,90 | 10 |
| 2 | Hollow Knight | PC | 46,99 | 20 |
| 3 | God of War Ragnarok | PLAYSTATION_5 | 249,90 | 8 |
| 4 | Gran Turismo 7 | PLAYSTATION_5 | 299,90 | 5 |
| 5 | Forza Horizon 5 | XBOX_SERIES | 199,90 | 6 |
| 6 | Halo Infinite | XBOX_SERIES | 149,90 | 4 |
| 7 | Zelda: Tears of the Kingdom | NINTENDO_SWITCH | 349,90 | 7 |
| 8 | Mario Kart 8 Deluxe | NINTENDO_SWITCH | 299,90 | 1 |

Para recomeçar do zero, pare os serviços e apague a pasta `data/` de cada um.

## 5. Como testar

O roteiro abaixo considera os **bancos novos** (primeira execução), com os 3 serviços e o RabbitMQ ligados. Os exemplos usam o Postman: nas requisições `POST`, use a aba **Body** → **raw** → **JSON**.

### Passo 1: ver o catálogo

`GET http://localhost:8081/produtos` → os 8 jogos da carga inicial.

`GET http://localhost:8081/produtos?plataforma=PLAYSTATION_5` → só os jogos de PlayStation 5.

### Passo 2: pedido que deve ser CONFIRMADO

`POST http://localhost:8082/pedidos`

```json
{
  "produtoId": 3,
  "quantidade": 2
}
```

Resposta: **202 Accepted**, com `"status": "PENDENTE"`. O processamento continua de forma assíncrona.

### Passo 3: conferir os efeitos do fluxo

| Requisição | Resultado esperado |
| --- | --- |
| `GET http://localhost:8082/pedidos/1` | `"status": "CONFIRMADO"` e `"valorTotal": 499.80` |
| `GET http://localhost:8081/produtos/3` | `"estoque": 6` (era 8) |
| `GET http://localhost:8083/notificacoes` | "Pedido #1 confirmado! Total: R$ 499,80. Obrigado pela compra." com `"lida": false` |

Se o pedido ainda estiver `PENDENTE`, repita o `GET`: o processamento leva uma fração de segundo.

### Passo 4: pedido que deve ser RECUSADO (estoque insuficiente)

`POST http://localhost:8082/pedidos`

```json
{
  "produtoId": 8,
  "quantidade": 2
}
```

Depois, `GET http://localhost:8082/pedidos/2` → `"status": "RECUSADO"` e `"motivo": "Estoque insuficiente (disponível: 1)"`. O estoque do produto 8 continua em 1, e aparece a notificação "Pedido #2 recusado: Estoque insuficiente (disponível: 1)".

Com `"produtoId": 999`, o pedido é recusado com o motivo "Produto não encontrado".

### Passo 5: marcar a notificação como lida

`PATCH http://localhost:8083/notificacoes/1/lida` (sem body) → a notificação volta com `"lida": true`.

`GET http://localhost:8083/notificacoes?lida=false` → só as que ainda não foram lidas.

### Passo 6 (opcional): serviço consumidor desligado

1. Pare o `produto-service`.
2. Faça um novo `POST /pedidos`. O pedido fica `PENDENTE`, e no painel do RabbitMQ a fila `produto.pedido-criado` mostra 1 mensagem em **Ready**.
3. Ligue o `produto-service` de novo. Ele consome a mensagem guardada, e o pedido muda para `CONFIRMADO`.

### Passo 7 (opcional): RabbitMQ fora do ar

1. Pause o RabbitMQ com `docker compose stop`.
2. Faça um `POST /pedidos` → resposta **503 Service Unavailable**, e o pedido fica `RECUSADO` com o motivo "Serviço de mensagens indisponível. Tente novamente.".
3. Volte o RabbitMQ com `docker compose start`. Os serviços se reconectam sozinhos.

### Respostas de erro

Todos os serviços respondem erros no mesmo formato JSON. Exemplos:

`GET http://localhost:8081/produtos/999`

```json
{
  "momento": "2026-11-22T15:00:00.000",
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Produto não encontrado"
}
```

`POST http://localhost:8081/produtos` com campos inválidos retorna **400** com a lista dos campos:

```json
{
  "momento": "2026-11-22T15:00:00.000",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos",
  "campos": { "nome": "não deve estar em branco" }
}
```

### Testando pelo terminal

Linux / macOS / Git Bash (`curl`):

```bash
curl localhost:8081/produtos
curl -X POST localhost:8082/pedidos -H "Content-Type: application/json" -d '{"produtoId":3,"quantidade":2}'
curl localhost:8082/pedidos/1
curl localhost:8083/notificacoes
```

Windows PowerShell:

```powershell
Invoke-RestMethod http://localhost:8081/produtos
Invoke-RestMethod -Method Post -Uri http://localhost:8082/pedidos -ContentType "application/json" -Body '{"produtoId":3,"quantidade":2}'
Invoke-RestMethod http://localhost:8082/pedidos/1
Invoke-RestMethod http://localhost:8083/notificacoes
```

## 6. Referência das APIs

### produto-service (8081)

| Método | Rota | Descrição |
| --- | --- | --- |
| POST | `/produtos` | Cadastra um jogo (`nome`, `plataforma`, `genero`, `preco`, `estoque`). Responde 201 |
| GET | `/produtos` | Lista os jogos. Filtro opcional: `?plataforma=PC` |
| GET | `/produtos/{id}` | Busca um jogo (404 se não existir) |
| PUT | `/produtos/{id}` | Edita um jogo (todos os campos) |
| DELETE | `/produtos/{id}` | Exclui um jogo. Responde 204 |

Valores aceitos em `plataforma`: `PC`, `PLAYSTATION_5`, `XBOX_SERIES`, `NINTENDO_SWITCH`.

Exemplo de body para `POST` e `PUT`:

```json
{
  "nome": "Elden Ring",
  "plataforma": "PC",
  "genero": "RPG",
  "preco": 229.90,
  "estoque": 10
}
```

### pedido-service (8082)

| Método | Rota | Descrição |
| --- | --- | --- |
| POST | `/pedidos` | Cria um pedido (`produtoId`, `quantidade` mínima 1) e inicia o fluxo assíncrono. Responde 202 (ou 503 se o RabbitMQ estiver fora do ar) |
| GET | `/pedidos` | Lista os pedidos |
| GET | `/pedidos/{id}` | Busca um pedido (404 se não existir) |

Status possíveis: `PENDENTE`, `CONFIRMADO` (com `valorTotal`) e `RECUSADO` (com `motivo`).

### notificacao-service (8083)

| Método | Rota | Descrição |
| --- | --- | --- |
| GET | `/notificacoes` | Lista as notificações. Filtros opcionais: `?pedidoId=1` e `?lida=false` |
| PATCH | `/notificacoes/{id}/lida` | Marca uma notificação como lida (404 se não existir) |

## 7. Estrutura do repositório

```
loja-games-microsservicos/
├── docs/
│   └── DOCUMENTACAO.md        # documentação da aplicação (com diagramas)
├── docker-compose.yml         # RabbitMQ
├── iniciar.cmd                # sobe tudo no Windows
├── README.md
├── produto-service/
├── pedido-service/
└── notificacao-service/
```

Os três serviços seguem a mesma organização em `src/main/java/br/com/lojagames/<servico>/`:

| Pasta | Conteúdo |
| --- | --- |
| `controller/` | endpoints HTTP |
| `messaging/` | listeners do RabbitMQ e eventos (mensagens) |
| `service/` | regras de negócio |
| `repository/` | acesso ao banco (Spring Data JPA) |
| `entity/` | entidades (tabelas) |
| `dto/` | dados recebidos pela API |
| `config/` | configuração do RabbitMQ (e a carga inicial, no produto-service) |
| `exception/` | tratamento de erros (`@RestControllerAdvice`) |

## 8. Problemas comuns

| Sintoma | Causa provável e solução |
| --- | --- |
| `Port 808x was already in use` | O serviço já está rodando em outro terminal. Pare a outra execução. No Windows, para ver quem usa a porta: `netstat -ano \| findstr :8081` |
| `Database may be already in use` / `The file is locked` | O mesmo serviço está rodando duas vezes e travou o arquivo do H2. Deixe só uma execução |
| Erro de conexão com o RabbitMQ (`Connection refused`, porta 5672) | O container não está rodando. Execute `docker compose up -d` e confira com `docker ps` |
| Pedido fica `PENDENTE` para sempre | O `produto-service` está desligado ou não conectou no RabbitMQ. Veja o log dele e as filas no painel |
| `415 Unsupported Media Type` | No Postman, o body precisa ser **raw → JSON** |
| `400 Bad Request` | JSON inválido ou campo obrigatório faltando. A resposta mostra a mensagem e os campos com erro |
| Erro de versão do Java ao compilar | Instale o JDK 25 ou ajuste `<java.version>` nos três `pom.xml` |
| `Permission denied` ao rodar `./mvnw` (Linux/macOS) | Rode `chmod +x mvnw` na pasta do serviço |