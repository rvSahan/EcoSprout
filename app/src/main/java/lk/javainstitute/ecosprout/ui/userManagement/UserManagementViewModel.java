package lk.javainstitute.ecosprout.ui.userManagement;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserManagementViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public UserManagementViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}