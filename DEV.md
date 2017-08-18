# JmsTool

## Build
Run `./mvnw clean install`

## Run Tomcat with ActiveMQ
After build, run `docker-compose up --build`. Start [JmsTool](http://localhost:8080/jmstool) or [ActiveMQ Web Console](http://admin:admin@localhost:8161/admin/)

## Development server
Run `npm start` in frontend/src/main/frontend for a dev server with proxy to Spring Boot. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.
