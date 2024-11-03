FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu

WORKDIR /app

COPY  SyslogSender5424.jar SyslogSender5424.jar

EXPOSE 54527

ENTRYPOINT ["java", "-jar", "SyslogSender5424.jar"]
