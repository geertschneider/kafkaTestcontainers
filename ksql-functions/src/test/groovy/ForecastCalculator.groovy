import org.junit.Assert
import org.junit.jupiter.api.Test

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ForecastCalculator {
    LocalTime endOfDay = new LocalTime(23,59,59,999999999)
    LocalTime beginOfDay = new LocalTime(0,0,0,0)


    @Test
    public void "InschattingOpgestart"(){

        LocalDateTime testDate =  new LocalDateTime(new LocalDate(2020,1,1),new LocalTime(17,20,54,0))
        InschattingsGesprekAlsOpdrachtGepland
        def opgestart = AbstractForecastPredictor.GetInstace("InschattingOpgestart",testDate)

        println (opgestart.toString())


        Assert.assertTrue("check if the affected property is set",opgestart.EventAffectsPredictions)
        //Inschattingsgesprek
        Assert.assertEquals('compare IGStart dates',   new LocalDateTime(new LocalDate(2020,2,5),beginOfDay), opgestart.IG[0])
        Assert.assertEquals("compare IGEnd dates",  new  LocalDateTime(new LocalDate( 2020,2,19),endOfDay),opgestart.IG[1])

//       Eerste opvolggesprek
        Assert.assertEquals('OG1Start',new LocalDateTime(new LocalDate(2020,4,30),beginOfDay),opgestart.OG1[0])
        Assert.assertEquals('OG1End',new LocalDateTime(new LocalDate( 2020,5,14),endOfDay),opgestart.OG1[1])

//      Tweede opvolggesprek
        Assert.assertEquals('OG2Start',new LocalDateTime(new LocalDate(2020,7,24),beginOfDay),opgestart.OG2[0])
        Assert.assertEquals('OG2End',new LocalDateTime(new LocalDate( 2020,8,7),endOfDay),opgestart.OG2[1])
    }

    @Test
    public void TestBeginOfWeeks(){

        def startDate = new LocalDateTime( LocalDate.of (2020,5,5),LocalTime.now())
        def testDates= new Tuple2<LocalDateTime,LocalDateTime>( startDate,startDate.plusWeeks(1))
        def weekStarts= AbstractForecastPredictor.getOverlappingWeeks(testDates)
        Assert.assertEquals("2 weeks expexted to cover period",2,weekStarts.size() )

        Assert.assertEquals("monday 04 MAY expected" , new LocalDateTime( LocalDate.of(2020,5,4),  LocalTime.of(0,0,0)),weekStarts[0])
        Assert.assertEquals("monday 04 MAY expected" , new LocalDateTime( LocalDate.of(2020,5,11),LocalTime.of(0,0,0)),weekStarts[1])
    }

@Test
    public void "InschattingsGesprekAlsOpdrachtGepland"(){

        LocalDateTime testDate =  new LocalDateTime(new LocalDate(2020,1,1),new LocalTime(17,20,54,0))
        def opgestart =  AbstractForecastPredictor.GetInstace("InschattingsGesprekAlsOpdrachtGepland",testDate)


        Assert.assertTrue("check if the affected property is set",opgestart.EventAffectsPredictions)
        //Inschattingsgesprek
        Assert.assertEquals('compare IGStart dates',   new LocalDateTime(new LocalDate(2020,1,2),beginOfDay), opgestart.IG[0])
        Assert.assertEquals("compare IGEnd dates",  new  LocalDateTime(new LocalDate( 2020,1,16),endOfDay),opgestart.IG[1])

//       Eerste opvolggesprek
        Assert.assertEquals('OG1Start',new LocalDateTime(new LocalDate(2020,3,27),beginOfDay),opgestart.OG1[0])
        Assert.assertEquals('OG1End',new LocalDateTime(new LocalDate( 2020,4,10),endOfDay),opgestart.OG1[1])

//      Tweede opvolggesprek
        Assert.assertEquals('OG2Start',new LocalDateTime(new LocalDate(2020,6,20),beginOfDay),opgestart.OG2[0])
        Assert.assertEquals('OG2End',new LocalDateTime(new LocalDate( 2020,7,4),endOfDay),opgestart.OG2[1])

    }

    @Test
    public void "InschattingsGesprekAlsBellijstGepland"(){

        LocalDateTime testDate =  new LocalDateTime(new LocalDate(2020,1,1),new LocalTime(17,20,54,0))


        def opgestart =  AbstractForecastPredictor.GetInstace("InschattingsGesprekAlsBellijstGepland",testDate)

        println (opgestart.toString())

        Assert.assertTrue("check if the affected property is set",opgestart.EventAffectsPredictions)
        //Inschattingsgesprek
        Assert.assertEquals('compare IGStart dates',   new LocalDateTime(new LocalDate(2020,1,1),beginOfDay), opgestart.IG[0])
        Assert.assertEquals("compare IGEnd dates",  new  LocalDateTime(new LocalDate( 2020,1,15),endOfDay),opgestart.IG[1])

//       Eerste opvolggesprek
        Assert.assertEquals('OG1Start',new LocalDateTime(new LocalDate(2020,3,26),beginOfDay),opgestart.OG1[0])
        Assert.assertEquals('OG1End',new LocalDateTime(new LocalDate( 2020,4,9),endOfDay),opgestart.OG1[1])

//      Tweede opvolggesprek
        Assert.assertEquals('OG2Start',new LocalDateTime(new LocalDate(2020,6,19),beginOfDay),opgestart.OG2[0])
        Assert.assertEquals('OG2End',new LocalDateTime(new LocalDate( 2020,7,3),endOfDay),opgestart.OG2[1])
    }

    @Test
    public void InschattingBeeindigd(){

        LocalDateTime testDate =  new LocalDateTime(new LocalDate(2020,1,1),new LocalTime(17,20,54,0))

        def opgestart =  AbstractForecastPredictor.GetInstace("InschattingBeeindigd",testDate)
        println (opgestart.toString())


        Assert.assertTrue("check if the affected property is set",opgestart.EventAffectsPredictions)
        //Inschattingsgesprek
        Assert.assertNull('compare IGStart dates',   opgestart.IG)
        Assert.assertNull('compare IGStart dates',   opgestart.OG1)
        Assert.assertNull('compare IGStart dates',   opgestart.OG2)
    }


    @Test
    public void "Check event that does not affect the forecast"(){

        LocalDateTime testDate =  new LocalDateTime(new LocalDate(2020,1,1),new LocalTime(17,20,54,0))

        def opgestart =  AbstractForecastPredictor.GetInstace("bla bla bla",testDate)
        Assert.assertFalse (opgestart.EventAffectsPredictions)
    }

}
