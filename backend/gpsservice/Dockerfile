FROM openjdk:11-jdk
<<<<<<<< HEAD:backend/checkinservice/Dockerfile
WORKDIR /var/jenkins_home/workspace/checkin-service/backend/checkinservice
VOLUME /tmp
EXPOSE 8084
========
WORKDIR /var/jenkins_home/workspace/baebooreung/backend/gpsservice
VOLUME /tmp
# EXPOSE 8083
>>>>>>>> 26fc49013b6ad86fe353f97ee911b38c3042d2ac:backend/gpsservice/Dockerfile
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]
ENV TZ=Asia/Seoul
RUN apt-get install -y tzdata
