package org.bottiger.podcast.TestUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.ViewInteraction;
import android.support.v7.util.SortedList;

import com.jakewharton.espresso.OkHttp3IdlingResource;

import org.bottiger.podcast.ApplicationConfiguration;
import org.bottiger.podcast.R;
import org.bottiger.podcast.SoundWaves;
import org.bottiger.podcast.provider.ItemColumns;
import org.bottiger.podcast.provider.PodcastOpenHelper;
import org.bottiger.podcast.provider.Subscription;
import org.bottiger.podcast.provider.SubscriptionColumns;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import okhttp3.OkHttpClient;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by aplb on 17-09-2015.
 */
public class TestUtils {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LEFT, RIGHT})
    public @interface Direction {}

    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    public static void waitForSubscriptionRefresh(@NonNull Activity argActivity) {
        Context context = argActivity.getApplicationContext();
        OkHttpClient client = SoundWaves.getAppContext(context).getRefreshManager().getHttpClient();
        IdlingResource resource = OkHttp3IdlingResource.create("OkHttp", client);

        Espresso.registerIdlingResources(resource);
    }

    public static void firstRun(@NonNull Context argContext, boolean argFirstRun) {
        SharedPreferences sharedPref = argContext.getSharedPreferences(ApplicationConfiguration.packageName, Context.MODE_PRIVATE);
        String key = argContext.getString(R.string.preference_first_run_key);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, argFirstRun);
        editor.commit();
    }

    private static void clearSettings(@NonNull Context argContext) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(argContext);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();

    }

    private static void clearEpisodes(@NonNull Context argContext) {
        PodcastOpenHelper helper = PodcastOpenHelper.getInstance(argContext);
        io.requery.android.database.sqlite.SQLiteDatabase database = helper.getWritableDatabase();
        database.execSQL("DELETE FROM " + ItemColumns.TABLE_NAME);
    }

    private static void clearSubscriptions(@NonNull Context argContext) {
        PodcastOpenHelper helper = PodcastOpenHelper.getInstance(argContext);
        io.requery.android.database.sqlite.SQLiteDatabase database = helper.getWritableDatabase();
        database.execSQL("DELETE FROM " + SubscriptionColumns.TABLE_NAME);
    }

    public static void clearDatabase(@NonNull Context argContext) {
        clearSubscriptions(argContext);
        clearEpisodes(argContext);
    }

    public static void clearAllData(@NonNull Context argContext) {
        clearDatabase(argContext);
        clearSettings(argContext);
    }

    /**
     * This assumes to be called from the viewpager
     * It returns to the subscription fragment
     *
     * @param argAmount
     * @return true if everything goes well
     */
    public static boolean subscribe(int argAmount) {
        //ViewPagerMove(RIGHT);
        //ViewPagerMove(RIGHT);

        ViewInteraction appCompatTextView1 = onView(
                allOf(withText("DISCOVER"), isDisplayed()));
        appCompatTextView1.perform(click());


        int subscriptionCount = argAmount;

        for (int i = 0; i < subscriptionCount; i++) {
            RecyclerTestUtils.subscribeToPodcast(i);
            /*
            onView(withId(R.id.search_result_view))
                    .perform(RecyclerTestUtils.actionOnItemViewAtPosition(i, R.id.result_subscribe_switch, click()));
                    */
        }

        /*
        ViewPagerMove(LEFT);

        onView(withId(R.id.gridview)).check(matches(isDisplayed()));

        for (int i = 0; i < subscriptionCount; i++) {
            RecyclerTestUtils.withRecyclerView(R.id.gridview).atPosition(i).matches(isDisplayed());
        }
        */

        return true;
    }

    public static void ViewPagerMove(@Direction int argDirection) {

        // USefull because to have to swipe the opposite direction you want to move
        switch (argDirection) {
            case LEFT: {
                onView(withId(R.id.app_content))
                        .perform(swipeRight());
                break;
            }
            case RIGHT: {
                onView(withId(R.id.app_content))
                        .perform(swipeLeft());
                break;
            }
        }
    }

    public static void unsubscribeAll(@NonNull Activity argActivity) {
        SortedList<Subscription> list = SoundWaves.getAppContext(argActivity).getLibraryInstance().getSubscriptions();
        for(int i = 0; i < list.size(); i++) {
            // get the object by the key.
            Subscription subscription = list.get(i);
            //subscription.unsubscribe(argActivity);
            SoundWaves.getAppContext(argActivity).getLibraryInstance().unsubscribe(subscription.getURLString(), "TestUtil:UnsubscribeAll");
        }
    }
}
