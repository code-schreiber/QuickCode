package com.toolslab.quickcode.db;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.toolslab.quickcode.model.CodeFile;
import com.toolslab.quickcode.model.CodeFileViewModel;
import com.toolslab.quickcode.util.Logger;

import java.util.ArrayList;
import java.util.List;

// TODO [Refactoring] clean other classes from import com.google.android.gms.tasks
// TODO [Refactoring] clean other classes from import com.google.firebase
// TODO Fix first ever code not showing
public class DatabaseReferenceWrapper {

    private static final String USERS_KEY = "users";
    private static final String CODE_FILES_LIST_KEY = "code-files-list";

    private static DatabaseReference dbReference;

    private interface OnSignedInListener {
        void onSignedIn(String userId);
    }

    private DatabaseReferenceWrapper() {
        // Hide utility class constructor
    }

    public static String getUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            return "";
        } else {
            return "\nAnonymous User: " + auth.getCurrentUser().getUid();
        }
    }

    public static void addValueEventListenerForCodeFileId(final String codeFileId, final ValueEventListener listener) {
        signInAnonymously(new OnSignedInListener() {

            @Override
            public void onSignedIn(String userId) {
                getCodeFileFromDb(userId, codeFileId).addValueEventListener(listener);
            }
        });
    }

    public static void addValueEventListener(final ValueEventListener listener) {
        signInAnonymously(new OnSignedInListener() {

            @Override
            public void onSignedIn(String userId) {
                addValueEventListener(userId, listener);
            }
        });
    }

    public static void addCodeFile(final CodeFile codeFile, final DatabaseReference.CompletionListener listener) {
        signInAnonymously(new OnSignedInListener() {

            @Override
            public void onSignedIn(String userId) {
                getCodeFileFromDb(userId, codeFile.id())
                        .setValue(codeFile.toFirebaseValue(), listener);
            }
        });
    }

    public static void deleteListItem(final CodeFile codeFile, final DatabaseReference.CompletionListener listener) {
        signInAnonymously(new OnSignedInListener() {

            @Override
            public void onSignedIn(String userId) {
                getCodeFileFromDb(userId, codeFile.id())
                        .removeValue(listener);
            }
        });
    }

    public static void clearAll() {
        signInAnonymously(new OnSignedInListener() {

            @Override
            public void onSignedIn(String userId) {
                clearAll(userId);
            }
        });
    }

    public static List<CodeFileViewModel> getCodeFilesFromDataSnapshot(DataSnapshot dataSnapshot) {
        List<CodeFileViewModel> models = new ArrayList<>();
        Logger.logInfo("onDataChange in addOnCodeFilesChangedListener. Count " + dataSnapshot.getChildrenCount());
        if (dataSnapshot.getChildren() != null) {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                CodeFileViewModel model = getCodeFileFromDataSnapshot(snapshot);
                if (model != null) {
                    models.add(model);
                }
            }
        } else {
            Logger.logError("codeFiles is null in onDataChange: " + dataSnapshot);
        }
        return models;
    }

    @Nullable
    public static CodeFileViewModel getCodeFileFromDataSnapshot(DataSnapshot dataSnapshot) {
        try {
            CodeFile codeFile = CodeFile.create(dataSnapshot);
            return CodeFileViewModel.create(codeFile);
        } catch (NullPointerException e) {
            Logger.logError(e.getMessage());// TODO delete try catch block
        }
        return null;
    }

    public static void removeEventListener(final ValueEventListener listener) {
        if (listener == null) {
            Logger.logError("listener is null");
        } else {
            signInAnonymously(new OnSignedInListener() {

                @Override
                public void onSignedIn(String userId) {
                    removeValueEventListener(userId, listener);
                }
            });
        }
    }

    public static void removeEventListener(final String codeFileId, final ValueEventListener listener) {
        if (listener == null) {
            Logger.logError("listener is null");
        } else {
            signInAnonymously(new OnSignedInListener() {

                @Override
                public void onSignedIn(String userId) {
                    getCodeFileFromDb(userId, codeFileId).removeEventListener(listener);
                }
            });
        }
    }

    private static void clearAll(String userId) {
        // Delete code files
        addListenerForSingleValueEvent(userId, new ValueEventListener() {
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
                Logger.logException("onCancelled", databaseError.toException());
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
                Logger.logException("onCancelled", databaseError.toException());
            }
        });
    }

    private static void signInAnonymously(final OnSignedInListener listener) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {

                        @Override
                        public void onSuccess(AuthResult authResult) {
                            String userId = authResult.getUser().getUid();
                            Logger.logInfo("signInAnonymously: success for userId " + userId);
                            listener.onSignedIn(userId);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {

                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Logger.logException("signInAnonymously: failure: ", e);

                        }
                    });
        } else {
            // Already signed in
            listener.onSignedIn(auth.getCurrentUser().getUid());
        }
    }

    private static void addListenerForSingleValueEvent(String userId, ValueEventListener listener) {
        getDbCodeFileListChild(userId).addListenerForSingleValueEvent(listener);
    }

    private static void addValueEventListener(String userId, ValueEventListener listener) {
        getDbCodeFileListChild(userId).addValueEventListener(listener);
    }

    private static void removeValueEventListener(String userId, ValueEventListener listener) {
        getDbCodeFileListChild(userId).removeEventListener(listener);
    }

    private static DatabaseReference getCodeFileFromDb(String userId, String codeFileId) {
        return getDbCodeFileListChild(userId).child(codeFileId);
    }

    private static DatabaseReference getDbCodeFileListChild(String userId) {
        return getDbReference().child(USERS_KEY).child(userId).child(CODE_FILES_LIST_KEY);
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
