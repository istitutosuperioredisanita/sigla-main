package it.cnr.test.util;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

/**
 * Per classi di test i cui metodi sono una catena dipendente (@TestMethodOrder):
 * dopo il primo test fallito i successivi della stessa classe vengono saltati,
 * invece di fallire a cascata dopo il timeout di attesa degli elementi.
 */
public class StopOnFirstFailure implements TestWatcher, ExecutionCondition {

    private static final ExtensionContext.Namespace NS =
            ExtensionContext.Namespace.create(StopOnFirstFailure.class);

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        context.getRoot().getStore(NS)
                .put(context.getRequiredTestClass().getName(), context.getDisplayName());
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (context.getTestMethod().isEmpty()) {
            return ConditionEvaluationResult.enabled("livello classe");
        }
        Object failed = context.getRoot().getStore(NS).get(context.getRequiredTestClass().getName());
        return failed == null
                ? ConditionEvaluationResult.enabled("nessun fallimento precedente")
                : ConditionEvaluationResult.disabled("Saltato: fallito in precedenza " + failed);
    }
}
