package com.example.waimeaHarriersDataInput;

import android.accounts.Account;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.services.sheets.v4.model.ValueRange;

import java.util.List;

public class InputDataActivity extends Activity {

    // Bundle key for input number
    private static final String INPUT_NUM_BUNDLE_KEY = "input_num";

    private int inputNum;

    private EditText mInputNumberEditText;
    private TextView mOutputNameTextView;
    private TextView mOutputCompetitorTypeTextView;

    private SheetReader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_data);

        // Getting input num from last session if possible
        if (savedInstanceState != null) {
            inputNum = savedInstanceState.getParcelable(INPUT_NUM_BUNDLE_KEY);
        }

        // Getting account from previous activity
        Account mAccount = (Account) getIntent().getParcelableExtra("UserAccount");
        reader = new SheetReader(mAccount);

        // Getting views from ID
        mInputNumberEditText = findViewById(R.id.input_data_choose_number);
        mOutputNameTextView = findViewById(R.id.input_data_name_output);
        mOutputCompetitorTypeTextView = findViewById(R.id.input_data_competitor_type_output);




        mInputNumberEditText.addTextChangedListener(new TextWatcher() {

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
        mOutputNameTextView.append(name);
        mOutputCompetitorTypeTextView.append(competitorType);
    }

}
