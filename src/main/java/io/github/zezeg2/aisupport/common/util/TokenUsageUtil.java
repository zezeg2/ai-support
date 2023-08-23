package io.github.zezeg2.aisupport.common.util;

import com.theokanning.openai.Usage;
import io.github.zezeg2.aisupport.core.function.prompt.MessageContext;

public class TokenUsageUtil {

    public static UsageAccumulator initAccumulator() {
        return new UsageAccumulator();
    }

    public static class UsageAccumulator {
        private long promptToken = 0;
        private long completionTokens = 0;
        private long totalTokens = 0;

        public void add(Usage usage) {
            promptToken += usage.getPromptTokens();
            completionTokens += usage.getCompletionTokens();
            totalTokens += usage.getTotalTokens();
        }

        public Usage toUsage() {
            Usage resultUsage = new Usage();
            resultUsage.setPromptTokens(promptToken);
            resultUsage.setCompletionTokens(completionTokens);
            resultUsage.setTotalTokens(totalTokens);
            return resultUsage;
        }
    }

    public static Usage mergeUsage(MessageContext messageContext, Usage operand) {
        Usage usage = messageContext.getUsage();
        usage.setPromptTokens(usage.getPromptTokens() + operand.getPromptTokens());
        usage.setCompletionTokens(usage.getCompletionTokens() + operand.getCompletionTokens());
        usage.setTotalTokens(usage.getTotalTokens() + operand.getTotalTokens());
        return usage;
    }

    public static Usage mergeUsage(Usage origin, Usage operand) {
        origin.setPromptTokens(origin.getPromptTokens() + operand.getPromptTokens());
        origin.setCompletionTokens(origin.getCompletionTokens() + operand.getCompletionTokens());
        origin.setTotalTokens(origin.getTotalTokens() + operand.getTotalTokens());
        return origin;
    }
}