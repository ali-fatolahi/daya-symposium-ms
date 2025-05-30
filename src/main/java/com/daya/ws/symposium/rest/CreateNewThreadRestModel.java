package com.daya.ws.symposium.rest;

import java.math.BigDecimal;

public class CreateNewThreadRestModel {
  private String title;
  private String body;
  private Integer totalAwards;
  private BigDecimal awardValue;
  
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getBody() {
    return body;
  }
  public void setBody(String body) {
    this.body = body;
  }
  public Integer getTotalAwards() {
    return totalAwards;
  }
  public void setTotalAwards(Integer totalAwards) {
    this.totalAwards = totalAwards;
  }
  public BigDecimal getAwardValue() {
    return awardValue;
  }
  public void setAwardValue(BigDecimal awardValue) {
    this.awardValue = awardValue;
  }
}
