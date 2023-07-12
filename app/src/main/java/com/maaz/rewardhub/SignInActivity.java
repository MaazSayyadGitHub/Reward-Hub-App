package com.maaz.rewardhub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.maaz.rewardhub.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity {

    ActivitySignInBinding binding;

    GoogleSignInClient googleSignInClient;
    GoogleSignInOptions gso;
    FirebaseAuth auth;
    FirebaseFirestore database;

    ProgressDialog dialog;

    private final static int RC_SIGN_IN = 9001;
    private final static String TAG = "GoogleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Signing In..");
        dialog.setCancelable(false);

        database = FirebaseFirestore.getInstance();


        // set click listener on cardView Button.
        binding.signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                signIn();
            }
        });

        // Google Configure
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // check if user signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);
    }

    private void signIn(){
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent().
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In Successful, authenticate with firebase.
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "FirebaseAuthWithGoogle: " + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                // Google Sign In Failed, Update UI appropriately.
//                Log.e("TAG", "Google Sign In Failed", e);
                Toast.makeText(this, "Google Sign In Failed" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            dialog.dismiss();
                            // storing name as well
                            String name = auth.getCurrentUser().getDisplayName();
                            User userModel = new User(name);

                            // storing default coins in firestore
                            database.collection("User")
                                    .document(auth.getUid())
                                    .set(userModel);

                            // sign in success, update ui with signed in user info.
                            Toast.makeText(SignInActivity.this, "SignIn with Google Successfully", Toast.LENGTH_LONG).show();
                            FirebaseUser user = auth.getCurrentUser();
                            updateUI(user);
                        } else {
                            dialog.dismiss();
                            // if signIn fail display message to user.
                            Toast.makeText(SignInActivity.this, "signInFailedWithCredential: failed", Toast.LENGTH_LONG).show();
                            updateUI(null);
                        }
                    }
                });
    }

    // if user already logIn then we will move to Home Screen. otherwise not.
    private void updateUI(FirebaseUser user){
        if (user != null){
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            finish();
        }
    }

}