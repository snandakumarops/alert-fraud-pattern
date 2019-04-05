# Deposit-Retention

Start Kafka Brokers: zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties

kafka-server-start /usr/local/etc/kafka/server.properties

mvn clean install

java -jar target/deposit-retention-1.0.0.jar

Input topic: kafka-console-consumer --bootstrap-server localhost:9092 --topic event-input-stream --from-beginning

Output topic: kafka-console-consumer --bootstrap-server localhost:9092 --topic offer-output-stream --from-beginning
