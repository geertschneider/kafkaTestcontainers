import groovy.util.logging.Slf4j
import io.confluent.ksql.function.udf.UdfDescription

@Slf4j
@UdfDescription(
        name = "Calculate forecast",
        description = """Calculate the forecast based on the incoming event.""")

class CallCenterForecaster {

    def createEmptyForecast(){
        return [IG:null,OG1:null,OG2:null]
    }

    Map<String,String> Forecast(String event,long eventTime,long timeInStateBeforePause){
        def forecast=createEmptyForecast()

        if(event=="InschattingOpgestart"){
            forecast.IG=eventTime+1000
            forecast.OG1=eventTime+2000
            forecast.OG2=eventTime+3000
        }
    }
}
