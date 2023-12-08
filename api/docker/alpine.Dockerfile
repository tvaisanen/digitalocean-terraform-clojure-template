FROM clojure:openjdk-17-tools-deps-alpine
WORKDIR app
COPY . .

RUN clojure -P

CMD clj -X:run
