FROM gradle:jdk17 as builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle --no-daemon --stacktrace build
WORKDIR /home/gradle/src/build/distributions/
RUN tar -xvf baryonic-auth-backend.tar

FROM openjdk:17
COPY --from=builder /home/gradle/src/build/distributions/baryonic-auth-backend /app/baryonic-auth-backend
WORKDIR /app/baryonic-auth-backend/
CMD bin/baryonic-auth-backend
EXPOSE 80