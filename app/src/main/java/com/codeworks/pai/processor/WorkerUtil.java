package com.codeworks.pai.processor;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import static com.codeworks.pai.processor.UpdateWorker.SCHEDULE_ONE_TIME;
import static com.codeworks.pai.processor.UpdateWorker.SCHEDULE_REPEATING;
import static com.codeworks.pai.processor.UpdateWorker.SCHEDULE_TYPE;

public class WorkerUtil {

    public static OneTimeWorkRequest startSingle(String serviceAction, String startLocation) {
        Constraints updateWorkerConstraints = new Constraints.Builder().build();
                //.setRequiredNetworkType(NetworkType.CONNECTED)
                //.setRequiresBatteryNotLow(true).build();
        OneTimeWorkRequest.Builder updateWorker = new OneTimeWorkRequest.Builder(UpdateWorker.class);

        Data input = new Data.Builder()
                .putString(UpdateWorker.SERVICE_ACTION, serviceAction)
                .putString(SCHEDULE_TYPE, SCHEDULE_ONE_TIME)
                .putString(UpdateWorker.SERVICE_START_LOCATION, startLocation).build();

        OneTimeWorkRequest updateWorkerRequest = updateWorker.setConstraints(updateWorkerConstraints).setInputData(input).build();
        WorkManager.getInstance().enqueue(updateWorkerRequest);
        return updateWorkerRequest;
    }

    public static PeriodicWorkRequest startRepeating(String serviceAction, String startLocation) {
        Constraints updateWorkerConstraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).setRequiresBatteryNotLow(true).build();
        PeriodicWorkRequest.Builder updateWorker = new PeriodicWorkRequest.Builder(UpdateWorker.class, 10, TimeUnit.MINUTES);

        Data input = new Data.Builder().putString(UpdateWorker.SERVICE_ACTION, serviceAction)
                .putString(SCHEDULE_TYPE, SCHEDULE_REPEATING)
                .putString(UpdateWorker.SERVICE_START_LOCATION, startLocation).build();

        PeriodicWorkRequest updateWorkerRequest = updateWorker.setConstraints(updateWorkerConstraints).setInputData(input).build();
        WorkManager.getInstance().enqueue(updateWorkerRequest);
        return updateWorkerRequest;
    }

    public static void cancel(UUID id) {
        WorkManager.getInstance().cancelWorkById(id);
    }


}
