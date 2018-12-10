package com.codeworks.pai.db.model;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;


@RunWith(AndroidJUnit4.class)
public class PaiStudyTest {

	@Test
	public void testDelayedPrice() {
		Study study = new Study("SPY");
		study.setDelayedPrice(false);
		assertTrue(!study.hasDelayedPrice());
		study.setDelayedPrice(true);
		assertTrue(study.hasDelayedPrice());
		assertFalse(study.hasInsufficientHistory());
		assertFalse(study.hasNoPrice());
	}

	@Test
	public void testNoPrice() {
		Study study = new Study("SPY");
		study.setNoPrice(false);
		assertTrue(!study.hasNoPrice());
		study.setNoPrice(true);
		assertTrue(study.hasNoPrice());
		assertFalse(study.hasDelayedPrice());
		assertFalse(study.hasInsufficientHistory());
	}

	@Test
	public void testInsufficientPrice() {
		Study study = new Study("SPY");
		study.setInsufficientHistory(false);
		assertTrue(!study.hasInsufficientHistory());
		study.setInsufficientHistory(true);
		assertTrue(study.hasInsufficientHistory());
		assertFalse(study.hasDelayedPrice());
		assertFalse(study.hasNoPrice());
	}

	@Test
	public void testStatusMap() {
		Study study = new Study("SPY");
		study.setInsufficientHistory(false);
		study.setDelayedPrice(false);
		study.setNoPrice(false);
		assertTrue(!study.hasInsufficientHistory());
		assertTrue(!study.hasDelayedPrice());
		assertTrue(!study.hasNoPrice());

		study.setInsufficientHistory(true);
		assertTrue(study.hasInsufficientHistory());
		assertTrue(!study.hasDelayedPrice());
		assertTrue(!study.hasNoPrice());

		study.setNoPrice(true);
		assertTrue(study.hasInsufficientHistory());
		assertTrue(!study.hasDelayedPrice());
		assertTrue(study.hasNoPrice());

		study.setDelayedPrice(true);
		assertTrue(study.hasInsufficientHistory());
		assertTrue(study.hasDelayedPrice());
		assertTrue(study.hasNoPrice());
		
		
	}
}
