package id.genta.ramadhan.latihan13;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // Deklarasi variabel untuk sensor dan UI
    private SensorManager sensorManager;
    private Sensor accelerometer, proximity, gyroscope;
    private TextView accelText, accelInfoText, proxText, proxStatusText, gyroText, gyroInfoText;
    private ProgressBar accelXProgress, accelYProgress, accelZProgress;
    private ImageView gyroArrow;
    private float lastAccel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi UI
        accelText = findViewById(R.id.accel_text);
        accelInfoText = findViewById(R.id.accel_info_text);
        accelXProgress = findViewById(R.id.accel_x_progress);
        accelYProgress = findViewById(R.id.accel_y_progress);
        accelZProgress = findViewById(R.id.accel_z_progress);
        proxText = findViewById(R.id.prox_text);
        proxStatusText = findViewById(R.id.prox_status_text);
        gyroText = findViewById(R.id.gyro_text);
        gyroInfoText = findViewById(R.id.gyro_info_text);
        gyroArrow = findViewById(R.id.gyro_arrow);

        // Inisialisasi SensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Inisialisasi sensor
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Cek dan daftarkan sensor
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            accelInfoText.setText("Gerakkan perangkat ke kiri/kanan, atas/bawah, atau depan/belakang");
        } else {
            accelText.setText("Accelerometer tidak tersedia");
            accelInfoText.setText("Sensor tidak tersedia");
        }

        if (proximity != null) {
            sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
            proxStatusText.setText("Status: Menunggu data...");
        } else {
            proxText.setText("Proximity tidak tersedia");
            proxStatusText.setText("Status: Tidak tersedia");
        }

        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
            gyroInfoText.setText("Putar perangkat ke kiri/kanan untuk melihat perubahan arah");
            gyroArrow.setImageResource(R.drawable.ic_clockwise); // Default ikon
        } else {
            gyroText.setText("Gyroscope tidak tersedia");
            gyroInfoText.setText("Sensor tidak tersedia");
            gyroArrow.setVisibility(ImageView.GONE);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // Ambil nilai X, Y, Z
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float magnitude = (float) Math.sqrt(x * x + y * y + z * z);

                accelText.setText(String.format(
                        "Accelerometer\nX: %.1f m/s²\nY: %.1f m/s²\nZ: %.1f m/s²", x, y, z));

                // Update ProgressBar untuk setiap sumbu (skala 0-100, maks 20 m/s²)
                accelXProgress.setProgress((int) (Math.min(Math.abs(x), 20) / 20 * 100));
                accelYProgress.setProgress((int) (Math.min(Math.abs(y), 20) / 20 * 100));
                accelZProgress.setProgress((int) (Math.min(Math.abs(z), 20) / 20 * 100));

                // Deteksi guncangan
                if (lastAccel > 0 && Math.abs(magnitude - lastAccel) > 10) {
                    Toast.makeText(this, "Perangkat diguncang!", Toast.LENGTH_SHORT).show();
                }
                lastAccel = magnitude;
                break;

            case Sensor.TYPE_PROXIMITY:
                // Ambil jarak dan perbarui status
                float distance = event.values[0];
                proxText.setText(String.format("Proximity: %.1f cm", distance));

                if (distance < 5) {
                    proxStatusText.setText("Status: Objek Dekat (misalnya, tangan di dekat layar)");
                    proxStatusText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_close, 0, 0, 0);
                    proxStatusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    proxStatusText.setText("Status: Objek Jauh");
                    proxStatusText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_far, 0, 0, 0);
                    proxStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                }
                break;

            case Sensor.TYPE_GYROSCOPE:
                // Ambil nilai rotasi X, Y, Z
                float rotX = event.values[0];
                float rotY = event.values[1];
                float rotZ = event.values[2];

                gyroText.setText(String.format(
                        "Gyroscope\nX: %.1f rad/s\nY: %.1f rad/s\nZ: %.1f rad/s", rotX, rotY, rotZ));

                // Perbarui ikon dan teks berdasarkan rotasi sumbu X
                if (Math.abs(rotX) > 0.5) {
                    if (rotX > 0) {
                        gyroInfoText.setText("Putar perangkat: Searah jarum jam");
                        gyroArrow.setImageResource(R.drawable.ic_clockwise);
                    } else {
                        gyroInfoText.setText("Putar perangkat: Berlawanan jarum jam");
                        gyroArrow.setImageResource(R.drawable.ic_counterclockwise);
                    }
                } else {
                    gyroInfoText.setText("Putar perangkat ke kiri/kanan untuk melihat perubahan arah");
                    gyroArrow.setImageResource(R.drawable.ic_clockwise); // Default
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Tidak digunakan
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Daftarkan kembali sensor
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (proximity != null) {
            sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hentikan sensor untuk hemat baterai
        sensorManager.unregisterListener(this);
    }
}
