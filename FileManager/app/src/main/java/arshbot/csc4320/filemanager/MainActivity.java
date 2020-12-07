package arshbot.csc4320.filemanager;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static String ROOT = "/";
    private ListAdapter mAdapter;
    private TextView mCurrentDirView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new ListAdapter(ROOT);
        mCurrentDirView = (TextView) findViewById(R.id.current_dir_view);
        mCurrentDirView.setText(mAdapter.getCurrentDirectory());
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.file_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        registerForContextMenu(recyclerView);
    }

    protected void setCurrentDirectory(int position) {
        try {
            setCurrentDirectory(mAdapter.getFile(position).getCanonicalPath());
        } catch (IOException ioe) {
            Log.i("FileManager: ", "Error setting current directory.");
        }
    }

    protected void setCurrentDirectory(String canonicalPath) {
        mAdapter.setDirectory(canonicalPath);
        mCurrentDirView.setText(canonicalPath);
    }

    /* Navigate to this application's data directory. */
    public void goToAppFolder(View v) {
        try {
            setCurrentDirectory(getFilesDir().getCanonicalPath());
        } catch (IOException ioe) {
            Log.i("FileManager: ", "Error opening app data directory");
        }
    }

    /* Navigate to the current directory's parent directory,
     * or root directory if parent can't be read. */
    public void goToParentDirectory(View v) {
        if (!mAdapter.getCurrentDirectory().equals(ROOT)) {
            File current = new File(mAdapter.getCurrentDirectory());
            File parent = current.getParentFile();

            if (parent.canRead()) {
                setCurrentDirectory(current.getParent());
            } else {
                setCurrentDirectory(ROOT);
            }
        }
    }

    protected void createDirectory(String name) {
        String path = mAdapter.getCurrentDirectory() + "/" + name;
        File newDir = new File(path);

        if (!mAdapter.createDirectory(newDir)) {
            Toast.makeText(this, "Unable to create directory", Toast.LENGTH_SHORT).show();
        }
    }

    protected void createFile(String name) {
        String path = mAdapter.getCurrentDirectory() + "/" + name;
        File newFile = new File(path);

        if (!mAdapter.createFile(newFile)) {
            Toast.makeText(this, "Unable to create folder", Toast.LENGTH_SHORT).show();
        }
    }

    protected void renameFile(int position, String newName) {
        String path = mAdapter.getCurrentDirectory() + "/" + newName;

        if (!mAdapter.renameFile(position, path)) {
            Toast.makeText(this, "Unable to rename file/folder",
                    Toast.LENGTH_SHORT).show();
        }
    }

    protected void deleteFile(int position) {
        if (!mAdapter.deleteFile(position)) {
            Toast.makeText(this, "Unable to delete", Toast.LENGTH_SHORT).show();
        }
    }

    /******************
     * Context Menu   *
     ******************/
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = mAdapter.getContextMenuPosition();

        switch(item.getItemId()) {
            case R.id.rename:
                showRenameFileDialog(position);
                break;
            case R.id.delete:
                showDeleteDialog(position);
        }

        return super.onContextItemSelected(item);
    }

    /******************
     * Dialogs        *
     ******************/
    public void showCreateDirDialog(View v) {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.create_folder_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.newDirEditText);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        createDirectory(editText.getText().toString());
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertDialogBuilder.create().show();
    }

    public void showCreateFileDialog(View v) {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.create_file_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.newFileEditText);

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        createFile(editText.getText().toString());
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertDialogBuilder.create().show();
    }

    protected void showRenameFileDialog(final int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.rename_file_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.renameFileEditText);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        renameFile(position, editText.getText().toString());
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertDialogBuilder.create().show();
    }

    protected void showDeleteDialog(final int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.delete_file_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        File selectedFile = mAdapter.getFile(position);
        String fileType = selectedFile.isDirectory() ? "folder" : "file";

        TextView confirmText = (TextView) promptView.findViewById(R.id.deleteFileConfirmTextView);
        confirmText.setText(String.format(getResources().getString(R.string.delete_confirm_prompt),
                fileType, selectedFile.getName()));

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteFile(position);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertDialogBuilder.create().show();
    }
}
