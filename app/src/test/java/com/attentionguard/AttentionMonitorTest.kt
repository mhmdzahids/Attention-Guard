package com.attentionguard

import com.attentionguard.service.AttentionMonitoringService
import org.junit.Assert.assertEquals
import org.junit.Test

class AttentionMonitorTest {

    @Test
    fun testLowRiskScoreCalculation() {
        // Mock signals that should yield low risk:
        // Session = 1.0 hr -> N(session) = 1.0 / 8.0 = 0.125
        // Scroll = 50 px/s -> N(scroll) = 50 / 250 = 0.20
        // Switches = 2/hr -> N(switches) = 2.0 / 20.0 = 0.10
        // Night Ratio = 0.10 -> N(night) = 0.10
        // Formula: 0.3*0.125 + 0.2*0.20 + 0.3*0.10 + 0.2*0.10
        // Expected API = 0.0375 + 0.04 + 0.03 + 0.02 = 0.1275 -> rounded to 0.13
        
        AttentionMonitoringService.updateCalculations(
            session = 1.0f,
            scroll = 50.0f,
            switches = 2.0f,
            night = 0.10f
        )
        
        assertEquals(0.13f, AttentionMonitoringService.apiScore, 0.001f)
        assertEquals("low", AttentionMonitoringService.riskTier)
    }

    @Test
    fun testModerateRiskScoreCalculation() {
        // Mock signals for moderate risk:
        // Session = 2.5 hrs -> N(session) = 2.5 / 8.0 = 0.3125
        // Scroll = 142 px/s -> N(scroll) = 142 / 250 = 0.568
        // Switches = 8.2/hr -> N(switches) = 8.2 / 20.0 = 0.41
        // Night Ratio = 0.75 -> N(night) = 0.75
        // Formula: 0.3*0.3125 + 0.2*0.568 + 0.3*0.41 + 0.2*0.75
        // Expected API = 0.09375 + 0.1136 + 0.123 + 0.15 = 0.48035 -> rounded to 0.48
        
        AttentionMonitoringService.updateCalculations(
            session = 2.5f,
            scroll = 142.0f,
            switches = 8.2f,
            night = 0.75f
        )
        
        assertEquals(0.48f, AttentionMonitoringService.apiScore, 0.001f)
        assertEquals("moderate", AttentionMonitoringService.riskTier)
    }

    @Test
    fun testHighRiskScoreCalculation() {
        // Mock signals for high risk:
        // Session = 7.0 hrs -> N(session) = 7.0 / 8.0 = 0.875
        // Scroll = 200 px/s -> N(scroll) = 200 / 250 = 0.80
        // Switches = 18.0/hr -> N(switches) = 18.0 / 20.0 = 0.90
        // Night Ratio = 0.95 -> N(night) = 0.95
        // Formula: 0.3*0.875 + 0.2*0.80 + 0.3*0.90 + 0.2*0.95
        // Expected API = 0.2625 + 0.16 + 0.27 + 0.19 = 0.8825 -> rounded to 0.88
        
        AttentionMonitoringService.updateCalculations(
            session = 7.0f,
            scroll = 200.0f,
            switches = 18.0f,
            night = 0.95f
        )
        
        assertEquals(0.88f, AttentionMonitoringService.apiScore, 0.001f)
        assertEquals("high", AttentionMonitoringService.riskTier)
    }
}
