package com.example.runningapp

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.runningapp.ui.fragments.TrackingFragment

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class UnitTests {
    private val trackingFragment: TrackingFragment=TrackingFragment()

    @Test
    fun testAvgSpeedCalculation() {
        // Context of the app under test.
        //val appContext = InstrumentationRegistry.getInstrumentation().targetContext


        assertEquals(1.0F, trackingFragment.calculateAvgSpeed(1000, 3600000))
        assertEquals(15.0F, trackingFragment.calculateAvgSpeed(15000, 3600000))
        assertEquals(6.0F, trackingFragment.calculateAvgSpeed(1000, 600000))
        assertEquals(201.0F, trackingFragment.calculateAvgSpeed(201000, 3600000))
    }

    @Test
    fun testMETCalculationRun() {
        //Futás tesztek
        assertEquals(5.0F, trackingFragment.calculateMET("Futás", 4.2F))
        assertEquals(10.0F, trackingFragment.calculateMET("Futás", 9.8F))
        assertEquals(13.5F, trackingFragment.calculateMET("Futás", 10.1F))
        assertEquals(16.0F, trackingFragment.calculateMET("Futás", 16.5F))

        //rosszul leírva - alap érték 1,3F
        assertNotEquals(16.0F, trackingFragment.calculateMET("FUtás", 16.5F))
        assertEquals(1.3F, trackingFragment.calculateMET("FUtás", 16.5F))


    }

    @Test
    fun testMETCalculationWalk() {
        //Gyaloglás tesztek


        assertEquals(2.0F, trackingFragment.calculateMET("Gyaloglás", 1.01F))
        assertEquals(3.0F, trackingFragment.calculateMET("Gyaloglás", 3.3F))
        assertEquals(4.0F, trackingFragment.calculateMET("Gyaloglás", 4.1F))
        assertEquals(5.0F, trackingFragment.calculateMET("Gyaloglás", 7.99F))

        //rosszul leírva - alap érték 1,3F
        assertNotEquals(5.0F, trackingFragment.calculateMET("GYaloGLAS", 7.99F))
        assertEquals(1.3F, trackingFragment.calculateMET("GYaloGLAS", 7.99F))
    }

    @Test
    fun testMETCalculationCycleing() {
        //Kerékpározás tesztek


        assertEquals(4.0F, trackingFragment.calculateMET("Kerékpározás", 16.0F))
        assertEquals(7.0F, trackingFragment.calculateMET("Kerékpározás", 21.987F))
        assertEquals(10.0F, trackingFragment.calculateMET("Kerékpározás", 31.9983F))

        //rosszul leírva - alap érték 1,3F
        assertNotEquals(10.0F, trackingFragment.calculateMET("Biciklizés", 31.9983F))
        assertEquals(1.3F, trackingFragment.calculateMET("Biciklizés", 31.9983F))
    }

    @Test
    fun testBurnedCaloriesCalculation() {
        //(durationInMillisec/60000.0f)*(MET*3.5f*weight)/200.0f
        trackingFragment.calculateBurnedCalories(3600000,13.5F,80.0F)

        assertEquals(1134, trackingFragment.calculateBurnedCalories(3600000,13.5F,80.0F))
        assertEquals(94, trackingFragment.calculateBurnedCalories(1800000,2.0F,90.0F))
        assertEquals(7, trackingFragment.calculateBurnedCalories(20000,16.0F,80.5F))
        assertEquals(2205, trackingFragment.calculateBurnedCalories(18000000,7.0F,60.0F))
    }
}