# FROM clojure:openjdk-17-tools-deps-alpine

# RUN apk add --update nodejs npm
# WORKDIR /app

# COPY . .

# RUN npm install
# RUN npx shadow-cljs release app

# RUN rm -r node_modules

# RUN clojure -P

# CMD clojure -X:run

FROM clojure:openjdk-17-tools-deps-alpine AS frontend-build
RUN apk add --update nodejs npm
WORKDIR /app
COPY  . .
RUN npm install
RUN npx shadow-cljs release app

# Use a base without node dependencies for serving
FROM clojure:openjdk-17-tools-deps-alpine
WORKDIR /app
COPY . .
RUN ls /app
COPY --from=frontend-build /app/compiled-resources /app/compiled-resources
RUN clojure -P
CMD clojure -X:run
