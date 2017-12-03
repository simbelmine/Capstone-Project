package eisenflow.app.com.eisenflow;

import android.app.Application;
import com.facebook.stetho.Stetho;

/**
 * ApplicationEisenFlow is an extension of Android's Application class.
 * Declarations in this class can be needed if are needed:
 * - Specialized tasks that need to run before the creation of your first activity.
 * - Global initializations that needs to be shared across all components (crash reports, persistence).
 * - Static methods for easy access to static immutable data such as a shared network client oject.
 *
 * Created on 12/3/17.
 */

public class ApplicationEisenFlow extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * Stetho is a debug bridge for Android applications. When enabled,
         * developers have access to the Chrome Developer Tools feature
         * natively part of the Chrome desktop browser.
         */
        Stetho.initializeWithDefaults(this);
    }
}
