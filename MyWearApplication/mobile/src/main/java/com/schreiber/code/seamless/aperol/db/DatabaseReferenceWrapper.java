package com.schreiber.code.seamless.aperol.db;


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

    public static void addValueEventListenerAuthFirst(final ValueEventListener listener) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = task.getResult().getUser();
                                Logger.logInfo("signInAnonymously: success for user " + user);
                                getDbReference().getRef().child(CODE_FILES_KEY).addValueEventListener(listener);
                            } else {
                                Logger.logException("signInAnonymously: failure: ", task.getException());
                            }
                        }
                    });
        } else {
            getDbReference().getRef().child(CODE_FILES_KEY).addValueEventListener(listener);
        }
    }

    public static void addListItemAuthFirst(final CodeFile codeFile) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = task.getResult().getUser();
                                Logger.logInfo("signInAnonymously: success for user " + user);
                                addListItem(codeFile);
                            } else {
                                Logger.logException("signInAnonymously: failure: ", task.getException());
                            }
                        }
                    });
        } else {
            addListItem(codeFile);
        }
    }

    public static void deleteListItemAuthFirst(final CodeFile codeFile, final DatabaseReference.CompletionListener listener) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = task.getResult().getUser();
                                Logger.logInfo("signInAnonymously: success for user " + user);
                                getDbCodeFilesChild().child(codeFile.id()).removeValue(listener);
                            } else {
                                Logger.logException("signInAnonymously: failure: ", task.getException());
                            }
                        }
                    });
        } else {
            getDbCodeFilesChild().child(codeFile.id()).removeValue(listener);
        }
    }

    public static void clearAllAuthFirst() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = task.getResult().getUser();
                                Logger.logInfo("signInAnonymously: success for user " + user);
                                clearAll();
                            } else {
                                Logger.logException("signInAnonymously: failure: ", task.getException());
                            }
                        }
                    });
        } else {
            clearAll();
        }
    }

    public static List<CodeFile> getCodeFilesFromDataSnapshot(final DataSnapshot dataSnapshot) {
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

    public static void removeEventListenerAuthFirst(final ValueEventListener listener) {
        if (listener == null) {
            Logger.logError("listener is null");
        } else {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() == null) {
                auth.signInAnonymously()
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = task.getResult().getUser();
                                    Logger.logInfo("signInAnonymously: success for user " + user);
                                    getDbReference().removeEventListener(listener);
                                } else {
                                    Logger.logException("signInAnonymously: failure: ", task.getException());
                                }
                            }
                        });
            } else {
                getDbReference().removeEventListener(listener);
            }
        }
    }

    private static void addListItem(CodeFile codeFile) {
        Task<Void> task = getDbCodeFilesChild()
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

    private static void clearAll() {
        // Delete code files
        getDbCodeFilesChild().addListenerForSingleValueEvent(new ValueEventListener() {
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

        // Delete everything
        getDbReference().addListenerForSingleValueEvent(new ValueEventListener() {
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

    private static DatabaseReference getDbCodeFilesChild() {
        return getDbReference().child(CODE_FILES_KEY);
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
