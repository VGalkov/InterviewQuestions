/**
 * 
 */
package ru.galkov.Repos;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ru.galkov.Entities.Message;

/**
 * @author s0506
 *
 */
public interface MessageRepository extends CrudRepository<Message, Long> {

	  List< Message> findByTag(String lastName);
	  Message findById(long id);
	
}
