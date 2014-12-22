package com.codeworks.pai.db.model;

import android.test.AndroidTestCase;

import org.joda.time.DateTime;

/**
 * Created by glennverner on 12/6/14.
 */
public class OptionTest extends AndroidTestCase {

    public void testCloneOption() throws CloneNotSupportedException {
        Option original = new Option("spy",OptionType.P, 200d, new DateTime());
        Option cloned = (Option)original.clone();

        //Let verify
        assertEquals("spy", cloned.getSymbol());
        assertEquals(200d, cloned.getStrike());

        //Verify JDK's rules

        //Must be true and objects must have different memory addresses
        assertTrue (original != cloned);

        //As we are returning same class; so it should be true
        assertTrue(original.getClass() == cloned.getClass());

        //Default equals method checks for refernces so it should be false. If we want to make it true,
        //we need to override equals method in Employee class.
        assertFalse(original.equals(cloned));
    }


}
