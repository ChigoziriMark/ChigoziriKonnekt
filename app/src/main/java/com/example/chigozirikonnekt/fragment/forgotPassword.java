package com.example.chigozirikonnekt.fragment;

import static com.example.chigozirikonnekt.fragment.LogInFragment.validate;

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
import com.example.chigozirikonnekt.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class forgotPassword extends Fragment {

    private TextView loginTv;
    private Button recoverBtn;
    private EditText emailEt;

    private FirebaseAuth auth;
    private ProgressBar progressBar;

    public forgotPassword() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        clickListener();
    }
    private void init(View view){
        loginTv = view.findViewById(R.id.loginTV);
        recoverBtn = view.findViewById(R.id.recoverBtn);
        emailEt = view.findViewById(R.id.emailET);
        progressBar = view.findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();

    }
    private void clickListener(){
        loginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ((FragmentReplacerActivity) getActivity()).setFragment(new LogInFragment());
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        });

        recoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEt.getText().toString();

                if (email.isEmpty() || validate(email) == false) {
                    emailEt.setError("Input valid email");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){
                                    Toast.makeText(getContext(), "Password reset email sent",
                                            Toast.LENGTH_SHORT).show();
                                    emailEt.setText("");
                                } else{
                                    String errMsg = task.getException().getMessage();
                                    Toast.makeText(getContext(), "Error: "+errMsg, Toast.LENGTH_SHORT).show();
                                }
                                progressBar.setVisibility(View.GONE);

                            }
                        });

            }
        });
    }


}