package com.toolslab.quickcode.db;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.toolslab.quickcode.model.CodeFile;
import com.toolslab.quickcode.model.CodeFileViewModel;
import com.toolslab.quickcode.util.Logger;

// TODO [Refactoring] clean other classes from import com.google.android.gms.tasks
// TODO [Refactoring] clean other classes from import com.google.firebase
public class DatabaseReferenceWrapper {

    private static final String USERS_KEY = "users";
    private static final String CODE_FILES_LIST_KEY = "code-files-list";

    private static DatabaseReference dbReference;

    private interface OnSignedInListener {

        void onSignedIn(String userId);

        void onSignedInFailed(Exception exception);
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

    public static void addValueEventListenerForCodeFileId(final String codeFileId, final ValueEventListener valueEventListener) {
        signInAnonymously(new OnSignedInListener() {

            @Override
            public void onSignedIn(String userId) {
                getCodeFileFromDb(userId, codeFileId).addValueEventListener(valueEventListener);
            }

            @Override
            public void onSignedInFailed(Exception exception) {
                valueEventListener.onCancelled(DatabaseError.fromException(exception));
            }
        });
    }

    public static void addEventListeners(final ChildEventListener childEventListener) {
        signInAnonymously(new OnSignedInListener() {

            @Override
            public void onSignedIn(String userId) {
                addEventListeners(userId, childEventListener);
            }

            @Override
            public void onSignedInFailed(Exception exception) {
                childEventListener.onCancelled(DatabaseError.fromException(exception));
            }
        });
    }

    public static void addCodeFile(final CodeFile codeFile) {
        signInAnonymously(new OnSignedInListener() {

            @Override
            public void onSignedIn(String userId) {
                getCodeFileFromDb(userId, codeFile.id())
                        .setValue(codeFile.toFirebaseValue(),
                                new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Logger.logException("Error in addCodeFile", databaseError.toException());
                                        }
                                    }
                                });
            }

            @Override
            public void onSignedInFailed(Exception exception) {
                Logger.logException("Sign in error in addCodeFile", exception);
            }
        });
    }

    public static void deleteListItem(final CodeFile codeFile) {// TODO more consistent naming
        signInAnonymously(new OnSignedInListener() {

            @Override
            public void onSignedIn(String userId) {
                getCodeFileFromDb(userId, codeFile.id())
                        .removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Logger.logException("Error in deleteListItem", databaseError.toException());
                                }
                            }
                        });
            }

            @Override
            public void onSignedInFailed(Exception exception) {
                Logger.logException("Sign in error in deleteListItem", exception);
            }
        });
    }

    public static void clearAll() {
        signInAnonymously(new OnSignedInListener() {

            @Override
            public void onSignedIn(String userId) {
                clearAll(userId);
            }

            @Override
            public void onSignedInFailed(Exception exception) {
                Logger.logException("Sign in error in clearAll", exception);
            }
        });
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

    public static void removeEventListeners(final ChildEventListener childEventListener) {
        signInAnonymously(new OnSignedInListener() {

            @Override
            public void onSignedIn(String userId) {
                removeEventListeners(userId, childEventListener);
            }

            @Override
            public void onSignedInFailed(Exception exception) {
                childEventListener.onCancelled(DatabaseError.fromException(exception));
            }
        });
    }

    public static void removeEventListenerForCodeFileId(final String codeFileId, final ValueEventListener valueEventListener) {
        if (valueEventListener == null) {
            Logger.logError("valueEventListener is null");
        } else {
            signInAnonymously(new OnSignedInListener() {

                @Override
                public void onSignedIn(String userId) {
                    getCodeFileFromDb(userId, codeFileId).removeEventListener(valueEventListener);
                }

                @Override
                public void onSignedInFailed(Exception exception) {
                    valueEventListener.onCancelled(DatabaseError.fromException(exception));
                }
            });
        }
    }

    private static void clearAll(String userId) {
        // Delete code files
        addListenerForSingleValueEvent(userId, new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    childSnapshot.getRef().removeValue(new DatabaseReference.CompletionListener() {

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
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    childSnapshot.getRef().removeValue(new DatabaseReference.CompletionListener() {

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

    private static void signInAnonymously(final OnSignedInListener onSignedInListener) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {

                        @Override
                        public void onSuccess(AuthResult authResult) {
                            String userId = authResult.getUser().getUid();
                            Logger.logInfo("signInAnonymously: success for userId " + userId);
                            onSignedInListener.onSignedIn(userId);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {

                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Logger.logException("signInAnonymously: failure: ", e);
                            onSignedInListener.onSignedInFailed(e);
                        }
                    });
        } else {
            // Already signed in
            onSignedInListener.onSignedIn(auth.getCurrentUser().getUid());
        }
    }

    private static void addListenerForSingleValueEvent(String userId, ValueEventListener valueEventListener) {
        getDbCodeFileListChild(userId).addListenerForSingleValueEvent(valueEventListener);
    }

    private static void addEventListeners(String userId, ChildEventListener childEventListener) {
        getDbCodeFileListChild(userId).addChildEventListener(childEventListener);
    }

    private static void removeEventListeners(String userId, ChildEventListener childEventListener) {
        if (childEventListener == null) {
            Logger.logError("childEventListener is null in removeEventListeners");
        } else {
            getDbCodeFileListChild(userId).removeEventListener(childEventListener);
        }
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
