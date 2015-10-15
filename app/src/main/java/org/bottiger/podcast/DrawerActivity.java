package org.bottiger.podcast;

import org.bottiger.podcast.activities.downloadmanager.DownloadManagerActivity;
import org.bottiger.podcast.playlist.PlaylistData;
import org.bottiger.podcast.utils.TransitionUtils;
import org.bottiger.podcast.utils.UIUtils;
import org.bottiger.podcast.views.SlidingTab.SlidingTabLayout;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.util.LinkedList;
import java.util.List;

public abstract class DrawerActivity extends MediaRouterPlaybackActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String FEEDBACK = "[Feedback]"; // NoI18N

    protected SharedPreferences mSharedPreferences;

    protected DrawerLayout mDrawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;
    private Activity mActivity;

    protected ViewGroup mDrawerMainContent;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;
    private View mFragmentTop;
    private View mHeaderContainerBackground;
    private ViewPager mAppContent;
    private SlidingTabLayout mSlidingTabLayout;

    private int mFragmentTopPosition = -1;
    private ViewTreeObserver mVto;

    public List<TopFound> listeners = new LinkedList<>();
    public interface TopFound {
        void topfound(int i);
    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_drawer);
        mToolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        mFragmentTop = (View) findViewById(R.id.fragment_top);
        mHeaderContainerBackground = findViewById(R.id.header_container_background);
        mAppContent = (ViewPager) findViewById(R.id.app_content);
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mNavigationView.setNavigationItemSelectedListener(this);

        observeToolbarHeight(mFragmentTop);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                mToolbar, //R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open, /* "open drawer" description for accessibility */
                R.string.drawer_close /* "close drawer" description for accessibility */
        );
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        // if we can use windowTranslucentNavigation=true
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //RelativeLayout.MarginLayoutParams params2 = (RelativeLayout.MarginLayoutParams) mDrawerMainContent.getLayoutParams();
            //params2.topMargin = getStatusBarHeight(getResources());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mDrawerLayout.getLayoutParams();
            params.topMargin = getStatusBarHeight(getResources());
            mDrawerLayout.setLayoutParams(params);

            //mDrawerMainContent.setLayoutParams(params2);
        }

    }

    public void goFullScreen(@NonNull View argFullScreenView, @ColorInt int argColor) {
        mToolbar.bringToFront();
        UIUtils.tintStatusBar(argColor, this);
    }

    public void exitFullScreen(@NonNull View argFullScreenView) {
        UIUtils.resetStatusBar(this);
    }

    public int getFragmentTop() {
        return mFragmentTopPosition;
    }

    public int getSlidingTabsHeight() {
        return mSlidingTabLayout.getHeight();
    }

    private int getToolbarBottomPosition() {
        if (mFragmentTopPosition > 0) {
            return mFragmentTopPosition;
        }

        int[] location = new int[2];
        mFragmentTop.getLocationOnScreen(location);
        //return location[1];// - mFragmentTop.getHeight();// + mSlidingTabLayout.getHeight();
        return mToolbar.getHeight() + mSlidingTabLayout.getHeight();
    }


    // More details: http://blog.xebia.com/2015/06/09/android-design-support-navigationview/
    public boolean onNavigationItemSelected (MenuItem item) {

        boolean isHandled = false;

        switch (item.getItemId()) {
            case R.id.navigation_clear: {
                isHandled = true;
                PlaylistData pld = new PlaylistData();
                pld.reset = true;
                SoundWaves.getBus().post(pld);
                break;
            }
            case R.id.navigation_feedback: {
                isHandled = true;
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", ApplicationConfiguration.ACRA_MAIL, null)); // NoI18N
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, FEEDBACK);
                startActivity(Intent.createChooser(emailIntent, getString(R.string.feedback_mail_client_picker)));
                break;
            }
            case R.id.navigation_downloading: {
                isHandled = true;
                TransitionUtils.openDownloadManager(this);
                break;
            }
        }

        if (isHandled) {
            mDrawerLayout.closeDrawers();
        }

        return isHandled;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/*
		 * MenuInflater inflater = getMenuInflater();
		 * inflater.inflate(R.menu.main, menu);
		 */
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
	}

    private void observeToolbarHeight(@NonNull final View argToolbarContainer) {

        mVto = argToolbarContainer.getViewTreeObserver();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mVto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                @TargetApi(16)
                public void onGlobalLayout() {
                    argToolbarContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    setViewPagerTopPadding(argToolbarContainer);

                }
            });
        } else {
            mVto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    argToolbarContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    setViewPagerTopPadding(argToolbarContainer);

                }
            });
        }
    }

    private void setViewPagerTopPadding(@NonNull final View argToolbar) {
        mFragmentTopPosition = mToolbar.getHeight() + mSlidingTabLayout.getHeight(); //argToolbar.getTop();

        if (mFragmentTopPosition > 0) {
            for (TopFound topfound: listeners) {
                // FIXME: ensure this is correct
                topfound.topfound(mFragmentTopPosition);
            }
            listeners = new LinkedList<>();
        }
    }

}