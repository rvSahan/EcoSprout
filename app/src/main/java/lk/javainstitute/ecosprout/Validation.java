package lk.javainstitute.ecosprout;

public class Validation {
    public static boolean validateMobileNumber(String mobile) {

        return mobile.matches("^07[01245678]{1}[0-9]{7}$");

    }

    public static boolean validateName(String name) {

        return name.matches("^[a-zA-Z]*$");

    }

    public static boolean validateEmail(String email) {

        return email.matches("^(?=.{1,64}@)[A-Za-z0-9\\+_-]+(\\.[A-Za-z0-9\\+_-]+)*@"
                + "[^-][A-Za-z0-9\\+-]+(\\.[A-Za-z0-9\\+-]+)*(\\.[A-Za-z]{2,})$");

    }

    public static boolean validatePassword(String password){
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$");
    }
}
