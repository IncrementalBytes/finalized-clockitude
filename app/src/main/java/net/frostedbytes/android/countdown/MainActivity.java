package net.frostedbytes.android.countdown;

import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import net.frostedbytes.android.countdown.fragments.CountdownFragment;
import net.frostedbytes.android.countdown.fragments.CreateEventFragment;
import net.frostedbytes.android.countdown.fragments.EventListFragment;
import net.frostedbytes.android.countdown.models.EventSummary;
import net.frostedbytes.android.countdown.utils.LogUtils;

import java.util.ArrayList;

import static net.frostedbytes.android.countdown.BaseActivity.BASE_TAG;

public class MainActivity extends AppCompatActivity implements
    EventListFragment.OnEventListListener,
    CreateEventFragment.OnCreateEventListener {

    private static final String TAG = BASE_TAG + MainActivity.class.getSimpleName();

    private ArrayList<EventSummary> mEventSummaries;
    private ListenerRegistration mEventListener;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtils.debug(TAG, "++onCreate(Bundle)");
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // look for user events to list
        mUserId = getIntent().getStringExtra(BaseActivity.ARG_USER_ID);
        if (mUserId == null || mUserId.isEmpty()) {
            // TODO: handle unknown user
        } else {
            getEventList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        LogUtils.debug(TAG, "++onCreateOptionsMenu(Menu)");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LogUtils.debug(TAG, "++onDestroy()");
        if (mEventListener != null) {
            mEventListener.remove();
        }

        mEventSummaries = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        LogUtils.debug(TAG, "++onOptionsItemSelected(MenuItem)");
        // Handle action bar
        // item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_home:
            case R.id.action_list:
                getEventList();
                break;
            case R.id.action_create:
                replaceFragment(CreateEventFragment.newInstance(mEventSummaries));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateEvent() {

        LogUtils.debug(TAG, "++onCreateEvent()");
        replaceFragment(CreateEventFragment.newInstance(mEventSummaries));
    }

    @Override
    public void onDeleteEvent(EventSummary eventSummary) {

        LogUtils.debug(TAG, "++onDeleteEvent(EventSummary)");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(EventSummary.ROOT).document(eventSummary.EventId).delete()
            .addOnSuccessListener(aVoid -> LogUtils.debug(TAG, "Deleted event: %s", eventSummary.EventId))
            .addOnFailureListener(e -> {
                LogUtils.error(TAG, "Could not delete event: %s; %s", eventSummary.EventId, e.getMessage());
                Snackbar.make(
                    findViewById(R.id.main_fragment_container),
                    getString(R.string.err_delete_event),
                    Snackbar.LENGTH_LONG).show();
            });
    }

    @Override
    public void onSelected(EventSummary eventSummary) {

        LogUtils.debug(TAG, "++onSelected(EventSummary)");
        replaceFragment(CountdownFragment.newInstance(eventSummary));
    }

    @Override
    public void onEventCreated(EventSummary eventSummary) {

        LogUtils.debug(TAG, "++onEventCreated(EventSummary)");
        eventSummary.UserId = mUserId;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(EventSummary.ROOT).add(eventSummary.toMap())
            .addOnSuccessListener(documentReference -> LogUtils.debug(TAG, "Event written with ID: " + documentReference.getId()))
            .addOnFailureListener(e -> LogUtils.warn(TAG, "Error adding event", e));
        getEventList();
    }

    private void getEventList() {

        mEventSummaries = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mEventListener = db.collection(EventSummary.ROOT)
            .whereEqualTo("UserId", mUserId)
            .addSnapshotListener((snapshots, e) -> {

                LogUtils.debug(TAG, "++addSnapshotListener::onEvent()");
                if (e != null) {
                    LogUtils.warn(TAG, "Collection listening failed.", e);
                    return;
                }

                assert snapshots != null;
                EventSummary active = null;
                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            LogUtils.debug(TAG, "New event: " + dc.getDocument().getData());
                            EventSummary summary = dc.getDocument().toObject(EventSummary.class);
                            summary.EventId = dc.getDocument().getId();
                            if (!mEventSummaries.contains(summary)) {
                                mEventSummaries.add(summary);
                            }

                            if (summary.IsActive) {
                                active = summary;
                            }
                            break;
                        case MODIFIED:
                            LogUtils.debug(TAG, "Modified event: " + dc.getDocument().getData());
                            // TODO: handle modifications
                            break;
                        case REMOVED:
                            LogUtils.debug(TAG, "Removed event: " + dc.getDocument().getData());
                            EventSummary delete = dc.getDocument().toObject(EventSummary.class);
                            mEventSummaries.remove(delete);
                            break;
                    }
                }

                if (mEventSummaries.size() > 0) {
                    if (active != null) {
                        replaceFragment(CountdownFragment.newInstance(active));
                    } else {
                        replaceFragment(EventListFragment.newInstance(mEventSummaries));
                    }
                } else {
                    replaceFragment(EventListFragment.newInstance(mEventSummaries));
                    Snackbar snackbar = Snackbar.make(
                        findViewById(R.id.main_fragment_container),
                        getString(R.string.no_events),
                        Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("Create", v -> {
                        snackbar.dismiss();
                        replaceFragment(CreateEventFragment.newInstance(mEventSummaries));
                    });
                    snackbar.show();
                }
            });
    }

    private void replaceFragment(Fragment fragment) {

        LogUtils.debug(TAG, "++replaceFragment(Fragment)");
        String backStateName = fragment.getClass().getName();
        FragmentManager fragmentManager = getSupportFragmentManager();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStateName, 0);
        if (!fragmentPopped) { //fragment not in back stack, create it.
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.main_fragment_container, fragment);
            fragmentTransaction.addToBackStack(backStateName);
            fragmentTransaction.commit();
        }
    }
}
