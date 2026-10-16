package workshop.ceremony;

import module java.base;
import module java.sql;
import java.util.Date;

public final class NamingConflictDemo {
    // A single-type import takes precedence over both on-demand module imports.
    public Date generatedAt() {
        return new Date();
    }

    // The other Date remains available when its qualified name communicates intent.
    public java.sql.Date serviceDay(LocalDate date) {
        return java.sql.Date.valueOf(date);
    }
}
