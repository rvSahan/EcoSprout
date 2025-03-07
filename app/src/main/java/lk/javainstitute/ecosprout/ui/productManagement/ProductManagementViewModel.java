package lk.javainstitute.ecosprout.ui.productManagement;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProductManagementViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ProductManagementViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}