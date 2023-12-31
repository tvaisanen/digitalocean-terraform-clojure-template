FROM clojure:openjdk-17-tools-deps-alpine
WORKDIR /web
COPY . .
RUN apk add --update nodejs npm
RUN npm install
RUN npx shadow-cljs compile app
RUN rm -r public/js/cljs-runtime
