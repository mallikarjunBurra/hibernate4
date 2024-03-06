package com.example.goodreads.service;

import com.example.goodreads.model.*;
import com.example.goodreads.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthorJpaService implements AuthorRepository {

    @Autowired
    private AuthorJpaRepository authorJpaRepository;

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Override
    public ArrayList<Author> getAuthors() {
        List<Author> authorList = authorJpaRepository.findAll();
        ArrayList<Author> authors = new ArrayList<>(authorList);
        return authors;
    }

    @Override
    public Author getAuthorById(int authorId) {
        try {
            Author author = authorJpaRepository.findById(authorId).get();
            return author;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Author addAuthor(Author author) {
        List<Integer> bookId = new ArrayList<>();
        for (Book book : author.getBooks()) {
            bookId.add(book.getId());
        }
        List<Book> books = bookJpaRepository.findAllById(bookId);
        author.setBooks(books);

        for (Book book : books) {
            book.getAuthors().add(author);
        }

        Author savedAuthor = authorJpaRepository.save(author);
        bookJpaRepository.saveAll(books);

        return savedAuthor;
    }

    @Override
    public Author updateAuthor(int authorId, Author author) {
        try {
            Author new_author = authorJpaRepository.findById(authorId).get();
            if (author.getAuthorName() != null) {
                new_author.setAuthorName(author.getAuthorName());
            }
            if (author.getBooks() != null) {

                List<Book> books = new_author.getBooks();
                for (Book book : books) {
                    book.getAuthors().remove(new_author);
                }
                bookJpaRepository.saveAll(books);

                List<Integer> bookIds = new ArrayList<>();
                for (Book book : author.getBooks()) {
                    bookIds.add(book.getId());
                }

                List<Book> newBooks = bookJpaRepository.findAllById(bookIds);
                for (Book book : newBooks) {
                    book.getAuthors().add(new_author);
                }
                bookJpaRepository.saveAll(newBooks);
                new_author.setBooks(newBooks);

            }

            authorJpaRepository.save(new_author);
            return new_author;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void deleteAuthor(int authorId) {
        try {
            Author author = authorJpaRepository.findById(authorId).get();
            List<Book> books = author.getBooks();
            for (Book book : books) {
                book.getAuthors().remove(author);
            }
            bookJpaRepository.saveAll(books);
            authorJpaRepository.deleteById(authorId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        throw new ResponseStatusException(HttpStatus.NO_CONTENT);
    }

    @Override
    public List<Book> getAuthorBooks(int authorId) {
        try {
            Author author = authorJpaRepository.findById(authorId).get();
            return author.getBooks();

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}