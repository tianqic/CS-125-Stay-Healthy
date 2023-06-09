package com.example.finalversion1;

import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.finalversion1.R;
import android.content.Intent;
import android.net.Uri;
public class ResultDisplayFragment extends Fragment {
    public ResultDisplayFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.resultdisplay, container, false);

        String result = getArguments().getString("result");
        String searchtext = getArguments().getString("searchtext");
        TextView print1 = view.findViewById(R.id.result);
        print1.setText(result);

        Button returnButton = view.findViewById(R.id.returnbutton);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Return to GalleryFragment
                getParentFragmentManager().popBackStack();
            }
        });

        Button googleSearchButton = view.findViewById(R.id.googleSearchButton);
        googleSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = "https://www.google.com/search?q=" + searchtext;
                Uri uri = Uri.parse(query);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        return view;
    }

}
