FROM amazonlinux:2

RUN yum -y update \
    && yum install -y tar unzip gzip bzip2-devel ed gcc gcc-c++ gcc-gfortran \
    less libcurl-devel openssl openssl-devel readline-devel xz-devel \
    zlib-devel glibc-static libcxx libcxx-devel llvm-toolset-7 zlib-static \
    && rm -rf /var/cache/yum

ENV GRAAL_VERSION 22.2.0
ENV GRAAL_FOLDERNAME graalvm-ce-java17-${GRAAL_VERSION}
ENV GRAAL_FILENAME graalvm-ce-java17-linux-amd64-${GRAAL_VERSION}.tar.gz
RUN curl -4 -L https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-${GRAAL_VERSION}/${GRAAL_FILENAME} | tar -xvz
RUN mv $GRAAL_FOLDERNAME /usr/lib/graalvm
RUN rm -rf $GRAAL_FOLDERNAME

# Gradle
ENV GRADLE_VERSION 7.4.2
ENV GRADLE_FOLDERNAME gradle-${GRADLE_VERSION}
ENV GRADLE_FILENAME gradle-${GRADLE_VERSION}-bin.zip
RUN curl -LO https://downloads.gradle-dn.com/distributions/gradle-${GRADLE_VERSION}-bin.zip
RUN unzip gradle-${GRADLE_VERSION}-bin.zip
RUN mv $GRADLE_FOLDERNAME /usr/lib/gradle
RUN rm -rf $GRADLE_FOLDERNAME

VOLUME /app
WORKDIR /app

RUN /usr/lib/graalvm/bin/gu install native-image
RUN ln -s /usr/lib/graalvm/bin/native-image /usr/bin/native-image
RUN ln -s /usr/lib/gradle/bin/gradle /usr/bin/gradle

ARG JAR_FILE
ADD ${JAR_FILE} /app/app.jar
#RUN export JAR_NAME=/app/$(basename *.jar)

ENV JAVA_HOME /usr/lib/graalvm

ENTRYPOINT ["native-image"]