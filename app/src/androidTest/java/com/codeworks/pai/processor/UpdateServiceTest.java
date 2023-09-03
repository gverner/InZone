package com.codeworks.pai.processor;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.HandlerThread;

import com.codeworks.pai.db.ServiceLogTable;
import com.codeworks.pai.db.model.ServiceType;
import com.codeworks.pai.mock.MockSharedPreferences;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.InstrumentationRegistry.getContext;
import static junit.framework.TestCase.assertEquals;

@RunWith(AndroidJUnit4.class)
public class UpdateServiceTest {

	class TestUpdateService extends UpdateService {
		int		month			= 6; // June
		int		day				= 3;
		int		year			= 2013;
		int		hour			= 9;
		int		minute			= 1;


		SharedPreferences sharedPref = new MockSharedPreferences();
		int progressBarStopCalls = 0;
		int progressBarStartCalls = 0;
		int clearServiceLogCalls = 0;
		int insertServiceLogCalls = 0;
		ArrayList<Integer> serviceLogs = new ArrayList<Integer>();

		boolean marketOpen = true;
		boolean callOriginalIsMarketOpen = false;

		@Override
		public void onCreate() {
			mockSystemTime = new DateTime(year,month,day,hour, minute, DateTimeZone.forID("America/New_York"));
		    notifier = new MockNotifier();
			processor = new MockProcessor();

			HandlerThread thread = new HandlerThread("ServiceStartArguments", android.os.Process.THREAD_PRIORITY_BACKGROUND);
			thread.start();

			// Get the HandlerThread's Looper and use it for our Handler
			shutdownInProcess = false;
			mServiceLooper = thread.getLooper();
			mServiceHandler = new ServiceHandler(mServiceLooper);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}


		@Override
		public synchronized int onStartCommand(Intent updateIntent, int flags, int startId) {
			int returnvalue =super.onStartCommand(updateIntent, flags, startId);
			try {  // allow time for AlarmSetup thread
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return returnvalue;
		}

		public void resetTime() {
			mockSystemTime = new DateTime(year,month,day,hour, minute, DateTimeZone.forID("America/New_York"));
		}
		
		@Override
		AlarmSetup getAlarmSetup() {
			return new MockAlarmSetup(getContext(), notifier);
		}
		
		@Override
		void clearServiceLog() {
			clearServiceLogCalls++;
		}
		@Override
		boolean isMarketOpen() {
			if (callOriginalIsMarketOpen) {
				return super.isMarketOpen();
			} else {
				return marketOpen;
			}
		}

		@Override
		void insertServiceLog(ContentValues values) {
			insertServiceLogCalls++;
			serviceLogs.add((Integer) values.get(ServiceLogTable.COLUMN_SERVICE_TYPE));
		}
		
		

		@Override
		void powerLockAquire(long timeout) {
			// Don't know how to mock
		}


		int getPrefUpdateFrequency() {
			return 3;
		}

		@Override
		SharedPreferences getSharedPreferences() {
			return sharedPref;
		}


		@Override
		public Context getApplicationContext() {
			
			Context context = null;/*new MockContext() {
				@Override
				public
				Resources getResources() {
					Resources resource = new MockResources() {
						@Override
						public String getString(int key) {
							if (key == R.string.scheduledStartSubject) {
								return "Value for Mock Key scheduledStartSubject";
							}
							if (key == R.string.scheduleSetupSubject) {
								return "Value for Mock Key R.string.scheduleSetupSubject";
							}
							if (key == R.string.updateServiceSubject) {
								return "Value for Mock key R.string.updateServiceSubject"; 
							}
							if (key == R.string.serviceStoppedMessage) {
								return "Value for Mock key R.string.serviceStoppedMessage";
							}
							if (key == R.string.serviceRunningMessage) {
								return "Value for Mock key R.string.serviceRunningMessage";
							}
							if (key == R.string.servicePausedMessage) {
								return "Value for Mock key R.string.servicePausedMessage";
							}
							if (key == R.string.scheduledStartMessage) {
								return "Value for Mock key R.string.scheduledStartMessage";
							}
							if (key == R.string.startTypeSchedule) {
								return "Value for Mock key R.string.startTypeSchedule";
							}
							if (key == R.string.startTypeRepeating) {
								return "Value for Mock key R.string.startTypeRepeating";
							}
							if (key == R.string.startTypeManual) {
								return "Value for Mock key R.string.startTypeManual";
							}
							if (key == R.string.startTypeManualMenu) {
								return "Value for Mock key R.string.startTypeManualMenu";
							}
							if (key == R.string.scheduleRepeatingAlreadySetup) {
								return "Value for Mock key R.string.scheduleRepeatingAlreadySetup";
							}
							if (key == R.string.scheduleSetupBusy) {
								return "Value for Mock key R.string.scheduleSetupBusy";
							}
							if (key == R.string.scheduleStateBusy) {
								return "Value for Mock key R.string.scheduleStateBusy";
							}
							throw new Resources.NotFoundException(String.valueOf(key));
						}
						@Override
						public String getString(int key, Object ... formatArgs) {
							if (key == R.string.scheduledStartMessage) {
								return "Value for Mock R.string.scheduledStartMessage";
							}
						
							if (key == R.string.scheduledStartSubject) {
								return "Value for Mock Key scheduledStartSubject";
							}
							if (key == R.string.scheduleSetupSubject) {
								return "Value for Mock Key R.string.scheduleSetupSubject";
							}
							throw new Resources.NotFoundException(String.valueOf(key));
						}

					};
					return resource;
				}
			};*/
			
			return context;
		}


		@Override
		void progressBarStart() {
			progressBarStartCalls++;
		}


		@Override
		void progressBarStop() {
			progressBarStopCalls++;
		}

		@Override
		DateTime getCurrentNYTime() {
			System.out.println("Run Time="+formatStartTime(mockSystemTime)+ " hour="+mockSystemTime.getHourOfDay());
			System.out.println("day of week="+mockSystemTime.getDayOfWeek()+ " Sat="+DateTimeConstants.SATURDAY+ " Sun="+DateTimeConstants.SUNDAY);
			System.out.println("Test Time="+formatStartTime(mockSystemTime));
			return mockSystemTime;
		}
		
	}
	public List<DateTime> startAlarmTimes = new ArrayList<>();
	public List<DateTime> repeatingAlarmTimes = new ArrayList<>();
	public DateTime mockSystemTime = new DateTime();

