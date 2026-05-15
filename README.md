# SpendSmart - Advanced Backend Microservices Ecosystem

SpendSmart is a state-of-the-art, highly robust, and resilient personal finance management system. Designed from the ground up using **Microservice Architecture**, it offers a scalable and automated way to manage finances, predict spending trends, and coach users toward better financial health.

**Live Production URL**: [http://13.48.183.110.sslip.io](http://13.48.183.110.sslip.io)

---

## 🏗️ System Architecture & Distributed Infrastructure

SpendSmart is built on a distributed network of **11 independent Spring Boot services** that work in perfect harmony. We prioritize **High Availability**, **Data Isolation**, and **Asynchronous Fault Tolerance**.

### Core Infrastructure Stack:
- **Framework**: Spring Boot 3.x with Spring Cloud for enterprise-grade orchestration.
- **Language**: Java 17 (utilizing modern features like Records, Sealed Classes, and enhanced switch patterns).
- **Service Mesh**: Netflix Eureka for dynamic service discovery, ensuring the system can scale horizontally with ease.
- **API Management**: Spring Cloud Gateway for centralized security, rate limiting, and routing.
- **Data Persistence**: MySQL with isolated schemas for every service to ensure that a failure in one database never impacts the others.
- **Asynchronous Backbone**: **RabbitMQ** for event-driven workflows, ensuring that heavy tasks don't slow down the user experience.
- **Security**: Stateless JWT with RSA encryption and multi-provider OAuth2 integration.

---

## 🧩 The Microservices Landscape (Detailed Breakdown)

### 1. API Gateway (`api-gateway`) | Port: 8080
The **API Gateway** is the "front door" and traffic controller of the entire platform. Every request from the mobile or web app passes through here first.
- **Intelligent Routing**: Using Eureka service discovery, it automatically finds the healthiest instance of any service (e.g., `lb://auth-service`) without needing hardcoded IP addresses.
- **Unified Security**: It serves as a security firewall. It intercepts every request, validates the JWT token, and extracts the user's ID. It then injects this context into headers so downstream services don't have to re-authenticate the user.
- **Circuit Breaker & Fallback**: Using **Resilience4j**, it monitors the health of all services. If a service becomes slow or fails, the Gateway "trips the circuit" and returns a graceful fallback response, preventing a "domino effect" failure.

### 2. Service Registry (`discovery-server`) | Port: 8761
Powered by **Netflix Eureka**, this is the "phonebook" of the SpendSmart ecosystem.
- **Dynamic Registration**: As soon as any service starts up, it announces its presence and capabilities here.
- **Instance Management**: If you start three instances of the `expense-service`, Eureka tracks all of them, allowing the Gateway to load-balance traffic between them for maximum speed.

### 3. Authentication & Identity Service (`auth-service`) | Port: 8081
The heart of security and user management.
- **Secure Onboarding**: Manages standard Email/Password registration as well as seamless OAuth2 logins via Google and GitHub.
- **Token Management**: Issues cryptographically signed, stateless JWT tokens that expire after a set time for maximum security.
- **Admin Command Center**: Provides powerful tools for administrators to view platform-wide user trends, promote users to Admin status, or suspend/delete accounts for security reasons.
- **Background Tasks**: Uses `@Async` threads to send Welcome emails, OTPs, and security alerts without making the user wait on the "Sign Up" screen.

### 4. Expense Management Service (`expense-service`) | Port: 8082
The engine responsible for tracking where your money goes.
- **Transaction Engine**: Handles the creation, updates, and deletion of every expense. It supports complex filtering by date, category, and amount.
- **Real-Time Integration**: When an expense is saved, it immediately communicates with the `budget-service` via **OpenFeign** to update your monthly budget progress in real-time.

### 5. Income Tracking Service (`income-service`) | Port: 8083
The counterpart to the Expense service, focusing on cash inflows.
- **Revenue Tracking**: Logs salaries, gifts, dividends, and other income sources.
- **Data Feed**: It provides the historical income data required by the `analytics-service` to calculate your monthly savings rate and financial momentum.

### 6. Taxonomy & Category Service (`category-service`) | Port: 8084
Manages how your data is organized and tagged.
- **Smart Seeding**: Automatically creates a default set of common categories (Food, Rent, Salary) for every new user so they can start tracking instantly.
- **Personalization**: Users can create their own custom categories, choosing from a rich palette of colors and icons to make their dashboard truly theirs.

### 7. Smart Budget Service (`budget-service`) | Port: 8085
The "guardrail" of your financial life.
- **Limit Enforcement**: Allows users to set hard limits for specific categories. It tracks your "Spent vs. Remaining" balance with precision.
- **Alert System**: Once you cross a threshold (like 80% or 100% of your budget), it doesn't just save a log—it fires a **RabbitMQ Event**. This triggers an instant notification to your app without slowing down the expense-logging process.

### 8. Recurring Automation Service (`recurring-transaction-service`) | Port: 8086
The "set it and forget it" engine of the platform.
- **Intelligent Scheduling**: Supports Daily, Weekly, Monthly, and Yearly frequencies for rent, bills, or salary.
- **Midnight Sweeps**: Every night at 12:00 AM, a cron job scans the database for transactions due today and automatically logs them into the Income or Expense services.
- **Duplication Protection**: Features a special `skipFirstGeneration` flag that ensures you don't get double-charged when you manually log a transaction while setting up its recurring schedule.

### 9. Advanced Analytics & AI Service (`analytics-service`) | Port: 8087
The "brain" that turns raw numbers into wisdom.
- **Financial Health Score**: Uses a weighted algorithm to grade your financial habits from 0 to 100. It looks at your savings rate (40%), your expense-to-income ratio (20%), and how well you stick to your budgets (40%).
- **Momentum Forecasting**: Analyzes your last 3 months of data to predict exactly how much you will have spent by the end of the current month, helping you adjust your habits before it's too late.
- **Professional Reporting**: Dynamically generates visual PDF invoices and monthly financial summaries for download.

### 10. Premium Payment Service (`payment-service`) | Port: 8088
Handles the monetization and "SpendSmart Pro" upgrades.
- **Razorpay Integration**: A secure, encrypted bridge for handling subscription payments.
- **Webhook Security**: Listens for Razorpay payment signals and verifies them using **HMAC SHA-256** signatures to prevent hacking or spoofing.
- **Global Upgrades**: Once a payment is confirmed, it triggers a cross-service event that unlocks Pro features (like advanced analytics and PDF reports) for the user instantly.

### 11. Resilient Notification Service (`notification-service`) | Port: 8089
The dedicated communication hub for the ecosystem.
- **Asynchronous Alerts**: Constantly listens to the **RabbitMQ Notification Exchange**. Whether it's a budget warning or a payment success, this service picks it up and processes it.
- **Decoupled Delivery**: Uses a dedicated, asynchronous `EmailService`. This ensures that even if the email server is slow or down, your notification is **still saved** to your in-app dashboard instantly and the email attempt is handled separately in the background. No more missing alerts!

### 12. Shared Common Library (`spendsmart-common`)
The "DNA" shared by all microservices to ensure consistency.
- **Standardized Responses**: Every single service responds with the exact same JSON format, making the frontend code clean and predictable.
- **Global Error Handling**: Automatically converts complex Java errors into easy-to-read error messages for the user.
- **Security Logic**: Contains the shared logic for JWT parsing and cryptographic validation, ensuring that security is applied identically across all 11 services.

---

## 🔄 Automated CI/CD & Deployment Workflow

We use a professional-grade DevOps pipeline to ensure that every code change is safe and reaches you instantly.

### 1. Continuous Integration (GitHub Actions)
- **Quality Gates**: Every push triggers a full build. The system runs **59+ unit tests** to ensure no new code breaks existing features.
- **Static Analysis**: We use **SonarCloud** to scan for "code smells," security vulnerabilities, and bugs. We maintain a high standard for clean, maintainable code.

### 2. Containerization (Docker Hub)
- **Microservice Images**: We build 11 optimized Docker images, one for each service. This ensures the environment in development is **identical** to the environment in production.
- **Cloud Storage**: These images are automatically pushed to a private Docker Hub repository, ready for deployment.

### 3. Automated Cloud Deployment (AWS EC2)
- **Secure SSH Hands-off Deployment**: Our GitHub workflow securely connects to our **AWS EC2** instance via SSH.
- **Persistent Orchestration**: It updates our `docker-compose.prod.yml` and re-launches all services.
- **Data Persistence**: We use **Persistent Docker Volumes** (`mysql_data`). This means your user data, transaction history, and notification logs are safely stored on the physical disk and survive even if the system is updated or restarted.

---

## 🚀 Getting Started for Developers

1. **Common Foundation**: Navigate to `spendsmart-common` and run `mvn clean install`.
2. **Infrastructure**: Ensure you have MySQL and RabbitMQ running.
3. **The Core**: Launch `discovery-server` and `api-gateway`.
4. **The Fleet**: Boot up any of the 11 microservices. They will automatically find each other and begin working!

---

**SpendSmart: Smart Finance, Built with Smarter Code.** 🟢🚀🏆
