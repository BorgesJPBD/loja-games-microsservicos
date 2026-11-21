@echo off
echo Ligando o RabbitMQ...
docker compose up -d

echo Esperando o RabbitMQ ficar pronto...
timeout /t 10 /nobreak >nul

echo Ligando os 3 microsservicos...
start "produto-service (8081)" /D "%~dp0produto-service" cmd /k mvnw.cmd spring-boot:run
start "pedido-service (8082)" /D "%~dp0pedido-service" cmd /k mvnw.cmd spring-boot:run
start "notificacao-service (8083)" /D "%~dp0notificacao-service" cmd /k mvnw.cmd spring-boot:run

echo Pronto! Aguarde aparecer "Started" nas 3 janelas.