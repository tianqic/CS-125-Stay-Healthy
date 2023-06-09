package com.example.finalversion1.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.finalversion1.R;
import com.example.finalversion1.databinding.FragmentGalleryBinding;
import org.tensorflow.lite.Interpreter;
import android.util.Log;
import android.content.res.AssetFileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;
import android.content.Context;

import com.example.finalversion1.ResultDisplayFragment;

public class GalleryFragment extends Fragment {
    private FragmentGalleryBinding binding;
    private Interpreter tflite;

    private void updateTextViews() {
        View root = binding.getRoot();
        SharedPreferences sharedPref = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        TextView print1 = root.findViewById(R.id.getbp);
        String storedbp = sharedPref.getString("highbp", "");
        print1.setText(storedbp);

        print1 = root.findViewById(R.id.getchol);
        String storedchol = sharedPref.getString("chol", "");
        print1.setText(storedchol);

        print1 = root.findViewById(R.id.getcholcheck);
        String storedcholcheck = sharedPref.getString("cholcheck", "");
        print1.setText(storedcholcheck);

        print1 = root.findViewById(R.id.getbmi);
        String storedbmi = sharedPref.getString("bmi", "");
        print1.setText(storedbmi);

        print1 = root.findViewById(R.id.getcigare);
        String storedcigare = sharedPref.getString("cigare", "");
        print1.setText(storedcigare);

        print1 = root.findViewById(R.id.getstroke);
        String storedstroke = sharedPref.getString("stroke", "");
        print1.setText(storedstroke);

        print1 = root.findViewById(R.id.getheart);
        String storedheart = sharedPref.getString("heart", "");
        print1.setText(storedheart);

        print1 = root.findViewById(R.id.getmyactivity);
        String storedactivity = sharedPref.getString("activity", "");
        print1.setText(storedactivity);

        print1 = root.findViewById(R.id.getfruit);
        String storedfruit = sharedPref.getString("fruit", "");
        print1.setText(storedfruit);

        print1 = root.findViewById(R.id.getvegetables);
        String storedvegetables = sharedPref.getString("vegetables", "");
        print1.setText(storedvegetables);

        print1 = root.findViewById(R.id.getheacydrinker);
        String storedheacydrinker = sharedPref.getString("heacydrinker", "");
        print1.setText(storedheacydrinker);

        print1 = root.findViewById(R.id.gethealthcare);
        String storedhealthcare = sharedPref.getString("healthcare", "");
        print1.setText(storedhealthcare);

        print1 = root.findViewById(R.id.getdoctor);
        String storeddoctor = sharedPref.getString("doctor", "");
        print1.setText(storeddoctor);

        print1 = root.findViewById(R.id.getgeneral);
        String storedgeneral = sharedPref.getString("general", "");
        print1.setText(storedgeneral);

        print1 = root.findViewById(R.id.getmental);
        String storedmental = sharedPref.getString("mental", "");
        print1.setText(storedmental);

        print1 = root.findViewById(R.id.getphysical);
        String storedphysical = sharedPref.getString("physical", "");
        print1.setText(storedphysical);

        print1 = root.findViewById(R.id.getdifficulty);
        String storeddifficulty = sharedPref.getString("difficulty", "");
        print1.setText(storeddifficulty);

        print1 = root.findViewById(R.id.getsex);
        String storedsex = sharedPref.getString("sex", "");
        print1.setText(storedsex);

        print1 = root.findViewById(R.id.getage);
        String storedage = sharedPref.getString("age", "");
        print1.setText(storedage);

        print1 = root.findViewById(R.id.geteducation);
        String storededucation = sharedPref.getString("education", "");
        print1.setText(storededucation);

        print1 = root.findViewById(R.id.getincome);
        String storedincome = sharedPref.getString("income", "");
        print1.setText(storedincome);
    }
    @Override
    public void onResume() {
        super.onResume();
        updateTextViews();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        Button button = root.findViewById(R.id.getvalues);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView t = root.findViewById(R.id.getsex);
                String sex = t.getText().toString();
                t = root.findViewById(R.id.getage);
                String age = t.getText().toString();
                t = root.findViewById(R.id.getbp);
                String highbp = t.getText().toString();
                t = root.findViewById(R.id.getchol);
                String chol = t.getText().toString();
                t = root.findViewById(R.id.getcholcheck);
                String cholcheck = t.getText().toString();
                t = root.findViewById(R.id.getbmi);
                String bmi = t.getText().toString();
                t = root.findViewById(R.id.getcigare);
                String cigare = t.getText().toString();
                t = root.findViewById(R.id.getstroke);
                String stroke = t.getText().toString();
                t = root.findViewById(R.id.getheart);
                String heart = t.getText().toString();
                t = root.findViewById(R.id.getmyactivity);
                String activity = t.getText().toString();
                t = root.findViewById(R.id.getfruit);
                String fruit = t.getText().toString();
                t = root.findViewById(R.id.getvegetables);
                String vegetables = t.getText().toString();
                t = root.findViewById(R.id.getheacydrinker);
                String heacydrinker = t.getText().toString();
                t = root.findViewById(R.id.gethealthcare);
                String healthcare = t.getText().toString();
                t = root.findViewById(R.id.getdoctor);
                String doctor = t.getText().toString();
                t = root.findViewById(R.id.getgeneral);
                String general = t.getText().toString();
                t = root.findViewById(R.id.getmental);
                String mental = t.getText().toString();
                t = root.findViewById(R.id.getphysical);
                String physical = t.getText().toString();
                t = root.findViewById(R.id.getdifficulty);
                String difficulty = t.getText().toString();
                t = root.findViewById(R.id.geteducation);
                String education = t.getText().toString();
                t = root.findViewById(R.id.getincome);
                String income = t.getText().toString();


                String[] inputStrings = new String[]{highbp, chol, cholcheck, bmi, cigare, stroke, heart, activity, fruit,
                        vegetables, heacydrinker, healthcare, doctor, general, mental, physical,
                        difficulty, sex, age, education, income};

                String[] inputNames = new String[]{"highbp", "chol", "cholcheck", "bmi", "cigare", "stroke", "heart", "activity", "fruit",
                        "vegetables", "heacydrinker", "healthcare", "doctor", "general", "mental", "physical",
                        "difficulty", "sex", "age", "education", "income"};

                boolean allInputsValid = true;

                float[][] input = new float[1][21];
                List<String> list = new ArrayList<>();

                for (int i = 0; i < inputStrings.length; i++) {
                    try {
                        input[0][i] = Float.parseFloat(inputStrings[i]);
                    } catch (NumberFormatException e) {
                        list.add(inputNames[i]);
                        allInputsValid = false;
                    }
                }
                String r = "";
                if (allInputsValid) {
                    SharedPreferences sharedPref = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    for (int i = 0; i < inputNames.length; i++) {
                        editor.putString(inputNames[i], String.valueOf(input[0][i]));
                    }
                    editor.apply();
                }

            }
        });

        return root;
    }
}