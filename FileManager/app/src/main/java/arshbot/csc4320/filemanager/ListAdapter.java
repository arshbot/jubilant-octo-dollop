package arshbot.csc4320.filemanager;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ItemViewHolder> {

    private String mCurrentDirectory;
    private List<File> FileArray;
    private int contextMenuPosition;

    public ListAdapter(String selectedDirectory) {
        mCurrentDirectory = selectedDirectory;
        mContextMenuPosition = -1;
        mFileList = new ArrayList<>();
        File directory = new File(selectedDirectory);

        if (directory.exists()) {
            File[] files = directory.listFiles();
            for (File f : files) {
                if (f.canRead()) {
                    mFileList.add(f);
                }
            }
        } else {
            Log.i("FileManager: ", "Directory does not exist");
        }
    }

    @Override
    public int getItemCount() { return mFileList.size(); }
    public int getContextMenuPosition() { return mContextMenuPosition; }
    public String getCurrentDirectory() { return mCurrentDirectory; }
    public int getLastItemPosition() { return mFileList.size()-1; }
    public File getFile(int position) { return mFileList.get(position); }

    public void setDirectory(String newDirectory) {
        mCurrentDirectory = newDirectory;
        mContextMenuPosition = -1;
        mFileList = new ArrayList<>();
        File directory = new File(mCurrentDirectory);

        if (directory.exists()) {
            File[] files = directory.listFiles();
            for (File f : files) {
                if (f.canRead()) {
                    mFileList.add(f);
                }
            }
            notifyDataSetChanged();
        } else {
            Log.i("FileManager: ", "Directory does not exist");
        }
    }

    public void setContextMenuPosition(int i) { mContextMenuPosition = i; }

    public boolean createDirectory(File newDir) {
        if ((newDir.mkdir())) {
            addToDataSet(newDir);
            return true;
        }
        return false;
    }

    public boolean createFile(File newFile) {
        try {
            if (newFile.createNewFile()) {
                addToDataSet(newFile);
                return true;
            }
        } catch (IOException ioe) {
            return false;
        }
        return false;
    }

    private void addToDataSet(File file) {
        mFileList.add(file);
        notifyItemInserted(getLastItemPosition());
    }

    public boolean renameFile(int position, String newName) {
        File newFile = new File(newName);

        if (mFileList.get(position).renameTo(newFile)) {
            mFileList.set(position, newFile);
            notifyItemChanged(position);
            return true;
        }

        return false;
    }

    private boolean deleteFile(File file) {
        if (file.isDirectory()) {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    if (!deleteFile(f)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }

    public boolean deleteFile(int position) {
        try {
            File f = mFileList.get(position);

            if (deleteFile(f)) {
                mFileList.remove(position);
                notifyItemRemoved(position);
                return true;
            } else {
                return false;
            }
        } catch (SecurityException se) {
            return false;
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup vg, int i) {
        View itemView = LayoutInflater.from(vg.getContext()).
                inflate(R.layout.list_item_layout, vg, false);

        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder fileItemViewHolder, int position) {
        File file = mFileList.get(position);
        fileItemViewHolder.mTextView.setText(file.getName());

        if (file.isDirectory()) {
            fileItemViewHolder.mImageView.setImageResource(R.drawable.ic_folder_black_48dp);
        } else {
            fileItemViewHolder.mImageView.setImageResource(R.drawable.ic_description_black_48dp);
        }

        fileItemViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setContextMenuPosition(fileItemViewHolder.getAdapterPosition());
                return false;
            }
        });
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnCreateContextMenuListener {
        public ImageView mImageView;
        public TextView mTextView;
        public RelativeLayout mRelativeLayout;

        public ItemViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
            mImageView = (ImageView) itemView.findViewById(R.id.item_icon);
            mTextView = (TextView) itemView.findViewById(R.id.item_name);
            mRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.item_layout);
        }

        @Override
        public void onClick(View itemView) {
            MainActivity context = (MainActivity) itemView.getContext();
            int position = getAdapterPosition();

            if (getFile(position).isDirectory()) {
                context.setCurrentDirectory(position);
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            int position = getAdapterPosition();
            menu.setHeaderTitle(getFile(position).getName());

            //menu.add(Menu.NONE, R.id.get_info, Menu.NONE, R.string.view_file_info);
            menu.add(Menu.NONE, R.id.rename, Menu.NONE, R.string.rename_menu_item);
            menu.add(Menu.NONE, R.id.delete, Menu.NONE, R.string.delete_menu_item);

        }
    }
}
