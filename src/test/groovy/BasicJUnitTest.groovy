import org.apache.kafka.clients.consumer.*
import org.apache.kafka.clients.producer.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.apache.kafka.common.serialization.*
import java.time.Duration

class BasicJUnitTest {
    @Container
    public static KafkaContainer kafkaContainer


    @BeforeAll
    public static void setup(){
        println("starting setup")
        kafkaContainer = new KafkaContainer()
        kafkaContainer.start()

        println("setup done")
    }


    def KafkaConsumer<String, String> createKafkaConsumer(String topic) {
        println("start setting producer properties")

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        props.setProperty("group.id","testgroup")
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        return new KafkaConsumer<>(props)

    }


    def KafkaProducer<String, String> createKafkaProducer() {
        println("start setting producer properties")

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new KafkaProducer<String, String>(props)
    }


    @Test
    public void TheTest(){
        def kafkaProducer= createKafkaProducer()
        int amountOfRecords =20
        1.upto(amountOfRecords,{
            kafkaProducer.send(new ProducerRecord<>("postToTopic","${it}".toString(),"post ${it}".toString()))
        })

        def kafkaConsumer=createKafkaConsumer()
        kafkaConsumer.subscribe(["postToTopic"])

        ConsumerRecords<String,String> consumedRecords
        int i=0
        while (i  <= 10) {
            consumedRecords= kafkaConsumer.poll(Duration.ofSeconds(1))
            if(consumedRecords!=null)
                break
        }
        consumedRecords.each(cr -> println ( "$cr.key  - $cr.value" ))
        Assertions.assertTrue( consumedRecords.count()==amountOfRecords)

    }
}
