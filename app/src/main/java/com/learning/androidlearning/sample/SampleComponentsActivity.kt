package com.learning.androidlearning.sample

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.learning.androidlearning.R

/*
 * Java equivalent:
 *
 * public class SampleActivity extends AppCompatActivity {
 *     private TextView sampleTextView;
 *     private CheckBox sampleCheckBox;
 *     private EditText sampleEditText;
 *     private ProgressBar sampleProgressBar;
 *     private ImageView sampleImageView;
 *     private Button sampleButton;
 *
 *     @Override
 *     protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         setContentView(R.layout.activity_sample);
 *
 *         // Initialize views
 *         sampleTextView = findViewById(R.id.sampleTextView);
 *         sampleCheckBox = findViewById(R.id.sampleCheckBox);
 *         sampleEditText = findViewById(R.id.sampleEditText);
 *         sampleProgressBar = findViewById(R.id.sampleProgressBar);
 *         sampleImageView = findViewById(R.id.sampleImageView);
 *         sampleButton = findViewById(R.id.sampleButton);
 *
 *         // Set CheckBox click listener
 *         sampleCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 *             @Override
 *             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 *                 if (isChecked) {
 *                     sampleTextView.setText("CheckBox is checked");
 *                 } else {
 *                     sampleTextView.setText("CheckBox is unchecked");
 *                 }
 *             }
 *         });
 *
 *         // Set Button click listener
 *         sampleButton.setOnClickListener(new View.OnClickListener() {
 *             @Override
 *             public void onClick(View v) {
 *                 String inputText = sampleEditText.getText().toString();
 *                 if (!inputText.isEmpty()) {
 *                     try {
 *                         int progress = Integer.parseInt(inputText);
 *                         if (progress >= 0 && progress <= 100) {
 *                             sampleProgressBar.setProgress(progress);
 *                             Toast.makeText(SampleActivity.this, "Progress updated", Toast.LENGTH_SHORT).show();
 *                         } else {
 *                             Toast.makeText(SampleActivity.this, "Please enter a number between 0 and 100", Toast.LENGTH_SHORT).show();
 *                         }
 *                     } catch (NumberFormatException e) {
 *                         Toast.makeText(SampleActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
 *                     }
 *                 } else {
 *                     Toast.makeText(SampleActivity.this, "Please enter a progress value", Toast.LENGTH_SHORT).show();
 *                 }
 *             }
 *         });
 *     }
 * }
 */

class SampleComponentsActivity : AppCompatActivity() {

    private lateinit var sampleTextView: TextView
    private lateinit var sampleCheckBox: CheckBox
    private lateinit var sampleEditText: EditText
    private lateinit var sampleProgressBar: ProgressBar
    private lateinit var sampleImageView: ImageView
    private lateinit var sampleButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        // Initialize views
        sampleTextView = findViewById(R.id.sampleTextView)
        sampleCheckBox = findViewById(R.id.sampleCheckBox)
        sampleEditText = findViewById(R.id.sampleEditText)
        sampleProgressBar = findViewById(R.id.sampleProgressBar)
        sampleImageView = findViewById(R.id.sampleImageView)
        sampleButton = findViewById(R.id.sampleButton)

        // Set CheckBox click listener
        sampleCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sampleTextView.text = "CheckBox is checked"
            } else {
                sampleTextView.text = "CheckBox is unchecked"
            }
        }

        // Set Button click listener
        sampleButton.setOnClickListener {
            val inputText = sampleEditText.text.toString()
            if (inputText.isNotEmpty()) {
                val progress = inputText.toIntOrNull()
                if (progress != null && progress in 0..100) {
                    sampleProgressBar.progress = progress
                    Toast.makeText(this, "Progress updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter a number between 0 and 100", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a progress value", Toast.LENGTH_SHORT).show()
            }
        }
    }
}