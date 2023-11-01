FROM clojure:openjdk-17-tools-deps-buster

WORKDIR app
COPY . .

RUN clojure -P

CMD clj -X:run
