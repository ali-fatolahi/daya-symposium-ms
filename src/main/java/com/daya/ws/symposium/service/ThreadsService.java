package com.daya.ws.symposium.service;

import com.daya.ws.symposium.rest.CreateNewThreadRestModel;

public interface ThreadsService {
  String createNewThread(CreateNewThreadRestModel createNewThreadRestModel) throws Exception;
}
