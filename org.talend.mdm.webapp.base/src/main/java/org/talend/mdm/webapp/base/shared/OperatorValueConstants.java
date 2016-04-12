package org.talend.mdm.webapp.base.shared;

import java.util.Arrays;
import java.util.List;

public class OperatorValueConstants {

    private static final long serialVersionUID = 1L;

    public static List<String> groupOperatorValues = Arrays.asList("AND", "OR"); //$NON-NLS-1$ //$NON-NLS-2$

    public static List<String> fullOperatorValues = Arrays.asList("CONTAINS", "EQUALS", "NOT_EQUALS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "GREATER_THAN", "GREATER_THAN_OR_EQUAL", "LOWER_THAN", "LOWER_THAN_OR_EQUAL", "STARTSWITH"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 

    public static List<String> fulltextOperatorValues = Arrays.asList("FULLTEXTSEARCH"); //$NON-NLS-1$

    public static List<String> dateOperatorValues = Arrays.asList("EQUALS", "LOWER_THAN", "GREATER_THAN"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    public static List<String> numOperatorValues = Arrays.asList("EQUALS", "NOT_EQUALS", "GREATER_THAN", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "GREATER_THAN_OR_EQUAL", "LOWER_THAN", "LOWER_THAN_OR_EQUAL"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    public static List<String> booleanOperatorValues = Arrays.asList("EQUALS"); //$NON-NLS-1$

    public static List<String> enumOperatorValues = Arrays.asList("EQUALS", "LOWER_THAN", "GREATER_THAN", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "LOWER_THAN_OR_EQUAL", "GREATER_THAN_OR_EQUAL"); //$NON-NLS-1$ //$NON-NLS-2$

    public static final String CONTAINS = "CONTAINS"; //$NON-NLS-1$

    public static final String STARTSWITH = "STARTSWITH"; //$NON-NLS-1$

    public static final String EMPTY_NULL = "Is Empty Or Null"; //$NON-NLS-1$

    public static final String FULLTEXTSEARCH = "FULLTEXTSEARCH"; //$NON-NLS-1$
}
