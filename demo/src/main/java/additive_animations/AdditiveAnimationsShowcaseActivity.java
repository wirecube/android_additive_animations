package additive_animations;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import additive_animations.fragments.AnimationChainingDemoFragment;
import additive_animations.fragments.CustomAnimationsWithoutSubclassDemoFragment;
import additive_animations.fragments.ExpandingButtons2DemoFragment;
import additive_animations.fragments.MarginsDemoFragment;
import additive_animations.fragments.MoveAlongPathDemoFragment;
import additive_animations.fragments.MultipleViewsAnimationDemoFragment;
import additive_animations.fragments.RepeatingChainedAnimationsDemoFragment;
import additive_animations.fragments.TapToChangeColorDemoFragment;
import additive_animations.fragments.TapToMoveDemoFragment;
import additive_animations.fragments.custom_drawing.CustomDrawingFragment;
import additive_animations.fragments.states.StateDemoFragment;
import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;

public class AdditiveAnimationsShowcaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static boolean ADDITIVE_ANIMATIONS_ENABLED = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additive_animations_showcase);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // load default fragment = tap to move
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new TapToMoveDemoFragment()).commit();
        Switch additiveEnabledSwitch = (Switch) findViewById(R.id.additive_animations_enabled_switch);
        additiveEnabledSwitch.setOnClickListener(v -> ADDITIVE_ANIMATIONS_ENABLED = ((Switch) v).isChecked());

        // set the default duration for all animations:
        AdditiveAnimator.setDefaultDuration(1000);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.additive_animations_showcase, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_tap_to_move) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TapToMoveDemoFragment()).commit();
        } else if (id == R.id.nav_multiple_views) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MultipleViewsAnimationDemoFragment()).commit();
        } else if (id == R.id.nav_margins) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MarginsDemoFragment()).commit();
        } else if (id == R.id.nav_color) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new TapToChangeColorDemoFragment()).commit();
        } else if (id == R.id.nav_path) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MoveAlongPathDemoFragment()).commit();
        } else if (id == R.id.nav_chaining) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AnimationChainingDemoFragment()).commit();
        } else if (id == R.id.nav_chaining_repeated) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RepeatingChainedAnimationsDemoFragment()).commit();
        } else if (id == R.id.nav_change_text_color) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CustomAnimationsWithoutSubclassDemoFragment()).commit();
        } else if (id == R.id.nav_custom_drawing) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CustomDrawingFragment()).commit();
        } else if (id == R.id.nav_state) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StateDemoFragment()).commit();
        } else if (id == R.id.nav_expanding_buttons) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ExpandingButtons2DemoFragment()).commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}