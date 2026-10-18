
## Running ParcelFlow Locally

### Prerequisites

- Docker Desktop
- Docker Compose
- Git

### Environment Configuration

Create a `.env` file in the project root with:

POSTGRES_DB=parcelflow
POSTGRES_USER=parcelflow
POSTGRES_PASSWORD=<your-local-database-password>
JWT_SECRET=<your-base64-encoded-jwt-secret>

Use a Base64-encoded secret containing at least 32 random bytes.

### Docker Volumes

This project uses externally managed Docker volumes.

Create them if they do not already exist:

```cmd
docker volume create parcelflow_postgres_data
docker volume create parcelflow_kafka_data
```

### Start the Application

```cmd
docker compose -f compose.yaml up -d --build
```

### Access the Application

Frontend: http://localhost:5173

Backend: http://localhost:8080

### Stop the Application

```cmd
docker compose -f compose.yaml down
```

This preserves the external PostgreSQL and Kafka volumes.

## Continuous Integration

GitHub Actions automatically validates:

- Spring Boot Maven build
- React TypeScript build
- Backend Docker image build
- Frontend Docker image build

The workflow runs on pushes and pull requests.
