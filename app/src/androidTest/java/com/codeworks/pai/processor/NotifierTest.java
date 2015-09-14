package com.codeworks.pai.processor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.db.model.MaType;
import com.codeworks.pai.db.model.Study;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

public class NotifierTest extends AndroidTestCase {

	public void testNetworkError() {
		Study study = new Study("SPY");
		study.setNetworkError(true);
		assertTrue(study.hasNetworkError());
		study.setNetworkError(false);
		assertFalse(study.hasNetworkError());
	}
	/**
	 * Testing for missing resource exception caused by DEPLAY_PRICE not having a notice subject and message.
	 * Probably obsolete after adding statusMap flags
	 * 
	 */
	public void testNotice() {
		NotifierImpl notifier = new NotifierImpl(getContext());
		Study study = new Study("SPY");
		study.setPrice(1.0d);
		study.setEmaWeek(0.50d);
		study.setEmaMonth(.050d);
		study.setEmaStddevWeek(0.55d);
		study.setEmaStddevMonth(0.55d);
		study.setNotice(Notice.NONE);
		List<Study> studies = new ArrayList<Study>();
		studies.add(study);
		notifier.updateNotification(studies);
		study.setDelayedPrice(true);
		notifier.updateNotification(studies);
		study.setNotice(Notice.NONE);
		study.setPrice(.55d);
		notifier.updateNotification(studies);
		study.setPrice(1.55d);
		notifier.updateNotification(studies);
		study.setPrice(.40d);
		notifier.updateNotification(studies);
		study.setPrice(.60d);
		notifier.updateNotification(studies);
	}

	class MockNotifier extends NotifierImpl {
		int numberOfCalls = 0;
		int numberOfStudies = 0;
		int numberOfSendNoticeCalls = 0;

		public MockNotifier(Context context) {
			super(context);
			numberOfCalls++;
		}
		@Override
		public void sendNotice(long securityId, String title, String text) {
			numberOfSendNoticeCalls++;
		}
		@Override
		public void sendServiceNotice(int notifyId, String title, String text, int numMessages) {
			numberOfSendNoticeCalls++;
			
		}		
	}
	
	public void testNoticeSma() {
		MockNotifier notifier = new MockNotifier(getContext());
		Study study = fetchStudyAndUpdateNotice("SPY",Notice.NONE);
		study.setMaType(MaType.S);
		study.setPrice(1.0d);
		study.setEmaWeek(0.50d);
		study.setEmaMonth(.050d);
		study.setEmaStddevWeek(0.55d);
		study.setEmaStddevMonth(0.55d);
		study.setNotice(Notice.NONE);
		List<Study> studies = new ArrayList<Study>();
		studies.add(study);
		notifier.updateNotification(studies);
		assertEquals(0, notifier.numberOfSendNoticeCalls );
		study.setDelayedPrice(true);
		notifier.updateNotification(studies);
		assertEquals(0, notifier.numberOfSendNoticeCalls );
		study.setNotice(Notice.NONE);
		study.setPrice(.55d);
		notifier.updateNotification(studies);
		assertEquals(0, notifier.numberOfSendNoticeCalls );
		study.setPrice(1.55d);
		notifier.updateNotification(studies);
		assertEquals(0, notifier.numberOfSendNoticeCalls );
		study.setPrice(.40d);
		notifier.updateNotification(studies);
		assertEquals(0, notifier.numberOfSendNoticeCalls );
		study.setPrice(.60d);
		notifier.updateNotification(studies);
		assertEquals(0, notifier.numberOfSendNoticeCalls );
	}
	
	public void testNoticeEma() {
		MockNotifier notifier = new MockNotifier(getContext());
		Study study = fetchStudyAndUpdateNotice("SPY",Notice.NONE);
		study.setMaType(MaType.E);
		study.setPrice(1.0d);
		study.setEmaWeek(0.50d);
		study.setEmaMonth(.050d);
		study.setSmaWeek(0.50d);
		study.setSmaMonth(.050d);
		study.setEmaStddevWeek(0.55d);
		study.setEmaStddevMonth(0.55d);
		study.setNotice(Notice.NONE);
		List<Study> studies = new ArrayList<Study>();
		studies.add(study);
		notifier.updateNotification(studies);
		assertEquals(0, notifier.numberOfSendNoticeCalls );
		study.setDelayedPrice(true);
		notifier.updateNotification(studies);
		assertEquals(0, notifier.numberOfSendNoticeCalls );
		study.setNotice(Notice.NONE);
		study.setPrice(.55d);
		notifier.updateNotification(studies);
		assertEquals(1, notifier.numberOfSendNoticeCalls );
		study.setPrice(1.55d);
		notifier.updateNotification(studies);
		assertEquals(2, notifier.numberOfSendNoticeCalls );
		study.setPrice(.40d);
		notifier.updateNotification(studies);
		assertEquals(3, notifier.numberOfSendNoticeCalls );
		study.setPrice(.60d);
		notifier.updateNotification(studies);
		assertEquals(4, notifier.numberOfSendNoticeCalls );
	}

	Study fetchStudyAndUpdateNotice(String symbol, Notice notice) {
		Study study = new Study(symbol);

		String[] projection = new String[] { StudyTable.COLUMN_ID, StudyTable.COLUMN_NOTICE, StudyTable.COLUMN_NOTICE_DATE };
		String selection = "symbol = ? ";
		String[] selectionArgs = new String[] { study.getSymbol() };
		Cursor studyCursor = getContext().getContentResolver().query(PaiContentProvider.PAI_STUDY_URI, projection, selection, selectionArgs,
				StudyTable.COLUMN_ID);
		try {
			if (studyCursor.moveToFirst()) {
				study.setSecurityId(studyCursor.getLong(studyCursor.getColumnIndex(StudyTable.COLUMN_ID)));
				study.setNotice(Notice.fromIndex(studyCursor.getInt(studyCursor.getColumnIndex(StudyTable.COLUMN_NOTICE))));
				study.setNotice(notice);
				ContentValues values = new ContentValues();
				values.put(StudyTable.COLUMN_NOTICE, notice.getIndex());
				Uri studyUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + study.getSecurityId());
				getContext().getContentResolver().update(studyUri, values, null, null);
			} else {
				//study.setSecurityId(studyCursor.getLong(studyCursor.getColumnIndex(StudyTable.COLUMN_ID)));
				study.setNotice(notice);
				ContentValues values = new ContentValues();
				values.put(StudyTable.COLUMN_NOTICE, notice.getIndex());
				values.put(StudyTable.COLUMN_SYMBOL, symbol);
				values.put(StudyTable.COLUMN_PORTFOLIO_ID, 1);
				values.put(StudyTable.COLUMN_NOTICE, notice.getIndex());
				Uri returnUri = getContext().getContentResolver().insert(PaiContentProvider.PAI_STUDY_URI,  values);
				Log.i("TAG", returnUri.toString());
			}
			return study;
		} finally {
			studyCursor.close();
		}
	}
	
	public void testSaveNotice() {
		NotifierImpl notifier = new NotifierImpl(getContext());
		Study study = fetchStudyAndUpdateNotice("SPY", Notice.NONE);
		study.setNotice(Notice.NONE);
		study.setNoticeDate(new Date());
		
		assertFalse(notifier.saveStudyNoticeIfChanged(study));
		study.setNotice(Notice.IN_BUY_ZONE);
		assertTrue(notifier.saveStudyNoticeIfChanged(study));
		assertFalse(notifier.saveStudyNoticeIfChanged(study));
	}
}
