# 🚀 Deployment Guide - TalentStream

Complete guide for deploying the TalentStream platform to various environments.

---

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Local Development](#local-development)
- [Docker Deployment](#docker-deployment)
- [Cloud Deployment](#cloud-deployment)
- [Production Considerations](#production-considerations)
- [Monitoring & Logging](#monitoring--logging)
- [Troubleshooting](#troubleshooting)

---

## ✅ Prerequisites

### Software Requirements

- **Java 17+** - [Download](https://adoptium.net/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **Docker & Docker Compose** - [Download](https://www.docker.com/products/docker-desktop)
- **Git** - [Download](https://git-scm.com/downloads)

### Hardware Recommendations

**Minimum:**
- CPU: 2 cores
- RAM: 4 GB
- Storage: 10 GB

**Recommended:**
- CPU: 4 cores
- RAM: 8 GB
- Storage: 20 GB

---

## 💻 Local Development

### Step 1: Clone Repository

```bash
git clone https://github.com/mudasirmujtaba/talentstream-spring-microservices.git
cd talentstream-spring-microservices
```

### Step 2: Build All Services

```bash
mvn clean install -DskipTests
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 2 min 30 s
```

### Step 3: Start RabbitMQ

```bash
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management
```

### Step 4: Start Services (In Order)

**Terminal 1 - Eureka Server:**
```bash
cd eureka-server
mvn spring-boot:run
```

**Terminal 2 - API Gateway:**
```bash
cd api-gateway
mvn spring-boot:run
```

**Terminal 3 - Auth Service:**
```bash
cd auth-service
mvn spring-boot:run
```

**Terminal 4 - Job Service:**
```bash
cd job-service
mvn spring-boot:run
```

**Terminal 5 - Notification Service:**
```bash
cd notification-service
mvn spring-boot:run
```

### Step 5: Verify

- **Eureka:** http://localhost:8761
- **API Gateway:** http://localhost:8080/actuator/health
- **RabbitMQ:** http://localhost:15672 (guest/guest)

---

## 🐳 Docker Deployment

### Quick Start (Recommended)

```bash
# Clone repository
git clone https://github.com/mudasirmujtaba/talentstream-spring-microservices.git
cd talentstream-spring-microservices

# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f
```

### Stop Services

```bash
docker-compose down
```

### Rebuild Specific Service

```bash
docker-compose up -d --build job-service
```

### Docker Compose Commands

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs (all services)
docker-compose logs -f

# View logs (specific service)
docker-compose logs -f auth-service

# Restart service
docker-compose restart job-service

# Check status
docker-compose ps

# Remove all containers and volumes
docker-compose down -v
```

---

## ☁️ Cloud Deployment

### Option 1: Render.com

**Prerequisites:**
- Render account
- GitHub repository

**Steps:**

1. **Create Web Services** for each microservice:
    - Go to Render Dashboard
    - Click "New +" → "Web Service"
    - Connect GitHub repository
    - Configure:
        - **Name:** eureka-server
        - **Environment:** Docker
        - **Region:** Select closest
        - **Branch:** main
        - **Docker Context:** ./eureka-server
        - **Docker File:** ./eureka-server/Dockerfile

2. **Set Environment Variables:**
    - For each service, add:
        - `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`: URL of Eureka service
        - `SPRING_RABBITMQ_HOST`: RabbitMQ host

3. **Repeat for all services**

4. **Add RabbitMQ:**
    - Use CloudAMQP add-on
    - Or deploy RabbitMQ separately

---

### Option 2: Railway.app

**Prerequisites:**
- Railway account
- GitHub repository

**Steps:**

1. **Create New Project** on Railway

2. **Deploy from GitHub:**
    - Click "Deploy from GitHub repo"
    - Select your repository

3. **Add Services:**
    - Add RabbitMQ from marketplace
    - Add each microservice

4. **Configure Environment Variables:**
   ```
   EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server.railway.internal:8761/eureka/
   SPRING_RABBITMQ_HOST=rabbitmq.railway.internal
   ```

5. **Set Dockerfiles:**
    - For each service, specify Dockerfile path

---

### Option 3: AWS ECS (Elastic Container Service)

**Prerequisites:**
- AWS account
- AWS CLI installed
- Docker images pushed to ECR

**High-Level Steps:**

1. Create ECR Repositories
2. Push Docker Images to ECR
3. Create ECS Cluster
4. Define Task Definitions
5. Create Services
6. Configure Load Balancer
7. Set up RDS for databases
8. Configure Amazon MQ for RabbitMQ

---

### Option 4: Kubernetes (GKE/EKS/AKS)

**Prerequisites:**
- Kubernetes cluster
- kubectl installed
- Helm (optional)

**Sample Deployment YAML:**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: eureka-server
spec:
  replicas: 1
  selector:
    matchLabels:
      app: eureka-server
  template:
    metadata:
      labels:
        app: eureka-server
    spec:
      containers:
      - name: eureka-server
        image: yourusername/eureka-server:latest
        ports:
        - containerPort: 8761
---
apiVersion: v1
kind: Service
metadata:
  name: eureka-server
spec:
  selector:
    app: eureka-server
  ports:
  - port: 8761
    targetPort: 8761
  type: LoadBalancer
```

---

## 🔐 Production Considerations

### 1. Database Configuration

Replace H2 with PostgreSQL/MySQL:

**application.yml (Production):**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/authdb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate  # Use Flyway/Liquibase for migrations
```

### 2. Security Enhancements

```yaml
jwt:
  secret: ${JWT_SECRET}  # Use strong secret from env variable
  expiration: 3600000  # 1 hour in production

spring:
  security:
    require-ssl: true
```

### 3. Email Configuration

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}  # Use app password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### 4. Environment Variables

Create `.env` file:

```bash
# Database
DB_USERNAME=admin
DB_PASSWORD=secure_password

# JWT
JWT_SECRET=your-256-bit-secret-key-here

# Email
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password

# RabbitMQ
RABBITMQ_HOST=rabbitmq.yourdomain.com
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=admin
RABBITMQ_PASSWORD=secure_password
```

### 5. HTTPS/TLS Configuration

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: tomcat
```

### 6. API Rate Limiting

Add Spring Cloud Gateway filters:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://AUTH-SERVICE
          predicates:
            - Path=/api/auth/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter:
                  replenishRate: 10
                  burstCapacity: 20
```

---

## 📊 Monitoring & Logging

### 1. Spring Boot Actuator

Enable endpoints:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

**Access metrics:**
- http://localhost:8080/actuator/metrics
- http://localhost:8080/actuator/health

### 2. Centralized Logging

Add Logback configuration:

```xml
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>
  
  <appender name="FILE" class="ch.qos.logback.core.FileAppender">
    <file>logs/application.log</file>
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>
  
  <root level="INFO">
    <appender-ref ref="STDOUT" />
    <appender-ref ref="FILE" />
  </root>
</configuration>
```

### 3. Distributed Tracing (Optional)

Add Zipkin:

```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
  <groupId>io.zipkin.reporter2</groupId>
  <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

---

## 🔧 Troubleshooting

### Issue: Services not registering with Eureka

**Solution:**

```bash
# Check Eureka is running
curl http://localhost:8761/actuator/health

# Check service can reach Eureka
docker-compose exec auth-service ping eureka-server

# Verify environment variable
docker-compose exec auth-service env | grep EUREKA
```

### Issue: RabbitMQ connection refused

**Solution:**

```bash
# Check RabbitMQ is healthy
docker-compose ps rabbitmq

# Check RabbitMQ logs
docker-compose logs rabbitmq

# Restart dependent services
docker-compose restart job-service notification-service
```

### Issue: Port already in use

**Solution:**

```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

### Issue: Out of memory errors

**Solution:**

Update docker-compose.yml:

```yaml
services:
  job-service:
    environment:
      - JAVA_OPTS=-Xmx512m -Xms256m
    mem_limit: 1g
```

### Issue: Slow startup times

**Solution:**

Increase health check intervals:

```yaml
healthcheck:
  interval: 30s  # Increase from 15s
  start_period: 60s  # Increase from 30s
```

---

## 📝 Deployment Checklist

### Pre-Deployment
- [ ] All tests passing
- [ ] Environment variables configured
- [ ] Database migrations ready
- [ ] Security secrets in place
- [ ] HTTPS/TLS certificates ready
- [ ] Backup strategy defined

### During Deployment
- [ ] Deploy in order: Eureka → Gateway → Services
- [ ] Monitor logs for errors
- [ ] Verify service registration
- [ ] Test health endpoints
- [ ] Smoke test critical APIs

### Post-Deployment
- [ ] All services healthy
- [ ] APIs responding correctly
- [ ] Notifications working
- [ ] Monitoring dashboards setup
- [ ] Alerts configured
- [ ] Documentation updated

---

## 🔗 Related Resources

- [Main README](../README.md)
- [API Documentation](./API_DOCUMENTATION.md)
- [Architecture Guide](./ARCHITECTURE.md)

---

<div align="center">

**Ready for Production Deployment** 🚀

</div>