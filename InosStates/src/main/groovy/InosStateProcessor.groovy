package be.vdab.vdp.InosStates

import be.vdab.vdp.InosStates.InosEvent
import groovy.json.JsonSlurper
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.ValueTransformer
import org.apache.kafka.streams.kstream.ValueTransformerSupplier
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.StoreBuilder
import org.apache.kafka.streams.state.Stores
import org.springframework.boot.autoconfigure.SpringBootApplication

import java.time.Duration

@SpringBootApplication
class InosStateProcessor {

    static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "inos-processor");
        props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, 'kafka01-mlb1abt.ops.vdab.be:9092,kafka02-mlb1abt.ops.vdab.be:9092,kafka03-mlb1abt.ops.vdab.be:9092');
        props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());


        ValueTransformerSupplier<String,  Iterable<String>> valueTransformerSupplier =getInosTransforer()

        StreamsBuilder builder = new StreamsBuilder();

        StoreBuilder storeBuilder = Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore("IKL"),Serdes.Integer(),Serdes.String());
        builder.addStateStore(storeBuilder);

        KStream<String,String> stream = builder.stream("STR_INOSEVENTS_RAW", Consumed.with(Topology.AutoOffsetReset.EARLIEST));

        stream.flatTransformValues(valueTransformerSupplier,"IKL")
                .to("OUT_INOS_STATECHANGES");

        Topology topo = builder.build();
        KafkaStreams streams =new KafkaStreams(topo,props);

streams.cleanUp()
        streams.start()

        Runtime.getRuntime().addShutdownHook({
            println("closing stream")
            streams.close(Duration.ofSeconds(10))
        })
    }

    static def ValueTransformerSupplier <String,Iterable<String>> getInosTransforer() {
        new ValueTransformerSupplier<String, Iterable<String>>() {

            @Override
            ValueTransformer<String, Iterable<String>> get() {
                return new ValueTransformer<String, Iterable<String>>() {
                    private KeyValueStore<Integer, String> store
                    def jsonGenerator = new groovy.json.JsonGenerator.Options()
                            .excludeFieldsByName("defaultEndDate")
                            .build()

                    public
                    @Override
                    void init(ProcessorContext context) {
                        store = (KeyValueStore<Integer, String>) context.getStateStore("IKL")

                    }

                    @Override
                    Iterable<String> transform(String value) {

                        JsonSlurper slurper = new JsonSlurper()
                        def fromJSON = slurper.parseText(value)
                        def transFomredValues = []
                        int IKL = fromJSON.IKL

                        try{


                        def currentInosEvent = new InosEvent(IKL, fromJSON.EVENTIDENTIFIER, fromJSON.EVENTNAME, fromJSON.EVENTTIME, fromJSON.PAYLOAD)
                        currentInosEvent.setEndDate()
                        transFomredValues << jsonGenerator.toJson(currentInosEvent)
                        def prevEventText = store.get(IKL)
                        if (prevEventText != null) {
                            def prevJson = slurper.parseText(prevEventText)
                            def prevInosEvent = new InosEvent(IKL, prevJson.eventIdentifier, prevJson.eventName, prevJson.startDate, prevJson.endDate, prevJson.payload)
                            prevInosEvent.setEndDate(currentInosEvent.startDate)
                            transFomredValues << jsonGenerator.toJson(prevInosEvent)
                        }

                        store.put(IKL, transFomredValues[0])
                        }
                        catch(Exception ex){
                            println("Failed to process IKL : ${IKL}")
                        }
                        return transFomredValues

                    }
                    @Override
                    void close() {

                    }
                }
            }

        }
    }
}
