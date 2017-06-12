# TransformX-Service-UCD-Trim

This project defines the code for generating UCD XML for super XML

This service runs on port :9018

To run the server ,enter into project folder and run

mvn spring-boot:run (or) java -jar *location of the jar file*

The above line will start the server at port 9018

If you want to change the port .Please start th server as mentioned below 

syntax : java -jar *location of the jar file* --server.port= *server port number*
 
example: java -jar target/UCDTrim.jar --server.port=9090

API to generate UCD XML(actualize/transformx/services/ucd/{version}/trim) with input as super XML

API to check the status of the service(actualize/transformx/services/ucd/{version}/ping); method :GET; 

syntax : *server address with port*/actualize/transformx/services/ucd/{version}/trim; method :POST; Header: Content-Type:application/xml

example: http://localhost:9018/actualize/transformx/services/ucd/{version}/trim ; method: POST; Header: Content-Type:application/xml