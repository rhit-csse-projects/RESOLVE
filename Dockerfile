# Use the official Ubuntu base image
FROM ubuntu:latest

# Install dependencies
RUN apt-get update && \
    apt-get install -y wget curl gnupg2 software-properties-common git

# Install Java 17
RUN wget -O- https://apt.corretto.aws/corretto.key | apt-key add - && \
    add-apt-repository 'deb https://apt.corretto.aws stable main' && \
    apt-get update && \
    apt-get install -y java-17-amazon-corretto-jdk

# Install Maven
RUN apt-get install -y maven

# Verify installations
RUN java -version && mvn -version

# Set the working directory
WORKDIR /app

# Copy the project files
COPY . .


# Default command
CMD ["bash"]