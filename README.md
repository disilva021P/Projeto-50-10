# Ent'Artes — Sistema de Gestão de Escola de Dança

> Projeto académico desenvolvido no âmbito da Licenciatura em Engenharia de Sistemas Informáticos — IPCA 2025/2026

![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-yellow)
![Java](https://img.shields.io/badge/Java-21+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![React](https://img.shields.io/badge/React-18-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.x-blue)
![Licença](https://img.shields.io/badge/Licença-Académica-lightgrey)

---

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Equipa](#-equipa)
- [Stack Tecnológica](#️-stack-tecnológica)
- [Funcionalidades](#-funcionalidades-principais)
- [Arquitetura](#-arquitetura)
- [Instalação](#-instalação-e-execução)
- [Metodologia](#-metodologia)
- [Milestones](#️-milestones)
- [Documentação](#-documentação)
- [Contacto](#-contacto)

---

## 🎯 Sobre o Projeto

A **Ent'Artes** é uma escola de dança que dependia de processos manuais para gerir inscrições, horários, pagamentos e comunicação interna, resultando em erros recorrentes, sobreposição de horários e informação dispersa por múltiplos canais informais.

Este projeto consiste no desenvolvimento de um **sistema de gestão integrada** que digitaliza e automatiza os principais processos da escola, contemplando:

- Gestão de alunos, professores e estúdios
- Dois tipos de aulas: **semanais fixas** e **coaching personalizadas**
- Visualização de pagamentos baseada em aulas validadas
- Inventário digital de figurinos com marketplace interno
- Comunicação interna centralizada (substituindo grupos externos)
- Controlo de acessos por perfil com RBAC
- Gestão de eventos que a escola organiza/participa

A solução segue uma arquitetura **cliente-servidor** com API RESTful, desenvolvida em contexto real com a Ent'Artes como entidade parceira no projeto **50+10**.

---

## 👥 Equipa

| Role | Nome | Nº |
|------|------|----|
| 🧭 Product Owner (Líder) | Diogo Silva | 31504 |
| 📋 Scrum Master (Secretário) | Rodrigo Miranda | 31509 |
| 💻 Developer | Alexandre Barbosa | 31539 |
| 💻 Developer | Hugo Carvalho | 31519 |
| 💻 Developer | Rui Barbosa | 31515 |

---

## 🛠️ Stack Tecnológica

### Backend
| Tecnologia | Versão | Utilização |
|-----------|--------|-----------|
| Java | 21+ | Linguagem principal |
| Spring Boot | 3.x | Framework REST API |
| Spring Security + JWT | — | Autenticação e autorização (RBAC) |
| Spring Data JPA + Hibernate | — | ORM e persistência |
| MySQL | 8.x | Base de dados relacional |
| JUnit 5 + Mockito | — | Testes unitários (cobertura mín. 70%) |
| SpringDoc OpenAPI | — | Documentação automática (Swagger UI) |

### Frontend
| Tecnologia | Versão | Utilização |
|-----------|--------|-----------|
| React | 18 | Biblioteca de UI |
| JavaScript / TypeScript | — | Linguagem |
| Tailwind CSS | — | Estilização utility-first |
| React Router | — | Navegação SPA |
| Axios | — | Comunicação com API REST |

### Ferramentas
| Ferramenta | Utilização |
|-----------|-----------|
| Git & GitHub | Controlo de versões |
| Jira | Gestão de backlog e sprints |
| Postman | Teste de endpoints |
| Figma | Prototipagem e mockups |

---

## ✨ Funcionalidades Principais

### 🔐 Autenticação e Controlo de Acessos
- Login com email/password e recuperação por email
- Controlo de acessos por perfil: **Coordenação, Professor, Encarregado, Aluno**
- Criação de contas exclusivamente pela coordenação

### 📅 Gestão de Aulas e Horários
- Horários semanais fixos com geração automática de aulas anuais
- Aulas de **coaching pontuais** e recorrentes com verificação de disponibilidade em tempo real
- Verificação de conflitos: professor, estúdio e aluno
- Suspensão de aulas em datas específicas (feriados, etc.)
- Compatibilidade estúdio-modalidade configurável

### ✅ Validação de Aulas
- Confirmação por professor + encarregado/aluno + coordenação
- Validação automática após 48h sem resposta da coordenação
- Histórico completo de validações com data, hora e utilizador

### 🚫 Faltas e Cancelamentos
- Distinção entre falta do aluno e falta do professor
- Cancelamentos com e sem penalização conforme antecedência configurável
- Submissão de justificações com prazo configurável
- Notificação à coordenação quando limite de faltas é atingido

### 💶 Faturação
- Cálculo automático após validação: `(duração_minutos / 60) × valor_hora`
- Multiplicadores configuráveis (ex: domingo = 1.5×)
- Valor/hora diferenciado para professores externos
- Exportação de dados em **CSV** para integração contabilística
- Relatórios financeiros detalhados aula a aula por perfil

### 👗 Inventário e Marketplace
- Registo de figurinos com fotografia
- Marcação como venda, aluguer ou doação
- Filtros por tamanho, modalidade e estado
- Contacto interno via mensagem privada entre encarregados
- Fluxo de doações validado pela coordenação

### 💬 Comunicação
- Mensagens privadas entre utilizadores autorizados
- Notificações in-app e por email para eventos relevantes
- Histórico de notificações com arquivo automático (30 dias)
- Privacidade garantida: coordenação sem acesso a mensagens privadas de terceiros

### 🎪 Eventos
- Criação de eventos com modalidades, participantes e requisitos de figurinos
- Confirmação de participação por encarregados
- Notificações automáticas a todos os envolvidos

---

## 🏗️ Arquitetura

```
┌─────────────────────────┐         ┌──────────────────────────────────────────────┐
│        BROWSER          │         │              SPRING BOOT API                 │
│                         │         │                                              │
│  ┌─────────────────┐    │  HTTP   │  ┌──────────┐  ┌──────────┐   ┌───────────┐  │
│  │ React + Tailwind│◄───┼─ REST ──┼─►│ Security │─►│Controllers│─►│ Services  │  │
│  │   React Router  │    │  JSON   │  │   JWT    │  │          │   │           │  │
│  │     Axios       │    │         │  └──────────┘  └──────────┘   └─────┬─────┘  │
│  └─────────────────┘    │         │                                     │        │
└─────────────────────────┘         │                              ┌──────▼──────┐ │
                                    │                              │ Repositórios│ │
                                    │                              │  JPA/Hib.   │ │
                                    │                              └──────┬──────┘ │
                                    └─────────────────────────────────────┼────────┘
                                                                          │
                                                                   ┌──────▼──────┐
                                                                   │    MySQL    │
                                                                   └─────────────┘
```

A arquitetura segue o padrão **Controller → Service → Repository**, com separação clara entre camadas e comunicação via JSON sobre HTTP/REST.

---

## 🚀 Instalação e Execução

> ⚠️ **Em construção** — instruções serão atualizadas à medida que o projeto avança.


### Pré-requisitos

```bash
Java 21+
MySQL 8.x
Node.js 18+
```

### Backend

```bash
# Clonar o repositório
git clone https://github.com/<org>/entartes.git
cd entartes/backend

# Configurar variáveis de ambiente (criar ficheiro .env ou application-local.properties)
# DB_URL=
# DB_USER=...
# DB_PASS=...
# JWT_SECRET=...

# Correr
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8080`  
Documentação Swagger: `http://localhost:8080/swagger-ui.html`

### Frontend

```bash
cd entartes/frontend

# Instalar dependências
npm install

# Correr em desenvolvimento
npm run dev
```

A aplicação ficará disponível em `http://localhost:3000`

---

## 📊 Metodologia

- **Scrum** com sprints de **1 semana**
- Daily standups de 15 minutos
- Sprint reviews e retrospetivas semanais
- Branching estruturado: `main` / `nº desenvolverdor` / `fetch/*` / `hotfix/*`
- Gestão de backlog e sprints no **Jira**


---

## 🗓️ Milestones

| Marco | Data | Entregáveis |
|-------|------|-------------|
| **M1 — Análise e Modelação** | 13/03/2026 ✅ | Requisitos, diagramas UML/BPMN, mockups, backlog |
| **M2 — Versão Beta** | 24/04/2026 | Backend completo, API funcional, testes implementados |
| **M3 — Versão Final** | 09/05/2026 | Sistema completo, UI (simplificada) finalizada |

---

## 📝 Documentação

A documentação completa está disponível em `/docs`:

```
/docs
├── relatorio/         # Relatório Completo
├── uml/               # Diagramas de Casos de Uso, Classes, Sequência
├── er/                # Diagrama Entidade-Relacionamento
├── bpmn/              # Processos de negócio (BPMN)
├── mockups/           # Protótipos de interface (Figma)
```

---

## 📧 Contacto

| Nome | Email |
|------|-------|
| Diogo Silva (Líder) | 31504@alunos.ipca.pt |
| Rodrigo Miranda (Secretário) | 31509@alunos.ipca.pt |

---

<div align="center">

**IPCA** — Instituto Politécnico do Cávado e do Ave  
Escola Superior de Tecnologia  
Licenciatura em Engenharia de Sistemas Informáticos  
Projeto 50+10 · 2º Ano · 2º Semestre · 2025/2026

</div>