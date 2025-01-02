FROM --platform=$BUILDPLATFORM amazoncorretto:17-alpine3.20

RUN mkdir /app
RUN mkdir /config
VOLUME /app
VOLUME /config

ADD target/video-gateway.jar /app/video-gateway.jar

CMD java -jar /app/video-gateway.jar

EXPOSE 9025