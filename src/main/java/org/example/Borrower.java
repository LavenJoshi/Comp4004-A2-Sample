package org.example;

public class Borrower {
    private final String username;
    private final String password;
    private int borrowedBooks;

    public Borrower(String username, String password) {
        this.username = username;
        this.password = password;
        this.borrowedBooks = 0;

    }
    public int getBorrowedBooks() { return borrowedBooks; }
    public void setBorrowedBooks(int count) {
        this.borrowedBooks = count;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }


//    public void incrementBorrowedBooks() {
//        if (borrowedBooks < 3) {
//            borrowedBooks++;
//        }
//    }
    public void incrementBorrowedBooks() {
        borrowedBooks++;
    }


    public void decrementBorrowedBooks() {
        if (borrowedBooks > 0) {   // prevent negative values
            borrowedBooks--;
        }
    }

}

