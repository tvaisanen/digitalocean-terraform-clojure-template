FROM clojure:openjdk-17-tools-deps-alpine

RUN apk update; apk add postgresql

WORKDIR app
COPY . .
RUN clojure -P
CMD clojure -X:run
