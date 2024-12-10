# Image de base pour exécuter l'application
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copier le JAR généré depuis Jenkins
COPY target/*.jar app.jar

# Exposer le port d'exécution (optionnel pour le moment)
EXPOSE 8080

# Commande pour exécuter l'application (si nécessaire plus tard)
ENTRYPOINT ["java", "-jar", "app.jar"]
