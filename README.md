# Budget Aggregator

A scalable personal budget aggregator designed to import and analyze bank transactions. Leveraging an event-driven architecture with RabbitMQ and MongoDB, the system efficiently handles large CSV datasets asynchronously. It transforms raw data into detailed spending statistics per category and IBAN, providing valuable insights for financial planning."

## Key Features

* **Event-Driven Architecture**: Decoupled file upload and processing phases using RabbitMQ.
* **Batch Processing**: Efficient bulk writes to MongoDB to handle large datasets.
* **Idempotency**: Duplicate detection logic ensures data consistency even if the same file is uploaded twice.
* **Aggregation Engine**: Utilizes MongoDB Aggregation Framework for calculating monthly spending statistics.
* **High Performance**: Custom CSV streaming parser (OpenCSV) with `Spliterator` for low memory footprint.

## Tech Stack

* **Java 21**
* **Spring Boot 4.0.2**
    * Spring Web MVC
    * Spring Data MongoDB
    * Spring AMQP (RabbitMQ)
    * Spring Validation
* **MongoDB** (Database)
* **RabbitMQ** (Message Broker)
* **Gradle** (Build Tool)

---

## Getting Started

### Prerequisites

* JDK 21 installed
* Docker & Docker Compose (for infrastructure)

### 1. Start Infrastructure

Use Docker Compose to start MongoDB and RabbitMQ containers.

```bash
docker-compose up -d
```

### 2. Run the Application
```bash
./gradlew bootRun
```

*The application will start on http://localhost:8080*

---
## API Documentation

### 1. Upload Transactions (CSV)

Triggers an asynchronous import job. The file is validated and queued for processing.

* **URL**: `/api/v1/imports`
* **Method**: `POST`
* **Content-Type**: `multipart/form-data`

**Request Example:**

```bash
# Create a dummy csv file with 3 transactions (2x Groceries, 1x Entertainment)
# (In Git Bash / Linux / MacOS)
echo "transactionId,iban,amount,currency,date,category" > data.csv
echo "tx001,PL12345678901234567890123456,150.50,PLN,2024-01-15,Groceries" >> data.csv
echo "tx002,PL12345678901234567890123456,49.50,PLN,2024-01-16,Groceries" >> data.csv
echo "tx003,PL12345678901234567890123456,120.00,PLN,2024-01-20,Entertainment" >> data.csv

# Send request
curl -X POST -F "file=@data.csv" http://localhost:8080/api/v1/imports
```

**Response (202 Accepted):**

```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 2. Check Import Status

Check the progress and final status of the import job.

> **Note:** You need to copy the `jobId` from the upload response (step 1) to query the status here.

* **URL**: `/api/v1/imports/{jobId}`
* **Method**: `GET`

**Request Example:**

```bash
curl http://localhost:8080/api/v1/imports/550e8400-e29b-41d4-a716-446655440000
```

**Response:**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "createdAt": "2024-02-03T10:00:00Z",
  "finishedAt": "2024-02-03T10:00:05Z",
  "errorReport": []
}
```

### 3. Get Monthly Statistics

Retrieve aggregated statistics grouped by category and currency for a specific month.

* **URL**: `/api/v1/statistics`
* **Method**: `GET`
* **Query Params**:
* `year` (int, required)
* `month` (int, required)
* `iban` (string, optional) - Filter by specific bank account



**Request Example:**

```bash
curl "http://localhost:8080/api/v1/statistics?year=2024&month=1"
```

**Response:**

The response aggregates data from the CSV uploaded in Step 1. Notice how "Groceries" are summed up.

```json
{
  "year": 2024,
  "month": 1,
  "iban": null,
  "stats": [
    {
      "category": "Groceries",
      "currency": "PLN",
      "totalAmount": 200.00,
      "transactionCount": 2
    },
    {
      "category": "Entertainment",
      "currency": "PLN",
      "totalAmount": 120.00,
      "transactionCount": 1
    }
  ]
}
```
## Configuration

The application is pre-configured to work with the Docker infrastructure provided.

* **MongoDB URL**: `mongodb://root:example@localhost:27017/budget`
* **RabbitMQ**: `localhost:5672` (User: `user`, Pass: `password`)

To change these settings, update `src/main/resources/application.properties`.

---

## Application Screenshots

### 1. Uploading a CSV File
*Sending a POST request to trigger the import process.*
![Upload CSV](screenshots/upload_response.png)

### 2. Checking Job Status
*Verifying that the background processing is completed.*
![Job Status](screenshots/status_check_postman.png)

![Job Status DB](screenshots/status_check_dbeaver.png)

*Completed with errors*
![Job Status Completed with Errors](screenshots/completed_with_errors_check_status.png)

*Import failed*
![Import failed](screenshots/failed_check_status.png)

### 3. Aggregated Monthly Statistics
*Result of MongoDB aggregation showing grouped expenses.*
![Statistics JSON](screenshots/get_aggregated_statistics.png)

---

## Stopping the System

To stop the Spring Boot application, simply press `Ctrl + C` in the terminal.

To stop and remove the background infrastructure containers (MongoDB & RabbitMQ):

```bash
docker-compose down