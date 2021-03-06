package com.example.android.datasellertransactionstracker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.android.datasellertransactionstracker.data.TransactionContract.*;
import com.example.android.datasellertransactionstracker.data.TransactionDbHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddTransactionActivity extends AppCompatActivity {
    // Declaring the various UI components
    private EditText nameEditText, phoneEditText, unitEditText, costEditText, descriptionEditText;
    private Spinner mPaymentStateSpinner, mTitleSpinner;
    // Array adapters for the payment state spinner and title spinner each
    private ArrayAdapter titleSpinnerAdapter, paymentStateSpinnerAdapter;
    // Integer variable stores the state of the title spinner selected.
    private int mTitle;
    // Integer variable stores the state of the payment state spinner selected.
    private int mPaymentState;

    // Contains the item's Uri when we are editing
    Uri itemUri;
    // Boolean variable checks whether we are adding an entry or updating
    boolean editing;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // editing is false by default
        editing = false;
        // Bind the UI components
        nameEditText = findViewById(R.id.ed_name);
        phoneEditText = findViewById(R.id.ed_phone);
        unitEditText = findViewById(R.id.ed_unit);
        costEditText = findViewById(R.id.ed_cost);
        descriptionEditText = findViewById(R.id.ed_description);
        mPaymentStateSpinner = findViewById(R.id.payment_state_spinner);
        mTitleSpinner = findViewById(R.id.title_spinner);

        // Initialize the array adapters
         titleSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.title_options_array, android.R.layout.simple_dropdown_item_1line);

         paymentStateSpinnerAdapter = ArrayAdapter.createFromResource(this,
                 R.array.payment_state_options_array, android.R.layout.simple_dropdown_item_1line);

         // Set dropdown view resource on the array adapters
        titleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        paymentStateSpinnerAdapter.setDropDownViewResource(android.R.layout
                .simple_dropdown_item_1line);

        // set array adapters on the title spinner
        mTitleSpinner.setAdapter(titleSpinnerAdapter);
        // Set item selection listener on the title spinner
        mTitleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selection
                String selection = (String) parent.getItemAtPosition(position);
                // Check the state of the selection
                // If customer
                if (selection == getString(R.string.customer)) {
                    // Set mTitle to 1
                    mTitle = TransactionEntry.CUSTOMER;
                } else if (selection == getString(R.string.service_provider)) {
                    // If service provider
                    // Set mTitle to 2
                    mTitle = TransactionEntry.SERVICE_PROVIDER;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mTitle = 0;
            }
        });
        // Set adapter on the payment state spinner
        mPaymentStateSpinner.setAdapter(paymentStateSpinnerAdapter);
        // Set item selected listener
        mPaymentStateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selection
                String selection = (String) parent.getItemAtPosition(position);
                // If selection == paid
                if (selection == getString(R.string.paid)) {
                    // Set mPaymentState to 3
                    mPaymentState = TransactionEntry.PAID;
                } else if (selection == getString(R.string.pending)) {// If payment is pending
                    mPaymentState = TransactionEntry.PENDING;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mPaymentState = 0;
            }
        });

        // Set the title of the activity
        setTitle(R.string.add_transaction_label);
        // Get the intent that started this activity
        Intent starterIntent = getIntent();
        // Get the item's Uri
        itemUri = starterIntent.getData();
        // If the item's Uri is not null
        if (itemUri != null) {
            // Then we are editing
            editing = true;
            // Set activity title to edit transaction and populate the UI
            setTitle(R.string.edit_transaction_label);
            populateUIComponents(itemUri);
        }
    }

    private void populateUIComponents(Uri itemUri) {
        // Query the database
        Cursor cursor = getContentResolver().query(itemUri,
                null, // Get all columns
                null,
                null,
                null);
        // Move to the item's row on table
        cursor.moveToFirst();
        // Get all data from the cursor
        String name = cursor.getString(cursor.getColumnIndex(TransactionEntry.NAME));
        String phone = cursor.getString(cursor.getColumnIndex(TransactionEntry.PHONE));
        int paymentState = cursor.getInt(cursor.getColumnIndex(TransactionEntry.PAYMENT_STATE));
        int title = cursor.getInt(cursor.getColumnIndex(TransactionEntry.TITLE));
        String unit = cursor.getString(cursor.getColumnIndex(TransactionEntry.UNIT));
        int cost = cursor.getInt(cursor.getColumnIndex(TransactionEntry.COST));
        String description = cursor.getString(cursor.getColumnIndex(TransactionEntry.DESCRIPTION));

        // Set the name
        nameEditText.setText(name);
        // Set the phone
        phoneEditText.setText(phone);
        // Set the description
        descriptionEditText.setText(description);
        // Set the unit
        unitEditText.setText(unit);
        // Set the cost
        costEditText.setText(String.valueOf(cost));

        // Set the spinners' default selections based on existing data
        if (paymentState == TransactionEntry.PAID) {
            mPaymentStateSpinner.setSelection(paymentStateSpinnerAdapter
                    .getPosition(getString(R.string.paid)));
        } else {
            mPaymentStateSpinner.setSelection(paymentStateSpinnerAdapter
                    .getPosition(getString(R.string.pending)));
        }
        if (title == TransactionEntry.CUSTOMER) {
            mTitleSpinner.setSelection(titleSpinnerAdapter.getPosition(R.string.customer));
        } else {
            mTitleSpinner.setSelection(titleSpinnerAdapter.getPosition(R.string.service_provider));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_transaction_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Check selected item
        switch (item.getItemId()) {
            // If save
            case R.id.action_save:
                String name = nameEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();
                String unit = unitEditText.getText().toString().trim();
                int cost = Integer.parseInt(costEditText.getText().toString().trim());
                String description = descriptionEditText.getText().toString();

                // Insert new entry into database
                insertIntoDatabase(name, phone, unit, cost, description, mTitle, mPaymentState);
                break;
            case R.id.action_cancel:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertIntoDatabase(String name, String phone, String unit, int cost, String description,
                                   int title, int paymentState) {
        // Get current date
        Date date = new Date();
        // Set simple date format for both time and date
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:MM a");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM, yyyy");

        // Format the time and date
        String timeString = timeFormat.format(date);
        String dateString = dateFormat.format(date);
        // Create an instance of the database helper class
        TransactionDbHelper mDbHelper = new TransactionDbHelper(getApplicationContext());
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Instantiate a ContentValue object
        ContentValues values = new ContentValues();

        // Put values into it
        values.put(TransactionEntry.NAME, name);
        values.put(TransactionEntry.PHONE, phone);
        values.put(TransactionEntry.UNIT, unit);
        values.put(TransactionEntry.COST, cost);
        values.put(TransactionEntry.DESCRIPTION, description);
        values.put(TransactionEntry.TITLE, title);
        values.put(TransactionEntry.PAYMENT_STATE, paymentState);
        values.put(TransactionEntry.DATE, dateString);
        values.put(TransactionEntry.TIME, timeString);

        // Insert into database if editing otherwise update
        if (editing) {
            int rowsUpdated = getContentResolver().update(itemUri,
                    values,
                    null
                    ,null);
        } else {
            Uri newRowUri = getContentResolver().insert(TransactionEntry.CONTENT_URI, values);
        }
            // Go back home
            finish();
    }
}
