package com.codeworks.pai;


/**
 * Created by glennverner on 4/26/15.
 */
public class InZone extends android.app.Application {
    static final String PROPERTY_ID = "UA-62292119-1";

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */

    public InZone() {
        super();
    }



}
