package net.reliqs.emonlight.web.controllers;

import net.reliqs.emonlight.web.git.CommitMessage;
import net.reliqs.emonlight.web.services.FileRepository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
@RequestMapping("/settings/*")
public class HistoryController {
    private static final Logger log = LoggerFactory.getLogger(HistoryController.class);

    @Autowired
    private FileRepository repo;

    private int pageSize = 10;

    List<CommitMessage> convertHistory(Iterable<RevCommit> history) {
        List<CommitMessage> list = StreamSupport.stream(history.spliterator(), false).map(CommitMessage::new).collect(Collectors.toList());
        return list;
    }

    @GetMapping("history/{page}")
    public String list(@PathVariable @Min(1) Integer page, Model model, HttpSession session) {
        log.debug("list {}", model);
        //        Settings settings = (Settings) session.getAttribute("settings");
        //        Settings settings = loadSettings(session);
        //        model.addAttribute("settings", settings);

        Iterable<RevCommit> res = repo.history((page - 1) * pageSize, pageSize);
        model.addAttribute("list", convertHistory(res));
        return "settings/history";
    }
}
