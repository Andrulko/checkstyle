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

import java.util.Arrays;
import java.util.List;

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

    /**
     * List of tokens that is used to check if token is a class member.
     *
     * @see #isClassMember(DetailAST)
     */
    private static final List<Integer> CLASS_MEMBER_TOKENS = Arrays.asList(
        TokenTypes.STATIC_INIT,
        TokenTypes.INSTANCE_INIT,
        TokenTypes.CTOR_DEF,
        TokenTypes.METHOD_DEF
    );

    /**
     * List of tokens that is used to check if token is a nested type.
     *
     * @see #isNestedTypeDeclaration(DetailAST)
     */
    private static final List<Integer> TYPE_TOKENS = Arrays.asList(
        TokenTypes.CLASS_DEF,
        TokenTypes.INTERFACE_DEF,
        TokenTypes.ENUM_DEF,
        TokenTypes.ANNOTATION_DEF
    );

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
            TokenTypes.SEMI,
        };
    }

    @Override
    public void visitToken(DetailAST ast) {
        final DetailAST prevSibling = ast.getPreviousSibling();
        if (prevSibling != null && (isClassMember(prevSibling) || isAloneSemicolon(ast))) {
            log(ast, MSG_SEMI);
        }
    }

    /**
     * Checks that {@code ast} is class member.
     *
     * @param ast token to check
     * @return true if ast is a class member, false otherwise
     */
    private static boolean isClassMember(DetailAST ast) {
        return CLASS_MEMBER_TOKENS.contains(ast.getType())
            || isFieldDeclaration(ast)
            || isNestedTypeDeclaration(ast);
    }

    /**
     * Checks that {@code ast} is a standalone semicolon.
     *
     * @param ast token to check
     * @return true if ast is a standalone semicolon, false otherwise
     */
    private static boolean isAloneSemicolon(DetailAST ast) {
        final int type = ast.getPreviousSibling().getType();
        return type == TokenTypes.SEMI
            || type == TokenTypes.LCURLY
            && !ScopeUtil.isInEnumBlock(ast);
    }

    /**
     * Checks that {@code ast} is a nested type.
     *
     * @param ast token to check
     * @return true if ast is a nested type, false otherwise
     */
    private static boolean isNestedTypeDeclaration(DetailAST ast) {
        return TYPE_TOKENS.contains(ast.getType()) && !ScopeUtil.isOuterMostType(ast);
    }

    /**
     * Checks that {@code ast} is a field declaration.
     *
     * @param ast token to check
     * @return true if ast is a field declaration, false otherwise
     */
    private static boolean isFieldDeclaration(DetailAST ast) {
        return ast.getType() == TokenTypes.VARIABLE_DEF
            && ast.getLastChild().getType() == TokenTypes.SEMI;
    }
}
