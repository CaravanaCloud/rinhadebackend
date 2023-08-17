# docker build --no-cache --progress=plain -f .gitpod.Dockerfile .
FROM gitpod/workspace-full

# OS Packages
RUN bash -c "sudo install-packages gettext htop net-tools mariadb-client"

# Java
RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh \
    && sdk install java 20.0.2-graalce \
    && sdk default java 20.0.2-graalce \
    && sdk install quarkus \
    && sdk install maven\
    "
