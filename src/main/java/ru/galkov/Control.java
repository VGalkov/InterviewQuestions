package ru.galkov;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.galkov.Entities.Message;
import ru.galkov.Repos.MessageRepository;

@Controller
public class Control {
	
	@Autowired
	private MessageRepository mRepo;
	
	@GetMapping("hello")
	public String makeAnswer(
				@RequestParam(name="name") String name,
				Map<String, Object> model
			) {
		
			Iterable<Message> msg = mRepo.findAll();
			
			
			model.put("name", name);
			model.put("msg", msg.toString());

			return "hello";
	}
	
	@PostMapping("add")
	public String addMessage(
				@RequestParam(name="text") String text,
				@RequestParam(name="tag") String tag,
				Map<String, Object> model
				) {
		
		Message msg = new Message(text,tag);
		mRepo.save(msg);
		
		
		model.put("msg", msg.toString());		
		return "hello";
		
	}
	
		
}
				


