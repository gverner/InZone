package com.codeworks.pai;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.db.model.Option;
import com.codeworks.pai.db.model.OptionType;
import com.codeworks.pai.db.model.Rules;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.processor.DownloadOptionTask;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by glennverner on 12/31/16.
 */

public abstract class StudyDetailFragmentBase extends Fragment {
    private static final String	TAG				= StudyDetailFragmentBase.class.getSimpleName();
    public static final String	ARG_STUDY_ID	= "arg_study_id";

    protected long studyId;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        long studyId = 0;
        if (getArguments() != null) {
            studyId = getArguments().getLong(ARG_STUDY_ID);
        }
        fillData(studyId);
    }
    private void fillData(Long id) {
        studyId = id;
        Uri uri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + id);
        Cursor cursor = getActivity().getContentResolver().query(uri, StudyTable.getFullProjection(), null, null, null);
        if (cursor != null)
            try {
                cursor.moveToFirst();
                Study security = StudyTable.loadStudy(cursor);

                ((TextView) getView().findViewById(R.id.sdfSymbol)).setText(security.getSymbol());
                ((TextView) getView().findViewById(R.id.sdfName)).setText(security.getName());
                populateView(security);

                // mSymbolText.setText(symbol);
            } catch (Exception e) {
                Log.e(TAG, "Exception reading Study from db ", e);
            } finally {
                // Always close the cursor
                cursor.close();
            }
    }

    abstract void populateView(Study study);

    void lookupOption(final Study study, Rules rules) {
        Option call = new Option(study.getSymbol(), OptionType.C, rules.AOACall(), DateTime.now());
        Option put = new Option(study.getSymbol(), OptionType.P, rules.AOBPut(), DateTime.now());
        DownloadOptionTask task = new DownloadOptionTask() {
            @Override
            protected void onPostExecute(List<Option> options) {
                if (!isAdded()) {
                    return;
                }
                SimpleDateFormat mmdd = new SimpleDateFormat("MM/dd");

                int row = 0;
                for (Option option : options) {
                    row++;
                    // Type Expires  Days Strike  Bid  ROI
                    // Call 12/20/14 15   211.00 3.00 4.5%
                    // Call 01/17/15 45   211.00 6.00 3.0%
                    // Put  12/20/14 15   202.00 0.41 4.5%
                    // Put  01/17/15 45   202.00 1.58 3.0%
                    //

                    double optionPrice = 0;
                    if (option.getBid() > 0) {
                        optionPrice = option.getBid();
                        setString(getResources().getString(R.string.sdfBidLabel), R.id.row0Bid);
                    } else {
                        optionPrice = option.getPrice();
                        setString(getResources().getString(R.string.sdfPriceLabel), R.id.row0Bid);
                    }

                    LocalDate expires = option.getExpires().toLocalDate();
                    if (expires.getDayOfWeek() == DateTimeConstants.SATURDAY) {
                        expires = expires.minusDays(1);
                    }
                    double days = Days.daysBetween(new DateTime().toLocalDate(), expires).getDays();
                    double roi = 0;
                    if (days > 0 && option.getStrike() > 0) {
                        roi = (((optionPrice / days) * 365) / option.getStrike()) * 100;
                    }
                    Log.d(TAG, option.getSymbol() + " " + option.getType() + " " + option.getStrike() + " " + option.getBid() + " " + option.getExpires() + " " + roi);
                    if (isAdded()) {
                        switch (row) {
                            case 1:
                                if (option.getError() != null && option.getError().length() > 0) {
                                    setString(option.getError(), R.id.rowError);
                                    TextView errorView = (TextView) getView().findViewById(R.id.rowError);
                                    errorView.setVisibility(View.VISIBLE);
                                }
                                setString(option.getType().getValue(), R.id.row1Type);
                                setString(mmdd.format(option.getExpires().toDate()), R.id.row1Expire);
                                setDouble(option.getStrike(), R.id.row1Strike);
                                setDouble(days, R.id.row1Days, 0);
                                setDouble(optionPrice, R.id.row1Bid);
                                setDouble(roi, R.id.row1Roi, 2);

                                break;
                            case 3:
                                setString(option.getType().getValue(), R.id.row2Type);
                                setString(mmdd.format(option.getExpires().toDate()), R.id.row2Expire);
                                setDouble(option.getStrike(), R.id.row2Strike);
                                setDouble(days, R.id.row2Days, 0);
                                setDouble(optionPrice, R.id.row2Bid);
                                setDouble(roi, R.id.row2Roi, 2);
                                break;
                            case 2:
                                setString(option.getType().getValue(), R.id.row3Type);
                                setString(mmdd.format(option.getExpires().toDate()), R.id.row3Expire);
                                setDouble(option.getStrike(), R.id.row3Strike);
                                setDouble(days, R.id.row3Days, 0);
                                setDouble(optionPrice, R.id.row3Bid);
                                setDouble(roi, R.id.row3Roi, 2);
                                break;
                            case 4:
                                setString(option.getType().getValue(), R.id.row4Type);
                                setString(mmdd.format(option.getExpires().toDate()), R.id.row4Expire);
                                setDouble(option.getStrike(), R.id.row4Strike);
                                setDouble(days, R.id.row4Days, 0);
                                setDouble(optionPrice, R.id.row4Bid);
                                setDouble(roi, R.id.row4Roi, 2);
                                break;
                        }

                    }
                }
                setProgressBar(100);
            }
        };
        task.execute(call, put);
        setProgressBar(0);
    }

    void setProgressBar(int value) {
        ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.progressBar1);
        if (value == 0) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    TextView setBackground(int viewId, int color) {
        TextView textView = (TextView) getView().findViewById(viewId);
        textView.setBackgroundColor(color);
        return textView;
    }

    TextView setDouble(double value, int viewId) {
        TextView textView = (TextView) getView().findViewById(viewId);
        textView.setText(Study.format(value));
        return textView;
    }

    TextView setDouble(double value, int viewId, int scale) {
        TextView textView = (TextView) getView().findViewById(viewId);
        textView.setText(new BigDecimal(value).setScale(scale, BigDecimal.ROUND_HALF_UP).toPlainString());
        return textView;
    }

    TextView setString(String value, int viewId) {
        TextView textView = (TextView) getView().findViewById(viewId);
        textView.setText(value);
        return textView;
    }

    double loadDemandZone(long id) {

        Uri uri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + id);
        Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{StudyTable.COLUMN_DEMAND_ZONE}, null, null, null);
        double demandZone = 0;
        if (cursor != null)
            try {
                cursor.moveToFirst();
                demandZone = cursor.getDouble(cursor.getColumnIndexOrThrow(StudyTable.COLUMN_DEMAND_ZONE));
            } catch (Exception e) {
                Log.e(TAG, "Exception reading Study from db ", e);
            } finally {
                // Always close the cursor
                cursor.close();
            }
        return demandZone;
    }

    protected void showDemandZoneInputDialog() {

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);


        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        double demandZone = loadDemandZone(studyId);
        if (Double.isNaN(demandZone) || demandZone == 0) {
            editText.setText("");
        } else {
            editText.setText(new BigDecimal(PaiUtils.round(demandZone)).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
        }
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String demandZone = editText.getText().toString();
                        if (demandZone != null && demandZone.trim().length() > 0) {
                            saveDemandZone(PaiUtils.round(Double.parseDouble(demandZone), 2));
                        } else {
                            saveDemandZone(0);
                        }
                        fillData(studyId);
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    private void saveDemandZone(double demandZone) {
        Uri uri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + studyId);

        ContentValues values = new ContentValues();
        values.put(StudyTable.COLUMN_DEMAND_ZONE, demandZone);
        //values.put(StudyTable.COLUMN_PDL2, readDouble(R.id.pdl2));
        //values.put(StudyTable.COLUMN_PDL1, readDouble(R.id.pdl1));

        if (uri != null) {
            // Update security
            getActivity().getContentResolver().update(uri, values, null, null);
        }
    }
}
