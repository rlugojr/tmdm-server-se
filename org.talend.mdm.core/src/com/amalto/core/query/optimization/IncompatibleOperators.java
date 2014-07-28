package com.amalto.core.query.optimization;

import com.amalto.core.query.user.*;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.TypeMetadata;

public class IncompatibleOperators implements Optimizer {

    private static final Logger LOGGER = Logger.getLogger(IncompatibleOperators.class);

    // Transformer for SQL server incompatible operators
    private static final SQLServerIncompatibleOperators SQL_SERVER = new SQLServerIncompatibleOperators();

    private final RDBMSDataSource dataSource;

    public IncompatibleOperators(RDBMSDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void optimize(Select select) {
        VisitorAdapter<Condition> transformer = getIncompatibleOperatorTransformer(dataSource);
        if (transformer != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Datasource (dialect " + dataSource.getDialectName() + ") may require operator replacements.");
            }
            Condition selectCondition = select.getCondition();
            if (selectCondition != null) {
                select.setCondition(selectCondition.accept(transformer));
            }
        }
    }

    private static VisitorAdapter<Condition> getIncompatibleOperatorTransformer(RDBMSDataSource dataSource) {
        RDBMSDataSource.DataSourceDialect dialect = dataSource.getDialectName();
        switch (dialect) {
        case ORACLE_10G:
        case MYSQL:
        case POSTGRES:
        case H2:
        case DB2:
            return null;
        case SQL_SERVER:
            // TMDM-7532: SQL Server does not like equals operator on large text values
            return SQL_SERVER;
        default:
            throw new NotImplementedException("Dialect '" + dialect + "' is not implemented.");
        }
    }

    private static class SQLServerIncompatibleOperators extends VisitorAdapter<Condition> {

        private Field currentField;

        @Override
        public Condition visit(Select select) {
            return select.getCondition().accept(this);
        }

        @Override
        public Condition visit(StringConstant constant) {
            return null;
        }

        @Override
        public Condition visit(Isa isa) {
            return isa;
        }

        @Override
        public Condition visit(UnaryLogicOperator condition) {
            return condition;
        }

        @Override
        public Condition visit(IsEmpty isEmpty) {
            return isEmpty;
        }

        @Override
        public Condition visit(IsNull isNull) {
            return isNull;
        }

        @Override
        public Condition visit(NotIsEmpty notIsEmpty) {
            return notIsEmpty;
        }

        @Override
        public Condition visit(NotIsNull notIsNull) {
            return notIsNull;
        }

        @Override
        public Condition visit(Field field) {
            currentField = field;
            return null;
        }

        @Override
        public Condition visit(BinaryLogicOperator condition) {
            condition.setLeft(condition.getLeft().accept(this));
            condition.setRight(condition.getRight().accept(this));
            return condition;
        }

        @Override
        public Condition visit(Compare condition) {
            Predicate predicate = condition.getPredicate();
            if (predicate == Predicate.EQUALS) {
                Expression left = condition.getLeft();
                Expression right = condition.getRight();
                left.accept(this);
                right.accept(this);
                TypeMetadata fieldType = currentField.getFieldMetadata().getType();
                if (fieldType.getData(MetadataRepository.DATA_MAX_LENGTH) != null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Replacing EQUALS with STARTS_WITH (can't use EQUALS on large text column).");
                    }
                    return new Compare(left, Predicate.STARTS_WITH, right);
                }
            }
            return condition;
        }

        @Override
        public Condition visit(Range range) {
            return range;
        }

        @Override
        public Condition visit(Condition condition) {
            return condition;
        }

        @Override
        public Condition visit(FullText fullText) {
            return fullText;
        }

        @Override
        public Condition visit(FieldFullText fieldFullText) {
            return fieldFullText;
        }
    }
}
