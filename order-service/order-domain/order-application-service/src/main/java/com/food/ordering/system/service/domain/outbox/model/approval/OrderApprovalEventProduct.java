package com.food.ordering.system.service.domain.outbox.model.approval;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class OrderApprovalEventProduct {

    @JsonProperty
    private String id;

    @JsonProperty
    private int quantity;
}
