package io.github.zezeg2.aisupport.common.bill;

import com.theokanning.openai.Usage;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.util.TokenUsageUtil;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Bill {
    private Map<AIModel, Usage> usages = new ConcurrentHashMap<>();
    private Map<AIModel, Double> prices = new ConcurrentHashMap<>();
    private Double totalPrice;

    public void addUsage(AIModel model, Usage usage) {
        if (usages.containsKey(model)) {
            Usage updatedUsage = TokenUsageUtil.mergeUsage(usages.get(model), usage);
            usages.put(model, updatedUsage);
        } else {
            usages.put(model, usage);
        }

        countBillByModel();
        countTotalBill();
    }

    public void countBillByModel() {
        for (Map.Entry<AIModel, Usage> entry : usages.entrySet()) {
            AIModel model = entry.getKey();
            Usage usage = entry.getValue();

            double promptCost = roundTo7DecimalPlaces(model.getRequestPrice() * usage.getPromptTokens());
            double completionCost = roundTo7DecimalPlaces(model.getResponsePrice() * usage.getCompletionTokens());

            double totalCost = roundTo7DecimalPlaces(promptCost + completionCost);

            prices.put(model, totalCost);
        }
    }

    public void countTotalBill() {
        totalPrice = 0.0;
        for (Double cost : prices.values()) {
            totalPrice += cost;
        }
        totalPrice = roundTo7DecimalPlaces(totalPrice);
    }

    public Bill merge(Bill operand) {
        for (Map.Entry<AIModel, Usage> entry : operand.usages.entrySet()) {
            AIModel model = entry.getKey();
            Usage operandUsage = entry.getValue();

            if (this.usages.containsKey(model)) {
                Usage mergedUsage = TokenUsageUtil.mergeUsage(this.usages.get(model), operandUsage);
                this.usages.put(model, mergedUsage);
            } else {
                this.usages.put(model, operandUsage);
            }
        }

        countBillByModel();
        countTotalBill();

        return this;
    }

    private double roundTo7DecimalPlaces(double value) {
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(7, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}