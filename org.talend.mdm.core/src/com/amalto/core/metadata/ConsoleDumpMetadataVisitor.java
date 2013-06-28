/*
 * Copyright (C) 2006-2012 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.metadata;

import org.apache.log4j.Logger;

import java.util.List;

/**
 *
 */
@SuppressWarnings({"HardCodedStringLiteral", "nls"})
public class ConsoleDumpMetadataVisitor extends DefaultMetadataVisitor<Void> {

    private int indent = 0;

    private Logger logger;

    public ConsoleDumpMetadataVisitor() {
    }

    public ConsoleDumpMetadataVisitor(Logger logger) {
        this.logger = logger;
    }

    private void log(String message) {
        if (logger != null) {
            StringBuilder indentString = new StringBuilder();
            for (int i = 0; i < indent; i++) {
                indentString.append('\t');
            }
            indentString.append(message);
            logger.info(indentString.toString());
        } else {
            for (int i = 0; i < indent; i++) {
                System.out.print('\t');
            }
            System.out.println(message);
        }
    }

    public Void visit(ComplexTypeMetadata complexType) {
        log("[Type] " + complexType.getName()); //$NON-NLS-1$
        String keyFields = "";
        for (FieldMetadata keyFieldMetadata : complexType.getKeyFields()) {
            keyFields += keyFieldMetadata.getName() + " "; //$NON-NLS-1$
        }
        if (!keyFields.isEmpty()) {
            log("\t[Key fields] " + keyFields); //$NON-NLS-1$
        }

        String superTypes = ""; //$NON-NLS-1$
        for (TypeMetadata superType : complexType.getSuperTypes()) {
            superTypes += superType.getName() + " "; //$NON-NLS-1$
        }
        if (!superTypes.isEmpty()) {
            log("[Super types] " + superTypes); //$NON-NLS-1$
        }

        indent++;
        {
            super.visit(complexType);
        }
        indent--;

        return null;
    }

    @Override
    public Void visit(ReferenceFieldMetadata referenceField) {
        if (referenceField.isKey()) {
            log("[Field (FK) (Key)] " + referenceField.getName()); //$NON-NLS-1$
        } else {
            TypeMetadata referencedType = referenceField.getReferencedType();
            if (referencedType == null) {
                throw new IllegalArgumentException("Field '" + referenceField.getName() + "' in type '" + referenceField.getContainingType().getName() + "' has invalid FK relation.");
            }

            try {
                log("[Field (FK -> " + referenceField.getDeclaringType().getName() + " to " + referenceField.getReferencedType().getName() + ")] " + referenceField.getName() + (referenceField.isMany() ? "*" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            } catch (Exception e) {
                throw new RuntimeException("Exception during display of field '" + referenceField.getName() + "' of type '" + referenceField.getContainingType().getName() + "'.", e);
            }
        }

        log("\t[Referenced field(s)]"); //$NON-NLS-1$
        indent += 2;
        referenceField.getReferencedField().accept(this);
        indent -= 2;

        log("\t[FKIntegrity=" + referenceField.isFKIntegrity() + " / FKIntegrityOverride=" + referenceField.allowFKIntegrityOverride() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (referenceField.hasForeignKeyInfo()) {
            log("\t[FKInfo=" + referenceField.getForeignKeyInfoField().getName() + " (" + referenceField.getForeignKeyInfoField() + ")]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        logUsers(referenceField);

        return super.visit(referenceField);
    }

    public Void visit(SimpleTypeFieldMetadata simpleField) {
        if (simpleField.isKey()) {
            log("[Field (Simple) (Key)] " + simpleField.getName()); //$NON-NLS-1$
        } else {
            log("[Field (Simple)] " + simpleField.getName() + (simpleField.isMany() ? "*" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        logUsers(simpleField);
        return super.visit(simpleField);
    }

    public Void visit(EnumerationFieldMetadata enumField) {
        log("[Field (Enumeration)] " + enumField.getName()); //$NON-NLS-1$
        logUsers(enumField);

        return super.visit(enumField);
    }

    @Override
    public Void visit(ContainedComplexTypeMetadata containedType) {
        List<FieldMetadata> fields = containedType.getFields();
        indent++;
        {
            for (FieldMetadata field : fields) {
                field.accept(this);
            }
        }
        indent--;
        return super.visit(containedType);
    }

    @Override
    public Void visit(ContainedTypeFieldMetadata containedField) {
        log("[Field (Contained type) -> " + containedField.getContainedType().getName() + ")] " + containedField.getName() + (containedField.isMany() ? "*" : ""));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        logUsers(containedField);

        indent++;
        {
            containedField.getContainedType().accept(this);
        }
        indent--;

        return super.visit(containedField);
    }

    private void logUsers(FieldMetadata metadata) {
        if (!metadata.getHideUsers().isEmpty()) {
            log("\t[Hide users: " + metadata.getHideUsers() + "]");  //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (!metadata.getWriteUsers().isEmpty()) {
            log("\t[Allow write users: " + metadata.getWriteUsers() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}