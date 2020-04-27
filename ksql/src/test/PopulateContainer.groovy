import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.kafka.clients.*
import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.serialization.*
import groovy.transform.Field

@Field final String TEST_TOPIC="TOPIC_TO_POST"
@Field final String BOOTSTRAP_SERVER="localhost:9092"

def KafkaProducer<String, String> createKafkaProducer() {
    println("start setting producer properties")

    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.RETRIES_CONFIG, 0);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

    return new KafkaProducer<String, String>(props)
}

def kafkaProducer = createKafkaProducer()
String pathToLoad ="C:\\Code\\kafkaTestcontainers\\ksql\\src\\test\\resources\\afspraken.kafka"
def jsonLines =  new File(pathToLoad).text
def slurper = new JsonSlurper()
def json= slurper.parseText(jsonLines)


json.each { event ->
    kafkaProducer.send( new ProducerRecord<String, String>("Axon.IntegrationEvents", JsonOutput.toJson(event)) )
}


