package com.example.chigozirikonnekt.fragment;

import static android.content.ContentValues.TAG;
import static com.example.chigozirikonnekt.fragment.CreateAccountFragment.EMAIL_REGEX;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
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
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LogInFragment<firebaseCredential, idToken> extends Fragment {

    private static final int RC_SIGN_IN = 1;
    private EditText emailET, passwordET;
    private TextView signUpTV, forgotPasswordTV;
    private Button loginBtn, googleSignInBtn;
    private ProgressBar progressBar;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth auth;


    public static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean validate(String emailStr) {
        Matcher matcher = EMAIL_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public LogInFragment() throws ApiException {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_log_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        clickListener();
    }

    private void init(View view) {
       emailET = view.findViewById(R.id.emailET);
       passwordET = view.findViewById(R.id.password_ET);
       loginBtn = view.findViewById(R.id.loginBtn);
       googleSignInBtn = view.findViewById(R.id.googleSignInBtn);
       signUpTV = view.findViewById(R.id.signUpTV);
       forgotPasswordTV = view.findViewById(R.id.forgotTV);
       progressBar = view.findViewById(R.id.progressBar);

       auth = FirebaseAuth.getInstance();
       GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
               .requestIdToken(getString(R.string.default_web_client_id))
               .requestEmail()
               .build();

       mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
    }

    private void clickListener(){
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();

                if (email.isEmpty() || validate(email) == false){
                    emailET.setError("Input valid email");
                    return;
                }
               if (password.isEmpty() || password.length() <6){
                   passwordET.setError("Input 6 digit valid password");
                   return;
               }
               progressBar.setVisibility(View.VISIBLE);
               auth.signInWithEmailAndPassword(email, password)
                       .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                           @Override
                           public void onComplete(@NonNull Task<AuthResult> task) {
                               if (task.isSuccessful()){
                                   FirebaseUser user = auth.getCurrentUser();
                                   progressBar.setVisibility(View.GONE);
                                   if (!user.isEmailVerified()){
                                       Toast.makeText(getContext(), "Please verify your email", Toast.LENGTH_SHORT).show();

                                   }

                                   sendUsertoMainActivity();

                               }else {
                                   String exception ="Error: " + task.getException().getMessage();
                                   Toast.makeText(getContext(), exception, Toast.LENGTH_SHORT).show();
                               }
                           }
                       });

            }

        });

        googleSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();

            }
        });
        signUpTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FragmentReplacerActivity) getActivity()).setFragment(new CreateAccountFragment());
            }
        });
    }


    private void sendUsertoMainActivity(){

        if (getActivity() == null)
            return;
        progressBar.setVisibility(View.GONE);
        startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
        getActivity().finish();
    }

    private void signIn(){
        Intent signInIntent =mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener((Executor) this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            updateUI(user);
                        }else {
                            Log.d("TAG", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        Map<String, Object> map = new HashMap<>();
        map.put("name", account.getDisplayName());
        map.put("email", account.getEmail());
        map.put("profileImage", String.valueOf(account.getPhotoUrl()));
        map.put("uid", user.getUid());
        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            assert getActivity() != null;
                            sendUsertoMainActivity();

                        }else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Error: "+task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }



}
