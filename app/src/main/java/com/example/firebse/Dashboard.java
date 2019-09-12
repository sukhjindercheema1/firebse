package com.example.firebse;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class Dashboard extends Fragment {

    TextView txt_dashname;
    FirebaseUser user;
    FirebaseFirestore db;
    Button btn_signout,btn_del;

    public Dashboard() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = getArguments().getParcelable("user");
        db = FirebaseFirestore.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txt_dashname = view.findViewById(R.id.txt_dashname);
        btn_signout = view.findViewById(R.id.btn_logut);
        btn_del = view.findViewById(R.id.btn_del);


        readFireStore();

        btn_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                NavController navController = Navigation.findNavController(getActivity(),R.id.host_frag);
                navController.navigate(R.id.loginScreen);
            }
        });

        btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delUser(getView());
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }


    public void readFireStore()
    {
        DocumentReference docref = db.collection("users").document(user.getUid());

        docref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful())
                {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists())
                    {
                        System.out.println(doc.getData());

                        txt_dashname.setText("Welcome "+doc.get("name")+" !");
                    }

                }

            }
        });
    }

    public void delUser(View v)
    {
        Toast.makeText(getActivity().getApplicationContext(),"Test",Toast.LENGTH_LONG).show();

        View popupview  = getActivity().getLayoutInflater().inflate(R.layout.popup_window, null);

        final PopupWindow popupWindow = new PopupWindow(popupview, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,true);

        if(Build.VERSION.SDK_INT>=21){
            popupWindow.setElevation(5.0f);
        }

        final EditText edt_email = popupview.findViewById(R.id.edt_reemail);
        final EditText edt_pass = popupview.findViewById(R.id.edt_repass);
        final Button btn_sub = popupview.findViewById(R.id.btn_resub);



            btn_sub.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity().getApplicationContext(),edt_email.getText().toString(),Toast.LENGTH_LONG).show();


                    if (edt_pass.getText().toString().length()<6) {
                       edt_pass.setError("Invalid Password ,Password should be at least 6 characters");
                       edt_pass.requestFocus();
                    }else {
                        if (TextUtils.isEmpty(edt_email.getText())) {
                            edt_email.setError("Email cannot be empty!");
                            edt_email.requestFocus();
                        } else if (TextUtils.isEmpty(edt_pass.getText())) {
                            edt_pass.setError("Password cannot be empty!");
                            edt_pass.requestFocus();
                        }else {
                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(edt_email.getText().toString(), edt_pass.getText().toString());


                            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {

                                        db.collection("users").document(user.getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            NavOptions navOptions = new NavOptions.Builder()
                                                                    .setPopUpTo(R.id.dashboard, true)
                                                                    .build();
                                                            NavController navController = Navigation.findNavController(getActivity(),R.id.host_frag);
                                                            navController.navigate(R.id.loginScreen,null,navOptions);
                                                            popupWindow.dismiss();
                                                            
                                                        }else {
                                                            System.out.println("Delete Task :"+task.getException().getMessage());
                                                        }

                                                    }
                                                });

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                System.out.println("Firestore Task :"+e.getMessage());
                                            }
                                        });


                                    }else {
                                        System.out.println("reauthenticate Task :"+task.getException().getMessage());
                                    }


                                }
                            });
                        }

                    }


                }
            });



        popupWindow.setFocusable(true);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        popupWindow.showAtLocation(getView(), Gravity.CENTER,0,0);



    }

}
