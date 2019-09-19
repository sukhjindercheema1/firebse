package com.example.firebse;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends Fragment {

    EditText edt_name,edt_email,edt_pass,edt_cpass;
    Button btn_reg;
    private FirebaseAuth mAuth;


    public Register() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.register, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edt_name = view.findViewById(R.id.edt_name);
        edt_email = view.findViewById(R.id.edt_email);
        edt_pass = view.findViewById(R.id.edt_pass);
        edt_cpass = view.findViewById(R.id.edt_cpass);
        btn_reg = view.findViewById(R.id.btn_register);


        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!checkEmptyField())
                {
                    if (edt_pass.getText().length()<6)
                    {
                      edt_pass.setError("Invalid Password ,Password should be at least 6 characters");
                      edt_pass.requestFocus();
                    }
                    else {
                        if (!edt_pass.getText().toString().equals(edt_cpass.getText().toString()))
                        {
                            edt_cpass.setError("Password not Match!");
                        }else
                        {
                            String email = edt_email.getText().toString();
                            String pass = edt_pass.getText().toString();
                            String name = edt_name.getText().toString();

                            createUser(email,pass,name);
                        }
                    }


                }


            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public boolean checkEmptyField()
    {
        if (TextUtils.isEmpty(edt_name.getText().toString()))
        {
            edt_name.setError("Name cannot be empty!");
            edt_name.requestFocus();
            return true;
        }else if (TextUtils.isEmpty(edt_email.getText().toString()))
        {
            edt_email.setError("Email cannot be empty!");
            edt_email.requestFocus();
            return true;
        }else if (TextUtils.isEmpty(edt_pass.getText().toString()))
        {
            edt_pass.setError("Password cannot be empty!");
            edt_pass.requestFocus();
            return true;
        }else if (TextUtils.isEmpty(edt_cpass.getText().toString()))
        {
            edt_cpass.setError("Confirm Password cannot be empty!");
            edt_cpass.requestFocus();
            return true;
        }

        return false;
    }

    public void createUser(final String email, String pass, final String name)
    {
        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful())
                {
                    FirebaseUser user = mAuth.getCurrentUser();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    Map<String,Object> usermap = new HashMap<>();
                    usermap.put("name",name);
                    usermap.put("email",email);

                    db.collection("users").document(user.getUid()).set(usermap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity().getApplicationContext(),"Registration Success!",Toast.LENGTH_LONG).show();

                        }
                    });



                }else {

                    System.out.println("Check Ex : "+task.getException());

                    Toast.makeText(getActivity().getApplicationContext(),task.getException().getMessage()   ,Toast.LENGTH_LONG).show();
                }


            }
        });

        FirebaseAuth.getInstance().signOut();

        NavController navController = Navigation.findNavController(getActivity(),R.id.host_frag);

        navController.navigate(R.id.loginScreen);

    }

}
