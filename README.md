# KEYSTONE — Field Service Management Platform

A full-stack **Field Service Management Platform** built to manage maintenance requests, work orders, technician assignments, service operations, SLA monitoring, status tracking, and reporting through a centralized web application.

KEYSTONE connects customers, dispatchers, technicians, and managers in a single platform. Customers can raise maintenance requests and track progress, dispatchers can create and assign work orders, technicians can manage assigned jobs, and managers can monitor operations, SLAs, and completed work.

---

## Table of Contents

* [Overview](#overview)
* [Key Features](#key-features)
* [User Roles](#user-roles)
* [Work Order Lifecycle](#work-order-lifecycle)
* [System Architecture](#system-architecture)
* [Technology Stack](#technology-stack)
* [Project Structure](#project-structure)
* [Domain Model](#domain-model)
* [Security](#security)
* [Backend](#backend)
* [Frontend](#frontend)
* [Database](#database)
* [Environment Configuration](#environment-configuration)
* [Prerequisites](#prerequisites)
* [Installation and Setup](#installation-and-setup)
* [Running the Application](#running-the-application)
* [Docker Setup](#docker-setup)
* [API Documentation](#api-documentation)
* [Testing](#testing)
* [Important Security Notes](#important-security-notes)
* [Project Highlights](#project-highlights)
* [Future Improvements](#future-improvements)
* [Author](#author)

---

## Overview

KEYSTONE is designed for organizations that manage field-service and maintenance operations.

The platform manages the complete service workflow:


Customer Request
       |
       v
Work Order Creation
       |
       v
Technician Assignment
       |
       v
Work Started
       |
       v
Work Completed
       |
       v
Manager Review
       |
       v
Work Order Closed


The system replaces fragmented maintenance coordination with a centralized platform where users can manage requests, assignments, work progress, service-level agreements, parts, time logs, and reporting.

---

## Key Features

### Authentication and Authorization

* JWT-based authentication
* Secure password hashing
* Role-based access control
* Protected API endpoints
* Protected frontend routes
* Backend authorization using Spring Security
* Resource ownership validation

### Work Order Management

* Create work orders
* Assign technicians
* Track work order status
* Start work
* Put work on hold
* Resume work
* Complete work
* Close completed work orders
* Cancel applicable work orders
* Maintain work order status history

### SLA Monitoring

* Track service-level agreements
* Monitor overdue work
* Maintain SLA-related states
* Support operational monitoring

### Technician Management

* View assigned work orders
* Start assigned jobs
* Record parts used
* Record working time
* Put work on hold
* Resume work
* Complete assigned work

### Customer Management

* Raise maintenance requests
* View service requests
* Track request progress
* View relevant work-order information

### Manager Operations

* Monitor work orders
* Monitor SLA performance
* Close completed work
* View operational information
* Support reporting and service monitoring

### Technical Features

* RESTful APIs
* DTO-based request and response handling
* Centralized exception handling
* Database migrations with Flyway
* JPA/Hibernate persistence
* Swagger/OpenAPI documentation
* Scheduled SLA checking
* Centralized work-order state machine
* PostgreSQL database integration



## User Roles

KEYSTONE supports four primary user roles.

### 1. Dispatcher

The dispatcher is responsible for managing and assigning service work.

Capabilities include:

* Creating work orders
* Assigning work orders
* Monitoring work order progress
* Managing service requests

### 2. Technician

The technician handles field-service work.

Capabilities include:

* Viewing assigned work orders
* Starting assigned work
* Putting work on hold
* Resuming work
* Recording parts
* Recording time
* Completing work orders

### 3. Manager

The manager oversees service operations.

Capabilities include:

* Monitoring work orders
* Monitoring SLA performance
* Closing completed work orders
* Reviewing operational information
* Monitoring team workload and service performance

### 4. Customer

Customers can interact with the service platform.

Capabilities include:

* Raising maintenance requests
* Tracking request status
* Monitoring service progress



## Work Order Lifecycle

KEYSTONE uses a centralized work-order state machine to control valid status transitions.


NEW
 |
 +-----> CANCELLED
 |
 v
ASSIGNED
 |
 +-----> CANCELLED
 |
 v
IN_PROGRESS
 |
 +-----> ON_HOLD
 |          |
 |          v
 |      IN_PROGRESS
 |
 v
COMPLETED
 |
 v
CLOSED


### Valid Transitions

| Current Status | Next Status | Authorized Role               |
| -------------- | ----------- | ----------------------------- |
| NEW            | ASSIGNED    | Dispatcher / Manager          |
| NEW            | CANCELLED   | Dispatcher / Manager          |
| ASSIGNED       | IN_PROGRESS | Assigned Technician / Manager |
| ASSIGNED       | CANCELLED   | Dispatcher / Manager          |
| IN_PROGRESS    | ON_HOLD     | Assigned Technician / Manager |
| IN_PROGRESS    | COMPLETED   | Assigned Technician / Manager |
| ON_HOLD        | IN_PROGRESS | Assigned Technician / Manager |
| COMPLETED      | CLOSED      | Manager                       |
| CLOSED         | None        | Terminal                      |
| CANCELLED      | None        | Terminal                      |

Invalid transitions are rejected by the backend.

Work-order status changes are recorded in status history along with the previous status, new status, actor, timestamp, and optional note.



## System Architecture


                    ┌─────────────────────────┐
                    │       Web Browser       │
                    └────────────┬────────────┘
                                 │
                                 │ HTTP / JSON
                                 │ JWT
                                 ▼
                    ┌─────────────────────────┐
                    │    React Frontend       │
                    │ React + TypeScript      │
                    │ Vite + React Router     │
                    │ Axios                   │
                    └────────────┬────────────┘
                                 │
                                 │ REST API
                                 ▼
                    ┌─────────────────────────┐
                    │    Spring Boot API      │
                    │                         │
                    │ Spring Security         │
                    │ JWT Authentication      │
                    │ REST Controllers        │
                    │ Services                │
                    │ DTOs                    │
                    │ JPA / Hibernate         │
                    └────────────┬────────────┘
                                 │
                                 │ JPA / JDBC
                                 ▼
                    ┌─────────────────────────┐
                    │      PostgreSQL         │
                    │                         │
                    │ Users                   │
                    │ Customers               │
                    │ Sites                   │
                    │ Work Orders             │
                    │ Status History          │
                    │ Parts                   │
                    │ Time Logs               │
                    └─────────────────────────┘

                         Flyway
                           │
                           ▼
                   Database Migrations




## Technology Stack

### Backend

* Java 21
* Spring Boot 3.3
* Spring Security
* JWT
* Spring Data JPA
* Hibernate
* PostgreSQL
* Flyway
* Bean Validation
* Maven
* Springdoc OpenAPI / Swagger

### Frontend

* React 18
* TypeScript
* Vite
* React Router
* Axios
* CSS

### Development Tools

* Git
* GitHub
* Visual Studio Code
* IntelliJ IDEA / Eclipse
* Postman
* Docker



## Project Structure

keystone/
│
├── backend/
│   │
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/zidio/keystone/
│   │       │       │
│   │       │       ├── config/
│   │       │       │   ├── SecurityConfig
│   │       │       │   ├── OpenApiConfig
│   │       │       │   └── SlaProperties
│   │       │       │
│   │       │       ├── controller/
│   │       │       │   └── REST Controllers
│   │       │       │
│   │       │       ├── dto/
│   │       │       │   └── Request / Response DTOs
│   │       │       │
│   │       │       ├── entity/
│   │       │       │   └── JPA Entities
│   │       │       │
│   │       │       ├── enums/
│   │       │       │   ├── Role
│   │       │       │   ├── Priority
│   │       │       │   ├── WorkOrderStatus
│   │       │       │   └── SlaState
│   │       │       │
│   │       │       ├── exception/
│   │       │       │   └── Custom Exceptions
│   │       │       │
│   │       │       ├── mapper/
│   │       │       │   └── Entity / DTO Mappers
│   │       │       │
│   │       │       ├── repository/
│   │       │       │   └── Spring Data Repositories
│   │       │       │
│   │       │       ├── scheduler/
│   │       │       │   └── SLA Scheduler
│   │       │       │
│   │       │       ├── security/
│   │       │       │   ├── JWT Filter
│   │       │       │   ├── JWT Service
│   │       │       │   └── UserDetails
│   │       │       │
│   │       │       └── service/
│   │       │           ├── Business Logic
│   │       │           └── WorkOrderStateMachine
│   │       │
│   │       └── resources/
│   │
│   └── pom.xml
│
├── frontend/
│   │
│   ├── src/
│   │   ├── api/
│   │   │   └── Axios API Clients
│   │   │
│   │   ├── auth/
│   │   │   ├── AuthContext
│   │   │   └── ProtectedRoute
│   │   │
│   │   ├── components/
│   │   │   ├── Layout
│   │   │   ├── Shared Components
│   │   │   └── Work Order Components
│   │   │
│   │   ├── pages/
│   │   │   └── Application Pages
│   │   │
│   │   └── types/
│   │       └── TypeScript Types
│   │
│   ├── package.json
│   └── vite.config.*
│
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md




## Domain Model

The main domain relationships are:


Customer
   |
   | 1
   |
   | *
   v
 Site
   |
   | 1
   |
   | *
   v
Work Order
   |
   +--------------------+
   |                    |
   v                    v
Status History       Part Usage
                        |
                        v
                       Part

Work Order
   |
   v
Time Log


The platform maintains relationships between customers, sites, work orders, status histories, parts, part usage, and time logs.



## Security

Security is implemented primarily through Spring Security and JWT authentication.

### Authentication

* User login
* Password hashing
* JWT token generation
* JWT bearer authentication
* Stateless authentication

### Authorization

* Role-based access control
* Method-level authorization
* Protected backend endpoints
* Protected frontend routes
* Resource ownership validation

The frontend only controls the user experience. Authorization is enforced by the backend.

For example, a technician cannot operate on a work order that is not assigned to them.



## Backend

The backend is a Maven-based Spring Boot application.

### Backend Responsibilities

* Authentication
* Authorization
* User management
* Work order management
* Customer management
* Technician operations
* SLA monitoring
* Parts management
* Time tracking
* Status history
* Validation
* Exception handling
* Database persistence
* API documentation

### Main Backend Layers


Controller
    |
    v
Service
    |
    v
Repository
    |
    v
PostgreSQL


Controllers handle HTTP requests, services contain business logic, and repositories communicate with the database.



## Frontend

The frontend is a React and TypeScript single-page application.

### Frontend Responsibilities

* Login and authentication UI
* Role-based navigation
* Dashboard interfaces
* Work order management
* Customer requests
* Technician workflows
* Manager operations
* API communication
* Protected routes
* Form handling
* Status display

Axios is used for communication with the backend REST API.

React Router is used for frontend navigation and protected routes.



## Database

KEYSTONE uses **PostgreSQL** as its relational database.

The application uses:

* Spring Data JPA
* Hibernate
* Flyway

Flyway manages versioned database schema migrations.

The domain includes entities related to:

* Users
* Customers
* Sites
* Work Orders
* Work Order Status History
* Parts
* Part Usage
* Time Logs



## Environment Configuration

The repository contains an `.env.example` file as a template.

Create your own local environment configuration based on the example.


.env.example


should be copied/configured locally as:

.env


### Important

Never commit the real `.env` file to GitHub.

Do not publish:

* Database passwords
* JWT secrets
* API keys
* Private credentials
* Production credentials

Only safe example values should be included in `.env.example`.



## Prerequisites

Before running the project, install:

* Java 21
* Maven
* Node.js and npm
* PostgreSQL
* Git
* Docker Desktop (optional)

Verify Java:


java -version


Verify Maven:


mvn -version


Verify Node.js:


node -v


Verify npm:


npm -v




## Installation and Setup

### 1. Clone the Repository


git clone https://github.com/shivamkumarsinha-blip/keystone-field-service-management.git


Move into the project:


cd keystone-field-service-management




### 2. Configure the Backend

Open the backend directory:


cd backend


Configure your local environment and PostgreSQL connection according to the project's `.env.example` and application configuration.

Make sure PostgreSQL is running before starting the backend.



### 3. Start the Backend

Run:


mvn spring-boot:run


The backend will start as a Spring Boot application.

---

### 4. Configure the Frontend

Open a new terminal and navigate to:


cd frontend


Install dependencies:


npm install


Start the frontend:


npm run dev


Open the local URL displayed by Vite in your browser.

---

## Running the Application

The application requires the following components:


PostgreSQL
    |
    v
Spring Boot Backend
    |
    v
React Frontend
```

Recommended startup order:

1. Start PostgreSQL
2. Start the Spring Boot backend
3. Start the React frontend
4. Open the frontend URL in the browser
5. Log in using an appropriate application account

---

## Docker Setup

The repository includes:


docker-compose.yml
```

If Docker configuration is complete for your local environment, start the services with:


docker compose up --build


To stop the services:


docker compose down


Before using Docker for deployment, verify the environment variables and database configuration.

---

## API Documentation

The backend uses **Springdoc OpenAPI / Swagger UI** for API documentation.

After starting the backend, open the Swagger UI endpoint configured by the application.

Swagger provides a browsable reference for the REST APIs.

---

## Testing

### Backend

Run backend tests with:


cd backend
mvn test


### Frontend

Install dependencies:


cd frontend
npm install


Run the frontend development server:


npm run dev


Before deployment, verify that the complete frontend and backend workflow works correctly with the configured PostgreSQL database.

---

## Error Handling

The backend uses centralized exception handling.

Examples include:

* Invalid work-order transition
* Unauthorized access
* Forbidden resource access
* Validation errors
* Database-related errors

Invalid work-order transitions are rejected instead of allowing inconsistent state changes.

---

## Project Highlights

### Centralized Work Order State Machine

The work-order lifecycle is centralized in a dedicated state-machine implementation so that transition rules remain consistent between the backend and frontend.

### Role-Based Security

The application does not rely only on hiding frontend buttons. Backend authorization verifies user roles and, where necessary, resource ownership.

### SLA Monitoring

The platform includes SLA-related states and scheduled SLA checking to help identify overdue service work.

### Audit History

Work-order status changes are recorded with:

* Previous status
* New status
* Actor
* Timestamp
* Optional note

### Layered Backend Architecture

The backend separates:


Controllers
    ↓
Services
    ↓
Repositories
    ↓
Database


This improves maintainability and keeps business logic separate from HTTP handling and persistence.

---

## Known Limitations

The project should be tested in the target development environment before being considered production-ready.

Recommended verification steps:

cd backend
mvn clean package


Then verify:

* PostgreSQL connectivity
* Database migrations
* Backend startup
* Frontend dependency installation
* API communication
* Authentication
* Authorization
* Work-order lifecycle
* SLA functionality
* Docker configuration

---

## Future Improvements

Possible future enhancements include:

* CI/CD pipeline
* Cloud deployment
* Automated integration testing
* Enhanced notification system
* Email notifications
* Mobile application
* Advanced analytics dashboards
* Production monitoring
* Centralized logging
* Automated deployment
* Additional reporting features

---

## Project Information

**Project:** KEYSTONE — Field Service Management Platform

**Type:** Full-Stack Web Application

**Backend:** Java, Spring Boot

**Frontend:** React, TypeScript

**Database:** PostgreSQL

**Authentication:** JWT + Spring Security

**ORM:** JPA / Hibernate

**Migration:** Flyway

**API:** REST

**Documentation:** Swagger / OpenAPI

**Team Size:** 5

---

## Author

Shivam Kumar Sinha

B.Tech — Computer Science & Engineering

GitHub:
https://github.com/shivamkumarsinha-blip

LinkedIn:
https://www.linkedin.com/in/shivam-kumar-sinha-51aa5221a/


