import groovy.util.logging.Slf4j
import io.confluent.ksql.function.udf.UdfDescription

import java.time.LocalDateTime
import java.time.LocalTime


@Slf4j
@UdfDescription(
        name = "Calculate forecast",
        description = """Calculate the forecast based on the incoming event.""")
class CallCenterForecaster {

    def createEmptyForecast(){
        return [Affected:false,IG:null,OG1:null,OG2:null]
    }

    public static AbstractForecastPredictor GetForecastPredictor(String event,LocalDateTime eventTime){

        switch (event.toUpperCase()) {
            case "INSCHATTINGOPGESTART":
                return new InschattingOpgestart(eventTime)
            case "INSCHATTINGBEEINDIGD":
                return new InschattingBeeindigd(eventTime)
            default:
                return new InosEventDoesNotAffectPrediction(eventTime)
        }
    }
    Map<String,String> Forecast(String event,long eventTime,long timeInStateBeforePause){
        def forecast=createEmptyForecast()

        if(event=="InschattingOpgestart"){
            forecast.Affected=true
            forecast.IG=eventTime+1000
            forecast.OG1=eventTime+2000
            forecast.OG2=eventTime+3000
        }
        return forecast
    }
}

abstract class AbstractForecastPredictor {
    public  LocalDateTime eventTime
    public  Tuple2<LocalDateTime,LocalDateTime> IG
    public Tuple2<LocalDateTime,LocalDateTime> OG1
    public Tuple2<LocalDateTime,LocalDateTime> OG2

    public final boolean PredictionsAffectedByEvent =true

    AbstractForecastPredictor(LocalDateTime eventTime){
        this.eventTime=eventTime
        CalculateIG()
        CalculateOG1()
        CalculateOG2()
    }

    static LocalDateTime GetStartDate(LocalDateTime date){
        return date.clearTime().plusNanos(0)
    }
    static LocalTime endOfDayTime =new LocalTime(23,59,59,999999999)
    static LocalDateTime GetEndDate(LocalDateTime date){
        return date.clearTime().plusNanos(endOfDayTime.toNanoOfDay())
    }
    static Tuple2<LocalDateTime,LocalDateTime> GetStartAndEnd(LocalDateTime startDate,int period ){
        new Tuple2<LocalDateTime ,LocalDateTime>(GetStartDate(startDate),GetEndDate(startDate.plusDays(period)))
    }


    def abstract void  CalculateIG()
    def abstract void  CalculateOG1()
    def abstract void  CalculateOG2()


}
class InosEventDoesNotAffectPrediction extends AbstractForecastPredictor{
    InosEventDoesNotAffectPrediction(Date eventTime) {
        super(eventTime)
        PredictionsAffectedByEvent=false
    }

    @Override
    void CalculateIG() {

    }

    @Override
    void CalculateOG1() {

    }

    @Override
    void CalculateOG2() {

    }
}

class InschattingOpgestart extends AbstractForecastPredictor{

    InschattingOpgestart(LocalDateTime eventTime) {
        super(eventTime)
    }

    @Override
    void CalculateIG() {
        IG= GetStartAndEnd(eventTime.plusDays(35),14)
    }

    @Override
    void CalculateOG1() {
        OG1 = GetStartAndEnd(IG.getV1().plusDays(85),14)
    }

    @Override
    void CalculateOG2() {
        OG2 = GetStartAndEnd(OG1.getV1().plusDays(85),14)
    }
}



class InschattingsGesprekAlsOpdrachtGepland extends AbstractForecastPredictor{

    InschattingsGesprekAlsOpdrachtGepland(LocalDateTime eventTime) {
        super(eventTime)
    }

    @Override
    void CalculateIG() {
        IG=GetStartAndEnd(eventTime.plusDays(1),14)
    }

    @Override
    void CalculateOG1() {
        OG1=GetStartAndEnd(IG.getV1().plusDays(85),14)
    }

    @Override
    void CalculateOG2() {
        OG2=GetStartAndEnd(OG1.getV1().plusDays(85),14)
    }
}

class InschattingsGesprekAlsBellijstGepland extends AbstractForecastPredictor{

    InschattingsGesprekAlsBellijstGepland(LocalDateTime eventTime) {
        super(eventTime)
    }

    @Override
    void CalculateIG() {
        IG=GetStartAndEnd(eventTime.plusDays(0),14)
    }

    @Override
    void CalculateOG1() {
        OG1=GetStartAndEnd(IG.getV1().plusDays(85),14)
    }

    @Override
    void CalculateOG2() {
        OG2=GetStartAndEnd(OG1.getV1().plusDays(85),14)
    }
}

class InschattingBeeindigd extends AbstractForecastPredictor{

    InschattingBeeindigd(LocalDateTime eventTime) {
        super(eventTime)
    }

    @Override
    void CalculateIG() {

    }

    @Override
    void CalculateOG1() {

    }

    @Override
    void CalculateOG2() {

    }
}
