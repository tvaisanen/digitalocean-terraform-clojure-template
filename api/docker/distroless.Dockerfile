FROM clojure:openjdk-17-tools-deps-buster as base

WORKDIR app
COPY . .
RUN clj -T:build uber

FROM gcr.io/distroless/java17-debian12
COPY --from=base /tmp/app/target/standalone.jar /tmp/app/standalone.jar

CMD ["/tmp/app/standalone.jar"]
