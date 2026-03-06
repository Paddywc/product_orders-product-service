# Product Service
The Product Service manages the product catalog for the Product Orders Platform.
It provides APIs for retrieving product information and maintaining product data such as price, name, and description.

The service acts as the source of truth for product metadata, including pricing, product details, and availability status.

## Responsibilities

- Create and manage product catalog entries
- Publish product creation events to Kafka
- Provide public product query endpoints
- Cache product views in Redis for read performance

## Architecture Role

The Product Service owns the product catalog and provides read and write APIs for product data.

- **Exposes product APIs** for catalog browsing and management
- **Publishes product events** on product creation, allowing the inventory service to create an inventory record
- **Caches product views** for fast reads

## Tech Stack

| Technology      | Purpose               |
|-----------------|-----------------------|
| Java 17         | Runtime               |
| Spring Boot     | Application framework |
| Spring Data JPA | Database access       |
| Kafka           | Event messaging       |
| MySQL           | Product storage       |
| Flyway          | Database migrations   |
| Redis           | Read caching          |
| Docker          | Containerization      |

## Environment Variables

An example list of environment variables is found in [`.env.example`](.env.example).

## Running the Service

Run the service using `docker-compose up --build` from [the root directory](../). To run this service in isolation, copy
the product service and mysql from the root [docker-compose](../docker-compose.yaml) file and run them separately. The
service will be available on port 8082.

## API Methods

Base path: `/products`

### 1) Create product

- **Method:** `POST`
- **Path:** `/products`
- **Description:** Creates a new product record.
- **Response:** `201 Created`

Example request:

{
"name": "Wireless Mouse",
"description": "Ergonomic wireless mouse",
"priceUSDCents": 2999,
"category": "ELECTRONICS",
"status": "ACTIVE"
}

Example response:

{
"productId": "<product-uuid>"
}

### 2) Change product price

- **Method:** `PATCH`
- **Path:** `/products/{productId}/price`
- **Description:** Updates the product price (USD cents).
- **Path parameter:**
  - `productId` (UUID) -- Product identifier
- **Response:** `204 No Content`

Example request:

{
"priceUSDCents": 3499
}

### 3) Deactivate product

- **Method:** `PUT`
- **Path:** `/products/{productId}/deactivate`
- **Description:** Marks a product as inactive.
- **Path parameter:**
  - `productId` (UUID) -- Product identifier
- **Response:** `204 No Content`

### 4) Activate product

- **Method:** `PUT`
- **Path:** `/products/{productId}/activate`
- **Description:** Marks a product as active.
- **Path parameter:**
  - `productId` (UUID) -- Product identifier
- **Response:** `204 No Content`

### 5) Get product by ID

- **Method:** `GET`
- **Path:** `/products/{productId}`
- **Description:** Returns details for one product.
- **Path parameter:**
  - `productId` (UUID) -- Product identifier
- **Response:** `200 OK`

Example response:

{
"productId": "<product-uuid>",
"name": "Wireless Mouse",
"description": "Ergonomic wireless mouse",
"priceUSDCents": 2999,
"category": "ELECTRONICS",
"status": "ACTIVE"
}

### 6) List active products

- **Method:** `GET`
- **Path:** `/products`
- **Description:** Returns active products, optionally filtered by category.
- **Query parameters:**
  - `category` (optional, enum) -- Filter by category (e.g., `ELECTRONICS`)
  - `page` (optional) -- Page number
  - `size` (optional) -- Page size
  - `sort` (optional) -- Sort property (default `name,asc`)
- **Response:** `200 OK`

Example response:

[
{
"productId": "<product-uuid>",
"name": "Wireless Mouse",
"description": "Ergonomic wireless mouse",
"priceUSDCents": 2999,
"category": "ELECTRONICS",
"status": "ACTIVE"
}
]

## Database Schema

#### `product`

- `product_id` `binary(16)` -- **PK**
- `product_name` `varchar(255)` -- not null
- `product_description` `varchar(2000)` -- nullable
- `price_usc_cents` `bigint(20)` -- not null
- `product_category` `enum('BOOKS','CLOTHING','ELECTRONICS','FOOD','HOME')` -- not null
- `product_status` `enum('ACTIVE','INACTIVE')` -- not null
- `created_at` `datetime(6)` -- not null
- `updated_at` `datetime(6)` -- not null
- `version` `bigint(20)` -- nullable (optimistic locking/versioning)

## Notes on security

Protected endpoints expect a JWT:

- `Authorization: Bearer <token>`

The JWT signature is verified using the Auth Service's JWKS endpoint.
