package org.example;

import java.util.ArrayList;
import java.util.List;

// Catalogue class
public class Catalogue {
    private ArrayList<Book> books = new ArrayList<>();


    public void addBook(Book book) {
        books.add(book);
    }

    public Book getBook(int index) {
        return books.get(index);
    }

    public int getCatalogueSize() {
        return books.size();
    }
    public List<Book> getBooks() {
        return books;
    }
    public Book findBook(String title) {
        for (Book book : books) {
            if (book.getTitle().equals(title)) {
                return book;
            }
        }
        return null; // or throw an exception if preferred
    }

}
