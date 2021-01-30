package additive_animations.fragments.custom_drawing;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import at.wirecube.additiveanimations.additiveanimationsdemo.R;


public class CustomDrawingFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_custom_drawing, container, false);
        View v = new DemoView(root.getContext());
        root.addView(v);
        return root;
    }
}
