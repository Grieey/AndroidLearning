package com.learning.androidlearning

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.learning.androidlearning.sample.SampleComponentsActivity
import com.learning.androidlearning.sample.SampleLayoutActivity
import com.learning.androidlearning.sample.profile.SampleProfileActivity
import com.learning.androidlearning.sample.SampleViewPagerActivity
import com.learning.androidlearning.sample.SampleViewPagerActivityJavaVersion
import com.learning.androidlearning.sample.customview.SampleCustomViewActivity
import com.learning.androidlearning.sample.profile.SampleProfileActivityJavaVersion

/*
 * Java equivalent:
 *
 * public class MainActivity extends AppCompatActivity {
 *     @Override
 *     protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         enableEdgeToEdge();
 *         setContentView(R.layout.activity_main);
 *         ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
 *             WindowInsetsCompat.Type.systemBars();
 *             Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
 *             v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
 *             return insets;
 *         });
 *
 *         Button button = findViewById(R.id.buttonToSampleActivity);
 *         button.setOnClickListener(v -> {
 *             Intent intent = new Intent(MainActivity.this, SampleActivity.class);
 *             startActivity(intent);
 *         });
 *     }
 * }
 */

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Add a button to navigate to SampleActivity
        val buttonToSample = findViewById<Button>(R.id.buttonToSampleActivity)
        buttonToSample.setOnClickListener {
            val intent = Intent(this, SampleComponentsActivity::class.java)
            startActivity(intent)
        }

        // Add a button to navigate to SampleLayoutActivity
        val buttonToLayout = findViewById<Button>(R.id.buttonToLayoutActivity)
        buttonToLayout.setOnClickListener {
            val intent = Intent(this, SampleLayoutActivity::class.java)
            startActivity(intent)
        }

        // Add a button to navigate to SampleProfileActivity
        val buttonToProfile = findViewById<Button>(R.id.buttonToProfileActivity)
        buttonToProfile.setOnClickListener {
            val intent = Intent(this, SampleProfileActivityJavaVersion::class.java)
            startActivity(intent)
        }

        // Add a button to navigate to SampleViewPagerActivity
        val buttonToViewPager = findViewById<Button>(R.id.buttonToViewPagerActivity)
        buttonToViewPager.setOnClickListener {
            val intent = Intent(this, SampleViewPagerActivityJavaVersion::class.java)
            startActivity(intent)
        }

        // Add a button to navigate to SampleCustomViewActivity
        val buttonToCustomView = findViewById<Button>(R.id.buttonToCustomViewActivity)
        buttonToCustomView.setOnClickListener {
            val intent = Intent(this, SampleCustomViewActivity::class.java)
            startActivity(intent)
        }

        // test
    }
}