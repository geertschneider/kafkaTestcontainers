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
        def forecaster = new CallCenterForecaster()
        Assert.assertNotNull(forecaster)
    }

    @Test
    public void "Check Convertion ToJSON"(){
        LocalDateTime testDate =  LocalDateTime.of(2020,1,1,17,20,54)

        def foreCaster = new CallCenterForecaster()
        String result = foreCaster.forecast('InschattingOpgestart',toEpoch(testDate) ,0)
        //this checks if the json convertion is valid
        def parsedJSON=  convertToObject(result)
        Assert.assertNotNull(parsedJSON)

        //check individual values of the json
        Assert.assertTrue('check parameter EventAffectsPredictions',parsedJSON.PredictionAffectd)
        def IG=parsedJSON.IG
        def expectedDate = new LocalDateTime(new LocalDate(2020,2,5),beginOfDay)
        Assert.assertEquals('check start',toEpoch(expectedDate),IG.Begin)

        expectedDate = new LocalDateTime(new LocalDate( 2020,2,19),endOfDay)
        Assert.assertEquals('check start',toEpoch(expectedDate),IG.End)
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
