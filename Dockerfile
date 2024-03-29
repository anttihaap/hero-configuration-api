FROM amazoncorretto:11 AS installer

WORKDIR /install

RUN curl -L https://www.scala-sbt.org/sbt-rpm.repo > sbt-rpm.repo && \
    mv sbt-rpm.repo /etc/yum.repos.d/

RUN yum install -y sbt && \
    yum clean all

FROM installer AS builder

WORKDIR /app

COPY . .

RUN sbt compile

FROM amazoncorretto:11

WORKDIR /app

COPY --from=builder /usr/bin/sbt /usr/bin/sbt
COPY --from=builder /app .

CMD ["sbt", "run"]