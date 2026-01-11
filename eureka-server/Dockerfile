# Runtime with JRE only
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the pre-built JAR file
COPY target/eureka-server-*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

USER spring:spring

# Health check for container orchestration
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8761/actuator/health || exit 1

# Expose the service port
EXPOSE 8761

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
