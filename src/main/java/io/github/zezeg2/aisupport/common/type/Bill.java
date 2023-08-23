package io.github.zezeg2.aisupport.common.type;

import com.theokanning.openai.Usage;
import io.github.zezeg2.aisupport.common.enums.model.AIModel;
import io.github.zezeg2.aisupport.common.util.TokenUsageUtil;
import lombok.Data;

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

            double promptCost = model.getRequestPrice() * usage.getPromptTokens();
            double completionCost = model.getResponsePrice() * usage.getCompletionTokens();

            double totalCost = promptCost + completionCost;

            prices.put(model, totalCost);
        }
    }

    public void countTotalBill() {
        totalPrice = 0.0;
        for (Double cost : prices.values()) {
            totalPrice += cost;
        }
    }
}

