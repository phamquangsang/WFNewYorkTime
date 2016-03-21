package app.com.phamsang.wfnewyorktime;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by Quang Quang on 3/19/2016.
 */
public class AdvancedSearchDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    NoticeDialogListener mListener;
    private TextView mBeginDate;
    private Spinner mOrder;
    private CheckBox isArts;
    private CheckBox isFashion;
    private CheckBox isSport;

    public static AdvancedSearchDialogFragment newInstance(int year, int month, int date, String order, boolean isArts, boolean isFashion, boolean isSports){
        AdvancedSearchDialogFragment instance = new AdvancedSearchDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("year",year);
        bundle.putInt("month",month);
        bundle.putInt("date",date);
        bundle.putString("order",order);
        bundle.putBoolean("is_arts",isArts);
        bundle.putBoolean("is_fashion",isFashion);
        bundle.putBoolean("is_sports", isSports);
        instance.setArguments(bundle);
        return instance;
    }




    public CheckBox getIsSport() {
        return isSport;
    }

    public CheckBox getIsFashion() {
        return isFashion;
    }

    public CheckBox getIsArts() {
        return isArts;
    }

    public Spinner getOrder() {
        return mOrder;
    }

    public TextView getBeginDate() {
        return mBeginDate;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mBeginDate.setText(year+"/"+(monthOfYear+1)+"/"+dayOfMonth);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle bundle = getArguments();
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View rootView = inflater.inflate(R.layout.advanced_search_layout, null);
        mBeginDate = (TextView) rootView.findViewById(R.id.textView_begin_date);
        Calendar calendar = Calendar.getInstance();
        final int year = bundle.getInt("year");
        final int month =  bundle.getInt("month");
        final int date =  bundle.getInt("date");
        mBeginDate.setText(year+"/"+(month+1)+"/"+date);
        mBeginDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),AdvancedSearchDialogFragment.this,year,month,date);
                datePickerDialog.show();
            }
        });
        mOrder = (Spinner) rootView.findViewById(R.id.spinner_order);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.orders, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mOrder.setAdapter(adapter);
        if(bundle.getString("order").equalsIgnoreCase("newest"))
            mOrder.setSelection(0,true);
        else
            mOrder.setSelection(1,true);

        isArts = (CheckBox) rootView.findViewById(R.id.checkBox_arts);
        isFashion = (CheckBox) rootView.findViewById(R.id.checkBox_fashion_style);
        isSport = (CheckBox) rootView.findViewById(R.id.checkBox_sport);

        isArts.setChecked(bundle.getBoolean("is_arts"));
        isFashion.setChecked(bundle.getBoolean("is_fashion"));
        isSport.setChecked(bundle.getBoolean("is_sports"));

        builder.setView(rootView)
                // Add action buttons
                .setPositiveButton(R.string.action_search, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(AdvancedSearchDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(AdvancedSearchDialogFragment.this);
                    }
                });
        return builder.create();
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    // Use this instance of the interface to deliver action events


    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener=null;
    }
}
