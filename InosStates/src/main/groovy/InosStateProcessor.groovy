package be.vdab.vdp

import be.vdab.vdp.InosEvent
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate

@SpringBootApplication
class InosStateProcessor {

    static void main(String... args){
        SpringApplication.run(InosStateProcessor.class,args)
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "STR_INOS_EVENTS_RAW", groupId = "myGroup3")
    public void ConsumeInosRawEvents(String jsonMessage){

//        JsonSlurper slurper = new JsonSlurper()
//        def fromJSON= slurper.parseText(jsonMessage)

//        InosEvent ie = new InosEvent(inosEvent.IKL,inosEvent.EVENTNAME)
//        ProduceOutput(ie)
        println("got following message ${jsonMessage}")
//        JsonSlurper slurper = new JsonSlurper()
//        def fromJSON= slurper.parseText(jsonMessage)

    }

    void ProduceOutput(InosEvent inosEvent){
        println("sending to kafka")
        this.kafkaTemplate.send("springOutput" , inosEvent.IKL.toString(),JsonOutput.toJson(inosEvent))
    }



//
//    @StreamListener(Sink.INPUT)
//    void getIntegrationEvents(LogMessage inputMessage){
//        print("message ${inputMessage}")
//    }


}

