package com.schreiber.code.seamless.aperol.db;


import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.schreiber.code.seamless.aperol.model.CodeFile;
import com.schreiber.code.seamless.aperol.util.Logger;

import java.util.ArrayList;
import java.util.List;


public final class DatabaseReferenceWrapper {

    private static final String CODE_FILES_KEY = "CODE_FILES_KEY";

    private static DatabaseReference dbReference;

    private DatabaseReferenceWrapper() {
        // Hide utility class constructor
    }

    public static void addListItemAuthFirst(final CodeFile codeFile) {
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {// TODO dont do this only on add, do it before
            auth.signInAnonymously()
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Logger.logInfo("signInAnonymously:success");
                                FirebaseUser user = auth.getCurrentUser();
                                addListItem(codeFile);
                            } else {
                                Logger.logException("signInAnonymously:failure: ", task.getException());
                            }
                        }
                    });
        } else {
            addListItem(codeFile);
        }
    }

    private static void addListItem(CodeFile codeFile) {
        Task<Void> task = getDbReference()
                .child(CODE_FILES_KEY)
                .child(codeFile.id())
                .setValue(codeFile.toFirebaseValue());
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Logger.logError("Task failed: " + e.getMessage());
            }
        });
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Logger.logInfo("Task succeeded");
            }
        });
    }

    public static void addValueEventListener(ValueEventListener listener) {
        getDbReference().getRef().child(CODE_FILES_KEY).addValueEventListener(listener);
    }

    public static List<CodeFile> getCodeFilesFromDataSnapshot(DataSnapshot dataSnapshot) {
        List<CodeFile> codeFiles = new ArrayList<>();
        Logger.logInfo("onDataChange in addOnCodeFilesChangedListener. Count " + dataSnapshot.getChildrenCount());
        if (dataSnapshot.getChildren() != null) {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                try {
                    CodeFile codeFile = CodeFile.create(snapshot);
                    codeFiles.add(codeFile);
                } catch (NullPointerException e) {
                    Logger.logError(e.getMessage());// TODO delete try catch block
                }
            }
        } else {
            Logger.logError("codeFiles is null in onDataChange: " + dataSnapshot);
        }
        return codeFiles;
    }

    public static void deleteListItem(final CodeFile codeFile, final DatabaseReference.CompletionListener listener) {
        getDbReference().child(CODE_FILES_KEY).child(codeFile.id()).removeValue(listener);
    }

    public static <T> boolean containsListItem(Context context) {
        // TODO
        return false;
    }

    public static void clearAll() {
        Query query = getDbReference().child(CODE_FILES_KEY);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            Logger.logInfo("onComplete " + databaseError + databaseReference);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Logger.logError("onCancelled" + databaseError.toException().getMessage());
            }
        });

        query = getDbReference();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            Logger.logInfo("onComplete " + databaseError + databaseReference);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Logger.logError("onCancelled" + databaseError.toException().getMessage());
            }
        });
    }

    public static void removeEventListener(ValueEventListener listener) {
        if (listener != null) {
            getDbReference().removeEventListener(listener);
        }
    }

    private static DatabaseReference getDbReference() {
        if (dbReference == null) {
            FirebaseDatabase instance = FirebaseDatabase.getInstance();
            instance.setPersistenceEnabled(true);
            dbReference = instance.getReference();
        }
        return dbReference;
    }

}
