@echo off
echo 🚀 Starting TalentStream...
echo ================================================

docker-compose up --build -d

echo.
echo ⏳ Waiting for services to be healthy...
timeout /t 30 /nobreak

echo.
echo ✅ Services Status:
docker-compose ps

echo.
echo 📊 Access Points:
echo   - Eureka Dashboard:    http://localhost:8761
echo   - API Gateway:         http://localhost:8080
echo   - RabbitMQ Management: http://localhost:15672 (guest/guest)
echo   - Auth Service:        http://localhost:8081
echo   - Job Service:         http://localhost:8082
echo   - Notification Service: http://localhost:8083
echo.
echo 🎉 Platform is ready!
pause