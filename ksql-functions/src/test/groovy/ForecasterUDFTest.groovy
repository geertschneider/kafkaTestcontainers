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

    def fromEpoch = { l -> Instant.ofEpochMilli(l).toDate().toLocalDateTime() }
    def toEpoch ={LocalDateTime dt -> dt.toInstant(ZoneOffset.UTC).toEpochMilli()}

    @Test
    public void "Check if forecaster can be created"() {
        def forecaster = new ForecastGesprekken_Period()
        Assert.assertNotNull(forecaster)
    }

    @Test
    public void "Check if all fields are supplied"(){
        LocalDateTime testDate =  LocalDateTime.of(2020,1,1,17,20,54)

        def foreCaster = new ForecastGesprekken_Period()
        def result = foreCaster.forecastGesprekken_Period ('InschattingOpgestart',toEpoch(testDate) ,0)


        println("""
        object received :
        ${result}
        """)

        println("OG1.Begin :${result["OG1"]["BeginPeriod"]}")

        def IG=result.IG

        def expectedDate = new LocalDateTime(new LocalDate(2020,2,5),beginOfDay)
        Assert.assertEquals('check start',toEpoch(expectedDate),IG.BeginPeriod)

        expectedDate = new LocalDateTime(new LocalDate( 2020,2,19),endOfDay)
        Assert.assertEquals('check end',toEpoch(expectedDate),IG.EndPeriod)

        Assert.assertNotNull("check if OG1 exists",result.OG1)
        Assert.assertNotNull("check if OG1 BeginPeriod",result.OG1.BeginPeriod)
        Assert.assertNotNull("check if OG1 EndPeriod",result.OG1.EndPeriod)

        Assert.assertNotNull("check if OG1 exists",result.OG2)
        Assert.assertNotNull("check if OG1 BeginPeriod",result.OG2.BeginPeriod)
        Assert.assertNotNull("check if OG1 EndPeriod",result.OG2.EndPeriod)
    }

    @Test
    public void "Check if all fields are supplierd for weeks"() {
        LocalDateTime testDate = LocalDateTime.of(2020, 1, 1, 17, 20, 54)

        def foreCaster = new ForecastGesprekken_Weeks()
        def result = foreCaster.forecastGesprekken_Weeks('InschattingOpgestart', toEpoch(testDate), 0)


        println("""
        object received :
        ${result}
        """)
        ///TODO : add test cases
    }
    @Test
    public void RealTest(){

        def foreCaster = new ForecastGesprekken_Period()
        String result = foreCaster.forecastGesprekken_Period('InschattingOpgestart',1577836800000)

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
