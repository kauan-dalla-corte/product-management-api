<div align="center">

# Product Management API

**API REST para gerenciamento de produtos, desenvolvida com foco em boas práticas de arquitetura, modelagem de domínio rico e qualidade de código.**

![Java](https://img.shields.io/badge/Java-21-9e9e9e?logo=openjdk&logoColor=white&labelColor=ED8B00)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.0-9e9e9e?logo=springboot&logoColor=white&labelColor=6DB33F)
![Spring Web](https://img.shields.io/badge/Spring_Web-REST_API-9e9e9e?logo=spring&logoColor=white&labelColor=6DB33F)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-Persistence-9e9e9e?logo=spring&logoColor=white&labelColor=6DB33F)
![MySQL](https://img.shields.io/badge/MySQL-8.4-9e9e9e?logo=mysql&logoColor=white&labelColor=1479A1)
![Hibernate](https://img.shields.io/badge/Hibernate-ORM-9e9e9e?logo=hibernate&logoColor=white&labelColor=57666C)
![H2](https://img.shields.io/badge/H2-Test_Database-9e9e9e?logoColor=white&labelColor=0A6EBD)
![JUnit 5](https://img.shields.io/badge/JUnit_5%20+%20Mockito-Tests-9e9e9e?logo=junit5&logoColor=white&labelColor=C21325)
![Maven](https://img.shields.io/badge/Maven-Build-9e9e9e?logo=apachemaven&logoColor=white&labelColor=C71A36)
![Flyway](https://img.shields.io/badge/Flyway-Migrations-9e9e9e?logo=flyway&logoColor=white&labelColor=CC0200)
![Docker](https://img.shields.io/badge/Docker-Containerization-9e9e9e?logo=docker&logoColor=white&labelColor=2496ED)
![Lombok](https://img.shields.io/badge/Lombok-Boilerplate-9e9e9e?logoColor=white&labelColor=a303a0)
![SpringDoc OpenAPI](https://img.shields.io/badge/SpringDoc_OpenAPI-3.0.2-9e9e9e?logo=swagger&logoColor=white&labelColor=6C9A2D)
</br>
</div>

---

> Projeto backend desenvolvido por Kauan Dalla Corte com foco em modelagem de domínio, controle de estoque e auditoria de alterações de preço.

---
## Table of Contents

- [Sobre o Projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Modelagem de Domínio](#modelagem-de-domínio)
- [Decisões Técnicas](#decisões-técnicas)
- [Documentação da API](#documentação-da-api)
- [Endpoints](#endpoints)
- [Testes](#testes)
- [Como Executar](#como-executar)
- [Trade-offs](#trade-offs)
- [Aprendizados](#aprendizados)
- [Evoluções Planejadas](#evoluções-planejadas)

---

## Sobre o Projeto

O Product Management Service é uma API para cadastro e controle de produtos, com rastreamento completo de movimentações de estoque e histórico de alterações de preço e custo.

O foco do projeto não é apenas manipulação de dados, mas a modelagem de um domínio onde mudanças relevantes são tratadas como eventos auditáveis, o estoque segue regras explícitas de integridade e o produto possui um ciclo de vida bem definido.

Em vez de centralizar regras nos services, o sistema foi projetado para que o próprio domínio seja responsável por proteger seu estado e garantir suas invariantes. Isso evita entidades anêmicas e torna o comportamento mais consistente, previsível e alinhado com as regras de negócio.

---

## Funcionalidades

- Cadastro, ativação e desativação de produtos
- Controle de estoque com entradas e saídas rastreadas
- Alteração de preço e custo com histórico completo de mudanças
- Validações de regras de negócio no domínio (preço abaixo do custo, estoque insuficiente, transições de estado inválidas, entre outras)
- Proteção contra concorrência com Optimistic Locking
- Migrations versionadas com Flyway
- Documentação interativa via Swagger UI

---

## Arquitetura

O projeto segue uma estrutura em camadas inspirada em princípios de DDD:

```
catalog/product/
├── domain/
│   ├── model/          → entidades e value objects
│   ├── exception/      → exceções de domínio
│   └── repository/     → interfaces (abstrações)
├── application/
│   ├── service/        → orquestração de casos de uso
│   ├── dto/            → objetos de request e response
│   └── mapper/         → conversão domínio ↔ DTO
└── infrastructure/
    ├── persistence/    → conversores JPA
    └── web/            → controllers, exception handler, envelope de resposta

shared/
├── valueobject/Money
└── exception/InvalidMonetaryValueException
```

O projeto aplica conceitos de DDD de forma pragmática, com algumas simplificações para manter o escopo viável.

---

## Modelagem de Domínio

### Product

Entidade principal responsável por proteger suas próprias invariantes:

- Preço não pode ser zero
- Preço não pode ser menor que o custo
- Estoque não pode ser negativo
- Produto com estoque não pode ser desativado

As operações retornam efeitos do domínio (ex: mudanças monetárias, movimentações de estoque) em vez de apenas mutar estado.

---

### Value Objects

**Money**
- Encapsula `BigDecimal`
- Escala fixa (2 casas decimais)
- Arredondamento `HALF_UP`
- Comparação via `compareTo` (evita problemas de escala)

**ProductName**
- Normalização (uppercase, trim)
- Validação por regex
- Garante consistência e evita duplicidade case-insensitive

---

### Estados do Produto

`ProductStatus` implementa comportamento diretamente no enum:

- `activate()`
- `deactivate()`
- `ensureModifiable()`

Transições inválidas são bloqueadas no próprio domínio. Cada status implementa seu próprio comportamento de transição, eliminando condicionais espalhados pelo código.

---

### Auditoria

- `ProductMonetaryChange` — histórico de alterações de preço e custo
- `ProductStockMovement` — histórico de movimentações de estoque

Esses registros são gerados como consequência natural das operações do domínio.

---

## Decisões Técnicas

### Domínio rico

As regras de negócio vivem dentro das entidades de domínio, não nos services. `Product` protege seu próprio estado e retorna os registros de auditoria como resultado das operações:

```java
public ProductMonetaryChange changePrice(Money newPrice) {
    status.ensureModifiable();

    if (newPrice.equals(Money.zero())) throw new InvalidMonetaryValueException();
    if (this.price.equals(newPrice)) throw new SamePriceException();

    validatePriceBelowCost(newPrice, this.cost);
    Money oldPrice = this.price;
    this.price = newPrice;

    return ProductMonetaryChange.priceUpdate(this, newPrice, oldPrice);
}
```

---

### Value Object `Money`

Encapsula `BigDecimal` com normalização de escala (2 casas decimais, `HALF_UP`), validação contra valores negativos e nulos, e operações aritméticas seguras (`add`, `subtract`, `multiply`). O `equals` é implementado via `compareTo` para evitar falsos negativos causados por diferença de escala, decisão crítica ao trabalhar com `BigDecimal`.

---

### `ProductName` como Value Object

Normaliza o nome para uppercase, valida por regex e garante que nomes duplicados sejam detectados de forma case-insensitive. Persistido como `String` via `ProductNameConverter` (`AttributeConverter` com `autoApply = true`), mantendo o domínio desacoplado do formato de persistência.

---

### Máquina de estados com enum

`ProductStatus` implementa os métodos `activate()`, `deactivate()` e `ensureModifiable()` diretamente em cada constante do enum, eliminando condicionais nos services. Transições inválidas lançam `InvalidProductStateTransitionException` imediatamente, sem possibilidade de bypass.

---

### Optimistic Locking

Controle de concorrência com `@Version`:

- Evita inconsistência em atualizações simultâneas
- Retorna `409 Conflict` em caso de conflito

---

### Double-check de unicidade

O service verifica `existsByName` antes de salvar e também captura `DataIntegrityViolationException` no save, cobrindo a janela de concorrência entre o check e o insert.

---

### Query condicional no repositório

`ProductStockMovementRepository` usa uma query JPQL com filtro opcional por tipo, eliminando bifurcações no controller e no service:

```java
@Query("""
    SELECT m FROM ProductStockMovement m
    WHERE m.product.id = :productId
      AND (:type IS NULL OR m.type = :type)
    ORDER BY m.createdAt DESC
    """)
List<ProductStockMovement> findAllByProductIdAndOptionalType(
    @Param("productId") Long productId,
    @Param("type") StockMovementType type
);
```

---

### Envelope de resposta padronizado

Todas as respostas seguem o envelope `ApiResult<T>`:

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "meta": { "timestamp": "2026-03-29T17:16:17.156Z" }
}
```

Erros sempre retornam um `ErrorCode` semântico (`PRICE_BELOW_COST`, `INSUFFICIENT_STOCK`, `OPTIMISTIC_LOCK_FAILURE`, etc.), facilitando o tratamento no cliente sem depender de mensagens de texto.

---

### Migrations com Flyway

- Versionamento explícito do schema
- `ddl-auto: validate` — Hibernate apenas valida, nunca altera o banco

---

## Documentação da API

A aplicação expõe sua especificação OpenAPI por meio do SpringDoc, permitindo visualizar e testar os endpoints de forma interativa via Swagger UI.

Além de facilitar a exploração da API, isso torna os contratos HTTP mais explícitos e melhora a legibilidade técnica do projeto para quem estiver avaliando o código.

Após iniciar a aplicação, a documentação pode ser acessada em:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## Endpoints

### Produtos

| Método | Rota                                 |
|--------|--------------------------------------|
| GET    | `/api/v1/products`                   |
| GET    | `/api/v1/products/{id}`              |
| POST   | `/api/v1/products`                   |
| PATCH  | `/api/v1/products/{id}/activate`     |
| PATCH  | `/api/v1/products/{id}/deactivate`   |

### Preço e Custo

| Método | Rota                                             |
|--------|--------------------------------------------------|
| PATCH  | `/api/v1/products/{id}/monetary/price`           |
| PATCH  | `/api/v1/products/{id}/monetary/cost`            |
| GET    | `/api/v1/products/{id}/monetary/price/history`   |
| GET    | `/api/v1/products/{id}/monetary/cost/history`    |

### Estoque

| Método | Rota                                      |
|--------|-------------------------------------------|
| PATCH  | `/api/v1/products/{id}/stock/inbound`     |
| PATCH  | `/api/v1/products/{id}/stock/outbound`    |
| GET    | `/api/v1/products/{id}/stock/movements`   |

---

## Testes

O projeto cobre diferentes níveis:

- **Domínio** — valida invariantes e regras de negócio
- **Service** — fluxo de casos de uso com mocks
- **Controller** — contratos HTTP e validações de entrada
- **Integração** — fluxo completo com banco H2
- **Persistência** — mapeamentos JPA e queries

Os testes de controller usam `ControllerTestSupport` com helpers expressivos (`successTrue()`, `errorCode()`, `dataArrayPath()`) que tornam as asserções legíveis e reutilizáveis. Os testes de domínio e de service usam `ProductBuilder` e `ProductMonetaryChangeBuilder` para construção fluente de objetos, sem dependência de infraestrutura.

---

## Como Executar

### Pré-requisitos

- Java 21
- Maven
- Docker

### Subir o banco

```bash
docker-compose up -d
```

### Rodar a aplicação

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Rodar os testes

```bash
./mvnw test
```

---

## Trade-offs

Algumas decisões foram tomadas para equilibrar complexidade e clareza:

- Uso de JPA diretamente no domínio para reduzir overhead estrutural
- Ausência de Event Sourcing, mantendo um modelo mais simples e direto
- Uso de H2 nos testes em vez de Testcontainers, priorizando velocidade de execução
- Separação em camadas sem adoção completa de arquitetura hexagonal

O objetivo foi aplicar conceitos de forma prática, evitando overengineering.

---
## Aprendizados

> Esse projeto não foi linear. Ele passou por múltiplas refatorações de modelagem, nomenclatura e decisões arquiteturais e cada uma surgiu de uma insatisfação real com o que estava construído.
>
>A dificuldade não esteve na implementação das funcionalidades, mas nas decisões de design. Modelar um domínio do zero, sem referência pronta, exige lidar constantemente com incerteza: saber se uma abstração resolve um problema real ou apenas adiciona complexidade, decidir se uma camada existe por necessidade ou por hábito, escolher entre simplicidade e pureza arquitetural sabendo que ambos têm custo.
>
>Ao longo do processo, várias abordagens foram descartadas. Nomes foram trocados, responsabilidades reorganizadas e partes inteiras reescritas. Não por perfeccionismo, mas porque o código, em certos momentos, deixava de representar o problema de forma honesta.
>
>Esse processo deixou alguns aprendizados claros:
>
>- Modelar bem é difícil. Não existe resposta certa no início, apenas decisões e suas consequências
>- Refatoração não é retrabalho, é parte essencial da construção
>- DDD e Clean Architecture são ferramentas, não objetivos. Aplicar sem critério gera complexidade sem valor
>- O equilíbrio entre abstração e pragmatismo não é dado ele precisa ser construído
>- Nomenclatura não é detalhe. Nome ruim geralmente indica um design mal resolvido
>
>Mais do que o resultado final, o valor deste projeto está no processo que o formou nas decisões tomadas, nos erros identificados e na disposição de ajustar o rumo sempre que algo deixava de fazer sentido.

---

## Evoluções Planejadas

- Paginação e filtros na listagem de produtos
- Autenticação e autorização
- Auditoria com identificação de usuário
- Separação completa entre domínio e persistência