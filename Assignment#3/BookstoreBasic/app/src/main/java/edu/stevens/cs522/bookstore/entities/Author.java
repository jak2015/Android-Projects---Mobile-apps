package edu.stevens.cs522.bookstore.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class Author implements Parcelable {

    // TODO Modify this to implement the Parcelable interface.

    // NOTE: middleInitial may be NULL!

    public String firstName;
    public String middleInitial;
    public String lastName;
    public int bookfk;

    public Author(String firstName, String lastName)
    {
        this.firstName = firstName;
        this.lastName=lastName;
    }

    public String toString() {
        String str_name="";
        str_name = str_name+firstName+'\t'+lastName;
        return str_name;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[]
                {
                        this.firstName,
                        this.lastName
                });
    }

    public Author(Parcel in)
    {
        String[] data = new String[3];
        in.readStringArray(data);
        this.firstName = data[0];
        this.lastName=data[1];
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public Author createFromParcel(Parcel in) {
            return new Author(in);
        }

        public Author[] newArray(int size) {
            return new Author[size];
        }
    };

}