### Authentication Service with Eureka and Docker

This document provides setup instructions and configuration for the Authentication Service, which includes JWT-based authentication, email service integration, Eureka service registration, and PostgreSQL in Docker. 


---

### **1. Overview** 🌍
This Authentication Service supports user registration, login, email activation, password reset, and JWT token validation. It uses **Eureka** for service registration and **Docker** for containerization. Additionally, the email service is integrated to send activation emails to users.

---

### **2. Service Architecture** 🏗️

- **Authentication Service**: Responsible for managing user accounts and generating JWT tokens.
- **Eureka Server**: A service registry used for discovering services.
- **Email Service**: Sends activation emails to users after they register.
- **PostgreSQL**: Database used to store user data.
- **Docker**: Used for containerizing the entire stack (PostgreSQL and the application).

---

### **3. Prerequisites** 📋

- **Eureka Server** and **Email Service** need to be set up and running before starting the Authentication Service.

#### **3.1 Setting Up Eureka Server** 🛠️

1. Clone the [Eureka Server repository](https://github.com/Kaweesha-mr/service-registry) or use an existing Eureka server.
2. Run the Eureka Server. The default port for Eureka is `8761`. Make sure it’s up and running.

#### **3.2 Setting Up Email Service** 📧

1. Clone the [Email Service repository](https://github.com/Kaweesha-mr/mail-service-template).
2. Build and run the Email Service locally or deploy it on a cloud service.
3. Ensure that the Email Service is properly integrated with your application, and update the `.env` file with the email service name (`EMAIL_SERVICE_NAME=NODEJS-MAIL-SERVICE`).

#### **3.3 Service Registration in Eureka** 🔗

1. In the **Authentication Service**, add the `@EnableEurekaClient` annotation to the main application class to enable service registration with Eureka.
2. The Authentication Service will automatically register itself in Eureka when it starts.

---

### **4. Setup & Installation** ⚙️

Follow these steps to set up and install the Authentication Service.

#### **4.1 Clone the Repository** 💻

Clone the Authentication Service repository and navigate to the project directory.

```bash
git clone https://github.com/Kaweesha-mr/authentication-service.git
cd authentication-service
```

#### **4.2 Set Up the Environment Variables** 🌱

Create a `.env` file in the root directory with the following content:

```env
APPLICATION_PORT=8080
DATABASE_PASSWORD=your_database_password
DATABASE_URL=jdbc:postgresql://localhost:5432/auth
DATABASE_USERNAME=your_database_username
EMAIL_SERVICE_NAME=NODEJS-MAIL-SERVICE
JWT_SECRET=your_jwt_secret_key
```

#### **4.3 Running the Application** 🚀

Run the Spring Boot application using:

```bash
mvn spring-boot:run
```

#### **4.4 Running the Application with Docker** 🐳

If you want to run the entire stack using Docker, including PostgreSQL and the Authentication Service, follow the instructions below.

---

### **5. Docker Setup** 🏗️

The Authentication Service and PostgreSQL can be run in Docker containers using `docker-compose`. The following `docker-compose.yml` file sets up the containers for PostgreSQL and pgAdmin.

#### **5.1 Docker Compose Configuration** ⚙️

Create or update your `docker-compose.yml` file with the following content:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:latest
    container_name: postgres_container
    environment:
      POSTGRES_USER: test
      POSTGRES_PASSWORD: 1234
      POSTGRES_DB: auth
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - mynetwork

  pgadmin:
    image: dpage/pgadmin4
    container_name: pgadmin_container
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@admin.com
      PGADMIN_DEFAULT_PASSWORD: admin
    ports:
      - "5050:80"
    depends_on:
      - postgres
    networks:
      - mynetwork

  auth-service:
    image: your-auth-service-image
    container_name: auth_service
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/auth
      - DATABASE_USERNAME=test
      - DATABASE_PASSWORD=1234
      - JWT_SECRET=your_jwt_secret_key
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    networks:
      - mynetwork

volumes:
  postgres_data:

networks:
  mynetwork:
    driver: bridge
```

#### **5.2 Running Docker Compose** 🚢

1. To start the entire application stack, use the following command:

   ```bash
   docker-compose up --build
   ```

2. This will bring up the PostgreSQL container, pgAdmin (for managing the database), and the Authentication Service. The PostgreSQL container will be available on `localhost:5432`, and pgAdmin will be accessible on `localhost:5050`.

---

### **6. API Endpoints** 📡

#### **6.1 POST `/register`** ✍️
- Registers a new user and sends an activation email.
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
- **Response**: 
  - **201 Created**: User registered and activation email sent.
  - **409 Conflict**: Email already exists.

#### **6.2 POST `/login`** 🔑
- Authenticates a user and generates a JWT token.
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
- **Response**:
  - **200 OK**: JWT token is generated and returned.

#### **6.3 POST `/validate`** ✅
- Validates the JWT token.
- **Request Body**:
  ```json
  {
    "token": "your_jwt_token_here"
  }
  ```
- **Response**:
  - **200 OK**: Token is valid.
  - **401 Unauthorized**: Token is invalid or expired.

#### **6.4 GET `/activate`** 🔓
- Activates the user's account.
- **Query Parameter**:
  - `token`: The activation token received by email.
- **Response**:
  - **200 OK**: User activated successfully.
  - **404 Not Found**: Activation failed.

#### **6.5 POST `/reset-password`** 🔄
- Resets the user's password.
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "oldPassword123",
    "newPassword": "newSecurePassword123"
  }
  ```
- **Response**:
  - **200 OK**: Password reset successful.
  - **400 Bad Request**: Incorrect old password or email.

---

### **7. Future Enhancements** 🔮

- **Role-Based Authentication**: Role-based access control will be implemented as a future feature. This will allow roles such as "admin", "user", etc., to manage user permissions.

---

### **8. Template Repository and Updates** 🛠️

This repository serves as a **template** for building authentication services with Eureka, Docker, and PostgreSQL. You can use this repo as a starting point for your own projects. 

To **update API endpoints** or **add new features**, simply modify the existing endpoints or create new ones within the provided structure. Ensure to follow the established pattern for adding authentication and token validation features.

- Clone the [Template Repository](https://github.com/Kaweesha-mr/authentication-service-template).
- Update the `.env` file for your specific environment configurations.
- Customize or add any new API endpoints, such as additional user features or role-based authentication.

---

### **9. Blog Series Overview** 📝  

This microservice is being developed and documented as part of a blog series. Each blog post covers a specific aspect of building the Authentication Service, from the basics to advanced features.  

#### **Blog Series Roadmap**  

1. **[Part 1: Introduction to Spring Boot and Authentication](https://dev.to/kaweeshamr/building-secure-authentication-microservices-with-spring-boot-part-1-getting-started-37n6)**  
   - Covers the basics of Spring Boot, why it's widely used for authentication, and an introduction to JWT-based authentication.  
   - Discusses foundational concepts and sets the stage for building the Authentication Service.  

2. **Part 2: Setting Up the Authentication Microservice (Coming Soon)**  
   - Details the project structure, environment setup, and core functionalities like user registration, login, and email activation.  
   - Includes code snippets and practical examples to implement the basics.  

3. **Part 3: Integrating Eureka for Service Discovery (Coming Soon)**  
   - Explains how to integrate Eureka Server for service registration and discovery.  
   - Shows how the Authentication Service interacts with other microservices via Eureka.  

4. **Part 4: Using Docker to Containerize the Application (Coming Soon)**  
   - Step-by-step guide to containerize the Authentication Service and its dependencies (PostgreSQL, Email Service) using Docker and Docker Compose.  

5. **Part 5: Advanced Features – Role-Based Authentication (Coming Soon)**  
   - Introduces role-based access control (RBAC) for managing user permissions.  
   - Shows how to extend the service to handle admin and user roles effectively.  

6. **Part 6: Deployment and CI/CD Pipeline Setup (Coming Soon)**  
   - Guides you through deploying the Authentication Service using cloud platforms.  
   - Demonstrates setting up CI/CD pipelines for continuous integration and deployment.  

#### **Stay Updated**  
Follow the series to build your Authentication Service step by step, gain hands-on experience with microservices, and learn industry-relevant practices!  

---
