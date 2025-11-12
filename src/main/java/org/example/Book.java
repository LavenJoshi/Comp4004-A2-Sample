package org.example;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Book {
    private final String title;
    private final String author;
    private Borrower currentHolder; // who currently has the book
    private final List<Borrower> holdQueue; // FIFO hold queue
    private LocalDate dueDate;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
        this.currentHolder = null;
        this.holdQueue = new ArrayList<>();
    }
    private boolean extendedOnce = false;
    private boolean extended = false;

    public boolean isExtended() {
        return extended;
    }
    public long countHoldOccurrences(Borrower borrower) {
        return holdQueue.stream()
                .filter(b -> b.equals(borrower))
                .count();
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }
    public boolean borrow(Borrower borrower) {
        if (currentHolder != null) return false;
        if (borrower.getBorrowedBooks() >= 3) return false;

        // If queue exists, borrower MUST be first
        if (!holdQueue.isEmpty() && !holdQueue.get(0).equals(borrower)) return false;

        currentHolder = borrower;
        borrower.incrementBorrowedBooks();
        dueDate = LocalDate.now().plusDays(14);

        // Remove from queue if they were first
        if (!holdQueue.isEmpty() && holdQueue.get(0).equals(borrower)) {
            holdQueue.remove(0);
        }

        return true;
    }

    public boolean extendDueDate() {
        if (extendedOnce || !holdQueue.isEmpty() || dueDate == null) {
            return false;
        }
        dueDate = dueDate.plusDays(7); // extend by 7 days (or 14, your choice)
        extendedOnce = true;
        return true;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Borrower getCurrentHolder() {
        return currentHolder;
    }

    public void setCurrentHolder(Borrower borrower) {
        this.currentHolder = borrower;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public List<Borrower> getHoldQueue() {
        return holdQueue;
    }

    public boolean placeHold(Borrower borrower) {
        if (!holdQueue.contains(borrower)) {
            holdQueue.add(borrower);
            return true;
        }
        return false;
    }

public String getStatusForBorrower(Borrower borrower) {
    if (currentHolder != null && currentHolder.equals(borrower)) {
        return "Checked Out (You)";
    } else if (holdQueue.contains(borrower)) {
        return "On Hold (You)";
    } else if (!holdQueue.isEmpty()) {
        return "On Hold";
    } else if (currentHolder != null) {
        return "Checked Out";
    } else {
        return "Available";
    }
}
    public String getStatus() {
        if (currentHolder == null && !holdQueue.isEmpty()) {
            return "On Hold"; // someone is waiting
        } else if (currentHolder == null) {
            return "Available";
        } else {
            return "Checked Out";
        }
    }
    public void addHold(Borrower borrower) {
        holdQueue.add(borrower);  // lets the system know someone put the book on hold
    }

    public String getAvailabilityStatus() {
        if (currentHolder == null) return "Available";
        else if (!holdQueue.isEmpty()) return "On Hold";
        else return "Borrowed";
    }

}


