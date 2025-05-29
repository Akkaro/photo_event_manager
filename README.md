# PhotoApp - Advanced Photo Management Platform

## Table of Contents
- [Overview](#overview)
- [Current Status](#current-status)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation and Setup](#installation-and-setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Known Issues](#known-issues)
- [Technology Stack](#technology-stack)
- [Database Schema](#database-schema)
- [Photo Editing Capabilities](#photo-editing-capabilities)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## Overview

PhotoApp is an advanced full-stack web application for organizing, storing, and editing photos in albums with robust user authentication, sophisticated image processing capabilities, and comprehensive sharing mechanisms. The platform combines photo editing tools implemented through teh Image processing labs with intuitive collaboration features, making it suitable for both personal use and event management.

## Current Status

The application has evolved significantly from its initial implementation and now includes:

### What's Working
- **Authentication & Authorization**: Complete JWT-based authentication with role-based access control (ADMIN, MODERATOR, USER)
- **Album Management**: Create, view, update, and delete photo albums with sharing capabilities
- **Photo Management**: Upload, view, organize, and delete photos with cloud storage integration
- **Advanced Photo Editing**: 12+ image processing operations including brightness/contrast, gamma correction, histogram equalization, noise reduction, edge detection, morphological operations, and more
- **Version Control**: Complete edit history with ability to revert to any previous version
- **Album Sharing**: Private sharing with specific users and public sharing via QR codes
- **Public Access**: QR code generation for public album viewing without authentication
- **Responsive UI**: Mobile-friendly interface with Bootstrap 5

## Features

### Core Functionality
- **User Authentication**: Secure registration and login with JWT tokens
- **Multi-Role System**: Three user roles with different permission levels
- **Album Management**: Create personal and shared photo albums
- **Photo Upload**: Support for multiple image formats with cloud storage
- **Advanced Photo Editing**: Professional-grade image processing tools
- **Version Control**: Complete edit history with one-click revert capabilities
- **Sharing Mechanisms**: Both private user-to-user and public QR code sharing

### Photo Editing Features
- **Basic Adjustments**: Brightness, contrast, and gamma correction
- **Enhancement Tools**: Histogram equalization and noise reduction
- **Effects & Filters**: Gaussian blur, edge detection, HSV conversion
- **Advanced Processing**: Thresholding and morphological operations
- **Edit Modes**: Simple, Advanced, and Combined processing modes
- **Version Management**: Complete edit history with thumbnails and descriptions

### Sharing & Collaboration
- **Private Sharing**: Share albums with specific registered users
- **Public Albums**: Generate QR codes for public access without registration
- **Access Control**: Granular permissions for different user roles
- **Album Status**: Public/Private toggle with visual indicators

## Architecture

### Backend
- **Framework**: Spring Boot 3.4.4 with Java 17
- **Security**: Spring Security with JWT authentication
- **Database**: PostgreSQL with Spring Data JPA
- **Image Processing**: OpenCV integration for advanced editing
- **Cloud Storage**: Cloudinary for scalable image storage and delivery
- **API Documentation**: OpenAPI 3 with Swagger UI

### Frontend
- **Framework**: Angular 19.2 with TypeScript
- **UI Library**: Bootstrap 5 for responsive design
- **State Management**: RxJS for reactive programming
- **HTTP Client**: Angular HttpClient with interceptors for authentication

### Database Design
- **Users**: Authentication and role management
- **Albums**: Album metadata with sharing configurations
- **Photos**: Image metadata with version tracking
- **Photo Edits**: Detailed edit history with all parameters
- **Album Shares**: User-to-user sharing relationships

## Prerequisites

Before running the application, ensure you have:

- **Java 17** or higher
- **Node.js 16+** and npm
- **PostgreSQL 12+** database
- **Cloudinary account** for image storage
- **Angular CLI** (`npm install -g @angular/cli`)
- **Maven 3.6+** (or use the included Maven wrapper)

## Installation and Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd PhotoApp
```

### 2. Database Setup
Create a PostgreSQL database:
```sql
CREATE DATABASE photo_mgmt_db;
```

### 3. Backend Configuration
Navigate to the backend directory and create the required configuration files:

```bash
cd photo-mgmt-backend/src/main/resources
```

Create `application.yaml`:
```yaml
spring:
    application:
      name: photo-mgmt-backend
    datasource:
      url: jdbc:postgresql://localhost:5432/photo_mgmt_db
      username: postgres
      password: your_database_password
    flyway:
      enabled: true
      locations:
         classpath:db/migration
         classpath:db/data
      driver-class-name: org.postgresql.Driver
    jpa:
      hibernate:
        ddl-auto: none
      properties:
        hibernate:
          dialect: org.hibernate.dialect.PostgreSQLDialect
      show-sql: true
    swagger-ui:
      enabled: true
security:
    secret-key: your_jwt_secret_key_here_make_it_long_and_secure
    token-expiration-days: 15
server:
    port: 8777
    servlet:
      context-path: '/api'
cloudinary:
    cloud-name: your_cloudinary_cloud_name
    api-key: your_cloudinary_api_key
    api-secret: your_cloudinary_api_secret
```

Create `application.properties`:
```properties
spring.application.name=photo-mgmt-backend
spring.jpa.properties.hibernate.globally_quoted_identifiers=true
```

### 4. Cloudinary Setup
1. Create a free account at [Cloudinary](https://cloudinary.com/)
2. Get your cloud name, API key, and API secret from the dashboard
3. Add these credentials to your `application.yaml` file

### 5. Frontend Setup
```bash
cd photo-mgmt-frontend
npm install
```

## Configuration

### Environment Variables
The application uses two critical configuration files that are excluded from version control for security:

#### application.yaml
Contains sensitive configuration including:
- Database connection details
- JWT secret key for token signing
- Cloudinary credentials for image storage
- Server port and context path configuration

#### application.properties
Contains additional Spring Boot configuration:
- Application name
- Hibernate dialect settings
- Database identifier quoting configuration

### Security Configuration
- **JWT Secret**: Use a strong, randomly generated secret key (minimum 32 characters)
- **Database Password**: Ensure your PostgreSQL user has appropriate permissions
- **Cloudinary Credentials**: Keep your API keys secure and never commit them to version control

## Running the Application

### Start the Backend
```bash
cd photo-mgmt-backend
./mvnw spring-boot:run
```
The backend will be available at `http://localhost:8777/api`

### Start the Frontend
```bash
cd photo-mgmt-frontend
ng serve
```
The frontend will be available at `http://localhost:4200`

### Database Migration
The application uses Flyway for database migrations. The database schema will be automatically created and populated with sample data when you first start the backend.

## API Documentation

Once the backend is running, you can access the interactive API documentation at:
- **Swagger UI**: `http://localhost:8777/api/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8777/api/v3/api-docs`

![image](https://github.com/user-attachments/assets/44830d90-0a12-412d-886d-fe9ad6775fa7)


### Sample API Endpoints

#### Authentication
- `POST /v1/auth/register` - User registration
- `POST /v1/auth/login` - User login

#### Albums
- `GET /v1/albums` - List all albums (filtered by user access)
- `POST /v1/albums` - Create new album
- `GET /v1/albums/{id}` - Get album details
- `POST /v1/albums/{id}/public` - Make album public
- `GET /v1/albums/{id}/qr-code` - Download QR code

#### Photos
- `GET /v1/photos` - List photos with filtering
- `POST /v1/photos` - Upload new photo
- `GET /v1/photos/{id}` - Get photo details
- `POST /v1/photos/{id}/edit` - Apply advanced photo editing

#### Public Access
- `GET /v1/public/album/{token}` - View public album (no auth required)

*Screenshot about API endpoint documentation*

## Known Issues

### Current Bugs
1. **Photo Editing**: Some edge cases in photo editing operations may cause processing failures (Remove Noise feature has bugs in teh OpenCVApplication.exe file)
2. **BMP Format**: BMP images may experience display issues during upload and processing (the picture borders are nto treated correctly, and they may be cropped or completed with white spaces)
3. **QR Code display**: Since the application was never hosted, the QR code feature is not complete. It was substitued with a URL generation, and copy feture, which can be tested with ease on localhost enironment.
   
### Limitations
- **Processing Timeout**: Large image operations may timeout on slower systems
- **Concurrent Editing**: Multiple users editing the same photo simultaneously may cause conflicts (but the application was never hosted so this was not tested)

### Workarounds
- Use JPEG/PNG formats for best compatibility
- Avoid very large images (>10MB) for editing operations
- Refresh the page if edit operations appear to hang

## Technology Stack

### Backend Technologies
- **Java 17** - Programming language
- **Spring Boot 3.4.4** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database abstraction layer
- **PostgreSQL** - Primary database
- **Flyway** - Database migration management
- **OpenCV** - Advanced image processing
- **Cloudinary** - Cloud image storage and delivery
- **ZXing** - QR code generation
- **Maven** - Dependency management and build tool

### Frontend Technologies
- **Angular 19.2** - Frontend framework
- **TypeScript** - Programming language
- **Bootstrap 5.3** - CSS framework for responsive design
- **FontAwesome** - Icon library
- **RxJS** - Reactive programming library
- **Angular CLI** - Development and build tools

### Development Tools
- **Swagger/OpenAPI** - API documentation
- **H2 Database** - Testing database (optional)
- **Postman** - API testing (recommended)

## Database Schema

The application uses a sophisticated database schema with the following main entities:

### Core Tables
- **user**: User accounts and authentication
- **album**: Photo albums with sharing metadata
- **photo**: Photo metadata and version tracking
- **album_share**: User-to-user sharing relationships

### Advanced Features
- **photo_edit**: Detailed edit history with all processing parameters

![Databse Schema](https://github.com/user-attachments/assets/9e4adfe3-db6d-48ed-8e2e-d0f76127d321)


### Key Relationships
- Users own albums and photos
- Albums can be shared with multiple users
- Photos maintain complete edit history
- Public albums use unique tokens for access

## Photo Editing Capabilities

### Basic Adjustments
- **Brightness**: Linear pixel value adjustment (-100 to +100)
- **Contrast**: Pixel value scaling (0.1 to 3.0)
- **Gamma Correction**: Non-linear exposure adjustment (0.1 to 3.0)

### Enhancement Operations
- **Histogram Equalization**: Automatic contrast enhancement
- **Noise Reduction**: Bilateral and median filtering options

### Creative Effects
- **Gaussian Blur**: Configurable kernel size and sigma
- **Edge Detection**: Canny and Sobel algorithms
- **HSV Conversion**: Alternative color space representation

### Advanced Processing
- **Thresholding**: Binary and automatic (Otsu's method)
- **Morphological Operations**: Opening and closing with configurable parameters

### Edit Modes
1. **Simple Mode**: Basic adjustments with sliders
2. **Advanced Mode**: Individual operation selection
3. **Combined Mode**: Multiple operations in single pass

![image](https://github.com/user-attachments/assets/8f0df587-6644-458f-a3ba-5aec37bc6745)

### Version Control
- Complete edit history with thumbnails
- One-click revert to any previous version
- Before/after comparison tools
- Version metadata with timestamps and descriptions

![image](https://github.com/user-attachments/assets/91eeb273-b881-4fb4-b309-1f2d1c1ecb9a)

## Testing

### Backend Testing
Run the backend tests:
```bash
cd photo-mgmt-backend
./mvnw test
```

### Frontend Testing
Run the frontend tests:
```bash
cd photo-mgmt-frontend
ng test
```

### Manual Testing
1. Register a new user account
2. Create an album and upload photos
3. Test photo editing operations
4. Share an album with another user
5. Generate and test QR code access
6. Verify version history functionality

## Contributing

### Development Setup
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Run tests to ensure everything works
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Code Style
- Follow existing Java and TypeScript conventions
- Use meaningful variable and method names
- Add appropriate comments for complex logic
- Include unit tests for new features

### Reporting Issues
Please use the GitHub issue tracker to report bugs or request features. Include:
- Detailed description of the issue
- Steps to reproduce
- Expected vs actual behavior
- Screenshots if applicable
- System information (OS, browser, etc.)

## Screenshots
*Screenshot about login page*
![image](https://github.com/user-attachments/assets/14c64cb5-1034-4cda-94d6-f65e604a25a3)

*Screenshot about user registration*
![image](https://github.com/user-attachments/assets/4c6a32a1-ab18-49b1-8518-e4025d1a6011)

*Screenshot about albums listing*
![image](https://github.com/user-attachments/assets/c493a92b-f553-4f87-9eae-7b6d846168ca)

*Screenshot about photo upload interface*
![image](https://github.com/user-attachments/assets/1abd6000-4c4e-4a1f-84c7-b51b5756244f)

*Screenshot about photo details page*
![image](https://github.com/user-attachments/assets/1f95002f-ca85-40f0-b924-ef7889000338)

*Screenshot about album sharing interface*
![image](https://github.com/user-attachments/assets/448047ee-82f2-4eb6-bd13-6fe5c047f156)

*Screenshot about public album viewer*
![image](https://github.com/user-attachments/assets/00bb4289-a9b5-47de-ac9a-39126cc0e51b)

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For questions or support, please contact:
- **Developer**: Benő Ákos
- **Email**: Beno.Fe.Akos@student.utcluj.ro/akos.beno@gmail.com
- **Institution**: Technical University of Cluj-Napoca

## Acknowledgments

- Sergiu Blaj, lab teacher of the Software Design course
- Andra Petrovai, lab teacher of the Image Processing course
