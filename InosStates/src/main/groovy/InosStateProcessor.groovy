package be.vdab.vdp

import org.apache.kafka.streams.processor.Processor
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink

@SpringBootApplication
@EnableBinding(Sink.class)
class InosStateProcessor {

    static void main(String... args){
        SpringApplication.run(InosStateProcessor.class,args)
    }



    @StreamListener(Sink.INPUT)
    void getIntegrationEvents(String inputMessage){
        print("message ${inputMessage}")
    }


}

