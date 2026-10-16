package workshop.ceremony;

import module java.base;

// LAB: Add `import module java.sql;`. Why does Date then become ambiguous?
public final class NamingConflictDemo {
    public Date generatedAt() {
        return new Date();
    }
}
