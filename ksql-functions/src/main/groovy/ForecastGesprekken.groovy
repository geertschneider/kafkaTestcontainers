import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import io.confluent.ksql.function.udf.Udf
import io.confluent.ksql.function.udf.UdfDescription
import io.confluent.ksql.function.udf.UdfParameter

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset


@Slf4j
@UdfDescription(
        name = "forecastGesprekken",
        description = "Calculate the forecast based on the incoming event.")
class ForecastGesprekken {


    def static AbstractForecastPredictor GetForecastPredictor(String event, LocalDateTime eventTime) {

        switch (event.toUpperCase()) {
            case "INSCHATTINGOPGESTART":
                return new InschattingOpgestart(eventTime)
            case "INSCHATTINGBEEINDIGD":
                return new InschattingBeeindigd(eventTime)
            case "INSCHATTINGSGESPREKALSOPDRACHTGEPLAND":
                return new InschattingsGesprekAlsOpdrachtGepland(eventTime)
            case "INSCHATTINGSGESPREKALSBELLIJSTGEPLAND":
                return new InschattingsGesprekAlsBellijstGepland(eventTime)
            default:
                return new InosEventDoesNotAffectPrediction(eventTime)
        }
    }
//    final SqlStruct outputSchema
//    final SqlStruct beingEndSchema
//TODO : replace by returning a sctructure or map instead of JSON
    @Udf(description = "calculate when calls will happen based on the inos event")
    public String forecastGesprekken(
            @UdfParameter(value = "event")  String event,
            @UdfParameter(value = "eventTime") long eventTime)
    {
        log.debug ("forecastGesprekken called with ${event} and ${eventTime}")
        return forecastGesprekken(event,eventTime,0l)
    }

    @Udf(description = "calculate when calls will happen based on the inos event. This by also taking a pauze period into account")
    public String forecastGesprekken(
            @UdfParameter(value = "event")  String event,
            @UdfParameter(value = "eventTime") long eventTime,
            @UdfParameter(value = "timeSpendinPauzeInPreviousStep") long timeInPauze){


    log.debug ("forecastGesprekken called with ${event} and ${eventTime} and pauze ${timeInPauze}")

        LocalDateTime eventTimeCasted = Instant.ofEpochMilli(eventTime).toDate().toLocalDateTime()
        def forecaster = GetForecastPredictor(event, eventTimeCasted)
        def map = forecaster.toMap()

    def ConvertToJson={  prediction ->
        def weekArray= prediction.StartEndWeeks.collect(date -> date.toInstant(ZoneOffset.UTC).toEpochMilli().toString()).join(',')
        """{"BeginPeriod":${prediction.StartEndDays.Begin},"EndPeriod":${prediction.StartEndDays.End},"OverlappingWeeks":[${weekArray}]}"""
    }

        //TODO:
        // replace by JSON functions instead of hand building this string
        // using json slurper gives issue when executing this on the server
        // java.lang.RuntimeException: Unable to load FastStringService

        def convertToString = {

        if (forecaster.EventAffectsPredictions)


            return '{'
                        .concat('"PredictionAffected":true,')
                        .concat('"IG":' + ConvertToJson(map.IG))
                        .concat(',"OG1":' + ConvertToJson(map.OG1))
                        .concat(',"OG2":' + ConvertToJson(map.OG2))
                    .concat('}')
                .replaceAll("\\s", "")
        else
        return '{"PredictionAffected":false}'
    }

        return convertToString()
}

//        """
//"OG1":{"BeginPeriod":${map.OG1.StartEndDays.Begin},"EndPeriod":${map.OG1.StartEndDays.End}},
//"OG2":{"BeginPeriod":${map.OG2.StartEndDays.Begin},"EndPeriod":${map.OG2.StartEndDays.End}}

}

abstract class AbstractForecastPredictor {
    public  LocalDateTime eventTime
    public  Tuple2<LocalDateTime,LocalDateTime> IG
    public Tuple2<LocalDateTime,LocalDateTime> OG1
    public Tuple2<LocalDateTime,LocalDateTime> OG2

    public boolean EventAffectsPredictions =true

    AbstractForecastPredictor(LocalDateTime eventTime){
        this.eventTime=eventTime
        CalculateIG()
        CalculateOG1()
        CalculateOG2()
    }

    static LocalDateTime GetStartDate(LocalDateTime date){
        return date.toLocalDate().atStartOfDay()
    }

    static LocalTime endOfDayTime =new LocalTime(23,59,59,999999999)

    static LocalDateTime GetEndDate(LocalDateTime date){
        return date.clearTime().plusNanos(endOfDayTime.toNanoOfDay())
    }
    static Tuple2<LocalDateTime,LocalDateTime> GetStartAndEnd(LocalDateTime startDate,int period ){
        new Tuple2<LocalDateTime ,LocalDateTime>(GetStartDate(startDate),GetEndDate(startDate.plusDays(period)))
    }
    static convertDates = { Tuple2<LocalDateTime,LocalDateTime>  input ->  [Begin:input.first.toInstant(ZoneOffset.UTC).toEpochMilli(), End:input.second.toInstant(ZoneOffset.UTC).toEpochMilli()]}

    static List<LocalDateTime>  getOverlappingWeeks(Tuple2<LocalDateTime,LocalDateTime> startEndDates){
        def startDate = startEndDates.getV1().toLocalDate()
        def endPeriod = startEndDates.getV2().toLocalDate()
        def firstMonday = startDate.plusDays(1 - DayOfWeek.from(startDate).value)
        List<LocalDateTime> firstDays=[]
        def nextMonday=firstMonday
        while(nextMonday<endPeriod){
            firstDays <<  nextMonday.atStartOfDay()
            nextMonday=nextMonday.plusWeeks(1)
        }
        return firstDays
    }


    def abstract void  CalculateIG()
    def abstract void  CalculateOG1()
    def abstract void  CalculateOG2()

    def Map<String,?> toMap(){

        return [
        PredictionAffected:true,
        IG:this.IG==null?'':[StartEndDays:AbstractForecastPredictor.convertDates(this.IG),StartEndWeeks:AbstractForecastPredictor.getOverlappingWeeks(this.IG)],
        OG1:this.OG1==null?'':[StartEndDays:AbstractForecastPredictor.convertDates(this.OG1),StartEndWeeks:AbstractForecastPredictor.getOverlappingWeeks(this.OG1)],
        OG2:this.OG2==null?'':[StartEndDays:AbstractForecastPredictor.convertDates(this.OG2),StartEndWeeks:AbstractForecastPredictor.getOverlappingWeeks(this.OG2)]
        ]
    }
}



class InosEventDoesNotAffectPrediction extends AbstractForecastPredictor{
    InosEventDoesNotAffectPrediction(LocalDateTime eventTime) {
        super(eventTime)
        EventAffectsPredictions=false
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
