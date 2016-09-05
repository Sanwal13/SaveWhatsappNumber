package appdeveloper.savewhatsappnumber;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    final static String TAG = "MainActivity";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    TextView tv1;
    Button b1;
    Context context = MainActivity.this;
    String message, number, title;
    String id;
    public Button getB1() {
        if (b1 == null)
            b1 = (Button) findViewById(R.id.btn_ok);
        return b1;
    }
   /* public TextView getTv1() {
        if (tv1 == null)
            tv1 = (TextView) findViewById(R.id.txt);
        return tv1;
    }*/

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Intent intent = getIntent();
            number = "7742526633";
            message = "Hello ! Black";
            title = "Riders";
            id = "Message ".concat(number);
            Log.e(TAG, "Whatsapp number : " + number);
            if (Build.VERSION.SDK_INT >= 23) {
                askForUsesPermission();
                Log.d(TAG, "Build.VERSION.SDK_INT>=23");
            } else {
                askForContactAccess();
                Log.d(TAG, "Build.VERSION.SDK_INT<23");
            }
          //  getTv1().setText("Save contact" + number + "or Press Ok to save contact to PhoneBook.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean contactExists(Context context, String number) {
        // number is the phone number
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER,
                ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
        try {
            if (cur != null ? cur.moveToFirst() : false) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }

    boolean SaveContact() {
        Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());
        //INSERT NAME
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.
                        StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, title) // Name of the person
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, title) // Name of the person
                .build());
        //INSERT PHONE
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number) // Number of the person
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract
                        .CommonDataKinds.Phone.TYPE_WORK)
                .build()); //

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO,
                        stream.toByteArray())
                .build());
        // SAVE CONTACT IN BCR Structure
        Uri newContactUri = null;
        //PUSH EVERYTHING TO CONTACTS
        try {
            ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            if (res != null && res[0] != null) {
                newContactUri = res[0].uri;
                //02-20 22:21:09 URI added contact:content://com.android.contacts/raw_contacts/612
                Log.d(TAG, "URI added contact:" + newContactUri);
            } else Log.e(TAG, "Contact not added.");
        } catch (RemoteException e) {
            // error
            Log.e(TAG, "Error (1) adding contact.");
            newContactUri = null;
        } catch (OperationApplicationException e) {
            // error
            Log.e(TAG, "Error (2) adding contact.");
            newContactUri = null;
        }
        Log.d(TAG, "Contact added to system contacts.");

        if (newContactUri == null) {
            Log.e(TAG, "Error creating contact");
            return false;
        }

        return true;
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    private void openWhatsApp(String id) {
        Log.d("String  Id", "fdsfd" + id);
        String whatsAppMessage = "Hello IAH-Aus!";
        Uri uri = Uri.parse("smsto:" + id);
        Intent i = new Intent(Intent.ACTION_SENDTO, uri);
        //i.setType("text/plain");
        //i.putExtra(Intent.EXTRA_TEXT,whatsAppMessage);
        i.setPackage("com.whatsapp");
        startActivity(i);
        finish();
        // i.putExtra(Intent.EXTRA_TEXT, whatsAppMessage);

    }

    private void askForContactAccess() {
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.WRITE_CONTACTS);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    android.Manifest.permission.WRITE_CONTACTS)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("You need to grant access to contact! Denying, " +
                        "would not allow you to use some features.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.WRITE_CONTACTS},
                                        REQUEST_CODE_ASK_PERMISSIONS);
                                Log.e("dfgfd", "chat start from zopim");
//                        Toast.makeText(Perspective.this, "Yes button pressed",
//                                Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
               /* showMessageOKCancel("You need to allow access to Contacts",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.WRITE_CONTACTS},
                                        REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        });*/
                return;
            }
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.WRITE_CONTACTS},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        startWhatsApp();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(android.Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.WRITE_CONTACTS, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_COARSE_LOCATION
                if (perms.get(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                        && perms.get(android.Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    startWhatsApp();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Some Permissions are Denied!", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void askForUsesPermission() {
        List<String> permissionsNeeded = new ArrayList<>();
        final List<String> permissionsList = new ArrayList<>();
        if (!addPermission(permissionsList, android.Manifest.permission.READ_CONTACTS))
            permissionsNeeded.add("Read Contacts");
        if (!addPermission(permissionsList, android.Manifest.permission.WRITE_CONTACTS))
            permissionsNeeded.add("Write Contacts");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("You need to grant access to contact! Denying, " +
                        "would not allow you to use some features.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                                Log.e("dfgfd", "chat start from zopim");
//                        Toast.makeText(Perspective.this, "Yes button pressed",
//                                Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();


                /*showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                                Log.e("in ok cancle dialog", "ok then proceed");
                            }
                        });*/
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            Log.e("Out of ok cancle dialog", "nnjnfjkdsjndfnjdk====");
            return;
        }

        startWhatsApp();
    }

    private void startWhatsApp() {

        if (contactExists(context, number)) {
            // String id = "+919667296937@s.whatsapp.net";
            Log.e("Contact Exist", ": In phone");
            boolean installed = appInstalledOrNot("com.whatsapp");
            if (installed) {
                Log.e("Whatsapp is installed", ": In phone");
                //This intent will help you to launch if the package is already installed
                try {
                    //String id = "+919667296937@s.whatsapp.net";
                    openWhatsApp(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("Installed", "fgfdg");
            } else {
                Toast.makeText(this, "Please Install whatsapp first!", Toast.LENGTH_SHORT).show();
                finish();
                Log.e("Whatsapp not installed", ":In phone");
            }

        } else {
            Log.e("Contact Not Exist", ": In phone");
            setContentView(R.layout.activity_main);
            getB1().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (SaveContact()) {
                        Toast.makeText(getBaseContext(), "Contact Saved!", Toast.LENGTH_SHORT).show();
                        finish();
                        boolean installed = appInstalledOrNot("com.whatsapp");
                        if (installed) {
                            //This intent will help you to launch if the package is already installed
                            try {
                                //String id = "+919667296937@s.whatsapp.net";

                                openWhatsApp(id);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.d("Installed", "fgfdg");
                        } else {
                            Log.d("Not Installed", "fgfdg");
                        }

                    } else {
                        Toast.makeText(getBaseContext(), "Error saving contact, see LogCat!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

}
