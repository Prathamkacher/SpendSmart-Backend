# SpendSmart - Backend Microservices Architecture

SpendSmart is a highly robust, scalable, and resilient personal finance management backend system. It is built strictly on microservice architecture principles, providing automated and insightful financial tracking with a strong focus on high availability, distributed data management, and clean code.

---

## 🏗️ System Architecture & Core Technologies

The backend is composed of **11 independent Spring Boot microservices** and **1 shared kernel library**. They communicate synchronously via **HTTP REST (OpenFeign)** and asynchronously via **Message Brokering (RabbitMQ)**.

- **Framework**: Spring Boot 3.x, Spring Cloud (Eureka, Gateway, OpenFeign)
- **Language**: Java 17
- **Database**: MySQL (Each service maintains its own isolated database schema to ensure loose coupling and independent scalability)
- **Database Migrations**: Flyway (Automated schema creation and versioning on startup)
- **Message Broker**: RabbitMQ (Used for decoupled event processing, such as notifications and budget alerts)
- **Security**: Spring Security + Stateless JWT Authentication, OAuth2 (Google/GitHub)
- **Resilience**: Resilience4j (Circuit Breakers, Timeouts, and Fallback handlers implemented at the Gateway level)
- **Payment Gateway**: Razorpay Integration for premium subscriptions

---

## 🧩 Microservices Landscape (In-Depth)

### 1. API Gateway (`api-gateway`) | Port: 8080
The API Gateway acts as the single entry point and traffic cop for all frontend requests, abstracting the complexity of the internal microservices.
- **Routing & Discovery**: Dynamically routes requests using `lb://` (Load Balancer) prefixes. It automatically discovers service instances registered in Eureka, meaning hardcoded IPs are never used.
- **Global Security & CORS**: Manages Cross-Origin Resource Sharing (CORS) rules centrally. It validates JWT tokens before allowing traffic to proceed to internal microservices.
- **Resilience4j Circuit Breakers**: If a downstream service (like `auth-service` or `expense-service`) takes longer than 5 seconds or throws consecutive 500 errors, the circuit trips. The Gateway reroutes traffic to custom `FallbackController` endpoints to prevent cascading network failures and returns a graceful error to the frontend.

### 2. Service Registry (`discovery-server`) | Port: 8761
Powered by Netflix Eureka, this service acts as the central phonebook of the architecture.
- **Dynamic Registration**: Services register themselves (e.g., `EXPENSE-SERVICE`) on startup.
- **Health Monitoring**: Eureka constantly pings registered services to ensure they are healthy.
- **Load Balancing**: Works in tandem with Spring Cloud LoadBalancer in the Gateway to distribute incoming traffic across multiple instances of the same service.

### 3. Authentication Service (`auth-service`) | Port: 8081
Handles all Identity and Access Management (IAM) responsibilities.
- **Core Entities**: `User` (email, password hash, role, authentication provider, plan type).
- **JWT & OAuth2**: Manages standard Email/Password registration as well as OAuth2 flows (Google, GitHub). Generates and cryptographically signs stateless JWT tokens.
- **User Roles & Admin Panel**: Differentiates between standard `USER` and `ADMIN`. Admins have the ability to suspend, activate, promote, or delete other users.
- **Asynchronous Processing**: Uses Spring's `@Async` threads to handle heavy background tasks such as sending Welcome Emails, Password Reset OTPs, and Account Suspension notifications without blocking the HTTP request thread.

### 4. Expense Service (`expense-service`) | Port: 8082
The core engine for tracking outbound cash flows.
- **Core Entities**: `Expense` (amount, date, description, categoryId).
- **Transaction Management**: Validates and stores user expenses. Supports filtering by date ranges, categories, and custom search queries.
- **Inter-Service Communication**: Upon saving an expense, it makes a synchronous OpenFeign call to the `budget-service` to immediately update the `spentAmount` against the user's active budget limit for that specific category.