	class MockAlarmSetup extends AlarmSetup {


		public boolean	alarmStarted	= false;

		public MockAlarmSetup(Context context, Notifier notifier) {
			super(context, notifier);
		}
		

		void setRepeatingAlarm(DateTime startTime) {
			alarmStarted = true;
			System.out.println("Call set Alarm with start of " + formatStartTime(startTime));
			repeatingAlarmTimes.add(startTime);
		}
		
		void setStartAlarm(DateTime startTime) {
			alarmStarted = true;
			System.out.println("Call set Alarm with start of " + formatStartTime(startTime));
			startAlarmTimes.add(startTime);
		}		
		@Override
		boolean isAlarmAlreadyUp(int intent) {
			if (alarmStarted) {
				System.out.println("Alarm is already installed");
				return true;
			} else {
				System.out.println("Alarm is not installed");
				return false;
			}
		}

		@Override
		public DateTime getCurrentNYTime() {
			System.out.println("Run Time="+formatStartTime(mockSystemTime)+ " hour="+mockSystemTime.getHourOfDay());
			System.out.println("day of week="+mockSystemTime.getDayOfWeek()+ " Sat="+DateTimeConstants.SATURDAY+ " Sun="+DateTimeConstants.SUNDAY);
			System.out.println("Test Time="+formatStartTime(mockSystemTime));
			return mockSystemTime;
		}
	
	}
	@Test
	public void testIsMarketOpen() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		System.out.println("isMarketOpen="+service.isMarketOpen());
		assertEquals("Market is open", true, service.isMarketOpen());
		service.onDestroy();
	}
	/*
	public void testRestartAndShutdown() throws InterruptedException {
		TestUpdateService service = new TestUpdateService();
		service.callOriginalIsMarketOpen = true;
		service.onCreate();
		MockAlarmSetup alarm = (MockAlarmSetup)service.getAlarmSetup();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		Thread.sleep(1000);
		while (service.updater.isRunning()) {
			mockSystemTime = mockSystemTime.hourOfDay().addToCopy(1);
			service.updater.restart(false);
			Thread.sleep(1000);
		}
		assertEquals("Number of Process Calls",8, ((MockProcessor)service.processor).numberOfProcessCalls);
		assertEquals("Number of Notifier Calls",8, ((MockNotifier)service.notifier).numberOfCalls);
		assertEquals("Progress Bar Start Calls", 8, service.progressBarStartCalls);
		assertEquals("Progress Bar Stop Calls", 8, service.progressBarStopCalls);
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 9, service.insertServiceLogCalls);

	}
	*/
	@Test
	public void testOneTimeIntent() throws InterruptedException {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		Intent oneTimeIntent = new Intent();
		oneTimeIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_ONE_TIME);
		service.onStartCommand(oneTimeIntent, 0, 0);
		Thread.sleep(1000);
		assertEquals("Number of Process Calls", 1, ((MockProcessor) service.processor).numberOfProcessCalls);
		assertEquals("Number of Notifier Calls", 1, ((MockNotifier) service.notifier).numberOfCalls);
		assertEquals("Number of ClearServiceLog Calls", 0, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 0, service.insertServiceLogCalls);

	}

	@Test
	public void testSetAlarmBeforeMarketOpen901() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = (MockAlarmSetup)service.getAlarmSetup();
		service.hour = 9;
		service.minute = 1;
		service.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, startAlarmTimes.size());
		assertEquals("Hour of Start",9, startAlarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", service.day, startAlarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", service.month, startAlarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", service.year, startAlarmTimes.get(0).getYear());
		System.out.println(startAlarmTimes.get(0).getMinuteOfHour());
		assertEquals("Minute", 30, startAlarmTimes.get(0).getMinuteOfHour());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 2, service.insertServiceLogCalls);
		System.out.println(ServiceType.fromIndex(service.serviceLogs.get(0)));
		assertEquals("Service Log Type", java.util.Optional.of(ServiceType.START.getIndex()), (Integer)service.serviceLogs.get(0));
	
	}

	@Test
	public void testSetAlarmBeforeMarketOpen859() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = (MockAlarmSetup)service.getAlarmSetup();
		service.hour = 8;
		service.minute = 59;
		service.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, startAlarmTimes.size());
		assertEquals("Hour of Start",9, startAlarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", service.day, startAlarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", service.month, startAlarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", service.year, startAlarmTimes.get(0).getYear());
		System.out.println(startAlarmTimes.get(0).getMinuteOfHour());
		assertEquals("Minute", 30, startAlarmTimes.get(0).getMinuteOfHour());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 2, service.insertServiceLogCalls);
		System.out.println(ServiceType.fromIndex(service.serviceLogs.get(0)));
		assertEquals("Service Log Type", (Integer)ServiceType.START.getIndex(), (Integer)service.serviceLogs.get(0));
	
	}

	@Test
	public void testSetAlarmBeforeMarketOpen930() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = (MockAlarmSetup)service.getAlarmSetup();
		service.hour = 9;
		service.minute = 30;
		service.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, repeatingAlarmTimes.size());
		assertEquals("Hour of Start",9, repeatingAlarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", service.day, repeatingAlarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", service.month, repeatingAlarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", service.year, repeatingAlarmTimes.get(0).getYear());
		System.out.println(repeatingAlarmTimes.get(0).getMinuteOfHour());
		assertEquals("Minute", 30, repeatingAlarmTimes.get(0).getMinuteOfHour());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 2, service.insertServiceLogCalls);
		System.out.println(ServiceType.fromIndex(service.serviceLogs.get(0)));
		assertEquals("Service Log Type", (Integer)ServiceType.START.getIndex(), (Integer)service.serviceLogs.get(0));
	
	}

	@Test
	public void testSetAlarmBeforeMarketOpen931() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = (MockAlarmSetup)service.getAlarmSetup();
		service.hour = 9;
		service.minute = 31;
		service.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",0, startAlarmTimes.size());
		assertEquals("Number of repeating alarm calls",1, repeatingAlarmTimes.size());
		assertEquals("Hour of Start",9, repeatingAlarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", service.day, repeatingAlarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", service.month, repeatingAlarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", service.year, repeatingAlarmTimes.get(0).getYear());
		System.out.println(repeatingAlarmTimes.get(0).getMinuteOfHour());
		assertEquals("Minute", 31, repeatingAlarmTimes.get(0).getMinuteOfHour());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 2, service.insertServiceLogCalls);
		System.out.println(ServiceType.fromIndex(service.serviceLogs.get(0)));
		assertEquals("Service Log Type", (Integer)ServiceType.START.getIndex(), (Integer)service.serviceLogs.get(0));
	}

	@Test
	public void testSetAlarmBeforeMarketOpenWeekEnd() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		service.hour = 5;
		service.day = 29;
		service.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, startAlarmTimes.size());
		assertEquals("Hour of Start",5, startAlarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", 1, startAlarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", 7, startAlarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", service.year, startAlarmTimes.get(0).getYear());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 2, service.insertServiceLogCalls);
		
	}

	@Test
	public void testSetAlarmAfterMarketClose() throws InterruptedException {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		service.hour = 17;
		service.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		Thread.sleep(2000);
		assertEquals("Number of set alarm calls",1, startAlarmTimes.size());
		assertEquals("Hour of Start",5, startAlarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", service.day+1, startAlarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", service.month, startAlarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", service.year, startAlarmTimes.get(0).getYear());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 2, service.insertServiceLogCalls);
		
	}

	@Test
	public void testSetAlarmAfterMarketCloseWeekEnd() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = (MockAlarmSetup)service.getAlarmSetup();
		service.hour = 17;
		service.day = 7;
		service.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, startAlarmTimes.size());
		assertEquals("Hour of Start",5, startAlarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", 8, startAlarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", service.month, startAlarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", service.year, startAlarmTimes.get(0).getYear());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 2, service.insertServiceLogCalls);
		
	}

	@Test
	public void testSetAlarmAfterMarketCloseMonthEnd() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = (MockAlarmSetup)service.getAlarmSetup();
		service.hour = 17;
		service.day = 28;
		service.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, startAlarmTimes.size());
		assertEquals("Hour of Start",5, startAlarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", 29, startAlarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", 6, startAlarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", service.year, startAlarmTimes.get(0).getYear());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 2, service.insertServiceLogCalls);
		
	}

	@Test
	public void testSetAlarmBeforeMarketOpenAlreadyRunning() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = (MockAlarmSetup)service.getAlarmSetup();
		alarm.alarmStarted = true;
		service.hour = 5;
		service.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, startAlarmTimes.size());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		assertEquals("Number of insertServiceLog Calls", 2, service.insertServiceLogCalls);
		
	}


	@Test
	public void testSetAlarmAfterMarketOpenAlreadyRunning() throws InterruptedException {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = (MockAlarmSetup)service.getAlarmSetup();
		alarm.alarmStarted = true;
		service.hour = 5;
		service.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, startAlarmTimes.size());
		assertEquals("Number of process Calls", 1, ((MockProcessor)service.processor).numberOfProcessCalls);
		assertEquals("Number of process Calls", 0, ((MockProcessor)service.processor).numberOfUpdatePriceCalls);
		//Thread.sleep(2000);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",2, startAlarmTimes.size());
		assertEquals("Number of process Calls", 2, ((MockProcessor)service.processor).numberOfProcessCalls);
		assertEquals("Number of update Price Calls", 0, ((MockProcessor)service.processor).numberOfUpdatePriceCalls);
		assertEquals("Number of ClearServiceLog Calls", 2, service.clearServiceLogCalls);

	}

	@Test
	public void testSetAlarmAfterMarketCloseAlreadyRunning() {
		TestUpdateService service = new TestUpdateService();
		service.onCreate();
		MockAlarmSetup alarm = (MockAlarmSetup)service.getAlarmSetup();
		alarm.alarmStarted = true;
		service.hour = 17;
		service.resetTime();
		Intent dailyIntent = new Intent();
		dailyIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		service.onStartCommand(dailyIntent, 0, 0);
		assertEquals("Number of set alarm calls",1, startAlarmTimes.size());
		assertEquals("Hour of Start",5, startAlarmTimes.get(0).getHourOfDay());
		assertEquals("Day of Month", service.day+1, startAlarmTimes.get(0).getDayOfMonth());
		assertEquals("Month", service.month, startAlarmTimes.get(0).getMonthOfYear());
		assertEquals("Year", service.year, startAlarmTimes.get(0).getYear());
		assertEquals("Number of ClearServiceLog Calls", 1, service.clearServiceLogCalls);
		
	}

}
