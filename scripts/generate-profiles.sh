#!/bin/bash

# Script to generate Spring Boot profile configurations for all services
# This creates application-local.yml, application-uat.yml, and application-prod.yml files

# Service configurations: service_name:port:db_name:has_file_upload
SERVICES=(
  "listing-service:8083:listing_db:true"
  "receipt-service:8082:receipt_db:false"
  "settlement-service:8085:settlement_db:false"
  "analytics-service:8086:analytics_db:false"
  "admin-service:8087:admin_db:false"
  "chat-service:8090:chat_db:false"
  "customer-service:8089:customer_db:false"
)

for service_config in "${SERVICES[@]}"; do
  IFS=':' read -r service port db_name has_file_upload <<< "$service_config"

  service_dir="../${service}/src/main/resources"

  echo "Generating profiles for ${service}..."

  # Generate application-local.yml
  cat > "${service_dir}/application-local.yml" <<EOF
spring:
  application:
    name: ${service^^}

  datasource:
    url: jdbc:postgresql://localhost:5432/${db_name}
    username: postgres
    password: manasp1977
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  flyway:
    enabled: true
    baseline-on-migrate: true
    validate-on-migrate: false
    locations: classpath:db/migration

server:
  port: ${port}

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    fetch-registry: true
    register-with-eureka: true
  instance:
    prefer-ip-address: true
    instance-id: \${spring.application.name}:\${spring.application.instance_id:\${random.value}}

jwt:
  secret: \${JWT_SECRET:dGhpcy1pcy1hLXNlY3JldC1rZXktZm9yLWRldmVsb3BtZW50LW9ubHktY2hhbmdlLWluLXByb2R1Y3Rpb24=}
  expiration: 86400000

feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
        loggerLevel: basic

logging:
  level:
    root: INFO
    com.ansh.${service%%-*}: DEBUG
    org.hibernate.SQL: DEBUG
EOF

  # Add file upload configuration if needed
  if [ "$has_file_upload" = "true" ]; then
    cat >> "${service_dir}/application-local.yml" <<EOF

file:
  upload-dir: \${FILE_UPLOAD_DIR:uploads/images}
  base-url: \${FILE_BASE_URL:http://localhost:${port}/images}

spring.servlet.multipart:
  enabled: true
  max-file-size: 5MB
  max-request-size: 10MB
EOF
  fi

  # Generate application-uat.yml
  cat > "${service_dir}/application-uat.yml" <<EOF
spring:
  application:
    name: ${service^^}

  datasource:
    url: jdbc:postgresql://postgres:5432/${db_name}
    username: anshshare_admin
    password: AnshShare2024!
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  flyway:
    enabled: true
    baseline-on-migrate: true
    validate-on-migrate: false
    locations: classpath:db/migration
    user: anshshare_admin
    password: AnshShare2024!

server:
  port: ${port}

eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
    fetch-registry: true
    register-with-eureka: true
  instance:
    prefer-ip-address: true
    instance-id: \${spring.application.name}:\${spring.application.instance_id:\${random.value}}

jwt:
  secret: \${JWT_SECRET:dGhpcy1pcy1hLXNlY3JldC1rZXktZm9yLWRldmVsb3BtZW50LW9ubHktY2hhbmdlLWluLXByb2R1Y3Rpb24=}
  expiration: 86400000

feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
        loggerLevel: basic

logging:
  level:
    root: INFO
    com.ansh.${service%%-*}: DEBUG
EOF

  # Add file upload configuration if needed
  if [ "$has_file_upload" = "true" ]; then
    cat >> "${service_dir}/application-uat.yml" <<EOF

file:
  upload-dir: \${FILE_UPLOAD_DIR:/app/uploads/images}
  base-url: \${FILE_BASE_URL:http://3.16.24.182:${port}/images}

spring.servlet.multipart:
  enabled: true
  max-file-size: 5MB
  max-request-size: 10MB
EOF
  fi

  # Generate application-prod.yml
  cat > "${service_dir}/application-prod.yml" <<EOF
spring:
  application:
    name: ${service^^}

  datasource:
    url: \${DB_URL:jdbc:postgresql://prod-db-host:5432/${db_name}}
    username: \${DB_USERNAME:prod_admin}
    password: \${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: false

  flyway:
    enabled: true
    baseline-on-migrate: true
    validate-on-migrate: true
    locations: classpath:db/migration
    user: \${DB_USERNAME:prod_admin}
    password: \${DB_PASSWORD}

server:
  port: ${port}

eureka:
  client:
    service-url:
      defaultZone: \${EUREKA_URL:http://prod-eureka-server:8761/eureka/}
    fetch-registry: true
    register-with-eureka: true
  instance:
    prefer-ip-address: true
    instance-id: \${spring.application.name}:\${spring.application.instance_id:\${random.value}}

jwt:
  secret: \${JWT_SECRET}
  expiration: 86400000

feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
        loggerLevel: basic

logging:
  level:
    root: WARN
    com.ansh.${service%%-*}: INFO
EOF

  # Add file upload configuration if needed
  if [ "$has_file_upload" = "true" ]; then
    cat >> "${service_dir}/application-prod.yml" <<EOF

file:
  upload-dir: \${FILE_UPLOAD_DIR:/app/uploads/images}
  base-url: \${FILE_BASE_URL:https://api.splitsavers.com/images}

spring.servlet.multipart:
  enabled: true
  max-file-size: 5MB
  max-request-size: 10MB
EOF
  fi

  echo "✓ Generated profiles for ${service}"
done

echo ""
echo "All profile configurations generated successfully!"