### 5. Income Service (`income-service`) | Port: 8083
The counterpart to the Expense service, responsible for tracking incoming cash flows.
- **Core Entities**: `Income` (amount, date, source, description).
- **Responsibilities**: Tracking cash inflows (salary, dividends, gifts). It provides specialized endpoints to aggregate total income per month, which the `analytics-service` relies heavily upon to calculate savings rates.

### 6. Category Service (`category-service`) | Port: 8084
Manages the taxonomy of financial data across the platform.
- **Core Entities**: `Category` (name, icon, color, type: SYSTEM vs CUSTOM).
- **System vs. Custom**: Pre-seeds default system categories (Food, Transport, Entertainment) for all users upon registration. It allows users to create their own custom categories with personalized color hex codes and icons to tag their specific expenses.

### 7. Budget Service (`budget-service`) | Port: 8085
Provides expenditure limits and financial discipline controls.
- **Core Entities**: `Budget` (limitAmount, spentAmount, categoryId, alertThreshold, isActive).
- **Tracking & Validation**: Tracks limits vs. spent amounts per category. 
- **Event-Driven Alerts (RabbitMQ)**: When the `expense-service` updates the spent amount, this service checks if the user has reached their warning threshold (e.g., 80% used) or exceeded 100%. If so, it publishes a `NotificationEvent` to **RabbitMQ**, completely decoupling the notification logic from the transaction logic to ensure fast API responses.

### 8. Recurring Transaction Service (`recurring-transaction-service`) | Port: 8086
The automation engine of SpendSmart.
- **Core Entities**: `RecurringTransaction` (amount, type, frequency, nextDueDate, status).
- **Cron Jobs**: Utilizes Spring's `@Scheduled` annotation to run daily sweeps at midnight.
- **Automation Logic**: Identifies active recurring configurations (Daily, Weekly, Monthly, Yearly). If a transaction is due today, it uses OpenFeign to automatically POST the transaction to the `income-service` or `expense-service`, simulating a user action, and then automatically calculates and advances the `nextDueDate` parameter.

### 9. Analytics Service (`analytics-service`) | Port: 8087
The "brain" of the platform, aggregating data across the ecosystem to provide actionable insights.
- **Financial Health Score Algorithm**: Fetches data from Income, Expense, and Budget services to calculate a 0-100 score. It weights the user's savings rate (40%), expense-to-income ratio (20%), and active budget adherence (40%) to output a grade (e.g., EXCELLENT, POOR) with actionable text insights.
- **Momentum Forecasting**: Calculates spending momentum based on a 3-month historical trend to predict future expenses by the end of the month.
- **PDF Generation**: Dynamically generates visual PDF invoices and monthly reports using `iText/Lowagie` libraries.

### 10. Payment Service (`payment-service`) | Port: 8088
Handles monetization for "SpendSmart Pro".
- **Razorpay Integration**: Creates secure order IDs and passes them to the frontend.
- **Webhook Fulfillment**: Listens for Razorpay webhooks. It verifies cryptographic webhook signatures using HMAC SHA-256 to prevent spoofing. Once a payment is verified, it communicates with the `auth-service` to permanently upgrade the user's `PlanType` from BASIC to PRO, unlocking premium features across all services.

### 11. Notification Service (`notification-service`) | Port: 8089
A pure, decoupled messaging consumer.
- **RabbitMQ Listener**: Constantly listens to the `notification.exchange`. When it receives events (e.g., Budget Exceeded, System Announcements from the Admin, Monthly Summaries), it formats the data and triggers the actual outgoing email via JavaMailSender or system push. This offloads heavy SMTP networking work from the core services.

