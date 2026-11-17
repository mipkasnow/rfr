package io.student.rangiffler.data.db;

import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.event.SimpleJdbcEventListener;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.util.ResultsUtils;

import java.sql.SQLException;
import java.util.Optional;

public final class AllureSql extends SimpleJdbcEventListener {

    @Override
    public void onAfterAnyExecute(StatementInformation statement, long elapsed, SQLException failure) {
        Allure.getLifecycle().getCurrentExecutableKey().ifPresent(key -> Allure.getLifecycle().logStep(key, step(statement, failure)));
    }

    private static StepResult step(StatementInformation statement, SQLException failure) {
        StepResult step = new StepResult().setName("SQL: " + statement.getSqlWithValues());
        return Optional.ofNullable(failure)
                .map(error -> step
                        .setStatus(Status.FAILED)
                        .setStatusDetails(ResultsUtils.getStatusDetails(error).orElseGet(StatusDetails::new)))
                .orElseGet(() -> step.setStatus(Status.PASSED));
    }
}
