import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.serialization.*
import groovy.transform.Field

import java.time.LocalDateTime

@Field final String BOOTSTRAP_SERVER="localhost:9092"

def KafkaProducer<String, String> createKafkaProducer() {
    println("start setting producer properties")

    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.RETRIES_CONFIG, 5);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

    return new KafkaProducer<String, String>(props)
}

class InschattingEvent{
    def eventID=UUID.randomUUID()
    def eersteInschatting=UUID.randomUUID()
def ikl=new Random().nextInt(100000)
    LocalDateTime eventTime
    InschattingEvent(){
        def shiftDays = new Random().nextInt(40)*-1
        eventTime = LocalDateTime.now().plusDays(shiftDays)
    }

    @Override String toString() {
        return  """
{
"eventIdentifier":"${eventID}",
"eventType":"inos/InschattingOpgestart",
"revision":"1",
"timestamp":"${eventTime}Z",
"payload": {"omschrijving":"Zelfredzaam registreren nog niet mogelijk","datumSinds":"2020-01-01","ikl":${ikl} ,"eersteInschattingId":"${eersteInschatting}"}
}
"""
    }
}


def kafkaProducer = createKafkaProducer()
def amountOfEvents = new Random().nextInt(100)
0.upto(amountOfEvents,{
    def ev = new InschattingEvent()
    ProducerRecord<String,String> producerRecord= new ProducerRecord<String, String>( "Axon.IntegrationEvents",'inos', ev.toString())
    kafkaProducer.send( producerRecord )
})
kafkaProducer.close()
println(amountOfEvents)