### 12. Shared Common Library (`spendsmart-common`)
A unified, standalone Maven library imported by all microservices to strictly enforce DRY (Don't Repeat Yourself) principles.
- **Standardized DTOs**: Provides the `ApiResponse<T>` wrapper class to ensure every single microservice responds with the exact same JSON schema (`success`, `message`, `data`).
- **Global Exception Handling**: The `BaseGlobalExceptionHandler` automatically maps standard Java exceptions (`ResourceNotFoundException`, `IllegalArgumentException`) to the correct HTTP status codes globally, ensuring consistent error responses.
- **Security Logic**: Contains the `AbstractJwtAuthenticationFilter`. Because every internal service needs to validate JWTs locally, this shared class ensures that token parsing, cryptographic validation, and `X-User-Id` header extraction logic is written exactly once.

---

## 🔄 Architectural Workflows in Detail

### 1. The Synchronous Request Flow (Frontend to Database)
1. The Angular frontend sends a request (e.g., `POST /api/expenses`) with a `Authorization: Bearer <JWT>` header.
2. The `api-gateway` receives it, evaluates its routing predicates, and forwards the exact request to the `expense-service` instance via Eureka.
3. The `expense-service` interceptor (inherited from `spendsmart-common`) intercepts the request, parses the JWT, mathematically validates the cryptographic signature, extracts the `userId`, and places it securely in the Spring `HttpServletRequest` attributes.
4. The Spring Controller processes the payload, performs business logic, and persists the data to the isolated `spendsmart_expense` MySQL schema.

### 2. The Asynchronous Event-Driven Flow (RabbitMQ Budget Alerts)
1. The user logs a massive expense. `expense-service` makes a synchronous Feign call to `budget-service` to update the spent amount.
2. `budget-service` calculates that the user has exceeded 100% of their limit.
3. Instead of pausing to send an email (which takes ~2 seconds and blocks the user UI), `budget-service` instantly fires a serialized `NotificationEvent` payload into RabbitMQ and returns a lightning-fast HTTP 200 success response to the user.
4. Milliseconds later, `notification-service` consumes the message from the RabbitMQ queue, renders an HTML email template, and sends the warning alert to the user entirely in the background.

---

## 🚀 Getting Started & Local Development

### Prerequisites
- JDK 17+
- Maven 3.8+
- MySQL Server (Running on default port 3306)
- RabbitMQ Server (Running on default port 5672)

### Database Provisioning & Flyway
You **do not** need to manually create tables. Ensure MySQL is running locally with the credentials defined in your environment variables (or `application.yml` files). 
On startup, Spring Boot and **Flyway** will:
1. Auto-create the databases (`spendsmart_auth`, `spendsmart_expense`, etc.) using `createDatabaseIfNotExist=true`.
2. Execute the `.sql` migration scripts to construct all required schemas, tables, and relationships.

### Startup Sequence
Because this is a distributed system, services must be started in a specific order:
1. **Compile the Common Library**: You must run `mvn clean install` inside the `spendsmart-common` directory first. This makes the shared `.jar` available in your local Maven cache for the other microservices to pull from.
2. **Start Service Registry**: Boot up `discovery-server` and wait for it to successfully start on port 8761.
3. **Start API Gateway**: Boot up `api-gateway` on port 8080.
4. **Start Core Services**: You can now boot up the remaining services (`auth-service`, `expense-service`, `analytics-service`, etc.) in any order. They will automatically register themselves with Eureka and begin communicating.

---

## 🧪 Testing & Code Quality Assurance

We strictly enforce high code quality and test coverage across the entire distributed ecosystem.

- **Unit Testing**: Every service contains a comprehensive suite of JUnit 5 tests utilizing Mockito for service layer isolation.
- **Execution**: Run `mvn clean test` in the root directory to execute the entire test suite.
- **Coverage & SonarQube**: We use JaCoCo to generate detailed HTML coverage reports. You can run `mvn verify` to generate these reports, and then utilize the provided `run-full-analysis.bat` script to pipe the coverage data directly into a local SonarQube instance for static code analysis, vulnerability scanning, and code smell detection. Target coverage is >80%.
