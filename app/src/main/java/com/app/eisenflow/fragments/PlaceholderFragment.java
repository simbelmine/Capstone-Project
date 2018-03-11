package com.app.eisenflow.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.eisenflow.R;

import static com.app.eisenflow.utils.Constants.ARG_SECTION_NUMBER;

/**
 * A placeholder fragment containing a simple view.
 *
 * Created on 12/18/17.
 */

public class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    int[] layouts = new int[] {R.layout.splashscreen_two, R.layout.splashscreen_three};
    private int number;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        number = getArguments() != null ? getArguments().getInt(ARG_SECTION_NUMBER) : 1;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(layouts[number], container, false);
        return rootView;
    }
}
