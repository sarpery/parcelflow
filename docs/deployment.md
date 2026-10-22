# ParcelFlow — Deployment Guide

## Overview

ParcelFlow is a full-stack logistics tracking application built with Spring Boot, React, TypeScript, PostgreSQL, and Apache Kafka.

The application is containerized using Docker and orchestrated through Docker Compose.

This guide describes the local production-style deployment environment. Public cloud deployment and HTTPS hosting are not yet configured.

## Architecture

ParcelFlow consists of four services:

| Service | Technology | Responsibility |
|---|---|---|
| Frontend | React + Nginx | User interface and API reverse proxy |
| Backend | Spring Boot + Java 21 | REST API, authentication, shipment management |
| Database | PostgreSQL 18 | Persistent application data |
| Messaging | Apache Kafka | Asynchronous shipment status events |

### Request Flow

1. A user opens the React frontend through Nginx.
2. Browser requests beginning with `/api/` are forwarded to Spring Boot.
3. Spring Boot validates JWT authentication and applies authorization rules.
4. Spring Boot reads and writes shipment information in PostgreSQL.
5. After a shipment status transaction commits, Spring Boot publishes a Kafka event.
6. The Kafka consumer processes the event asynchronously.

Containers communicate over the Docker Compose network using service names such as `backend`, `postgres`, and `kafka`.

## Prerequisites

- Docker Desktop with Docker Compose
- Git
- Sufficient memory to run all four containers

Java, Maven, and Node.js are not required directly on the host when building the application through the supplied Dockerfiles.

## Environment Configuration

Create a `.env` file in the project root.

Required variables:

- `POSTGRES_DB` — PostgreSQL database name
- `POSTGRES_USER` — PostgreSQL application username
- `POSTGRES_PASSWORD` — PostgreSQL password
- `JWT_SECRET` — Base64-encoded JWT signing secret representing at least 32 random bytes

Never commit `.env` or real credentials to Git.

The backend uses the `prod` Spring profile inside Docker Compose. Configuration is supplied through environment variables.

For security, use new credentials for any public deployment.

## Persistent Storage

This project uses external Docker volumes:

- `parcelflow_postgres_data`
- `parcelflow_kafka_data`

On a new Docker host, create the volumes before starting Compose:

- `docker volume create parcelflow_postgres_data`
- `docker volume create parcelflow_kafka_data`

New database and Kafka volumes must be initialized using their corresponding service configuration. Creating empty volumes does not restore existing application data.

## Starting the Application

From the project root, run:

`docker compose -f compose.yaml config -q`

Then:

`docker compose -f compose.yaml up -d --build`

Check the services:

`docker compose -f compose.yaml ps`

Wait for PostgreSQL, Kafka, Spring Boot, and Nginx to become healthy.

## Accessing ParcelFlow

Frontend:

`http://localhost:5173`

Backend health endpoint:

`http://localhost:8080/actuator/health`

The frontend sends API requests through Nginx using the `/api` prefix.

The current Compose setup publishes development-facing ports to the host's loopback interface. This configuration is intended for local use.

## Monitoring and Troubleshooting

View all service logs:

`docker compose -f compose.yaml logs --tail=100`

View backend logs:

`docker compose -f compose.yaml logs --tail=100 backend`

Check running containers and health status:

`docker compose -f compose.yaml ps`

A healthy status indicates that the configured health probe is passing; it does not guarantee all application functionality is working.

## Stopping the Application

Run:

`docker compose -f compose.yaml down`

This stops and removes the Compose containers and network without deleting external Docker volumes.

Do not remove persistent volumes unless intentionally resetting application data.

## Continuous Integration and Container Images

GitHub Actions builds the Spring Boot backend, React frontend, and both Docker images.

After successful validation, the publishing workflow can push versioned images to GitHub Container Registry from the `master` branch.

Images use the naming pattern:

- `ghcr.io/OWNER/parcelflow-backend:TAG`
- `ghcr.io/OWNER/parcelflow-frontend:TAG`

Published tags include `latest` and the corresponding Git commit SHA.

Registry packages may require authentication if they are private.

### Security and Event Reliability

ParcelFlow uses JWT-based authentication and role-based
authorization, with Spring Security tests covering
authenticated, unauthenticated, and forbidden requests.

Shipment status updates publish application events after
the database transaction commits.

Current limitation: Kafka publishing failures are logged,
but events are not durably queued for automatic recovery.

Potential future improvements:

- Add a PostgreSQL transactional outbox.
- Implement reliable event retries and monitoring.
- Make Kafka consumers idempotent.
- Test real JWT signing, expiration, and tampering.
- Run security and integration tests in GitHub Actions.
- Review permission rules for shipment creation and other operations.

## Production Considerations

Before hosting ParcelFlow publicly, additional work is required:

- Configure HTTPS and a public-facing ingress or reverse proxy.
- Restrict backend, database, and Kafka network exposure.
- Implement controlled database migrations.
- Configure automated backups and recovery procedures.
- Review Kafka event-delivery reliability, including a possible transactional outbox.

The current environment is a local production-style simulation rather than a fully hardened production deployment.