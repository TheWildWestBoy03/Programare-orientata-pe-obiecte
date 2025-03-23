package org.poo.app.utils;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExchangeMoneyTypes {
    private String sourceMoney;
    private String targetMoney;
    public ExchangeMoneyTypes(String sourceMoney, String targetMoney) {
        this.sourceMoney = sourceMoney;
        this.targetMoney = targetMoney;
    }

}
