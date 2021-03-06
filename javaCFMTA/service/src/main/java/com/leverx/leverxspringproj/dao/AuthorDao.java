package com.leverx.leverxspringproj.dao;

import com.leverx.leverxspringproj.intfce.IAuthorDao;
import com.leverx.leverxspringproj.model.Author;
import com.leverx.leverxspringproj.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class AuthorDao implements IAuthorDao {
	private static final Logger logger = LoggerFactory.getLogger(AuthorDao.class);

	private final DataSource dataSource;
	private static final String TABLE_NAME = "javaCFMTA::Author";
	private static final String AUTHOR_ID = "author_id";
	private static final String AUTHOR_NAME = "name";
	private static final String sequenceName = "javaCFMTA::author_id";
	private static final String RELATIVE_TABLE_NAME = "ExtraInfo.Book";

	@Autowired
	public AuthorDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override  
	public Optional<Author> getById(String id) {
		Optional<Author> entity = Optional.empty();
		try (Connection conn = dataSource.getConnection();
				PreparedStatement stmnt = conn.prepareStatement(
				String.format("SELECT TOP 1 * FROM \"%s\" WHERE \"%s\" = ?", TABLE_NAME, AUTHOR_ID)))
		{
			stmnt.setString(1, id);
			ResultSet result = stmnt.executeQuery();

			if (result.next()) {
				Author author = new Author(
				        id,
                        result.getString(AUTHOR_NAME)
                );
				entity = Optional.of(author);
			} else {
				entity = Optional.empty();
			}
		} catch (SQLException e) {
			logger.error("Can't get entity by Id: " + e.getMessage());
		}
		return entity;  
	} 
	 
	@Override
	public List<Author> getAll() {
		 List<Author> authorsList = new ArrayList<>();

		 try (Connection conn = dataSource.getConnection();
				 PreparedStatement stmnt = conn.prepareStatement(String.format("SELECT * FROM \"%s\"", TABLE_NAME)))
		 {
			 ResultSet result = stmnt.executeQuery();
			 while (result.next()) {
				 Author author = new Author(
                         result.getString(AUTHOR_ID),
                         result.getString(AUTHOR_NAME)
                 );
				 authorsList.add(author);
			 }
		 } catch (SQLException e) {
			 logger.error("Can't get list of entities: " + e.getMessage());
		 }
		 return authorsList;
	} 
	 
	@Override
	public Author createEntity(Author entity) {
		 entity.setAuthor_id(Sequence.getNextValue(dataSource, sequenceName, logger));

		 try (Connection conn = dataSource.getConnection();
				 PreparedStatement stmnt = conn.prepareStatement(
				String.format("INSERT INTO \"%s\"(\"author_id\", \"name\") VALUES (?, ?)", TABLE_NAME)))
		 {
			 stmnt.setString(1, entity.getAuthor_id());
			 stmnt.setString(2, entity.getName());
			 stmnt.execute();
		 } catch (SQLException e) {
			 logger.error("Can't to create entity: " + e.getMessage());
		 }

		 return entity;
	} 
	 
	@Override
	public String deleteEntity(String id) {
		 try (Connection conn = dataSource.getConnection();
			 	PreparedStatement stmnt = conn.prepareStatement(
			 			String.format("DELETE FROM \"%s\" WHERE \"%s\" = ?", TABLE_NAME, AUTHOR_ID)))
		 {
			 stmnt.setString(1, id);
			 stmnt.execute();
		 } catch (SQLException e) {
			 logger.error("Can't delete entity: " + e.getMessage());
		 }

		 return id;
	}
	 
	@Override
	public Author updateEntity(Author entity, String id) {
		 try(Connection conn = dataSource.getConnection();
				 PreparedStatement stmnt = conn.prepareStatement(
				 		String.format("UPDATE \"%s\" SET \"%s\" = ? WHERE \"%s\" = ?", TABLE_NAME, AUTHOR_NAME, AUTHOR_ID)))
		 {
			 stmnt.setString(1, entity.getName());
			 stmnt.setString(2, id);
			 stmnt.executeUpdate();
		 } catch (SQLException e) {
			 logger.error("Can't update entity: " + e.getMessage());
		 }

		 return entity;
	}

	@Override
	public Map<Author, List<Book>> getRelativeEntity(String id){
		Map<Author, List<Book>> map = new HashMap<>();
		try (Connection connection = dataSource.getConnection();
			 PreparedStatement stmnt = connection.prepareStatement(
					 "SELECT \"A\".\"name\" as \"a_name\", \"B\".\"name\" as \"b_name\", \"B\".\"author_id\", \"book_id\" FROM \"javaCFMTA::Author\" AS \"A\" INNER JOIN \"javaCFMTA::ExtraInfo.Book\" AS \"B\" ON \"A\".\"author_id\"=\"B\".\"author_id\" WHERE \"A\".\"author_id\"=?"))
		{
			stmnt.setString(1, id);
			ResultSet result = stmnt.executeQuery();
			List<Book> books = new ArrayList<>();
			Author author = null;
			while (result.next()) {
				if (author == null){
					author = new Author(
								result.getString("author_id"),
								result.getString("a_name")
					);
				}

				Book book = new Book(
								result.getString("book_id"),
								result.getString("b_name"),
								result.getString("author_id")
				);
				books.add(book);
			}

			map.put(author, books);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Can't join tables: " + e.getMessage());
		}

		return map;
	}
	
}
