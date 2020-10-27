package com.example.sociopathy.controller;

import com.example.sociopathy.model.Message;
import com.example.sociopathy.model.User;
import com.example.sociopathy.repo.MessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class MessageController {
    @Autowired
    private MessageRepo messageRepo;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/main")
    public String getMessages(@RequestParam(required = false, defaultValue = "") String filter,
                              Model model,
                              @PageableDefault(sort = { "id" }, direction = Sort.Direction.DESC) Pageable pageable){
        Page<Message> page;

        if (filter != null && !filter.isEmpty()) {
            page = messageRepo.findByTag(filter, pageable);
        } else {
            page = messageRepo.findAll(pageable);
        }

       /* if(page.getContent() == null){
            page = Page.empty(pageable);
        }*/

        model.addAttribute("page", page);
        model.addAttribute("url", "/main");
        model.addAttribute("filter", filter);

        return "main";
    }

    @PostMapping("/main")
    public String add(@AuthenticationPrincipal User user,
                      @RequestParam String title,
                      @RequestParam String text,
                      @RequestParam String tag,
                      @RequestParam("file") MultipartFile file,
                      Model model,
                      @PageableDefault(sort = { "id" }, direction = Sort.Direction.DESC) Pageable pageable)
            throws IOException {
        String postId = UUID.randomUUID().toString();
        Message message = new Message(title, text, tag, user, postId);

        if (file != null && !file.getOriginalFilename().isEmpty()) {
            File uploadDir = new File(uploadPath);

            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            String uuidFile = UUID.randomUUID().toString();
            String resultFilename = uuidFile + "." + file.getOriginalFilename();

            file.transferTo(new File(uploadPath + "/" + resultFilename));

            message.setFilename(resultFilename);
        }

        messageRepo.save(message);

        Page<Message> page;
        page = messageRepo.findAll(pageable);

        model.addAttribute("page", page);
        model.addAttribute("url", "/main");

        return "main";
    }

    @GetMapping("/main/post/{postId}")
    public String getPost(@PathVariable("postId") String postId, Model model){
        Message message = messageRepo.findByPostId(postId);

        model.addAttribute("message", message);

        return "post";
    }

}
