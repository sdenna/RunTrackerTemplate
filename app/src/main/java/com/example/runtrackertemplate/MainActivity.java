package com.example.runtrackertemplate;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // constant to use for Log so we know which activity generated the Log entry
    private static final String TAG = "MainActivity";
    public static final String NAME_KEY = "name";
    public static final String DISTANCE_KEY = "distance";
    public static final String TIME_KEY = "time";

    // declare the references to XML elements in the Activity
    private Button btnAdd, btnView;
    private EditText nameEditText, distanceEditText, timeEditText;
    private TextView allRunsTextView;

    // reference to entire database, not just one collection in particular
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate the references to all view elements in XML file
        btnAdd = (Button) findViewById(R.id.addRunButton);
        btnView = (Button) findViewById(R.id.viewRunButton);
        nameEditText = (EditText) findViewById(R.id.name);
        distanceEditText = (EditText) findViewById(R.id.distance);
        timeEditText = (EditText) findViewById(R.id.time);
        allRunsTextView = (TextView) findViewById(R.id.allRunsTextView);
        // This line is needed in order to allow the scrolling to work.  You also need to set the
        // proper xml properties on the TextView as well.  But without this it won't scroll
        allRunsTextView.setMovementMethod(new ScrollingMovementMethod());
    }

    public void onClickAddButton(View v) {
        // Gets data the user entered
        String name = nameEditText.getText().toString();
        String distance = distanceEditText.getText().toString();
        String time = timeEditText.getText().toString();

        // Make sure that none of the fields are empty and leaves method if they are empty
        if (name.isEmpty() || distance.isEmpty() || time.isEmpty()) {
            toastMessage("Please fill in all text fields");
            return;
        }

        // Creates a key-value map of the object to add to the collection
        Map<String, Object> runToAdd = new HashMap<String, Object>();
        // Adds the all the key-value pairs to this object
        runToAdd.put(NAME_KEY, name);
        runToAdd.put(DISTANCE_KEY, distance);
        runToAdd.put(TIME_KEY, time);

        // adds this object to the collection called "runs" as defined globally
        db.collection("runs")
                .add(runToAdd)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        toastMessage("Run stored successfully");
                        resetEditTexts();
                        getData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        toastMessage("Run failed to add");
                    }
                });
    }

    /*
        This method demonstrates how we can access all the documents in the runs collection.
        This could be used to fill an ArrayList of RunEntry objects and then display them in
        a listview.

        *** NEED TO FIGURE OUT HOW TO PULL THE VALUES OF THE MAP OUT AS THEIR VALUES
     */

    public void getData() {
        db.collection("runs")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String str = "";
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                String nameData = document.getString(NAME_KEY);
                                String distanceData = document.getString(DISTANCE_KEY);
                                String timeData = document.getString(TIME_KEY);
                                str += nameData + " " + distanceData + " " + timeData + "\n";

                                // This is a VERY basic way of displaying the data - but it shows how to access it

                                // video mentions using this to get all data at once in a String
                                // object map (need to learn how to iterate through this
                                // Map<String, Object> myData = document.getData();

                                // There is also a toObject helper function this will take the data
                                // and attempt to create an object using the data specified.

                                // in order for it to work, the RunEntry class needs to have a
                                // constructor with NO parmaters
                                // RunEntry myRun = document.toObject(RunEntry.class);
                            }
                            allRunsTextView.setText(str);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void resetEditTexts() {
        // clears the edit text fields in case more data entry follows
        nameEditText.setText("");
        distanceEditText.setText("");
        timeEditText.setText("");
    }


    /**
     * Customizable toast message
     * @param message
     */

    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void onClickViewRuns(View view) {
        getData();
    }
}