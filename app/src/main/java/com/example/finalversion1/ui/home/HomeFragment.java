package com.example.finalversion1.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.finalversion1.R;
import com.example.finalversion1.ResultDisplayFragment;
import com.example.finalversion1.databinding.FragmentGalleryBinding;
import com.example.finalversion1.databinding.FragmentHomeBinding;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Interpreter tflite;

    private void updateTextViews() {
        View root = binding.getRoot();
        SharedPreferences sharedPref =  getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        TextView print1 = root.findViewById(R.id.updatebmi);
        String storedbmi = sharedPref.getString("bmi", "");
        print1.setText(storedbmi);
        print1 = root.findViewById(R.id.updatemyactivity);
        String storedactivity = sharedPref.getString("activity", "");
        print1.setText(storedactivity);

        print1 = root.findViewById(R.id.updatefruit);
        String storedfruit = sharedPref.getString("fruit", "");
        print1.setText(storedfruit);

        print1 = root.findViewById(R.id.updatevegetables);
        String storedvegetables = sharedPref.getString("vegetables", "");
        print1.setText(storedvegetables);
        print1 = root.findViewById(R.id.updategeneral);
        String storedgeneral = sharedPref.getString("general", "");
        print1.setText(storedgeneral);

        print1 = root.findViewById(R.id.updatemental);
        String storedmental = sharedPref.getString("mental", "");
        print1.setText(storedmental);

        print1 = root.findViewById(R.id.updatephysical);
        String storedphysical = sharedPref.getString("physical", "");
        print1.setText(storedphysical);
    }
    @Override
    public void onResume() {
        super.onResume();
        updateTextViews();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button button = root.findViewById(R.id.buttonupdate);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref =  getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                TextView t = root.findViewById(R.id.updatebmi);
                String bmi = t.getText().toString();
                t = root.findViewById(R.id.updatemyactivity);
                String activity = t.getText().toString();
                t = root.findViewById(R.id.updatefruit);
                String fruit = t.getText().toString();
                t = root.findViewById(R.id.updatevegetables);
                String vegetables = t.getText().toString();
                t = root.findViewById(R.id.updategeneral);
                String general = t.getText().toString();
                t = root.findViewById(R.id.updatemental);
                String mental = t.getText().toString();
                t = root.findViewById(R.id.updatephysical);
                String physical = t.getText().toString();

                try {
                    tflite = new Interpreter(loadModelFile());
                }catch (Exception ex){
                    ex.printStackTrace();
                }

//                String[] inputStrings = new String[]{highbp, chol, cholcheck, bmi, cigare, stroke, heart, activity, fruit,
//                        vegetables, heacydrinker, healthcare, doctor, general, mental, physical,
//                        difficulty, sex, age, education, income};

                String[] inputNames = new String[]{"highbp", "chol", "cholcheck", "bmi", "cigare", "stroke", "heart", "activity", "fruit",
                        "vegetables", "heacydrinker", "healthcare", "doctor", "general", "mental", "physical",
                        "difficulty", "sex", "age", "education", "income"};

                boolean allInputsValid = true;

                float[][] input = new float[1][21];
                String[] inputstring = new String[21];
                List<String> list = new ArrayList<>();
                for (int i = 0; i < inputNames.length; i++) {
                    inputstring[i] = sharedPref.getString(inputNames[i], "");
                }
                inputstring[3] = bmi;
                inputstring[7] = activity;
                inputstring[8] = fruit;
                inputstring[9] = vegetables;
                inputstring[13] = general;
                inputstring[14] = mental;
                inputstring[15] = physical;

                for (int i = 0; i < inputNames.length; i++) {
                    try {
                        input[0][i] = Float.parseFloat(inputstring[i]);
                    } catch (NumberFormatException e) {
                        list.add(inputNames[i]);
                        allInputsValid = false;
                    }
                }
                String[] r = new String[]{"",""};
                String Sresult;
                if (allInputsValid) {
                    float result = doInference(input);
                    sharedPref = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    for (int i = 0; i < inputNames.length; i++) {
                        editor.putString(inputNames[i], inputstring[i]);
                    }
                    editor.apply();
                    r = formal_message(getSuggestion(input));
                    Sresult = "You have " + String.valueOf(result * 100.0) + "% risk to have diabetes. \n\n" + r[0];
                    SharedPreferences saveresult = getActivity().getSharedPreferences("MyResult", Context.MODE_PRIVATE);
                    editor = saveresult.edit();
                    editor.putString("savedresult", Sresult);
                    editor.putString("searchtext", r[1]);
                    editor.apply();
                } else {
                    String message = String.join(", ", list);
                    Sresult = String.format("Your response for %s is/are not given or is not a valid float.",message);
                }
                ResultDisplayFragment resultDisplayFragment = new ResultDisplayFragment();
                Bundle bundle = new Bundle();
                bundle.putString("result", Sresult);
                bundle.putString("searchtext", r[1]);
                resultDisplayFragment.setArguments(bundle);

                // Use FragmentManager to replace the current Fragment with the new one
                getParentFragmentManager().beginTransaction().replace(R.id.nav_host_fragment_content_main, resultDisplayFragment).addToBackStack(null).setReorderingAllowed(true).commit();
            }
        });




        return root;
    }
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getActivity().getAssets().openFd("model.tflite");
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=fileDescriptor.getStartOffset();
        long declareLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declareLength);
    }

    private float doInference(float input[][]) {
        float[][] output=new float[1][1];
        tflite.run(input,output);
        return output[0][0];
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(tflite != null) {
            tflite.close();
        }
    }
    private List<Float> getSuggestion(float input[][]){
        List<Float> floatList = new ArrayList<>();
        float p = 0.2f;

        float bestRes = 0;
        float bestRes_value = 1;
        float Inc = 0;

        float[][] temp= new float[1][21];
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 21; j++) {
                temp[i][j] = input[i][j];
            }
        }

        for (int i = 0; i < 21; i++){

            if (i == 0){

                if (temp[0][i] == 1){
                    temp[0][i] = 0;
                    float result = doInference(temp);
                    bestRes = i;
                    bestRes_value = result;
                    Inc = 0;
                    temp[0][i] = 1;
                }
            }

            if (i == 1){
                if (temp[0][i] == 1){
                    temp[0][i] = 0;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 0;
                        temp[0][i] = 1;
                    }
                }
            }

            if (i == 2){
                if (temp[0][i] == 0){
                    temp[0][i] = 1;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 1;
                        temp[0][i] = 0;
                    }
                }
            }

            if (i == 3){
                float original_bmi = temp[0][i];

                temp[0][i] = temp[0][i] * (1+p);
                if (temp[0][i] < 12){
                    temp[0][i] = 12;
                }
                if (temp[0][i] > 98){
                    temp[0][i] = 98;
                }
                float result0 = doInference(temp);
                temp[0][i] = original_bmi;

                temp[0][i] = temp[0][i] * (1-p);
                if (temp[0][i] < 12){
                    temp[0][i] = 12;
                }
                if (temp[0][i] > 98){
                    temp[0][i] = 98;
                }
                float result1 = doInference(temp);
                temp[0][i] = original_bmi;

                if (result0 <= result1){
                    if (result0 < bestRes_value){
                        bestRes = i;
                        bestRes_value = result0;
                        Inc = 1;
                    }
                }
                if (result1 < result0){
                    if (result1 < bestRes_value){
                        bestRes = i;
                        bestRes_value = result1;
                        Inc = 0;
                    }
                }
            }

            if (i == 4){
                if (temp[0][i] == 1){
                    temp[0][i] = 0;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 0;
                        temp[0][i] = 1;
                    }
                }
            }

            if (i == 5){
                if (temp[0][i] == 1){
                    temp[0][i] = 0;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 0;
                        temp[0][i] = 1;
                    }
                }
            }

            if (i == 6){
                if (temp[0][i] == 1){
                    temp[0][i] = 0;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 0;
                        temp[0][i] = 1;
                    }
                }
            }

            if (i == 7){
                if (temp[0][i] == 0){
                    temp[0][i] = 1;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 1;
                        temp[0][i] = 0;
                    }
                }
                if (temp[0][i] == 1){
                    temp[0][i] = 0;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 0;
                        temp[0][i] = 1;
                    }
                }
            }

            if (i == 8){
                if (temp[0][i] == 0){
                    temp[0][i] = 1;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 1;
                        temp[0][i] = 0;
                    }
                }
                if (temp[0][i] == 1){
                    temp[0][i] = 0;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 0;
                        temp[0][i] = 1;
                    }
                }
            }

            if (i == 9){
                if (temp[0][i] == 0){
                    temp[0][i] = 1;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 1;
                        temp[0][i] = 0;
                    }
                }
                if (temp[0][i] == 1){
                    temp[0][i] = 0;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 0;
                        temp[0][i] = 1;
                    }
                }
            }

            if (i == 10){
                if (temp[0][i] == 1){
                    temp[0][i] = 0;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 0;
                        temp[0][i] = 1;
                    }
                }
            }

            if (i == 11){
                if (temp[0][i] == 0){
                    temp[0][i] = 1;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 1;
                        temp[0][i] = 0;
                    }
                }
            }

            if (i == 12){
                if (temp[0][i] == 0){
                    temp[0][i] = 1;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 1;
                        temp[0][i] = 0;
                    }
                }
                if (temp[0][i] == 1){
                    temp[0][i] = 0;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 0;
                        temp[0][i] = 1;
                    }
                }
            }

            if (i == 14){
                float original_bmi = temp[0][i];

                temp[0][i] = temp[0][i] * (1+p);
                if (temp[0][i] < 0){
                    temp[0][i] = 0;
                }
                if (temp[0][i] > 30){
                    temp[0][i] = 30;
                }
                float result0 = doInference(temp);
                temp[0][i] = original_bmi;

                temp[0][i] = temp[0][i] * (1-p);
                if (temp[0][i] < 0){
                    temp[0][i] = 0;
                }
                if (temp[0][i] > 30){
                    temp[0][i] = 30;
                }
                float result1 = doInference(temp);
                temp[0][i] = original_bmi;
                if (result0 <= result1){
                    if (result0 < bestRes_value){
                        bestRes = i;
                        bestRes_value = result0;
                        Inc = 1;
                    }
                }
                if (result1 < result0){
                    if (result1 < bestRes_value){
                        bestRes = i;
                        bestRes_value = result1;
                        Inc = 0;
                    }
                }
            }

            if (i == 15){
                float original_bmi = temp[0][i];

                temp[0][i] = temp[0][i] * (1+p);
                if (temp[0][i] < 0){
                    temp[0][i] = 0;
                }
                if (temp[0][i] > 30){
                    temp[0][i] = 30;
                }
                float result0 = doInference(temp);
                temp[0][i] = original_bmi;

                temp[0][i] = temp[0][i] * (1-p);
                if (temp[0][i] < 0){
                    temp[0][i] = 0;
                }
                if (temp[0][i] > 30){
                    temp[0][i] = 30;
                }
                float result1 = doInference(temp);
                temp[0][i] = original_bmi;
                if (result0 <= result1){
                    if (result0 < bestRes_value){
                        bestRes = i;
                        bestRes_value = result0;
                        Inc = 1;
                    }
                }
                if (result1 < result0){
                    if (result1 < bestRes_value){
                        bestRes = i;
                        bestRes_value = result1;
                        Inc = 0;
                    }
                }
            }

            if (i == 16){
                if (temp[0][i] == 0){
                    temp[0][i] = 1;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 1;
                        temp[0][i] = 0;
                    }
                }
                if (temp[0][i] == 1){
                    temp[0][i] = 0;
                    float result = doInference(temp);
                    if (result < bestRes_value){
                        bestRes = i;
                        bestRes_value = result;
                        Inc = 0;
                        temp[0][i] = 1;
                    }
                }
            }
        }
        floatList.add(bestRes);
        floatList.add(bestRes_value);
        floatList.add(Inc);
        return floatList;
    }
    private String[] formal_message(List<Float> floatList){
        String search = "";
        String result = "";
        if (floatList.get(0) == 0){
            search = "How can I decrease my blood pressure?";
            result = "Main purpose: You should be aiming to decrease your blood pressure\n\n";
            result += "Activity: Regular physical activity is crucial for maintaining a healthy blood pressure level. Engage in moderate aerobic activities for at least 150 minutes per week or vigorous aerobic exercises for at least 75 minutes per week, according to your physical capabilities and health condition. Activities like walking, swimming, cycling, or even gardening can be included. Resistance training twice a week can also be beneficial. Remember, consistency is key. Start slow and gradually increase the duration and intensity of your workouts to avoid injury.\n\n" +
                    "Sleep: Adequate sleep is a key factor in regulating blood pressure. Aim for 7 to 9 hours of sleep per night. A consistent sleep schedule, going to bed and waking up at the same time every day, can greatly help. Also, make your sleep environment as comfortable as possible – dark, quiet, and cool. Try to avoid screens and stressful discussions right before bedtime, as these can interfere with your ability to fall asleep. In addition, addressing sleep disorders such as sleep apnea could have a significant impact on blood pressure levels.\n\n" +
                    "Food: Your diet plays a vital role in blood pressure management. Adopt the DASH (Dietary Approaches to Stop Hypertension) diet which emphasizes fruits, vegetables, whole grains, lean protein, and low-fat dairy products, while reducing sodium, red meat, sweets, and sugary beverages. Aim for a diet rich in potassium, magnesium, and fiber, and low in sodium. Maintaining a healthy weight by balancing your calorie intake with your physical activity level is also crucial to lower blood pressure. Remember to stay hydrated and limit alcohol intake.\n";
        }
        if (floatList.get(0) == 1){
            search = "How can I lower my cholesterol?";
            result = "Main purpose: You should be aiming to lower your cholesterol\n\n";
            result += "Activity: Regular physical activity is essential in lowering cholesterol levels. Strive for at least 30 minutes of exercise most days of the week, which can be as simple as taking a brisk walk, cycling, or swimming. Resistance training exercises, like weightlifting, at least twice a week can also be beneficial. This not only helps to increase your \"good\" HDL cholesterol but also decreases your \"bad\" LDL cholesterol and triglycerides. Consistency is crucial, so find an activity you enjoy which will make you more likely to stick with it.\n\n" +
                    "Sleep: Proper sleep plays a significant role in maintaining good health, including cholesterol levels. Aim for 7 to 9 hours of good quality sleep every night. A regular sleep schedule, with consistent times for going to bed and waking up, is beneficial. Poor sleep or sleep disorders like sleep apnea can negatively impact your body's ability to regulate lipid levels, so it's crucial to address these issues. Consider creating a peaceful and comfortable sleep environment, avoiding screens before bedtime, and managing stress levels for better sleep.\n\n" +
                    "Food: Your dietary choices have a direct impact on your cholesterol levels. Aim for a diet rich in soluble fiber and omega-3 fatty acids, and low in saturated and trans fats. Soluble fiber, found in foods like oats, fruits, vegetables, and legumes, can help reduce the absorption of cholesterol in your bloodstream. Omega-3 fatty acids, found in fatty fish, can lower your cholesterol levels. Reduce the intake of saturated fats, which are found in red meat and full-fat dairy products, and avoid trans fats, which are often in fried foods and commercial baked products. Incorporate more monounsaturated fats (found in olive oil, avocados, and nuts) into your meals, as they can help lower your \"bad\" LDL cholesterol while increasing your \"good\" HDL cholesterol.\n";
        }
        if (floatList.get(0) == 2){
            search = "Where can I do cholesterol check?";
            result = "Main purpose: You should do cholesterol check in each 5 years.\n\n";
            result += "Getting your cholesterol levels checked at least once every five years is an important aspect of maintaining your overall health and wellness. Cholesterol levels can subtly rise without noticeable symptoms, potentially increasing your risk of heart disease, stroke, and other serious health conditions over time. Regular monitoring allows you to stay on top of any changes and react appropriately, making necessary adjustments to your diet, exercise routine, or, if needed, beginning a medication regimen. Even if you feel healthy, remember that high cholesterol often shows no signs until it's too late. So consider this simple test as an essential part of your preventative healthcare routine, to help ensure a longer, healthier life. Don't wait for symptoms to appear - be proactive about your health and get your cholesterol checked regularly.\n";
        }
        if (floatList.get(0) == 3){
            if (floatList.get(2) == 0){
                search = "How can I decrease my body mass?";
                result = "Main purpose: You should decrease your body mass\n\n";
                result += "Activity: Regular physical activity is a crucial factor in reducing body mass and maintaining a healthy weight. Incorporate both aerobic exercises and strength training into your routine. Aerobic exercises, such as brisk walking, running, cycling or swimming, can help burn calories, while strength training can help build muscle mass, which can boost your metabolism and aid weight loss. Aim for at least 150 minutes of moderate aerobic activity or 75 minutes of vigorous activity each week, and do strength training exercises twice a week.\n\n" +
                        "Sleep: Adequate and quality sleep is essential for managing your weight. Poor sleep or lack of sleep can disrupt hormones that regulate appetite, leading to increased hunger and calorie intake. Aim for 7 to 9 hours of sleep each night and establish a consistent sleep schedule. Create a restful environment and limit exposure to screens before bedtime. If you have sleep problems, it may be beneficial to consult a healthcare professional, as untreated sleep disorders can hinder weight loss efforts.\n\n" +
                        "Food: When it comes to reducing body mass, diet plays an indispensable role. Opt for a balanced diet rich in fruits, vegetables, whole grains, and lean proteins. Limit intake of processed foods, sugary drinks, and high-fat and high-sugar snacks. Pay attention to portion sizes and try to eat smaller, balanced meals throughout the day to maintain a steady metabolism. Consider meal planning and prepping in advance to avoid resorting to unhealthy, convenient options. Lastly, ensure you are staying hydrated, as sometimes feelings of hunger are actually signs of dehydration.\n";
            }
            if (floatList.get(2) == 1){
                search = "How can I increase my body mass?";
                result = "Main purpose: You should increase your body mass\n\n";
                result += "Activity: When it comes to increasing your body mass, particularly in muscle, strength training exercises are the most effective. Incorporate weightlifting or resistance training into your routine at least 2-3 times a week. Activities that engage multiple muscle groups, like squats, deadlifts, or bench presses, can help stimulate muscle growth effectively. Remember to gradually increase the intensity of your workouts to prevent injury and to ensure progressive muscle growth.\n\n" +
                        "Sleep: Adequate sleep is critical for muscle recovery and growth. Aim for 7-9 hours of sleep each night. During deep sleep, your body releases growth hormones that help repair and build muscles, which is essential if you're trying to gain body mass. Ensure your sleep environment is dark, quiet, and cool, and consider establishing a bedtime routine to help signal your body that it's time to rest.\n\n" +
                        "Food: A balanced diet rich in protein, healthy fats, and complex carbohydrates can assist in gaining body mass. Protein is essential for muscle growth and repair, so include sources like lean meat, poultry, fish, eggs, dairy, legumes, and nuts in your meals. Carbohydrates and fats are important for providing the energy needed for workouts and daily activities. Don't forget fruits, vegetables, and whole grains, which provide essential vitamins and minerals for optimal body functioning. Aim to eat slightly more calories than your body uses daily, but make sure these extra calories come from nutrient-dense foods to promote healthy weight gain.\n";
            }
        }
        if (floatList.get(0) == 4){
            search = "How can I stop smoking?";
            result = "Main purpose: You should not smoke cigarettes.\n\n";
            result += "Activity: Engaging in regular physical activity can serve as a helpful strategy to curb cravings and reduce the desire to smoke. Exercise releases endorphins, often called \"feel-good\" hormones, which can alleviate symptoms of nicotine withdrawal and distract from the desire to smoke. Try incorporating moderate exercise into your daily routine, such as brisk walking, cycling, or swimming, for at least 30 minutes a day. Physical activity can also help in managing stress and maintaining a healthy weight, which are often concerns when quitting smoking.\n\n" +
                    "Sleep: Good sleep hygiene can play a significant role in the journey of quitting smoking. Lack of sleep can lead to increased cravings for cigarettes as your body tries to combat feelings of fatigue. Strive for 7 to 9 hours of quality sleep each night and maintain a consistent sleep schedule. A restful sleeping environment and a bedtime routine that promotes relaxation can improve sleep quality. If you're having trouble sleeping without nicotine, consult a healthcare provider for strategies to improve your sleep during this transition.\n\n" +
                    "Food: Maintaining a balanced diet can be an essential part of your strategy to stop smoking. Nicotine withdrawal can lead to increased hunger and cravings for unhealthy food, but by choosing nutrient-dense meals and snacks, you can help keep these cravings at bay. Staying hydrated, consuming small, frequent meals, and choosing high-fiber foods can help manage hunger and provide the energy needed to combat withdrawal symptoms. Foods like fruits, vegetables, lean proteins, and whole grains can help maintain your overall health and support your immune system, which is especially important during this transition.\n";
        }
        if (floatList.get(0) == 5){
            search = "How can I reduce the risk of stroking?";
            result = "Main purpose: What you should to reduce the risk of stroking.\n\n";
            result += "Activity: Regular physical activity is a key factor in reducing your risk of stroke. Strive for at least 150 minutes of moderate-intensity aerobic activity or 75 minutes of high-intensity activity each week, along with two or more days of muscle-strengthening activities. This could include activities like brisk walking, cycling, swimming, or weightlifting. Regular exercise helps to maintain a healthy weight, reduce blood pressure, and improve overall cardiovascular health, all of which lower your risk of stroke.\n\n" +
                    "Sleep: Getting sufficient quality sleep is crucial for reducing your stroke risk. Aim for 7-9 hours of sleep per night and maintain a consistent sleep schedule. Chronic sleep deprivation can lead to health issues like hypertension and obesity, which are risk factors for stroke. Additionally, untreated sleep disorders such as sleep apnea can increase your risk of stroke. If you suspect you have a sleep disorder, seek medical advice promptly.\n\n" +
                    "Food: Maintaining a healthy diet is a vital strategy in reducing your stroke risk. Embrace the DASH (Dietary Approaches to Stop Hypertension) or Mediterranean diets, both of which are rich in fruits, vegetables, whole grains, and lean proteins, and low in sodium and saturated fats. These diets help lower blood pressure and cholesterol levels, reducing your risk of stroke. Limit the intake of processed foods, sugary beverages, and alcohol. Drinking moderate to high amounts of alcohol can lead to a variety of health problems, including high blood pressure and stroke.\n";
        }
        if (floatList.get(0) == 6){
            search = "How can I prevent coronary heart disease?";
            result = "Main purpose: To prevent coronary heart disease.\n\n";
            result += "Activity: Regular physical activity is paramount in preventing coronary heart disease (CHD). Aim for at least 150 minutes of moderate-intensity or 75 minutes of high-intensity aerobic exercise each week, and try to include muscle-strengthening activities on two or more days a week. Activities like walking, cycling, swimming, or weightlifting can help control your weight, reduce blood pressure and cholesterol levels, and strengthen your heart, all of which are essential in preventing CHD.\n\n" +
                    "Sleep: Adequate sleep plays a crucial role in heart health. Chronic sleep deprivation or disturbances can lead to conditions like high blood pressure, obesity, and diabetes, all of which increase the risk of CHD. Strive for 7-9 hours of good-quality sleep each night, and ensure your sleeping environment is conducive to rest. If you struggle with sleep problems, such as sleep apnea, seek professional advice as untreated sleep disorders can contribute to heart disease.\n\n" +
                    "Food: A balanced, heart-healthy diet is a key strategy in preventing CHD. Emphasize fruits, vegetables, whole grains, lean proteins, and healthy fats in your diet, while reducing the intake of sodium, saturated and trans fats, and added sugars. The DASH (Dietary Approaches to Stop Hypertension) and Mediterranean diets are two well-studied dietary patterns known to promote heart health. Remember to also control portion sizes and aim for a healthy weight to further reduce your risk of CHD.\n";
        }
        if (floatList.get(0) == 7){
            if (floatList.get(2) == 0){
                search = "What can I do except exercising?";
                result = "Main purpose: You should do less exercise.\n\n";
                result += "Activity: While regular physical activity is important for overall health, it's also crucial to listen to your body and understand when you may be pushing yourself too hard. Overexertion can lead to injuries, hinder recovery, and negatively impact your immune system. Therefore, if you've been advised to do less exercise, consider switching to low-impact activities such as walking, swimming, or gentle yoga. These activities can help maintain fitness and flexibility without placing undue stress on your body. Also, remember to incorporate rest days into your routine to allow your body adequate time to recover.\n\n" +
                        "Sleep: Ensuring quality sleep is particularly important when reducing exercise. Adequate sleep supports physical recovery, mental health, and overall wellbeing. Aim for 7 to 9 hours of uninterrupted sleep per night. Make your sleep environment comfortable and free from distractions, and establish a bedtime routine to help your body and mind understand that it's time to rest. Reducing physical strain through less intensive exercise should help improve sleep quality.\n\n" +
                        "Food: With reduced physical activity, it's essential to adjust your diet to match your lower energy needs. Keep your meals balanced with an emphasis on whole grains, lean proteins, fruits, vegetables, and healthy fats, while reducing calorie-dense foods with little nutritional value. This ensures that you continue to get the necessary nutrients without overconsuming calories, which could lead to weight gain. Also, hydrate adequately and maintain regular eating schedules to support your overall health and well-being.\n";
            }
            if (floatList.get(2) == 1){
                search = "What evercise can I do?";
                result = "Main purpose: You should do more exercise.\n\n";
                result += "Activity: Increasing your physical activity is an excellent strategy for promoting overall health. Try to incorporate a mixture of cardio, strength training, and flexibility exercises into your routine. Cardio exercises like running, cycling, or swimming can boost your cardiovascular health, while strength training can build muscle and boost your metabolism. Activities like yoga or stretching can improve your flexibility and balance. Remember, it's not just about the quantity but also the quality of exercise. Start slow and gradually increase the intensity and duration to prevent injuries. Make sure to listen to your body and rest when needed.\n\n" +
                        "Sleep: As you ramp up your exercise routine, it's important to also ensure you're getting sufficient sleep. Exercise can naturally help improve sleep quality, but you must also prioritize rest to allow your body to recover. Aim for 7-9 hours of quality sleep per night. If you're exercising more, you may find that you need a bit more sleep than before to fully recover. Consider setting a consistent sleep schedule and creating a restful sleep environment to further promote good sleep.\n\n" +
                        "Food: When increasing your activity level, it's important to also adjust your diet to fuel your body. You'll likely need more energy, which means consuming more nutrient-dense foods. Focus on a balanced diet rich in lean proteins, complex carbohydrates, and healthy fats. Protein is especially important for muscle recovery and growth, so include good sources in your meals and snacks. Also, stay hydrated by drinking plenty of water, especially around workouts, to replace any fluids lost through sweat. Lastly, remember to listen to your body's hunger and fullness cues, as you may need to eat more to meet your increased energy needs.\n";
            }
        }
        if (floatList.get(0) == 8){
            if (floatList.get(2) == 0){
                search = "What food can I eat except fruits?";
                result = "Main purpose: You should consume less fruits per day.\n\n";
                result += "Activity: Even if you're reducing your fruit intake, maintaining a regular exercise routine remains crucial for overall health. Exercise helps regulate your metabolism, supports cardiovascular health, and boosts mood among other benefits. Choose activities you enjoy, from walking or cycling to more vigorous activities like swimming or weightlifting. If you're concerned about potential sugar imbalances from consuming less fruit, regular physical activity can help regulate your body's blood sugar levels.\n\n" +
                        "Sleep: Quality sleep is a vital component of a healthy lifestyle, regardless of your fruit consumption. Aim for 7-9 hours of sleep per night, and keep a regular sleep schedule. Create a sleep-friendly environment by keeping your room dark, quiet, and cool, and consider a bedtime routine that includes relaxation techniques such as reading or deep breathing. Even without the natural sugars from fruit that may have helped promote sleep, maintaining these habits will help ensure restful sleep.\n\n" +
                        "Food: If you're advised to reduce your fruit intake, you should replace the nutrients commonly provided by fruits with other foods. Fruits are rich in fiber, vitamins, and antioxidants, so consider increasing your intake of vegetables, whole grains, and legumes to make up for the shortfall. While fruit does contain natural sugars, it's generally a healthier choice than many other snacks. So, if you're reducing your fruit intake due to sugar content, remember to avoid replacing them with foods high in added sugars or unhealthy fats. Always aim for a balanced, nutrient-dense diet.\n";
            }
            if (floatList.get(2) == 1){
                search = "What fruits can I eat?";
                result = "Main purpose: You should consume more fruits per day.\n\n";
                result += "Activity: While increasing your fruit intake, it's equally important to maintain regular physical activity. Exercise can help regulate your metabolism and utilize the natural sugars in fruits effectively for energy. It's recommended to get at least 150 minutes of moderate-intensity or 75 minutes of vigorous-intensity physical activity each week, along with strength training activities on two or more days. You can choose activities that you enjoy and can sustain in the long run, such as walking, running, cycling, or swimming.\n\n" +
                        "Sleep: Quality sleep is vital for overall health and wellbeing. As you increase your fruit consumption, some fruits may even aid your sleep. For instance, kiwi and tart cherry juice are known to support better sleep. Aim for 7-9 hours of good-quality sleep per night. Make sure to establish a regular sleep schedule and create a conducive sleep environment—dark, quiet, and cool—to promote restful sleep.\n\n" +
                        "Food: If you're looking to consume more fruits, try to incorporate them into your meals and snacks throughout the day. Fruits are rich in essential nutrients, fiber, and antioxidants, and can add a lot of variety and flavor to your diet. Use them in your breakfast cereals, add them to your salads for lunch, or have them as snacks. Ensure your fruit choices are diverse to benefit from different nutrient profiles. However, remember to balance fruit intake with other food groups such as vegetables, lean proteins, whole grains, and healthy fats to ensure you're getting a wide range of nutrients necessary for optimal health.\n";
            }
        }
        if (floatList.get(0) == 9){
            if (floatList.get(2) == 0){
                search = "What food can I eat except vegetables?";
                result = "Main purpose: You should eat less vegetables per day.\n\n";
                result += "Activity: Regular physical activity is crucial for overall health, irrespective of vegetable intake. Exercise helps regulate your metabolism, enhances cardiovascular health, and supports mood among numerous other benefits. Choose activities you enjoy and can consistently practice over time such as walking, cycling, swimming, or weightlifting. Regardless of changes in your vegetable consumption, maintaining a consistent exercise routine can help balance energy intake and expenditure.\n\n" +
                        "Sleep: Quality sleep is paramount for good health and wellbeing. Regardless of your vegetable intake, it's important to strive for 7-9 hours of quality sleep each night, maintain a consistent sleep schedule, and create a conducive sleep environment that is dark, quiet, and cool. While some vegetables are known to aid sleep due to their nutrient content, other aspects of your diet and lifestyle can also contribute to good sleep hygiene.\n\n" +
                        "Food: If you're consuming an excess of vegetables and need to reduce your intake, it's essential to ensure the nutrients usually provided by vegetables are obtained from other sources. Vegetables are rich in fiber, vitamins, minerals, and antioxidants, so consider other nutrient-dense foods to make up for this change. Whole grains, legumes, and fruits can be good alternatives. Balancing your meals with appropriate amounts of protein and healthy fats will also support overall nutrition. However, it's crucial to consult with a healthcare provider or a registered dietitian before making significant changes to your diet to ensure all nutritional needs are met.\n";
            }
            if (floatList.get(2) == 1){
                search = "What vegetables can I eat?";
                result = "Main purpose: You should eat more vegetables per day.\n\n";
                result += "Activity: Maintaining regular physical activity is a vital part of a healthy lifestyle, no matter your vegetable intake. Exercise can help manage your weight, improve cardiovascular health, and boost mood among many other benefits. Aim for at least 150 minutes of moderate-intensity or 75 minutes of high-intensity aerobic activity each week, complemented with muscle-strengthening exercises on two or more days. Physical activities like walking, running, swimming, or weightlifting can help your body utilize the nutrients from the increased vegetable intake more effectively.\n\n" +
                        "Sleep: Ensuring quality sleep is essential for good health. Aim for 7-9 hours of sleep per night, keeping a consistent sleep schedule and creating a sleep-friendly environment that is dark, quiet, and cool. Increasing your vegetable intake can positively impact your sleep, as many vegetables contain nutrients like potassium and magnesium that can promote sleep.\n\n" +
                        "Food: If you're planning to increase your vegetable intake, aim to incorporate a variety of vegetables into your meals and snacks each day. This can ensure a wide range of nutrients in your diet. Vegetables are rich in vitamins, minerals, and fiber but low in calories, making them an excellent choice for overall health. Try adding them to dishes you already enjoy, like soups, stir-fries, or pasta, or have them as snacks with a healthy dip. However, remember to balance your vegetable intake with other food groups like whole grains, lean proteins, and healthy fats to ensure you're getting all the nutrients you need for optimal health.\n";
            }
        }
        if (floatList.get(0) == 10){
            search = "How can I abstained from alcohol?";
            result = "Main purpose: You should drink less alcohol per week.\n\n";
            result += "Food: Adopt a balanced and nutritious diet to support your efforts in reducing" +
                    " alcohol consumption. Opt for foods rich in vitamins, minerals, and antioxidants," +
                    " which can help replenish your body and support overall health. Include fruits, " +
                    "vegetables, whole grains, lean proteins, and healthy fats in your meals. " +
                    "Additionally, consuming foods that are high in water content, such as soups, salads," +
                    " and fruits, can keep you hydrated and minimize alcohol cravings.\n\n";
            result += "Activity: Engaging in regular physical activity can be an effective strategy" +
                    " to reduce alcohol consumption. Exercise releases endorphins, which boost mood" +
                    " and provide a natural sense of well-being, reducing the desire for alcohol as a" +
                    " coping mechanism. Find activities you enjoy, such as walking, jogging, dancing, " +
                    "or joining group exercise classes. \n\n";
            result += "Sleep: Prioritize getting sufficient and quality sleep as it plays a crucial role " +
                    "in reducing alcohol consumption. Poor sleep can increase stress levels and contribute" +
                    " to alcohol cravings as a means of relaxation.\n\n";

        }

        if (floatList.get(0) == 11){
            search = "Where can I buy health insurance?";
            result = "You should have a health care coverage like health insurance.\n\n";
            result += "Firstly, research and compare health insurance plans available in your region. " +
                    "Explore options through your employer, government programs, or private insurance providers. " +
                    "Assess the coverage details, including premiums, deductibles, copayments, and network providers.\n\n";
            result += "Secondly, if your employer offers health insurance, inquire about the available " +
                    "plans and any employer contributions. If you're self-employed or your employer " +
                    "doesn't provide coverage, explore individual or family health insurance plans.\n\n";
            result += "Thirdly, investigate government-sponsored programs like Medicaid or the Children's" +
                    " Health Insurance Program (CHIP) if you meet the eligibility criteria. These programs can " +
                    "provide affordable or free healthcare coverage for low-income individuals and families.\n\n";
            result += "Lastly, consider health insurance marketplaces or insurance brokers who can assist" +
                    " in finding suitable plans based on your needs and budget.\n";
        }

        if (floatList.get(0) == 12){
            search = "Where can I see doctor?";
            result = "You should see a doctor when necessary, regardless of costs.\n\n";
            result += "Health Priority: Prioritizing your health and well-being should be the primary" +
                    " motivation to see a doctor when necessary, regardless of costs. Your health is " +
                    "invaluable, and seeking medical attention when needed is essential for timely diagnosis," +
                    " treatment, and prevention of potential complications. Remember that early detection " +
                    "and intervention can often lead to better outcomes and potentially save you from more" +
                    " significant health issues down the line. Viewing your health as a top priority will " +
                    "help you overcome cost-related concerns and make informed decisions about seeking medical care.\n";
        }

        if (floatList.get(0) == 14){

            if (floatList.get(2) == 1){
                search = "How can I be more optimistic?";
                result = "Main purpose: You should be more optimistic.\n\n";
                result += "Positive Thinking: Cultivating a habit of positive thinking is key to becoming " +
                        "more optimistic. Challenge negative thoughts and replace them with positive " +
                        "affirmations or statements. Focus on your strengths, accomplishments, " +
                        "and positive aspects of your life. Surround yourself with uplifting and supportive " +
                        "people who encourage a positive mindset.\n\n";
                result += "Self-Care: Taking care of yourself physically, mentally, and emotionally " +
                        "plays a significant role in fostering optimism. Prioritize self-care activities" +
                        " that bring you joy and relaxation, such as engaging in hobbies, spending " +
                        "time in nature, practicing mindfulness or meditation, or indulging in activities " +
                        "that recharge your energy. Pay attention to your physical well-being by maintaining " +
                        "a balanced diet, exercising regularly, and getting sufficient sleep.\n\n";
                result += "Surroundings and Influences: Surrounding yourself with positive influences " +
                        "can have a profound impact on your outlook. Seek out supportive and optimistic" +
                        " individuals who inspire and uplift you. Limit exposure to negative news or toxic" +
                        " environments that can dampen your spirits. Instead, engage in activities or hobbies" +
                        " that inspire you and expose yourself to positive and motivational content, " +
                        "such as books, podcasts, or TED talks.\n";
            }
        }
        if (floatList.get(0) == 15){
            if (floatList.get(2) == 0){
                search = "How can I avoid physical injuries?";
                result = "Main purpose: You should avoid more physical injuries.\n\n";
                result += "Activity and Exercise: Engaging in regular physical activity and exercise" +
                        " is vital for avoiding further physical injuries. Focus on exercises that " +
                        "improve strength, flexibility, and balance, as these can help stabilize joints, " +
                        "enhance muscle support, and reduce the risk of falls or accidents. Gradually " +
                        "increase the intensity and duration of your workouts to allow your body to adapt" +
                        " and avoid overexertion.\n\n";
                result += "Warm-Up and Cool-Down: Prioritizing warm-up and cool-down routines is crucial for" +
                        " injury prevention. Before starting any exercise or physical activity, spend a few " +
                        "minutes engaging in light cardio, such as brisk walking or cycling, to increase blood " +
                        "flow and warm up your muscles. Follow this with dynamic stretches to loosen up the joints" +
                        " and muscles. After your workout, cool down with gentle stretching exercises to promote" +
                        " flexibility and gradually bring your heart rate back to normal. Proper warm-up" +
                        " and cool-down routines prepare your body for exercise and reduce the risk of muscle strains," +
                        " sprains, or other injuries.\n";
            }

        }

        if (floatList.get(0) == 16){
            if (floatList.get(2) == 0){
                search = "How can I resolve my walking difficulty?";
                result = "Main purpose: You should resolve your walking difficulty.\n\n";
                result += "Food: A healthy diet can play a significant role in improving walking difficulty. " +
                        "Focus on consuming foods that support joint health and reduce inflammation, " +
                        "such as omega-3 fatty acids found in fish like salmon, mackerel, and sardines." +
                        "Include nutrient-rich fruits and vegetables, whole grains, lean proteins, and low-fat" +
                        " dairy products to provide the necessary energy and nutrients for optimal mobility.\n\n";
                result += "Sleep: Sufficient and restorative sleep is essential for resolving walking difficulties. " +
                        "Establish a consistent sleep routine that includes a calming bedtime routine," +
                        " a comfortable sleep environment, and a regular sleep schedule. Avoid stimulating activities" +
                        " and electronics before bed, and create a peaceful atmosphere to promote relaxation.\n\n";
                result += "Physical activity: Engaging in appropriate physical activities and exercises can help " +
                        "alleviate walking difficulties. Consult with a healthcare professional or physical " +
                        "therapist to determine suitable exercises for your condition. Focus on exercises that improve " +
                        "strength, flexibility, and balance, such as walking, swimming, cycling, and gentle stretching.\n";

            }

        }
        String[] r = new String[]{result, search};
        return r;
    }
}
