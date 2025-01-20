#
# Dockerfile
# ---------------------------------
# Copyright (c) 2024
# RESOLVE Software Research Group
# School of Computing
# Clemson University
# All rights reserved.
# ---------------------------------
# This file is subject to the terms and conditions defined in
# file 'LICENSE.txt', which is part of this source code package.
#

# Use the official Ubuntu base image
FROM ubuntu:latest

# Install dependencies
RUN apt-get update && \
    apt-get install -y wget curl gnupg2 software-properties-common git && \
    wget -O- https://apt.corretto.aws/corretto.key | apt-key add - && \
    add-apt-repository 'deb https://apt.corretto.aws stable main' && \
    apt-get update && \
    apt-get install -y java-17-amazon-corretto-jdk maven && \
    java -version && mvn -version

# Create Maven cache volume
VOLUME ["/app/.m2"]

# Set the working directory
WORKDIR /app

# Copy the pom.xml file first (to utilize Docker's build cache for dependencies)
COPY pom.xml /app/

# Clear and cache Maven dependencies
RUN mvn dependency:go-offline

# Copy the rest of the project files
COPY . /app

# Default command
CMD ["bash"]
