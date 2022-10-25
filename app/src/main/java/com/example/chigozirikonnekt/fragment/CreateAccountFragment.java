package com.example.chigozirikonnekt.fragment;

import static com.example.chigozirikonnekt.fragment.LogInFragment.validate;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chigozirikonnekt.FragmentReplacerActivity;
import com.example.chigozirikonnekt.MainActivity;
import com.example.chigozirikonnekt.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CreateAccountFragment extends Fragment {
    private EditText nameET, emailET, passwordET, confirmPassET;
    private ProgressBar progressBar;
    private TextView loginTV;
    private Button signUpBtn;
    private FirebaseAuth auth;

    public static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean validate(String emailStr) {
        Matcher matcher = EMAIL_REGEX.matcher(emailStr);
        return matcher.find();
    }



    public CreateAccountFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        clickListener();
    }

    private void init(View view){
        nameET = view.findViewById(R.id.nameET);
        emailET = view.findViewById(R.id.emailET);
        passwordET = view.findViewById(R.id.password_ET);
        confirmPassET = view.findViewById(R.id.confirmPassET);
        loginTV = view.findViewById(R.id.loginTV);
        signUpBtn = view.findViewById(R.id.signupBtn);
        progressBar = view.findViewById(R.id.progressBar);


        auth = FirebaseAuth.getInstance();


    }

    private void clickListener(){
        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ((FragmentReplacerActivity) getActivity()).setFragment(new LogInFragment());
                } catch (ApiException e) {
                    e.printStackTrace();
                }

            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameET.getText().toString();
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();
                String confirmPassword = confirmPassET.getText().toString();

                if (name.isEmpty() || name.equals(" ")){
                    nameET.setError("Please Enter your name here");
                    return;
                }
                if (email.isEmpty() || validate(email) == false){
                    emailET.setError("Please Enter a valid email address");
                    return;
                }
                if (password.isEmpty() || password.length() < 6){
                    passwordET.setError("Please Enter a valid password here");
                    return;
                }
                if (!confirmPassword.equals(password)){
                    confirmPassET.setError("Error!, Please re-enter your password");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                createAccount(name, email, password);
            }
        });
    }
    private void createAccount(final String name, final String email, String password){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            user.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Toast.makeText(getContext(), "Email verification link sent", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                            uploadUser(user, name, email);

                        } else {
                            progressBar.setVisibility(View.GONE);
                            String exception = task.getException().getMessage();
                            Toast.makeText(getContext(), "Error: " + exception, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
    private void uploadUser(FirebaseUser user, String name, String email){
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("profileImage", "");
        map.put("uid", user.getUid());
        map.put("following", 0);
        map.put("followers", 0);
        map.put("status", " ");

        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            assert getActivity() != null;
                            startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                            getActivity().finish();

                        }else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Error: "+task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }

}



















