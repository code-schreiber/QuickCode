package com.toolslab.quickcode.db;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
import com.toolslab.quickcode.model.CodeFile;
import com.toolslab.quickcode.model.CodeFileViewModel;
import com.toolslab.quickcode.util.Logger;

import java.util.ArrayList;
import java.util.List;


public final class DatabaseReferenceWrapper {

    private static final String CODE_FILES_LIST_KEY = "CODE_FILES_LIST_KEY";

    private static DatabaseReference dbReference;

    private DatabaseReferenceWrapper() {
        // Hide utility class constructor
    }

    public static void addValueEventListenerForCodeFileId(final String codeFileId, final ValueEventListener listener) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = task.getResult().getUser();
                                Logger.logInfo("signInAnonymously: success for user " + user);
                                getDbCodeFileListChild().child(codeFileId).addValueEventListener(listener);
                            } else {
                                Logger.logException("signInAnonymously: failure: ", task.getException());
                            }
                        }
                    });
        } else {
            getDbCodeFileListChild().child(codeFileId).addValueEventListener(listener);
        }
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
                                getDbCodeFileListChild().addValueEventListener(listener);
                            } else {
                                Logger.logException("signInAnonymously: failure: ", task.getException());
                            }
                        }
                    });
        } else {
            getDbCodeFileListChild().addValueEventListener(listener);
        }
    }

    public static void addCodeFileAuthFirst(final CodeFile codeFile) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = task.getResult().getUser();
                                Logger.logInfo("signInAnonymously: success for user " + user);
                                addCodeFile(codeFile);
                            } else {
                                Logger.logException("signInAnonymously: failure: ", task.getException());
                            }
                        }
                    });
        } else {
            addCodeFile(codeFile);
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
                                getDbCodeFileListChild().child(codeFile.id()).removeValue(listener);
                            } else {
                                Logger.logException("signInAnonymously: failure: ", task.getException());
                            }
                        }
                    });
        } else {
            getDbCodeFileListChild().child(codeFile.id()).removeValue(listener);
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

    @Nullable
    public static CodeFileViewModel getCodeFileFromDataSnapshot(final DataSnapshot dataSnapshot) {
        try {
            CodeFile codeFile = CodeFile.create(dataSnapshot);
            return CodeFileViewModel.create(codeFile);
        } catch (NullPointerException e) {
            Logger.logError(e.getMessage());// TODO delete try catch block
        }
        return null;
    }

    public static List<CodeFileViewModel> getCodeFilesFromDataSnapshot(final DataSnapshot dataSnapshot) {
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

    public static void removeEventListenerAuthFirst(final String codeFileId, final ValueEventListener listener) {
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
                                    getDbCodeFileListChild().child(codeFileId).removeEventListener(listener);
                                } else {
                                    Logger.logException("signInAnonymously: failure: ", task.getException());
                                }
                            }
                        });
            } else {
                getDbCodeFileListChild().child(codeFileId).removeEventListener(listener);
            }
        }
    }

    private static void addCodeFile(CodeFile codeFile) {
        Task<Void> task = getDbCodeFileListChild()
                .child(codeFile.id())
                .setValue(codeFile.toFirebaseValue());
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Logger.logException("addCodeFile Task failed", e);
            }
        });
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Logger.logInfo("addCodeFile Task succeeded");
            }
        });
    }

    private static void clearAll() {
        // Delete code files
        getDbCodeFileListChild().addListenerForSingleValueEvent(new ValueEventListener() {
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

    private static DatabaseReference getDbCodeFileListChild() {
        return getDbReference().child(CODE_FILES_LIST_KEY);
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
