#Pipeline Status : [![pipeline status](https://cygit.eu/public-projects/MicroservicesInfrastructrureSpringBootKafkaElasticSearch/badges/master/pipeline.svg)](https://cygit.eu/public-projects/MicroservicesInfrastructrureSpringBootKafkaElasticSearch/-/commits/master)
_________
# Getting Started
_________________
### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.3.4.RELEASE/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.3.4.RELEASE/maven-plugin/reference/html/#build-image)



# Running the application
- Please enter the correct credentials in twitter4j.properties file.
- Then run mvn install -DskipTests command
- Then go to Docker folder and run docker-compose up command to run kafka cluster and twitter-to-kafka-service together
- Check the pom.xml file and spring-boot-maven-plugin section in twitter-to-kafka-service, where we configure 
the build-image goal to create docker image with mvn install command
- Check the services.yml file under docker-compose folder which includes to compose definition 
for microservice, twitter-to-kafka-service