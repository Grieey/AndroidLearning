package com.learning.androidlearning

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.learning.androidlearning.home.HomeActivity
import com.learning.androidlearning.sample.SampleComponentsActivity
import com.learning.androidlearning.sample.SampleLayoutActivity
import com.learning.androidlearning.sample.SampleViewPagerActivityJavaVersion
import com.learning.androidlearning.sample.customview.SampleCustomViewActivity
import com.learning.androidlearning.sample.danmu.DanmuDemoActivity
import com.learning.androidlearning.sample.database.SampleDatabaseActivity
import com.learning.androidlearning.sample.dialog.DialogDemoActivity
import com.learning.androidlearning.sample.flipclock.FlipClockDemoActivity
import com.learning.androidlearning.sample.gradient.GradientTextDemoActivity
import com.learning.androidlearning.sample.profile.SampleProfileActivityJavaVersion
import com.learning.androidlearning.sample.schedule.ScheduleActivityWithJava
import com.learning.androidlearning.sample.schedule.ScheduleGridActivity

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

        // Add a button to navigate to com.learning.androidlearning.home.HomeActivity
        val buttonToHome = findViewById<Button>(R.id.buttonToHomeActivity)
        buttonToHome.setOnClickListener { HomeActivity.start(this) }

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

        // Add a button to navigate to database sample
        val buttonToDatabase = findViewById<Button>(R.id.buttonToDatabaseActivity)
        buttonToDatabase.setOnClickListener {
            val intent = Intent(this, SampleDatabaseActivity::class.java)
            startActivity(intent)
        }

        // Add after other buttons
        val buttonToSchedule = findViewById<Button>(R.id.buttonToScheduleActivity)
        buttonToSchedule.setOnClickListener {
            val intent = Intent(this, ScheduleGridActivity::class.java)
            startActivity(intent)
        }

        // Add a button to navigate to ScheduleActivity
        val buttonToTimeTable = findViewById<Button>(R.id.buttonToTimeTableActivity)
        buttonToTimeTable.setOnClickListener {
            val intent = Intent(this, ScheduleActivityWithJava::class.java)
            startActivity(intent)
        }

        // Add a button to navigate to DanmuDemoActivity
        val buttonToDanmu = findViewById<Button>(R.id.buttonToDanmuActivity)
        buttonToDanmu.setOnClickListener {
            val intent = Intent(this, DanmuDemoActivity::class.java)
            startActivity(intent)
        }

        // Add a button to navigate to DialogDemoActivity
        val buttonToDialog = findViewById<Button>(R.id.buttonToDialogActivity)
        buttonToDialog.setOnClickListener {
            val intent = Intent(this, DialogDemoActivity::class.java)
            startActivity(intent)
        }

        // Add a button to navigate to FlipClockDemoActivity
        val buttonToFlipClock = findViewById<Button>(R.id.buttonToFlipClockActivity)
        buttonToFlipClock.setOnClickListener {
            val intent = Intent(this, FlipClockDemoActivity::class.java)
            startActivity(intent)
        }

        // Add a button to navigate to GradientTextDemoActivity
        val buttonToGradientText = findViewById<Button>(R.id.buttonToGradientTextActivity)
        buttonToGradientText.setOnClickListener {
            val intent = Intent(this, GradientTextDemoActivity::class.java)
            startActivity(intent)
        }
    }
}
