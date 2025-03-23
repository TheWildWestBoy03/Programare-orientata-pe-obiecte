package org.poo.app.utils;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CommerciantSummary {
    private String commerciantName;
    private Double amount;

    public CommerciantSummary(String commerciantName, Double amount) {
        this.commerciantName = commerciantName;
        this.amount = amount;
    }
}
