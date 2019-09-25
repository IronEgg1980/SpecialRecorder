package th.yzw.specialrecorder;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;

public abstract class MyActivity extends AppCompatActivity {

    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        if (resources != null && resources.getConfiguration().fontScale != 1.0f) {
            android.content.res.Configuration configuration = resources.getConfiguration();
            configuration.fontScale = 1.0f;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
        return resources;
    }
}
