FROM clojure:openjdk-17-tools-deps-slim-buster
WORKDIR app
COPY . .

RUN clojure -P

CMD clj -X:run
