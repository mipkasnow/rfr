package io.student.rangiffler;

import io.student.rangiffler.jupiter.extension.StandSchemaExtension;
import org.junit.jupiter.api.Tag;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("io.student.rangiffler.test")
@IncludeTags("ui")
@Tag("ui")
class E2eSuite {

    @BeforeSuite
    static void verifyMigrations() {
        StandSchemaExtension.check();
    }
}
