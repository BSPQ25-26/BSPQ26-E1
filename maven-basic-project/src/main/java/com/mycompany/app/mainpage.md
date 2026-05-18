@mainpage ExpenSense Documentation

## 1. Overview
ExpenSense is a financial management system designed to track personal and group expenses. 
Built as a RESTful web service using Spring Boot, it provides a secure and scalable backend 
for managing financial data, resolving shared debts, and offering AI-driven financial insights.

## 2. Core Modules
The application is divided into several functional domains:
* **User Management:** Registration, authentication, and profile management.
* **Transactions:** Tracking incomes, expenses, and automated debt settlements.
* **Budgets & Categories:** Categorizing spending and enforcing monthly budget limits.
* **Group Finances:** Managing shared expenses and calculating optimal debt resolutions between members.
* **AI Advisor:** Natural language processing module that analyzes user spending patterns to provide tailored saving tips.

## 3. Architectural Pattern
The system implements the following multi-layer architecture to separate the network interface from business rules and data access:

1. **Facade Layer (Controllers):** Functions as the Remote Method Invocation (RMI) interface. It is responsible for routing HTTP requests, validating incoming JSON payloads against Data Transfer Objects (DTOs), and enforcing authorization.
2. **Service Layer:** Contains the core business logic. It performs calculations, enforces business rules (such as budget constraints), and coordinates data processing before saving state changes.
3. **Persistence Layer (Repositories):** Manages all database interactions using Spring Data JPA, mapping Java Entities to relational database tables.
4. **DTO Layer:** Ensures that internal database entities are never exposed directly to external clients. DTOs control exactly what data enters and leaves the system.

## 4. Security Implementation
The application uses a stateless authentication model based on **JSON Web Tokens (JWT)**.
* The `AuthController` handles the initial login and issues a signed token.
* All subsequent requests to the Facade layer require this token in the request body or header.
* The `AuthService` validates the token's integrity and expiration before allowing the operation to proceed.

## 5. Technology Stack
* **Language:** Java 21
* **Framework:** Spring Boot 3.2.3 (Web, Data JPA, Security)
* **Database:** PostgreSQL (Production environment) / H2 (In-memory testing)
* **AI Integration:** Spring AI with OpenAI client
* **Quality Assurance:** JUnit 5, Mockito, JaCoCo, and Checkstyle

## 6. Navigating the Documentation
This Doxygen documentation represents the technical blueprint of the application:
* **Packages:** Navigate here to view the high-level grouping of the N-Tier architecture (facade, service, model, repository).
* **Classes:** Contains the detailed specifications for every component. The classes within the `facade` package act as the primary API reference, detailing endpoints, expected inputs, and HTTP response codes.
* **Files:** Provides a view of the physical source code directory structure.