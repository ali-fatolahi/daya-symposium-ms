package com.daya.ws.symposium.rest;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.daya.ws.symposium.service.ThreadsService;

@RestController
@RequestMapping("/threads")
public class SymposiumController {
  private ThreadsService threadsService;

  public SymposiumController(ThreadsService threadsService) {
    this.threadsService = threadsService;
  }

  @PostMapping
  public ResponseEntity<Object> createNewThread(@RequestBody CreateNewThreadRestModel CreateNewThreadRestModel) {
    String threadId;

    try {
      threadId = threadsService.createNewThread(CreateNewThreadRestModel);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorMessage(new Date(), e.getMessage(), "/threads"));
    }
    
    return ResponseEntity.status(HttpStatus.CREATED).body(threadId);
  }
}
