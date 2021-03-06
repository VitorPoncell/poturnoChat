package com.poturno.poturnochat.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.poturno.poturnochat.R;
import com.poturno.poturnochat.activity.ChatActivity;
import com.poturno.poturnochat.adapter.ContactsAdapter;
import com.poturno.poturnochat.config.FirebaseConfig;
import com.poturno.poturnochat.helper.Preferences;
import com.poturno.poturnochat.model.Contact;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private ListView listView;
    private ArrayAdapter adapter;
    private ArrayList<Contact> contacts;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListenerContacts;


    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        databaseReference.addValueEventListener(valueEventListenerContacts);

    }

    @Override
    public void onStop() {
        super.onStop();
        databaseReference.removeEventListener(valueEventListenerContacts);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        contacts = new ArrayList<>();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        listView = (ListView) view.findViewById(R.id.lv_contacts);
        adapter = new ContactsAdapter(getActivity(),contacts);
        listView.setAdapter(adapter);


        Preferences preferences = new Preferences(getActivity());
        String logedUserIdentifier = preferences.getIdentifier();
        databaseReference = FirebaseConfig.getDatabaseReference().child("contacts").child(logedUserIdentifier);

        valueEventListenerContacts = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                contacts.clear();
                for ( DataSnapshot data : dataSnapshot.getChildren()){
                    Contact contact = data.getValue(Contact.class);
                    contacts.add(contact);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);

                Contact contact = contacts.get(position);

                intent.putExtra("name", contact.getName());
                intent.putExtra("email", contact.getEmail());

                startActivity(intent);
            }

        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                AlertDialog.Builder aleBuilder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogAddContact);
                aleBuilder.setTitle("Excluir contato?");
                aleBuilder.setCancelable(false);

                aleBuilder.setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Contact contact = contacts.get(position);

                        try {
                            databaseReference.child(contact.getUserIdentifier()).removeValue();
                            Toast.makeText(getActivity(),"Contato excluido com sucesso",Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            Toast.makeText(getActivity(),"Erro ao excluir contato",Toast.LENGTH_LONG).show();
                        }



                    }
                });

                aleBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                aleBuilder.create();
                aleBuilder.show();

                return true;
            }
        });


        return view;
    }

}
