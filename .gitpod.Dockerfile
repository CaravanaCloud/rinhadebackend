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

# Gatling
ARG PKG_URL="https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/3.9.5/gatling-charts-highcharts-bundle-3.9.5-bundle.zip"
ARG PKG_TMP="/tmp/gatling.zip"
ARG PKG_PATH="$HOME/gatling/"
ARG PKG_DIR="gatling-charts-highcharts-bundle-3.9.5"
ARG PKG_LINK="3.9.5"
ARG PKG_BIN="bin/gatling.sh"
ARG PKG_INSTALL="/usr/local/bin/"
RUN bash -c "curl -lo ${PKG_TMP} ${PKG_URL} \
    && mkdir -p ${PKG_PATH} \
    && unzip ${PKG_TMP} -d ${PKG_PATH} \
    && ln -s "${PKG_PATH}/${PKG_DIR}" "${PKG_PATH}/${PKG_LINK}" \
    && sudo ln -sf "${PKG_PATH}/${PKG_LINK}/${PKG_BIN}" "${PKG_INSTALL}" \
    && rm ${PKG_TMP} \    
    "
