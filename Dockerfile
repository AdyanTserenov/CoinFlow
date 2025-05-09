# Используем официальный образ OpenJDK
FROM openjdk:17-jdk

# Указываем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем jar-файл приложения
COPY target/CoinFlow-0.0.1-SNAPSHOT.jar app.jar

# Открываем порт 8080
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
