package com.example.jamminbaru.ControllerUser;

import static com.example.jamminbaru.user.LoginUser.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.jamminbaru.ControllerCreator.ProfileCreator;
import com.example.jamminbaru.Model.ModelProfileUser;
import com.example.jamminbaru.R;
import com.example.jamminbaru.user.LoginUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class ProfileUser extends AppCompatActivity {
    ImageButton playlist;
    ImageButton home;
    TextView usernameTag;
    Button logoutDirectLogin, upload_btn, delete_btn;

    private ImageView imageView;
    private ProgressBar progressBar;
    final private String displayUsername = user.getPhoneTxt();
    private final DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("User").child(displayUsername).child("Profile_Image");
    private final StorageReference reference = FirebaseStorage.getInstance().getReference().child("User").child(displayUsername).child("Profile_Image");
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);

        usernameTag = findViewById(R.id.usernameTag);
        usernameTag.setText(displayUsername);

        logoutDirectLogin = findViewById(R.id.logoutDirectLogin);
        upload_btn = findViewById(R.id.upload_btn);
        delete_btn = findViewById(R.id.delete_btn);
        imageView = findViewById(R.id.profileimg);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent , 2);
            }
        });

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageUri != null){
                    uploadToFirebase(imageUri);
                }else{
                    Toast.makeText(ProfileUser.this, "Please Select Image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        logoutDirectLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(ProfileUser.this, LoginUser.class);
                startActivity(intent);
            }
        });

        home = (ImageButton) findViewById(R.id.homeLogo3);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileUser.this, HomeUser.class);
                startActivity(intent);
            }
        });

//        playlist = (ImageButton) findViewById(R.id.playlistLogo3);
//        playlist.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ProfileUser.this, PlaylistUser.class);
//                startActivity(intent);
//            }
//        });

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                root.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // data Link URL image ditemukan, hapus data tersebut
                            root.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    // data Link URL image berhasil dihapus, ubah gambar di ImageView
                                    imageView.setImageDrawable(ContextCompat.getDrawable(ProfileUser.this, R.drawable.ic_baseline_add_photo_alternate_24));
                                    Toast.makeText(ProfileUser.this, "Delete Picture Successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // data Link URL image tidak ditemukan, munculkan pesan error
                            Toast.makeText(ProfileUser.this, "No Image Found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // error handling
                    }
                });
            }
        });

        getUserInfo();
    }

    private void getUserInfo(){
        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0){
                    if (snapshot.hasChild("imageUrl")){
                        // link image foto ditemukan, tampilkan gambar
                        String image = snapshot.child("imageUrl").getValue().toString();
                        Picasso.get().load(image).into(imageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    private void uploadToFirebase(Uri uri){
        StorageReference fileRef = reference.child(System.currentTimeMillis() + "." + getFileExtension(uri));
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        ModelProfileUser model = new ModelProfileUser(downloadUrl);
                        root.setValue(model);
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(ProfileUser.this, "Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(ProfileUser.this, "Uploading Failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri mUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }

    //    public void GoPlaylist(View v){
//        Intent intent =new Intent(this,Playlist.class);
//        startActivity(intent);
//        finish();
//
//    }
//
//    public void GoHome(View v){
//        Intent intent =new Intent(this,Home.class);
//        startActivity(intent);
//        finish();
//
//    }

}