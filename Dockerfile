FROM eclipse-temurin:17-jdk

WORKDIR /app

RUN groupadd --system mystikos \
    && useradd --system --gid mystikos --home-dir /app --shell /usr/sbin/nologin mystikos \
    && mkdir -p /config \
    && chown -R mystikos:mystikos /app /config

USER mystikos

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8" \
    SPRING_CONFIG_ADDITIONAL_LOCATION="optional:file:/config/"

VOLUME ["/app", "/config"]

EXPOSE 8099

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/mystikos-app.jar"]
s