# 📚 API Documentation - TalentStream

Complete REST API reference for the TalentStream platform.

---

## 📋 Table of Contents

- [Base URLs](#base-urls)
- [Authentication](#authentication)
- [Auth Service APIs](#auth-service-apis)
- [Job Service APIs](#job-service-apis)
- [User Preference APIs](#user-preference-apis)
- [Error Responses](#error-responses)
- [Status Codes](#status-codes)

---

## 🌐 Base URLs

| Environment | Base URL |
|-------------|----------|
| **Development** | `http://localhost:8080` |
| **Docker** | `http://localhost:8080` |
| **Production** | `https://your-domain.com` |

**All requests go through API Gateway on port 8080**

---

## 🔐 Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### How to Get a Token

1. Register or Login
2. Copy the `token` from the response
3. Use it in subsequent requests

---

## 🔑 Auth Service APIs

### 1. Register User

**Endpoint:** `POST /api/auth/register`

**Description:** Create a new user account

**Authentication:** None (Public)

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "role": "USER"
}
```

**Field Validations:**

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| username | String | Yes | 3-50 characters, unique |
| email | String | Yes | Valid email format, unique |
| password | String | Yes | Min 6 characters |
| fullName | String | Yes | Not blank |
| role | Enum | Yes | USER, EMPLOYER, ADMIN |

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTYzODM2NzI2NywiZXhwIjoxNjM4NDUzNjY3fQ...",
  "type": "Bearer",
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "role": "USER",
  "message": "User registered successfully"
}
```

**Error Responses:**
```json
// 409 Conflict - Username exists
{
  "timestamp": "2025-10-26T10:30:00",
  "status": 409,
  "error": "User Already Exists",
  "message": "Username already exists: john_doe",
  "path": "/api/auth/register"
}

// 400 Bad Request - Validation error
{
  "username": "Username must be between 3 and 50 characters",
  "email": "Email should be valid",
  "password": "Password must be at least 6 characters"
}
```

---

### 2. Login

**Endpoint:** `POST /api/auth/login`

**Description:** Authenticate user and get JWT token

**Authentication:** None (Public)

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "role": "USER",
  "message": "Login successful"
}
```

**Error Responses:**
```json
// 401 Unauthorized
{
  "timestamp": "2025-10-26T10:30:00",
  "status": 401,
  "error": "Invalid Credentials",
  "message": "Invalid username or password",
  "path": "/api/auth/login"
}
```

---

### 3. Get Current User

**Endpoint:** `GET /api/auth/me`

**Description:** Get currently authenticated user's details

**Authentication:** Required

**Headers:**
```
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "role": "USER",
  "createdAt": "2025-10-26T10:00:00",
  "active": true
}
```

**Error Responses:**
```json
// 403 Forbidden - No token provided
{
  "timestamp": "2025-10-26T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

---

## 💼 Job Service APIs

### 4. Create Job

**Endpoint:** `POST /api/jobs`

**Description:** Post a new job listing (EMPLOYER only)

**Authentication:** Required (EMPLOYER role)

**Headers:**
```
Authorization: Bearer <employer-token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "Senior Java Developer",
  "description": "We are looking for an experienced Java developer with strong Spring Boot knowledge. The ideal candidate will have 5+ years of experience in backend development and microservices architecture. You will be responsible for designing, developing, and maintaining scalable backend services.",
  "companyName": "Tech Corp",
  "location": "Bangalore, India",
  "jobType": "FULL_TIME",
  "category": "SOFTWARE_DEVELOPMENT",
  "experienceLevel": "SENIOR",
  "salaryMin": 1500000,
  "salaryMax": 2500000,
  "skillsRequired": "Java, Spring Boot, Microservices, PostgreSQL, Docker, Kubernetes",
  "applicationDeadline": "2025-12-31T23:59:59"
}
```

**Field Details:**

| Field | Type | Required | Options |
|-------|------|----------|---------|
| title | String | Yes | 3-200 chars |
| description | String | Yes | 50-5000 chars |
| companyName | String | Yes | - |
| location | String | Yes | - |
| jobType | Enum | Yes | FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP, FREELANCE |
| category | Enum | Yes | SOFTWARE_DEVELOPMENT, DATA_SCIENCE, DESIGN, MARKETING, SALES, HR, FINANCE, OPERATIONS, CUSTOMER_SUPPORT, OTHER |
| experienceLevel | Enum | Yes | ENTRY_LEVEL, INTERMEDIATE, SENIOR, LEAD, EXECUTIVE |
| salaryMin | Number | No | Positive value |
| salaryMax | Number | No | >= salaryMin |
| skillsRequired | String | No | Comma-separated |
| applicationDeadline | DateTime | No | ISO 8601 format |

**Response (201 Created):**
```json
{
  "id": 1,
  "title": "Senior Java Developer",
  "description": "We are looking for...",
  "companyName": "Tech Corp",
  "location": "Bangalore, India",
  "jobType": "FULL_TIME",
  "category": "SOFTWARE_DEVELOPMENT",
  "experienceLevel": "SENIOR",
  "salaryMin": 1500000,
  "salaryMax": 2500000,
  "skillsRequired": "Java, Spring Boot, Microservices...",
  "postedByUserId": 5,
  "postedByUsername": "tech_hr",
  "isActive": true,
  "applicationDeadline": "2025-12-31T23:59:59",
  "createdAt": "2025-10-26T10:00:00",
  "updatedAt": "2025-10-26T10:00:00",
  "applicationCount": 0
}
```

**Error Responses:**
```json
// 403 Forbidden - Not an employer
{
  "timestamp": "2025-10-26T10:30:00",
  "status": 403,
  "error": "Unauthorized Access",
  "message": "Only employers can create jobs",
  "path": "/api/jobs"
}

// 400 Bad Request - Invalid salary range
{
  "timestamp": "2025-10-26T10:30:00",
  "status": 400,
  "error": "Invalid Job Data",
  "message": "Minimum salary cannot be greater than maximum salary",
  "path": "/api/jobs"
}
```

---

### 5. Get All Jobs

**Endpoint:** `GET /api/jobs`

**Description:** Get paginated list of all active jobs

**Authentication:** None (Public)

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | Integer | 0 | Page number (0-indexed) |
| size | Integer | 10 | Items per page |
| sortBy | String | createdAt | Field to sort by |
| sortDir | String | DESC | Sort direction (ASC/DESC) |

**Example Request:**
```
GET /api/jobs?page=0&size=10&sortBy=createdAt&sortDir=DESC
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Senior Java Developer",
      "companyName": "Tech Corp",
      "location": "Bangalore, India",
      "jobType": "FULL_TIME",
      "category": "SOFTWARE_DEVELOPMENT",
      "experienceLevel": "SENIOR",
      "salaryMin": 1500000,
      "salaryMax": 2500000,
      "createdAt": "2025-10-26T10:00:00",
      "applicationCount": 5
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 50,
  "totalPages": 5,
  "last": false,
  "first": true,
  "numberOfElements": 10
}
```

---

### 6. Get Job by ID

**Endpoint:** `GET /api/jobs/{jobId}`

**Description:** Get detailed information about a specific job

**Authentication:** None (Public)

**Path Parameters:**
- `jobId` (Long) - Job ID

**Example:**
```
GET /api/jobs/1
```

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "Senior Java Developer",
  "description": "We are looking for an experienced...",
  "companyName": "Tech Corp",
  "location": "Bangalore, India",
  "jobType": "FULL_TIME",
  "category": "SOFTWARE_DEVELOPMENT",
  "experienceLevel": "SENIOR",
  "salaryMin": 1500000,
  "salaryMax": 2500000,
  "skillsRequired": "Java, Spring Boot, Microservices, PostgreSQL, Docker, Kubernetes",
  "postedByUserId": 5,
  "postedByUsername": "tech_hr",
  "isActive": true,
  "applicationDeadline": "2025-12-31T23:59:59",
  "createdAt": "2025-10-26T10:00:00",
  "updatedAt": "2025-10-26T10:00:00",
  "applicationCount": 5
}
```

**Error Responses:**
```json
// 404 Not Found
{
  "timestamp": "2025-10-26T10:30:00",
  "status": 404,
  "error": "Job Not Found",
  "message": "Job not found with ID: 999",
  "path": "/api/jobs/999"
}
```

---

### 7. Search Jobs

**Endpoint:** `GET /api/jobs/search`

**Description:** Search jobs by keyword (searches in title, company, location)

**Authentication:** None (Public)

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| keyword | String | Yes | Search term |
| page | Integer | No | Page number (default: 0) |
| size | Integer | No | Items per page (default: 10) |

**Example:**
```
GET /api/jobs/search?keyword=java&page=0&size=10
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Senior Java Developer",
      "companyName": "Tech Corp",
      "location": "Bangalore, India",
      "jobType": "FULL_TIME",
      "category": "SOFTWARE_DEVELOPMENT"
    },
    {
      "id": 2,
      "title": "Java Architect",
      "companyName": "Software Solutions",
      "location": "Mumbai, India",
      "jobType": "FULL_TIME",
      "category": "SOFTWARE_DEVELOPMENT"
    }
  ],
  "totalElements": 15,
  "totalPages": 2
}
```

---

### 8. Filter Jobs by Category

**Endpoint:** `GET /api/jobs/category/{category}`

**Description:** Get all jobs in a specific category

**Authentication:** None (Public)

**Path Parameters:**
- `category` - One of: SOFTWARE_DEVELOPMENT, DATA_SCIENCE, DESIGN, MARKETING, SALES, HR, FINANCE, OPERATIONS, CUSTOMER_SUPPORT, OTHER

**Query Parameters:**
- `page` (default: 0)
- `size` (default: 10)

**Example:**
```
GET /api/jobs/category/SOFTWARE_DEVELOPMENT?page=0&size=10
```

**Response:** Same format as "Get All Jobs"

---

### 9. Filter Jobs by Type

**Endpoint:** `GET /api/jobs/type/{jobType}`

**Description:** Get all jobs of a specific type

**Authentication:** None (Public)

**Path Parameters:**
- `jobType` - One of: FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP, FREELANCE

**Example:**
```
GET /api/jobs/type/FULL_TIME?page=0&size=10
```

---

### 10. Filter by Salary Range

**Endpoint:** `GET /api/jobs/salary-range`

**Description:** Find jobs within a salary range

**Authentication:** None (Public)

**Query Parameters:**

| Parameter | Type | Required |
|-----------|------|----------|
| minSalary | Number | Yes |
| maxSalary | Number | Yes |
| page | Integer | No |
| size | Integer | No |

**Example:**
```
GET /api/jobs/salary-range?minSalary=1000000&maxSalary=2000000&page=0&size=10
```

---

### 11. Get My Jobs

**Endpoint:** `GET /api/jobs/my-jobs`

**Description:** Get all jobs posted by the authenticated employer

**Authentication:** Required (EMPLOYER role)

**Headers:**
```
Authorization: Bearer <employer-token>
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Senior Java Developer",
    "companyName": "Tech Corp",
    "isActive": true,
    "applicationCount": 5,
    "createdAt": "2025-10-26T10:00:00"
  },
  {
    "id": 2,
    "title": "Frontend Developer",
    "companyName": "Tech Corp",
    "isActive": true,
    "applicationCount": 3,
    "createdAt": "2025-10-25T14:30:00"
  }
]
```

---

### 12. Update Job

**Endpoint:** `PUT /api/jobs/{jobId}`

**Description:** Update a job posting (only by the employer who created it)

**Authentication:** Required (EMPLOYER role, must be owner)

**Headers:**
```
Authorization: Bearer <employer-token>
Content-Type: application/json
```

**Request Body:** Same as Create Job

**Response (200 OK):** Updated job object

**Error Responses:**
```json
// 403 Forbidden - Not the owner
{
  "timestamp": "2025-10-26T10:30:00",
  "status": 403,
  "error": "Unauthorized Access",
  "message": "You can only update your own jobs",
  "path": "/api/jobs/1"
}
```

---

### 13. Delete Job

**Endpoint:** `DELETE /api/jobs/{jobId}`

**Description:** Soft delete a job (sets isActive=false)

**Authentication:** Required (EMPLOYER role, must be owner)

**Headers:**
```
Authorization: Bearer <employer-token>
```

**Response (200 OK):**
```json
{
  "message": "Job deleted successfully"
}
```

---

### 14. Apply for Job

**Endpoint:** `POST /api/jobs/{jobId}/apply`

**Description:** Submit an application for a job (USER only)

**Authentication:** Required (USER role)

**Headers:**
```
Authorization: Bearer <user-token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "coverLetter": "I am very interested in this position. I have 6 years of experience in Java development with extensive knowledge of Spring Boot and microservices. I have worked on multiple large-scale projects and I believe I would be a great fit for this role.",
  "resumeUrl": "https://example.com/resume/john_doe.pdf"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "jobId": 1,
  "jobTitle": "Senior Java Developer",
  "companyName": "Tech Corp",
  "userId": 10,
  "username": "john_doe",
  "userEmail": "john@example.com",
  "coverLetter": "I am very interested...",
  "resumeUrl": "https://example.com/resume/john_doe.pdf",
  "status": "PENDING",
  "appliedAt": "2025-10-26T11:00:00",
  "updatedAt": "2025-10-26T11:00:00"
}
```

**Error Responses:**
```json
// 403 Forbidden - Employer trying to apply
{
  "status": 403,
  "error": "Unauthorized Access",
  "message": "Employers cannot apply for jobs"
}

