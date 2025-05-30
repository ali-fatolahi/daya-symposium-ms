package com.daya.ws.symposium.service;

import java.math.BigDecimal;

public class ThreadCreatedEvent {
  private String id;
  private String title;
  private String body;
  private Integer totalAwards;
  private BigDecimal awardValue;

  public ThreadCreatedEvent() { }

  public ThreadCreatedEvent(
    final String id,
    final String title,
    final String body,
    final Integer totalAwards,
    final BigDecimal awardValue) {
      this.id = id;
      this.title = title;
      this.body = body;
      this.totalAwards = totalAwards;
      this.awardValue = awardValue;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

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
