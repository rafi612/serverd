FROM eclipse-temurin:17-jdk as build

WORKDIR /workspace/app

COPY . .

RUN ./mvnw package -DskipTests -Dmaven.javadoc.skip=true -Dmaven.source.skip=true

FROM eclipse-temurin:17-jre

VOLUME /app/data

WORKDIR /app

COPY --from=build /workspace/app/target/serverd-*.jar /app

EXPOSE 9999/tcp
EXPOSE 9998/udp

ENTRYPOINT ["java","-cp","*","com.serverd.main.Main","--working-loc","/app/data"]
