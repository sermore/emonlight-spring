package net.reliqs.emonlight.web.controllers;

import net.reliqs.emonlight.web.entities.Node;
import net.reliqs.emonlight.web.services.DataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Controller
public class ChartController {

	private DataRepo repo;

	@Autowired
	public ChartController(DataRepo repo) {
		super();
		this.repo = repo;
	}

	@RequestMapping("/live")
	public String live(@RequestParam(value = "id", required = true, defaultValue = "0") long id, Model model) {
		Node n = repo.findNode(id);
		if (n != null) {
			model.addAttribute("id", id);
			model.addAttribute("node", repo.findNode(id));
		}
		return "live";
	}

	@RequestMapping("/data")
	public @ResponseBody
	Iterable<Number[]> data(@RequestParam(value = "id", required = true, defaultValue = "0") long id,
						@RequestParam(value = "timeStart", required = true, defaultValue = "0") long timeStart,
						Model model) {
		Iterable<Number[]> d = repo.getData(Arrays.asList(id), timeStart);
		return d;
	}

}
