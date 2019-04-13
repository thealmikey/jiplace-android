package com.almikey.jiplace


import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.runner.AndroidJUnit4
import com.almikey.jiplace.ui.homepage.HomeFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JiPlaceNowTest {

    @Test fun testEventFragment() {
        // The "state" and "factory" arguments are optional.

       // val homeFragment = HomeFragment()
        val scenario = launchFragmentInContainer<HomeFragment>()
        onView(withId(R.id.jiPlaceNow)).check(matches(withText("Jiplace Now")))
        onView(withId(R.id.jiPlaceOther)).check(matches(withText("Jiplace Other")))
    }



}