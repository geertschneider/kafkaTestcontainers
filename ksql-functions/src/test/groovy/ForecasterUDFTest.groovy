import groovy.json.JsonSlurper
import org.junit.Assert
import org.junit.jupiter.api.*

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class ForecasterUDFTest {

    LocalTime endOfDay = new LocalTime(23,59,59,999999999)
    LocalTime beginOfDay = new LocalTime(0,0,0,0)

    def convertToObject = {text -> new JsonSlurper().parseText(text)}
    def fromEpoch = { l -> Instant.ofEpochMilli(l).toDate().toLocalDateTime() }
    def toEpoch ={LocalDateTime dt -> dt.toInstant(ZoneOffset.UTC).toEpochMilli()}

    @Test
    public void "Check if forecaster can be created"() {
        def forecaster = new ForecastGesprekken()
        Assert.assertNotNull(forecaster)
    }

    @Test
    public void "Check Convertion ToJSON"(){
        LocalDateTime testDate =  LocalDateTime.of(2020,1,1,17,20,54)

        def foreCaster = new ForecastGesprekken()
        String result = foreCaster.forecastGesprekken('InschattingOpgestart',toEpoch(testDate) ,0)
        //this checks if the json convertion is valid
        def parsedJSON=  convertToObject(result)
        Assert.assertNotNull(parsedJSON)
        println("""
        JSON RECEIVED :
        ${result}
        """)
        //check individual values of the json
        Assert.assertTrue('check parameter EventAffectsPredictions',parsedJSON.PredictionAffected)
        def IG=parsedJSON.IG
        def expectedDate = new LocalDateTime(new LocalDate(2020,2,5),beginOfDay)
        Assert.assertEquals('check start',toEpoch(expectedDate),IG.Begin)

        expectedDate = new LocalDateTime(new LocalDate( 2020,2,19),endOfDay)
        Assert.assertEquals('check start',toEpoch(expectedDate),IG.End)
    }
    @Test
    public void RealTest(){

        def foreCaster = new ForecastGesprekken()
        String result = foreCaster.forecastGesprekken('InschattingOpgestart',1577836800000)
        //this checks if the json convertion is valid
        println(result)
        def parsedJSON=  convertToObject(result)

        Assert.assertFalse('no results',result.isEmpty())
        println("JSON received : ${result}")

        Assert.assertNotNull('return not parsed',parsedJSON)

    }

//    @Test
//    public void "Check Convertion of NULL values to JSON"(){
//        LocalDateTime testDate =  LocalDateTime.of(2020,1,1,17,20,54)
//
//        def foreCaster = new CallCenterForecaster()
//        String result = foreCaster.forecast('xxx',toEpoch(testDate) ,0)
//        //this checks if the json convertion is valid
//        def parsedJSON=  convertToObject(result)
//        Assert.assertNotNull(parsedJSON)
//
//        //check individual values of the json
//        Assert.assertFalse('check parameter EventAffectsPredictions',parsedJSON.EventAffectsPredictions)
//        def IG=parsedJSON.IG
//        Assert.assertNull(IG)
//
//    }



}