// 409 Conflict - Already applied
{
  "status": 409,
  "error": "Application Already Exists",
  "message": "You have already applied for this job"
}

// 403 Forbidden - Job not active
{
  "status": 403,
  "error": "Unauthorized Access",
  "message": "This job is no longer accepting applications"
}
```

---

### 15. Get Applications for Job

**Endpoint:** `GET /api/jobs/{jobId}/applications`

**Description:** View all applications for a job (EMPLOYER only, must be job owner)

**Authentication:** Required (EMPLOYER role)

**Headers:**
```
Authorization: Bearer <employer-token>
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "jobId": 1,
    "jobTitle": "Senior Java Developer",
    "companyName": "Tech Corp",
    "userId": 10,
    "username": "john_doe",
    "userEmail": "john@example.com",
    "coverLetter": "I am very interested...",
    "resumeUrl": "https://example.com/resume/john_doe.pdf",
    "status": "PENDING",
    "appliedAt": "2025-10-26T11:00:00",
    "updatedAt": "2025-10-26T11:00:00"
  },
  {
    "id": 2,
    "jobId": 1,
    "username": "jane_dev",
    "userEmail": "jane@example.com",
    "status": "SHORTLISTED",
    "appliedAt": "2025-10-26T12:00:00"
  }
]
```

---

### 16. Get My Applications

**Endpoint:** `GET /api/jobs/applications/my-applications`

**Description:** View all jobs the authenticated user has applied to

**Authentication:** Required (USER role)

**Headers:**
```
Authorization: Bearer <user-token>
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "jobId": 1,
    "jobTitle": "Senior Java Developer",
    "companyName": "Tech Corp",
    "status": "SHORTLISTED",
    "appliedAt": "2025-10-26T11:00:00",
    "updatedAt": "2025-10-26T15:00:00"
  },
  {
    "id": 2,
    "jobId": 3,
    "jobTitle": "Frontend Developer",
    "companyName": "Web Solutions",
    "status": "PENDING",
    "appliedAt": "2025-10-25T10:00:00"
  }
]
```

---

### 17. Update Application Status

**Endpoint:** `PATCH /api/jobs/applications/{applicationId}/status`

**Description:** Update the status of an application (EMPLOYER only)

**Authentication:** Required (EMPLOYER role, must own the job)

**Headers:**
```
Authorization: Bearer <employer-token>
```

**Query Parameters:**
- `status` - One of: REVIEWED, SHORTLISTED, INTERVIEWED, ACCEPTED, REJECTED

**Example:**
```
PATCH /api/jobs/applications/1/status?status=SHORTLISTED
```

**Response (200 OK):**
```json
{
  "id": 1,
  "jobId": 1,
  "jobTitle": "Senior Java Developer",
  "username": "john_doe",
  "userEmail": "john@example.com",
  "status": "SHORTLISTED",
  "appliedAt": "2025-10-26T11:00:00",
  "updatedAt": "2025-10-26T16:00:00"
}
```

---

### 18. Withdraw Application

**Endpoint:** `DELETE /api/jobs/{jobId}/applications/withdraw`

**Description:** Withdraw an application (USER only)

**Authentication:** Required (USER role)

**Headers:**
```
Authorization: Bearer <user-token>
```

**Response (200 OK):**
```json
{
  "message": "Application withdrawn successfully"
}
```

---

## 🎛️ User Preference APIs

### 19. Update Preferences

**Endpoint:** `PUT /api/auth/preferences`

**Description:** Subscribe to job categories to receive notifications

**Authentication:** Required (USER role)

**Headers:**
```
Authorization: Bearer <user-token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "subscribedCategories": ["SOFTWARE_DEVELOPMENT", "DATA_SCIENCE"],
  "emailNotificationsEnabled": true
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 10,
  "subscribedCategories": ["SOFTWARE_DEVELOPMENT", "DATA_SCIENCE"],
  "emailNotificationsEnabled": true
}
```

---

### 20. Get My Preferences

**Endpoint:** `GET /api/auth/preferences`

**Description:** Get current user's notification preferences

**Authentication:** Required

**Headers:**
```
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 10,
  "subscribedCategories": ["SOFTWARE_DEVELOPMENT", "DATA_SCIENCE"],
  "emailNotificationsEnabled": true
}
```

---

## ❌ Error Responses

### Standard Error Format
```json
{
  "timestamp": "2025-10-26T10:30:00",
  "status": 400,
  "error": "Error Type",
  "message": "Detailed error message",
  "path": "/api/endpoint"
}
```

### Validation Error Format
```json
{
  "username": "Username must be between 3 and 50 characters",
  "email": "Email should be valid",
  "password": "Password must be at least 6 characters"
}
```

---

## 📊 HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful GET, PUT, PATCH, DELETE |
| 201 | Created | Successful POST (resource created) |
| 400 | Bad Request | Validation errors, invalid data |
| 401 | Unauthorized | Invalid credentials |
| 403 | Forbidden | Valid token but insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource (username, email, application) |
| 500 | Internal Server Error | Server-side error |

---

## 🔄 Application Status Flow
```
PENDING → REVIEWED → SHORTLISTED → INTERVIEWED → ACCEPTED
                                              ↘ REJECTED

User can also: → WITHDRAWN
```

---

## 📝 Notes

1. **All timestamps** are in ISO 8601 format
2. **Pagination** is 0-indexed
3. **JWT tokens** expire after 24 hours
4. **Soft deletes** are used (isActive flag)
5. **Search** is case-insensitive
6. **Salary** values are in INR (Indian Rupees)

---

## 🧪 Testing Tips

### Postman Environment Variables
```json
{
  "baseUrl": "http://localhost:8080",
  "token": "",
  "employerToken": "",
  "userToken": ""
}
```

### Common Test Flow

1. Register EMPLOYER → Save token
2. Create Job → Note job ID
3. Register USER → Save token
4. Subscribe to categories
5. Apply for job
6. Get applications (EMPLOYER)
7. Update application status
8. Check notifications in logs

---

## 🔗 Related Documentation

- [Main README](../README.md)
- [Architecture Documentation](./ARCHITECTURE.md)
- [Deployment Guide](./DEPLOYMENT.md)

---

<div align="center">

**Built with Spring Boot Microservices** 🚀

</div>