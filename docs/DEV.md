# JmsTool

## Build
Run `./mvnw clean install` to build all
Run `./mvnw clean install -pl backend` to build only the backend


## Run Tomcat with ActiveMQ
After build, run `docker-compose up --build`. This will start Tomcat with deployed applicaiton and ActiveMQ server. Start [JmsTool](http://localhost:8080/jmstool) or [ActiveMQ Web Console](http://admin:admin@localhost:8161/admin/)

## Development server
Run `npm start` in frontend/src/main/frontend for a dev server with proxy to Spring Boot. Navigate to [http://localhost:4200/](http://localhost:4200/). The app will automatically reload if you change any of the source files.

## Development
Git flow with Atlassian's JGitFlow maven plugin is used as development model.

### Release
```
./mvnw jgitflow:release-start
./mvnw jgitflow:release-finish

#push changes on both branches
git push origin develop
git push origin master

#push all tags
git push --tags
```

