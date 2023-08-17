# docker build --no-cache --progress=plain -f .gitpod.Dockerfile .
FROM gitpod/workspace-full

# OS Packages
RUN bash -c "sudo install-packages gettext htop net-tools mariadb-client"

# Java
ARG JAVA_VERSION "20.0.2-graalce"
RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh \
    && sdk install java $JAVA_VERSION \
    && sdk default java $JAVA_VERSION \
    && sdk install quarkus \
    && sdk install maven\
    "

