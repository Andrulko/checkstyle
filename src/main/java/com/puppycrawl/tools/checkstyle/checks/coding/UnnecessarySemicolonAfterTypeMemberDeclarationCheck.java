////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2019 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.checks.coding;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtil;

/**
 * <p>
 * Checks if unnecessary semicolon is used after code type member declaration.
 * The check will flag the following with warnings:
 * </p>
 * <pre>
 * class A {
 *     ; // standalone semicolon
 *     {}; // semicolon after init block
 *     static {}; // semicolon after static init block
 *     A(){}; // semicolon after constructor definition
 *     void method() {}; // semicolon after method definition
 *     int field = 10;; // semicolon after field declaration
 * }
 * </pre>
 * <p>
 * To configure the check:
 * </p>
 * <pre>
 * &lt;module name=&quot;UnnecessarySemicolonAfterTypeMemberDeclaration&quot;/&gt;
 * </pre>
 *
 * @since 8.23
 */
@StatelessCheck
public final class UnnecessarySemicolonAfterTypeMemberDeclarationCheck extends AbstractCheck {

    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_SEMI = "unnecessary.semicolon";

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] {
            TokenTypes.CLASS_DEF,
            TokenTypes.INTERFACE_DEF,
            TokenTypes.ENUM_DEF,
            TokenTypes.ANNOTATION_DEF,
            TokenTypes.VARIABLE_DEF,
            TokenTypes.STATIC_INIT,
            TokenTypes.INSTANCE_INIT,
            TokenTypes.CTOR_DEF,
            TokenTypes.METHOD_DEF,
            TokenTypes.ENUM_CONSTANT_DEF,
        };
    }

    @Override
    public void visitToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.CLASS_DEF:
            case TokenTypes.INTERFACE_DEF:
            case TokenTypes.ENUM_DEF:
            case TokenTypes.ANNOTATION_DEF:
                checkTypeDefinition(ast);
                break;
            case TokenTypes.VARIABLE_DEF:
                checkVariableDefinition(ast);
                break;
            case TokenTypes.ENUM_CONSTANT_DEF:
                checkEnumConstant(ast);
                break;
            default:
                checkTypeMember(ast);
                break;
        }
    }

    /**
     * Checks if type member has unnecessary semicolon.
     *
     * @param ast type member
     */
    private void checkTypeMember(DetailAST ast) {
        if (isSemicolon(ast.getNextSibling())) {
            log(ast.getNextSibling(), MSG_SEMI);
        }
    }

    /**
     * Checks if type definition has unnecessary semicolon.
     *
     * @param ast type definition
     */
    private void checkTypeDefinition(DetailAST ast) {
        if (!ScopeUtil.isOuterMostType(ast) && isSemicolon(ast.getNextSibling())) {
            log(ast.getNextSibling(), MSG_SEMI);
        }
        final DetailAST firstMember =
            ast.findFirstToken(TokenTypes.OBJBLOCK).getFirstChild().getNextSibling();
        if (isSemicolon(firstMember) && !ScopeUtil.isInEnumBlock(firstMember)) {
            log(firstMember, MSG_SEMI);
        }
    }

    /**
     * Checks if variable definition has unnecessary semicolon.
     *
     * @param ast variable definition
     */
    private void checkVariableDefinition(DetailAST ast) {
        if (isFieldDeclaration(ast) && isSemicolon(ast.getNextSibling())) {
            log(ast.getNextSibling(), MSG_SEMI);
        }
    }

    /**
     * Checks if enum constant has unnecessary semicolon.
     *
     * @param ast enum constant
     */
    private void checkEnumConstant(DetailAST ast) {
        final DetailAST next = ast.getNextSibling();
        if (isSemicolon(next) && isSemicolon(next.getNextSibling())) {
            log(next.getNextSibling(), MSG_SEMI);
        }
    }

    /**
     * Checks that {@code ast} is a semicolon.
     *
     * @param ast token to check
     * @return true if ast is semicolon, false otherwise
     */
    private static boolean isSemicolon(DetailAST ast) {
        return ast.getType() == TokenTypes.SEMI;
    }

    /**
     * Checks that {@code variableDef} is a field declaration.
     *
     * @param variableDef token to check
     * @return true if variableDef is a field declaration, false otherwise
     */
    private static boolean isFieldDeclaration(DetailAST variableDef) {
        return isSemicolon(variableDef.getLastChild());
    }
}
