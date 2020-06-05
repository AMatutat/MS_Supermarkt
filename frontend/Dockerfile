# build stage
FROM openjdk:8-jdk as build-stage

RUN \
  apt-get update && \
  apt-get install -y --no-install-recommends sudo && \
  apt-get install -y --no-install-recommends curl ca-certificates apt-transport-https && \
  apt-get install -y --no-install-recommends bzip2 bsdtar build-essential python git wget

ENV SBT_VERSION 1.3.10

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt


WORKDIR /build 
COPY build.sbt .
COPY src/ src/
COPY project/ project/



#RUN sbt 
RUN sbt fastOptJS 



# production stage
FROM nginx:stable-alpine as production-stage

WORKDIR /usr/share/nginx/html
RUN mkdir target
COPY html/index.html .
COPY html/main.css .
COPY --from=build-stage /build/target/ /usr/share/nginx/html/target
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]