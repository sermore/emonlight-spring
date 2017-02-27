package net.reliqs.emonlight.web.controllers;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.reliqs.emonlight.web.services.DataRepo;

@Controller
public class ChartController {
	private static final Logger log = LoggerFactory.getLogger(ChartController.class);

	private DataRepo repo;

	@Autowired
	public ChartController(DataRepo repo) {
		super();
		this.repo = repo;
	}

	@RequestMapping("/live")
	public String live(@RequestParam(value = "id[]", required = true) Long[] ids, Model model) {
//		Node n = repo.findNode(id);
//		if (n != null) {
			model.addAttribute("ids", ids);
//			model.addAttribute("node", repo.findNode(id));
//		}
		return "live";
	}

	@RequestMapping("/data")
	public @ResponseBody
	Iterable<Number[]> data(@RequestParam(value = "id[]", required = true) Long[] ids,
						@RequestParam(value = "timeStart", required = true, defaultValue = "0") long timeStart,
						Model model) {
		log.debug("PARAM {}", Arrays.toString(ids));
		Iterable<Number[]> d = repo.getData(Arrays.asList(ids), timeStart);
		return d;
	}

}
