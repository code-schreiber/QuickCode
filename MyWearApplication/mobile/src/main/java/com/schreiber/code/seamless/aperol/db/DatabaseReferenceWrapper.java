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

    private static List<ValueEventListener> codeFilesChangedListeners = new ArrayList<>();

    public interface OnCodeFilesChangedListener {

        void codeFilesChanged(CodeFile codeFile);

    }

    public interface OnCodeFilesLoadedListener {

        void codeFilesLoaded(ArrayList<CodeFile> codeFiles);

    }

    private DatabaseReferenceWrapper() {
        // Hide utility class constructor
    }

    private static void deleteAll() {
        Query applesQuery = getDbReference().child(CODE_FILES_KEY);

        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
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

        applesQuery = getDbReference();
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
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

    public static void addListItemAuthFirst(final CodeFile codeFile) {
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
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
                                // If sign in fails, display a message to the user.
                                Logger.logError("signInAnonymously:failure: " + task.getException().getMessage());
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
        task.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Logger.logInfo("Task completed");
            }
        });
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

    public static void loadCodeFiles(final OnCodeFilesLoadedListener onCodeFilesLoadedListener) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<CodeFile> codeFiles = new ArrayList<>();
                Logger.logInfo("Count " + dataSnapshot.getChildrenCount());
                if (dataSnapshot.getChildren() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CodeFile codeFile = null;
                        try {
                            codeFile = CodeFile.create(snapshot);
                        } catch (NullPointerException e) {
                            Logger.logError(e.getMessage());
                        }
                        if (codeFile != null) {
                            codeFiles.add(codeFile);
                        }
                    }
                } else {
                    Logger.logError("codeFiles is null in onDataChange: " + dataSnapshot);
                }
                onCodeFilesLoadedListener.codeFilesLoaded(codeFiles);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Logger.logException("onCancelled", databaseError.toException());
            }
        };
        getDbReference().getRef().child(CODE_FILES_KEY).addListenerForSingleValueEvent(listener);
    }

    public static ValueEventListener addOnCodeFilesChangedListener(final OnCodeFilesChangedListener onCodeFilesChangedListener) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CodeFile codeFile = null;
                if (dataSnapshot.exists()) {
                    try {
                        codeFile = CodeFile.create(dataSnapshot);//TODO somehow this is not working
                    } catch (NullPointerException e) {
                        Logger.logError(e.getMessage());
                    }
                    if (codeFile != null) {
                        onCodeFilesChangedListener.codeFilesChanged(codeFile);
                    } else {
                        Logger.logError("codeFile is null in onDataChange: " + dataSnapshot);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Logger.logException("onCancelled", databaseError.toException());
            }
        };
        getDbReference().getRef().child(CODE_FILES_KEY).addValueEventListener(listener);
        codeFilesChangedListeners.add(listener);
        return listener;
    }

    public static void removeOnCodeFilesChangedListener(ValueEventListener listener) {
        if (listener != null) {
//            getDbReference().getRef().removeEventListener(codeFilesChangedListeners.get(listener)); TODO
        }
    }

    public static boolean deleteListItem(CodeFile codeFile) {
        // TODO
        return false;
    }

    public static <T> boolean containsListItem(Context context) {
        // TODO
        return false;
    }

    public static void clearAll(Context context) {
        // TODO
    }

    private static DatabaseReference getDbReference() {
        return FirebaseDatabase.getInstance().getReference();
    }

}
