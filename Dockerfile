FROM ubuntu:20.04

ARG UID=1000
ARG GID=1000
ARG USERNAME="user"
ARG TZ="Asia/Taipei"

ENV INSTALLATION_TOOLS apt-utils \
    sudo \
    curl \
    wget \
    software-properties-common

ENV DEVELOPMENT_PACKAGES python3.8 \
    python3-pip \
    build-essential \
    valgrind \
    make \
    gdb \
    verilator \
    qemu-system-riscv32 \
    openjdk-8-jdk \
    sbt

ENV TOOL_PACKAGES bash \
    dos2unix \
    git \
    locales \
    nano \
    tree \
    vim \
    nano

ENV USER ${USERNAME}
ENV TERM xterm-256color
ENV APT_KEY_DONT_WARN_ON_DANGEROUS_USAGE DontWarn

# install system packages
RUN apt-get -qq update && \
    apt-get -qq install ${INSTALLATION_TOOLS} && \
    # prerequisite - git
    add-apt-repository ppa:git-core/ppa && \
    # prerequisite - sbt
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add 2> /dev/null && \
    # start install
    apt-get -qq update && \
    apt-get -qq upgrade && \
    apt-get -qq install ${DEVELOPMENT_PACKAGES} ${TOOL_PACKAGES}

# set env var JAVA_HOME
ENV JAVA_HOME "/usr/lib/jvm/java-8-openjdk-*"

# install sifive elf2hex (Verilog/Chisel friendly hex file generator)
ARG SIFIVE_ELF2HEX_URL=https://github.com/sifive/elf2hex/releases/download/v1.0.1/elf2hex-1.0.1.tar.gz
ENV RISCV "/opt/riscv"
RUN mkdir -p ${RISCV} && cd ${RISCV} && \
    wget -q ${SIFIVE_ELF2HEX_URL} && \
    tar -xvzpf elf2hex-1.0.1.tar.gz >> /dev/null && \
    cd elf2hex-1.0.1 && \
    ./configure --target=riscv64-unknown-elf && \
    make && \
    make install && \
    cd .. && rm -rf elf2hex-1.0.1.tar.gz elf2hex-1.0.1

# install RISC-V GNU Toolchain (x86_64 or Arm64 according to TARGETARCH)
# TODO: the pre-built executables should be moved to a new file server
# ARG RISCV_GNU_TOOLCHAIN_URL_X86_64="https://playlab.computing.ncku.edu.tw/downloads/riscv-gnu-toolchain/riscv64-elf-Linux-x86_64-65056bd.tar.gz"
# ARG RISCV_GNU_TOOLCHAIN_URL_ARM64="https://playlab.computing.ncku.edu.tw/downloads/riscv-gnu-toolchain/riscv64-elf-Linux-aarch64-65056bd.tar.gz"
# ARG TARGETARCH
# RUN cd ${RISCV} && \
#     mkdir "riscv-gnu-toolchain" && \
#     if [ [ "${TARGETARCH}" = "arm64" ] ]; then \
#     wget -q ${RISCV_GNU_TOOLCHAIN_URL_ARM64} -O "riscv-gnu-toolchain.tar.gz"; \
#     else \
#     wget -q ${RISCV_GNU_TOOLCHAIN_URL_X86_64} -O "riscv-gnu-toolchain.tar.gz"; \
#     fi && \
#     tar zxvf "riscv-gnu-toolchain.tar.gz" -C "riscv-gnu-toolchain" --strip-components 1 >> /dev/null && \
#     rm -rf "riscv-gnu-toolchain.tar.gz"
# ENV PATH=${PATH}:${RISCV}/riscv-gnu-toolchain/bin"

# install python libraries
COPY ./config/requirements.txt /tmp/requirements.txt
RUN pip3 install --upgrade pip && \
    pip3 install -r /tmp/requirements.txt && \
    rm /tmp/requirements.txt

# setup time zone
RUN ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime && echo ${TZ} > /etc/timezone

# add support of locale zh_TW
RUN sed -i 's/# en_US.UTF-8/en_US.UTF-8/g' /etc/locale.gen && \
    sed -i 's/# zh_TW.UTF-8/zh_TW.UTF-8/g' /etc/locale.gen && \
    sed -i 's/# zh_TW BIG5/zh_TW BIG5/g' /etc/locale.gen && \
    locale-gen && \
    dpkg-reconfigure --frontend=noninteractive locales && \
    update-locale LANG=en_US.UTF-8 && \
    update-locale LC_ALL=en_US.UTF-8
ENV LC_ALL en_US.UTF-8

# add non-root user account
RUN groupadd -o -g ${GID} ${USERNAME} && \
    useradd -u ${UID} -m -s /bin/bash -g ${GID} ${USERNAME} && \
    echo "${USERNAME} ALL = NOPASSWD: ALL" > /etc/sudoers.d/${USERNAME} && \
    chmod 0440 /etc/sudoers.d/${USERNAME} && \
    passwd -d ${USERNAME}

# add scripts and setup permissions
COPY --chown=${UID}:${GID} ./scripts/.bashrc /home/${USERNAME}/.bashrc
COPY --chown=${UID}:${GID} ./scripts/start.sh /docker/start.sh
COPY --chown=${UID}:${GID} ./scripts/login.sh /docker/login.sh
COPY --chown=${UID}:${GID} ./scripts/startup.sh /usr/local/bin/startup
RUN dos2unix -ic "/home/${USERNAME}/.bashrc" | xargs dos2unix && \
    dos2unix -ic "/docker/start.sh" | xargs dos2unix && \
    dos2unix -ic "/docker/login.sh" | xargs dos2unix && \
    dos2unix -ic "/usr/local/bin/startup" | xargs dos2unix && \
    chmod +x "/usr/local/bin/startup"

# user account configuration
RUN mkdir -p /home/${USERNAME}/.ssh && \
    mkdir -p /home/${USERNAME}/.vscode-server && \
    mkdir -p /home/${USERNAME}/projects
RUN chown -R ${UID}:${GID} /home/${USERNAME}

USER ${USERNAME}

WORKDIR /home/${USERNAME}

CMD [ "bash", "/docker/start.sh" ]
