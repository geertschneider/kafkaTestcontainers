import org.junit.Assert
import org.junit.jupiter.api.*

class ForecasterUDFTest {


    @Test
    public void "Test event which does not influence forecasts"(){
        def forecaster = new CallCenterForecaster()

    }
    @Test
    public void "InschattingOpgestart"(){
        def forecaster = new CallCenterForecaster()
        def forecastResult= forecaster.Forecast("InschattingOpgestart",1000,0)
        Assertions.assertTrue(forecastResult.Affected,"in case of no matching event don't give")
        Assertions.assertEquals(forecastResult.IG,2000)
        Assertions.assertEquals(forecastResult.OG1,3000)
        Assertions.assertEquals(forecastResult.OG2,4000)

    }
}
