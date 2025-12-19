# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2025-12-19
### Added
- Add ELK stack integration
- Add gRPC Moderation Service integration
- Add batch Media upload
- Add Kafka for notification and audit data
- Add multitenancy support

## [1.1.0] - 2025-12-06
### Added
- Add multitenancy support
- Add healthcheck endpoint
- Add prometheus metrics endpoint
- Add kafka for notifications

## [1.0.0] - 2025-11-10
### Added
- Initial release of Traversium Trip Service
- User authentication and authorization
- RESTful API for trip management
- Docker support with multi-platform builds (amd64, arm64)
- GitHub Actions CI/CD pipeline

### Security
- Implemented JWT-based authentication