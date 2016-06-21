package edu.stevens.cs522.bookstore.entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import contracts.BookContract;

public class Book implements Parcelable
{
    public int id;
    public String title;
    public String price;
    public String isbn;
    public String author;

    public Book(Parcel in)
    {
        this.title = in.readString();
        this.price = in.readString();
    }

    public Book (String title,String isbn, String price)
    {
        this.title = title;
        this.isbn = isbn;
        this.price = price;
    }
    @Override
    public void writeToParcel(Parcel out,int i)
    {
        out.writeString(this.title);
        out.writeString(this.price);
    }
    public int describeContents()
    {
        return 0;
    }

    public static Creator<Book>CREATOR = new Creator<Book>()
    {
        public Book
        createFromParcel(Parcel in)
        {
            return new Book(in);
        }
        public Book[]newArray(int size)
        {
            return new Book[size];
        }
    };

    public Book(Cursor cursor){
        this.id = BookContract.getID(cursor);
        this.title = BookContract.getTitle(cursor);
        this.isbn = BookContract.getIsbn(cursor);
        this.price = BookContract.getPrice(cursor);
        this.author = BookContract.getAuthor(cursor);
    }

    public void writeToProvider(ContentValues values){
        BookContract.putTitle(values,this.title);
        BookContract.putIsbn(values,this.isbn);
        BookContract.putPrice(values,this.price);
        BookContract.putAuthor(values,this.author);
    }

    public  String toString()
    {
        String x = "";
        x = x + this.title + '\n' + this.price;
        return x;
    }
}
