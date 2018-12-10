package com.codeworks.pai.processor;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ProgressBar;

import com.codeworks.pai.InZone;
import com.codeworks.pai.R;
import com.codeworks.pai.TrackerUtil;
import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.ServiceLogTable;
import com.codeworks.pai.db.model.ServiceType;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.util.Holiday;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.List;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class UpdateWorker extends Worker {
    private static final String TAG = UpdateWorker.class.getSimpleName();

    public static final String SERVICE_ACTION = "com.codeworks.pai.updateservice.action";
    public static final String SERVICE_START_LOCATION = "startLocation";

    public static final String BROADCAST_UPDATE_PROGRESS_BAR = "com.codeworks.pai.updateservice.progressBar";
    public static final String PROGRESS_BAR_STATUS = "com.codeworks.pai.updateservice.progress.status";

    public static final String ACTION_FULL = "action_full";
    public static final String ACTION_PRICE_ONLY = "action_price_only";
    public static final String ACTION_RELOAD_HISTORY = "action_reload_history";

    public static final String SCHEDULE_TYPE = "SCHEDULE_TYPE";
    public static final String SCHEDULE_REPEATING = "repeating";
    public static final String SCHEDULE_ONE_TIME = "oneTime";


    Processor processor = null;
    Notifier notifier = null;
    ProgressBar progressBar = null;

    Context context;
    WorkerParameters workerParams;

    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        this.workerParams = workerParams;
    }

    @NonNull
    @Override
    public Result doWork() {
        onCreate();
        handleMessage();
        onDestroy();
        return Result.SUCCESS;
    }

    public void onCreate() {
        processor = new ProcessorImpl(getApplicationContext().getContentResolver(), new DataReaderYahoo(), getApplicationContext());
        notifier = new NotifierImpl(getApplicationContext());
        createLogEventStart();
        Log.d(TAG, "on Create'd2");
    }

    public void onDestroy() {
        if (notifier != null) {
            notifier = null;
        }
        if (processor != null) {
            processor.onClose();
            processor = null;
        }
        Log.d(TAG, "on Destroy'd");
    }

    public void handleMessage() {
        String action = getInputData().getString(SERVICE_ACTION);
        String startLocation = getInputData().getString(SERVICE_START_LOCATION);
        String scheduleType = getInputData().getString(SCHEDULE_TYPE);

        boolean priceOnly = false;
        boolean updateHistory = false;

        if (ACTION_FULL.equals(action)) {
            priceOnly = false;
        } else if (ACTION_RELOAD_HISTORY.equals(action)) {
            updateHistory = true;
        } else if (ACTION_PRICE_ONLY.equals(action)) {
            priceOnly = true;
        }
        int numMessages = 0;

        long startTime;
        try {
            Log.d(TAG, "Update Running action=" + action+ " startLocation="+startLocation);
            startTime = System.currentTimeMillis();
            progressBarStart();
            List<Study> studies;
            if (priceOnly) {
                studies = processor.updatePrice(null);
                Log.i(TAG, "Processor UpdatePrice Runtime milliseconds=" + (System.currentTimeMillis() - startTime));
            } else {
                studies = processor.process(null, updateHistory);
                Log.i(TAG, "Processor process Runtime milliseconds=" + (System.currentTimeMillis() - startTime));
            }
            if (isMarketOpen()) { // Notify only during market hours
                notifier.updateNotification(studies);
                notifier.notifyUserWhenErrors(studies);
            }
            boolean historyReloaded = scanHistoryReloaded(studies);
            int logMessage;
            if (SCHEDULE_REPEATING.equals(scheduleType) && isMarketOpen()) {
                logMessage = historyReloaded ? R.string.servicePausedHistory : R.string.servicePausedMessage;
            } else {
                if (SCHEDULE_REPEATING.equals(scheduleType)) {
                    Log.d(TAG, "Market is Closed - Service will stop");
                } else {
                    Log.d(TAG, "Service will stop");
                }
                logMessage = historyReloaded ? R.string.serviceStoppedHistory : R.string.serviceStoppedMessage;
            }
            createLogEvent(logMessage, numMessages++, priceOnly, System.currentTimeMillis() - startTime, Thread.currentThread().getId());
            progressBarStop();
            priceOnly = !(numMessages % 10 == 0);

        } catch (InterruptedException e) {
            Log.d(TAG, "Service has been interrupted");
        }

        Log.d(TAG, "Update Complete action=" + action+ " start location="+startLocation);
    }


    /**
     * Wrap AlarmSetup to allow replacement during Unit Test
     *
     * @return
     */
    AlarmSetup getAlarmSetup() {
        return new AlarmSetup(getApplicationContext(), notifier);
    }

    void createLogEventStart() {
        String scheduleType = getInputData().getString(SCHEDULE_TYPE);
        Resources res = getApplicationContext().getResources();
        String message;
        if (SCHEDULE_REPEATING.equals(scheduleType)) {
            message = res.getString(R.string.startTypeRepeating);
        } else if (SCHEDULE_ONE_TIME.equals(scheduleType)) {
            message = "oneTime";
        } else {
            message = "Unknown Schedule Type" + scheduleType;
            // return; // no logging
        }
        ContentValues values = new ContentValues();

        values.put(ServiceLogTable.COLUMN_MESSAGE, message);
        values.put(ServiceLogTable.COLUMN_SERVICE_TYPE, ServiceType.START.getIndex());
        values.put(ServiceLogTable.COLUMN_TIMESTAMP, DateTime.now().toString(ServiceLogTable.timestampFormat));

        insertServiceLog(values);
    }

    void createLogEvent(int messageKey, int numMessages, boolean priceOnly, long runtime, long threadId) {
        Resources res = getApplicationContext().getResources();
        ContentValues values = new ContentValues();
        values.put(ServiceLogTable.COLUMN_MESSAGE, res.getString(messageKey) + " t" + threadId);
        values.put(ServiceLogTable.COLUMN_SERVICE_TYPE, priceOnly ? ServiceType.PRICE.getIndex() : ServiceType.FULL.getIndex());
        values.put(ServiceLogTable.COLUMN_TIMESTAMP, DateTime.now().toString(ServiceLogTable.timestampFormat));
        values.put(ServiceLogTable.COLUMN_ITERATION, numMessages);
        values.put(ServiceLogTable.COLUMN_RUNTIME, runtime);

        insertServiceLog(values);
        TrackerUtil.sendTiming((InZone) getApplicationContext(), res.getString(messageKey), priceOnly ? ServiceType.PRICE.name() : ServiceType.FULL.name(), runtime);
    }

    void logServiceEvent(ServiceType serviceType, int stringId) {
        Resources res = getApplicationContext().getResources();
        ContentValues values = new ContentValues();
        values.put(ServiceLogTable.COLUMN_MESSAGE, res.getString(stringId));
        values.put(ServiceLogTable.COLUMN_SERVICE_TYPE, serviceType.getIndex());
        values.put(ServiceLogTable.COLUMN_TIMESTAMP, DateTime.now().toString(ServiceLogTable.timestampFormat));

        insertServiceLog(values);
    }

    /**
     * Wrap insert to allow replacement during unit test.
     *
     * @param values
     */
    void insertServiceLog(ContentValues values) {
        getApplicationContext().getContentResolver().insert(PaiContentProvider.SERVICE_LOG_URI, values);
    }

    void clearServiceLog() {
        String selection = ServiceLogTable.COLUMN_TIMESTAMP + " < ? ";
        String[] selectionArgs = {new DateTime().toString(ServiceLogTable.timestampFormat).substring(0, 10)};
        int rowsDeleted = getApplicationContext().getContentResolver().delete(PaiContentProvider.SERVICE_LOG_URI, selection, selectionArgs);
        Log.d(TAG, rowsDeleted + " Deleted Sevice Log Events Deleted " + selectionArgs[0]);
    }

    /*
     * String formatStartTime
     */
    String formatStartTime(DateTime startTime) {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        return fmt.print(startTime);
    }

    boolean isMarketOpen() {
        DateTime cal = getCurrentNYTime();
        int hour = cal.getHourOfDay();
        Log.d(TAG, "Is EST Hour of day (" + hour + ") between " + AlarmSetup.RUN_START_HOUR + " and " + AlarmSetup.RUN_END_HOUR + " " + cal.toString());
        boolean marketOpen = false; // set to true to ignore market
        if (hour >= AlarmSetup.RUN_START_HOUR && hour < AlarmSetup.RUN_END_HOUR && !Holiday.isHolidayOrWeekend(cal)) {
            marketOpen = true;

        }
        return marketOpen;
    }

    /**
     * getCurrentNYTime hook for Testing.
     *
     * @return
     */
    DateTime getCurrentNYTime() {
        return InZoneDateUtils.getCurrentNYTime();
    }

    void progressBarStart() {
        Intent intent = new Intent(BROADCAST_UPDATE_PROGRESS_BAR);
        // Add data
        intent.putExtra(PROGRESS_BAR_STATUS, 0);
        getApplicationContext().sendBroadcast(intent);
        Log.d(TAG, "Broadcast Progress Bar 0");
    }

    void progressBarStop() {
        Intent intent = new Intent(BROADCAST_UPDATE_PROGRESS_BAR);
        // Add data
        intent.putExtra(PROGRESS_BAR_STATUS, 100);
        getApplicationContext().sendBroadcast(intent);
        Log.d(TAG, "Broadcast Progress Bar 100");
    }

    boolean scanHistoryReloaded(List<Study> studies) {
        boolean historyReloaded = false;
        for (Study study : studies) {
            if (study.wasHistoryReloaded()) {
                historyReloaded = true;
            }
        }
        return historyReloaded;
    }

}
