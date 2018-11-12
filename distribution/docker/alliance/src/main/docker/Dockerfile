FROM alpine as prep

RUN apk add --no-cache unzip
RUN mkdir /prep /zips
COPY maven/alliance-${project.version}.zip /zips
RUN unzip /zips/alliance-${project.version}.zip -d /prep
RUN mv /prep/alliance-${project.version} /prep/alliance

FROM codice/ddf-base:${docker.ddf.base.version}
LABEL maintainer=codice

ENV APP_HOME=/app
ENV APP_NAME=/alliance
ENV APP_LOG=$APP_HOME/data/log/$APP_NAME.log

COPY --from=prep /prep/alliance $APP_HOME

VOLUME $APP_HOME/data $APP_HOME/deploy $APP_HOME/etc

EXPOSE 8993 8181 8101
