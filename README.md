# 🚗 LocusPark - Sistema de Gestão de Estacionamentos

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Angular](https://img.shields.io/badge/angular-%23DD0031.svg?style=for-the-badge&logo=angular&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonwebservices&logoColor=FF9900)

---

## 📜 Descrição do Projeto

O **LocusPark** é uma plataforma de gestão voltada para o controle, monitoramento e precificação inteligente de estacionamentos e pátios comerciais. Desenvolvido com foco absoluto em qualidade técnica, o sistema utiliza a metodologia **TDD (Test-Driven Development)** na construção do backend e adota componentização reativa baseada em **Signals** no frontend.

A aplicação utiliza uma arquitetura totalmente desacoplada, dividida em duas branches principais no repositório:
* **`backend`**: Uma API RESTful em Java e Spring Boot governando o isolamento de dados, autenticação JWT Stateless e regras financeiras complexas.
* **`frontend`**: Uma aplicação SPA em Angular e Tailwind CSS contendo o painel administrativo e o configurador dinâmico de pátio e tarifas.

## ✨ Funcionalidades Principais

* *Isolamento Multi-Tenant Estrito*:
    * Arquitetura de banco compartilhado com coluna discriminadora (company_id). Toda requisição intercepta o token JWT e valida o pátio correto no servidor, impedindo vazamento de dados entre concorrentes.

* *Hierarquia de Níveis de Acesso (RBAC Blindado)*:
    * SUPER_ADMIN: Controle máster global sobre as empresas cadastradas no SaaS.
    * ADMIN: Dono ou gerente do pátio, com controle de CRUD de usuários locais, mensalistas e parametrização financeira.
    * EMPLOYEE: Operador de guarita focado em operações de check-in e check-out em tempo real.

* *Configurador Tarifário Avançado (Baseado nas Telas Operacionais)*:
    * *Tarifas Rotativas*: Cobrança customizada da primeira hora e fracionamento sequencial (ex: blocos de 15 minutos).
    * *Regras Extras*: Configuração de tolerâncias por lei, taxas fixas de pernoite e multa por perda de ticket físico.
    * *Gatilhos de Diária*: Conversão automática do valor acumulado por horas em diária cheia para beneficiar o cliente.
    * *Convênios e Selos*: Parcerias com o comércio local aplicando descontos fixos, porcentagens ou abono de horas.
 
---
## ✅ Cobertura de Testes

O projeto possui testes automatizados tanto no backend quanto no frontend:

* **Backend (Java/Spring Boot):** Suíte completa de testes unitários e de integração utilizando **JUnit 5** com **Mockito** para mock de dependências e **MockMvc** para simulação de requisições HTTP. Os testes rodam com banco H2 em memória com rollback automático.
* **Frontend (Angular):** Testes unitários de componentes e serviços escritos com **Vitest**, garantindo a integridade da lógica reativa baseada em Signals.

---

## 📖 Documentação da API (Swagger)

A API possui documentação interativa gerada automaticamente pelo **SpringDoc OpenAPI**. Com o backend rodando, os seguintes endpoints ficam disponíveis:

| Descrição | URL |
|---|---|
| Swagger UI (interface visual) | http://localhost:8080/swagger-ui.html |
| Especificação OpenAPI (JSON) | http://localhost:8080/v3/api-docs |

> O acesso à documentação é público e não exige autenticação JWT.

---

## 🌐 Infraestrutura e Deploy

O ecossistema do **LocusPark** foi projetado para alta escalabilidade, baixo custo operacional e resiliência em produção através de um modelo moderno de computação em nuvem:

* **Backend (API):** Hospedado de forma Serverless no **AWS Lambda**. Para mitigar o problema de inicialização lenta (*cold start*) comum em ecossistemas Java/Spring, o projeto adota o **AWS Lambda SnapStart**. Essa tecnologia pré-inicializa a máquina virtual e armazena um snapshot criptografado da memória, permitindo que a API acorde de forma quase instantânea para processar as requisições.
* **Frontend (Painel SPA):** Hospedado e distribuído globalmente pela **Vercel**, garantindo carregamento rápido da interface, otimização automática de pacotes e CI/CD integrado diretamente com a branch de produção.

---

## 🧠 Conceitos Arquiteturais e Boas Práticas Aplicados

* *Value Objects (VOs)*: Encapsulamento de regras e validações estritas de negócio direto em tipos complexos customizados na camada global types, como Cpf, Cnpj e Plate (Placa com validação automática para os formatos Tradicional e Mercosul).
* *Segurança Programática Cruzada*: Validação no backend que impede que usuários EMPLOYEE alterem perfis, ou que um ADMIN manipule dados ou mude papéis de usuários pertencentes a outros pátios.
* *TDD Avançado*: Suíte de testes unitários e de integração integrando MockMvc e Mockito com banco de dados H2 em memória, garantindo rollback automático a cada execução de teste.

## 📂 Estrutura das Pastas

O projeto está dividido de forma isolada através das pastas:

### Pasta: backend

```text
api/src/main/java/com/locuspark/api/
├── config/                  # Configurações de segurança e cors
├── controller/              # Endpoints HTTP REST (MockMvc tested)
├── dto/                     # Records rígidos de Request e Response
├── entity/                  # Modelos JPA mapeados para o banco de dados
├── enums/                   # Controle de papéis (UserRole) e status
├── exception/               # Handler global de exceções da API
├── infrastructure/          # Conversores JPA customizados para os VOs
├── mapper/                  # Interfaces MapStruct de conversão rápida
├── repository/              # Camada de persistência Spring Data
├── security/                # Filtros de interceptação de tokens JWT
└── types/                   # Value Objects de domínio (Cpf, Cnpj, Plate)
```

### Pasta: frontend

```text
src/app/
├── core/
│   ├── models/              # Interfaces TypeScript de domínio (Vaga, Veículo)
│   └── services/            # Serviços Angular de integração HTTP e reatividade
├── layout/                  # Componentes estruturais globais (Sidebar, Navbar)
├── pages/
│   ├── dashboard/           # Métricas financeiras e de ocupação
│   ├── controle-vagas/      # Monitoramento em tempo real do pátio
│   └── historico/           # Registro geral de entradas e saídas
└── shared/                  # Componentes reutilizáveis (Modais de Entrada e Saída)
```

## 🚀 Como Instalar e Rodar o Projeto

### Pré-requisitos

* *Java 21* ou superior instalado.
* *Node.js 20+* e *Angular CLI* instalados globalmente.
* *Git* configurado.

---

### 🟢 Rodando o Backend (API)

1. *Abra o terminal e mude para a pasta backend:*
    ```bash
    cd backend
    ```
2. *Navegue até a pasta da API:*
     ```bash
    cd api
    ```
3. *Configure as credenciais locais:*
    * Renomeie ou configure o arquivo src/main/resources/application-local.properties com as credenciais do seu banco MySQL/PostgreSQL local.
4. *Execute a suíte de testes automatizados (Garante a integridade do código):*
    ```bash
    ./mvnw clean test
    ```
5. *Suba a aplicação localmente:*
    ```bash
    ./mvnw spring-boot:run
    ```
    * A API estará respondendo em: http://localhost:8080

---

### 🔵 Rodando o Frontend (Painel Angular)

1. *Abra outro terminal e mude para a pasta frontend:*
    ```bash
    cd frontend
    ```
2. *Instale todas as dependências do projeto:*
    ```bash
    npm install
    ```
3. *Execute o servidor de desenvolvimento do Angular:*
   ```bash
    ng serve
    ```
4. *Acesse a aplicação no seu navegador:*
    * Abra a URL: http://localhost:4200

---

## 🖼️ Fluxo Operacional (Exemplo de Uso da API)

*1. Registro de Entrada de Veículo (Rotativo):*
* *Endpoint*: POST /tickets/check-in
* *Payload enviado pelo Frontend:*
```json
{
  "plate": "ABC1D23",
  "model": "Honda Civic",
  "color": "Preto"
}
```

* *Resposta segura da API (201 Created):*

```json
{
  "ticketId": "a8527b7d-b6a8-4c3e-89a7-9f1e1cb027fb",
  "plate": "ABC1D23",
  "enteredAt": "2026-06-18T14:46:35",
  "status": "ACTIVE",
  "companyId": "7359c468-4a16-46ad-b914-2c68d7a120db"
}
```
