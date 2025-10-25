# 🏋️‍♂️ Fitness Club Management System

A comprehensive Spring Boot REST API for managing fitness club operations including members, trainers, classes, and bookings.

## ✨ Features

- **Member Management** - Complete CRUD operations for club members
- **Subscription System** - Membership management with validity periods
- **Trainer Management** - Manage fitness trainers and their specializations  
- **Class Scheduling** - Create and manage workout classes with capacity limits
- **Smart Booking** - Book classes with automatic validation:
  - Active membership check
  - Available spots verification
  - Cancellation policy enforcement (2-hour notice)

## 🛠️ Tech Stack

- **Backend:** Java 21, Spring Boot 3.2.0
- **Database:** H2 (in-memory), Spring Data JPA
- **Build Tool:** Maven
- **API:** RESTful Web Services

## 🚀 Quick Start

```bash
# Clone repository
git clone https://github.com/savikthk/fitnessclub-management.git

# Run application
mvn spring-boot:run
