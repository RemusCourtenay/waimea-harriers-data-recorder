package com.example.waimeaHarriersDataInput;

import android.accounts.Account;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.api.services.sheets.v4.model.ValueRange;

import java.util.List;

public class InputDataActivity extends Activity {

    // Bundle key for input number
    private static final String BIB_NUM_BUNDLE_KEY = "bib_num";

    private int bibNumber;

    private Button mDecrementFinishNumButton;

    private EditText mInputBibNumberEditText;
    private TextView mOutputNameTextView;
    private Spinner mOutputCompetitorTypeSpinner;
    private CheckBox mOutputOwnTimeCheckBox;
    private EditText mOutputOwnTimeEditText;

    private SheetReader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_data);

        // Getting bib num from last session if possible
        if (savedInstanceState != null) {
            bibNumber = savedInstanceState.getParcelable(BIB_NUM_BUNDLE_KEY);
        }

        // Getting account from previous activity
        Account mAccount = getIntent().getParcelableExtra("UserAccount");
        reader = new SheetReader(mAccount);

        // Getting views from ID


        mInputBibNumberEditText = findViewById(R.id.input_data_edit_bib_number);
        mOutputNameTextView = findViewById(R.id.input_data_name_output);
        mOutputCompetitorTypeSpinner = findViewById(R.id.input_data_competitor_type_spinner);
        mOutputOwnTimeCheckBox = findViewById(R.id.input_data_own_time_check_box);
        mOutputOwnTimeEditText = findViewById(R.id.input_data_own_time_edit_time);




        mInputBibNumberEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                getSheetsData(Integer.parseInt(s.toString()));
            }
        });
    }

    private void getSheetsData(int inputNum) {
        reader.getDataRow(inputNum, this);
    }


    public void onSheetsReturnedData(ValueRange rowData) {
        List<Object> singleRowList = rowData.getValues().get(0);
        String name = (String) singleRowList.get(0);
        String competitorType = (String) singleRowList.get(1);
        String ownTimeBoolean = (String) singleRowList.get(2);
        String ownTimeRecordedTime = (String) singleRowList.get(3);
        mOutputNameTextView.append(name);
        mOutputCompetitorTypeSpinner.setSelection(convertCompetitorTypeToInt(competitorType));
        mOutputOwnTimeCheckBox.setChecked(convertOwnTimeToBoolean(ownTimeBoolean));
        if (mOutputOwnTimeCheckBox.isChecked()) {
            mOutputOwnTimeEditText.append(ownTimeRecordedTime);
        }
    }

    private int convertCompetitorTypeToInt(String competitorType) {
        switch (competitorType) {
            case "ER":
                return 0;
            case "R":
                return 1;
            case "W":
                return 2;
            default:
                throw new RuntimeException("Unexpected competitor type: " + competitorType);
        }
    }

    private boolean convertOwnTimeToBoolean(String ownTimeBoolean) {
        switch (ownTimeBoolean) {
            case "":
                return false;
            case "ot":
                return true;
            default:
                throw new RuntimeException("Unexpected own time boolean value: " + ownTimeBoolean);
        }
    }

}
