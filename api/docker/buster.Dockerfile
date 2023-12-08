FROM clojure:openjdk-17-tools-deps-buster as base

WORKDIR app
COPY . .

RUN clojure -P

CMD clj -X:run


FROM gcr.io/distroless/java:17
