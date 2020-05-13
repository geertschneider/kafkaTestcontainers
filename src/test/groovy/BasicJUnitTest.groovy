import org.apache.kafka.clients.consumer.*
import org.apache.kafka.clients.producer.*
import org.junit.Assert
import org.junit.Rule
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.apache.kafka.common.serialization.*
import java.time.Duration
import org.testcontainers.containers.DockerComposeContainer

class BasicJUnitTest {
    private static String kafkaUrl
    private static String zoopekerUrl
    private static String schemaRegistryUrl
    private static String ksqlServeryUrl
    private static KafkaProducer kafkaProducer
    private static KafkaConsumer kafkaConsumer
    private final static String kafkaTopic = "Axon.IntegrationEvents"

    private static void createKafkaConsumer() {
        println("start setting consumer properties")

        String kafkaBootstrapServers = "PLAINTEXT://$kafkaUrl"

        Properties props = new Properties()
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers)
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        props.setProperty("group.id","testgroup")
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        kafkaConsumer = new KafkaConsumer<>(props)
    }

    private static void createKafkaProducer() {
        println("start setting producer properties")

        String kafkaBootstrapServers = "PLAINTEXT://$kafkaUrl"

        Properties props = new Properties()
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers)
        props.put(ProducerConfig.ACKS_CONFIG, "all")
        props.put(ProducerConfig.RETRIES_CONFIG, 0)
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class)
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class)

        kafkaProducer =  new KafkaProducer<String, String>(props)
    }

    static void createKafkaTopicWithTestMessages() {
        int amountOfRecords =20
        1.upto(amountOfRecords,{
            kafkaProducer.send(new ProducerRecord<>(kafkaTopic,"${it}".toString(),"post ${it}".toString()))
        })
    }

    static void consumeTestMessages() {
//      Read test messages of subscribed topic
        ConsumerRecords<String, String> consumedRecords
        int i=0
        while (i  <= 10) {
            consumedRecords= kafkaConsumer.poll(Duration.ofSeconds(1))
            if(consumedRecords!=null)
                break
        }

        consumedRecords.each{cr -> println ( "$cr.key  - $cr.value" )}
    }

    static void startKSQLpipeline() {
//      Execture gradle plugin to start KSQL task: pipilineExecute
        String command ="./gradlew pipelineExecute --pipeline-dir src/main/pipeline/ --rest-url http://$ksqlServeryUrl -i -s"
        Runtime rt = Runtime.getRuntime()
        Process proc = rt.exec(command)

//      Get sdtOut and stdErr from process
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()))
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()))

        // Read the output from the command
        System.out.println("\n======================\nHere is the standard output of the command:\n")
        String s = null
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s)
        }

        // Read any errors from the attempted command
        System.out.println("\n======================\nHere is the standard error of the command (if any):\n")
        while ((s = stdError.readLine()) != null) {
            System.out.println(s)
        }
    }

    @Rule
    public static DockerComposeContainer contCompose = new DockerComposeContainer(new File("docker-compose.yml"))
            .withExposedService("zookeeper_1", 2181)
            .withExposedService("kafka_1", 9092)
            .withExposedService("schema-registry_1", 8081)
            .withExposedService("ksql-server_1", 8088)

    @BeforeAll
    static void setup(){
        println("Start setup")
        contCompose.start()
        println("Containers defined in docker compose have started")

        kafkaUrl = contCompose.getServiceHost("kafka_1", 9092) + ":" + contCompose.getServicePort("kafka_1", 9092)
        zoopekerUrl = contCompose.getServiceHost("zookeeper_1", 2181) + ":" + contCompose.getServicePort("zookeeper_1", 2181)
        schemaRegistryUrl = contCompose.getServiceHost("schema-registry_1", 8081) + ":" + contCompose.getServicePort("schema-registry_1", 8081)
        ksqlServeryUrl = contCompose.getServiceHost("ksql-server_1", 8088) + ":" + contCompose.getServicePort("ksql-server_1", 8088)
        println("kafka_1 is running on:" + kafkaUrl)
        println("zookeeper_1 is running on:" + zoopekerUrl)
        println("schema-registry_1 is running on:" + schemaRegistryUrl)
        println("server_1 is running on:" + ksqlServeryUrl)

        createKafkaProducer()
        createKafkaTopicWithTestMessages()
        createKafkaConsumer()
        kafkaConsumer.subscribe([kafkaTopic])
        consumeTestMessages()
        startKSQLpipeline()
    }

    @Test
    void temp(){
        println("This is a temporary test which will automatically succeed")
        Assert.assertEquals(1,1)
    }



}
