import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public AST.Program parse() {
        AST.Block mainBlock;
        if (match(TokenType.MAIN)) {
            mainBlock = block();
        } else {
            throw error(peek(), "Programa deve iniciar com 'main'.");
        }
        // Avançar para o próximo token após o bloco main
        // Isso garante que o ponteiro esteja no EOF
        while (!isAtEnd() && peek().type != TokenType.EOF)
            advance();
        consume(TokenType.EOF, "Tokens extras após o fim do programa.");
        return new AST.Program(mainBlock);
    }

    // ------------------ Statements ------------------
    private AST.Statement declarationOrStmt() {
        if (match(TokenType.VAR))
            return varDecl();
        if (match(TokenType.IF))
            return ifStmt();
        if (match(TokenType.WHILE))
            return whileStmt();
        if (match(TokenType.DO))
            return doWhileStmt();
        if (match(TokenType.FOR))
            return forStmt();
        if (match(TokenType.SWITCH))
            return switchStmt();
        if (match(TokenType.BREAK))
            return breakStmt();
        if (match(TokenType.CONTINUE))
            return continueStmt();
        if (match(TokenType.PRINT))
            return printStmt();
        if (match(TokenType.RETURN))
            return returnStmt();
        return exprStmt();
    }

    // print(expr);
    private AST.Statement printStmt() {
        consume(TokenType.LEFT_PAREN, "Esperado '(' após 'print'.");
        AST.Expr value = expression();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após expressão do print.");
        consume(TokenType.SEMICOLON, "Esperado ';' após print().");
        return new AST.Print(value);
    }

    // break statement
    private AST.Statement breakStmt() {
        consume(TokenType.SEMICOLON, "Esperado ';' após 'break'.");
        return new AST.Break();
    }

    // continue statement
    private AST.Statement continueStmt() {
        consume(TokenType.SEMICOLON, "Esperado ';' após 'continue'.");
        return new AST.Continue();
    }

    // switch (expr) { case valor: bloco ... default: bloco }
    private AST.Statement switchStmt() {
        consume(TokenType.LEFT_PAREN, "Esperado '(' após 'switch'.");
        AST.Expr expr = expression();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após expressão do switch.");
        consume(TokenType.LEFT_BRACE, "Esperado '{' para iniciar switch.");
        java.util.List<AST.Case> cases = new java.util.ArrayList<>();
        AST.Block defaultBlock = null;
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            if (match(TokenType.CASE)) {
                AST.Expr value = expression();
                consume(TokenType.COLON, "Esperado ':' após valor do case.");
                AST.Block block = block();
                cases.add(new AST.Case(value, block));
            } else if (match(TokenType.DEFAULT)) {
                consume(TokenType.COLON, "Esperado ':' após 'default'.");
                defaultBlock = block();
            } else {
                throw error(peek(), "Esperado 'case' ou 'default' em switch.");
            }
        }
        consume(TokenType.RIGHT_BRACE, "Esperado '}' para fechar switch.");
        return new AST.Switch(expr, cases, defaultBlock);
    }

    // for (init; cond; inc) { ... }
    private AST.Statement forStmt() {
        consume(TokenType.LEFT_PAREN, "Esperado '(' após 'for'.");
        AST.Statement init = null;
        if (!check(TokenType.SEMICOLON))
            init = declarationOrStmt();
        else
            consume(TokenType.SEMICOLON, "Esperado ';' após inicialização do for.");
        AST.Expr cond = null;
        if (!check(TokenType.SEMICOLON))
            cond = expression();
        consume(TokenType.SEMICOLON, "Esperado ';' após condição do for.");
        AST.Expr inc = null;
        if (!check(TokenType.RIGHT_PAREN))
            inc = expression();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após incremento do for.");
        AST.Block body = block();
        return new AST.For(init, cond, inc, body);
    }

    private AST.Statement varDecl() {
        Token name = consume(TokenType.IDENTIFIER, "Esperado identificador após 'var'.");
        consume(TokenType.COLON, "Esperado ':' após identificador.");
        AST.TypeNode type = type();
        AST.Expr initializer = null;
        if (match(TokenType.EQUAL))
            initializer = expression();
        consume(TokenType.SEMICOLON, "Esperado ';' ao final da declaração de variável.");
        return new AST.VarDecl(name, type, initializer);
    }

    private AST.TypeNode type() {
        String baseType = null;
        if (match(TokenType.INT))
            baseType = "int";
        else if (match(TokenType.FLOAT_KW))
            baseType = "float";
        else if (match(TokenType.DOUBLE_KW))
            baseType = "double";
        else if (match(TokenType.CHAR_KW))
            baseType = "char";
        else if (match(TokenType.BOOL))
            baseType = "bool";
        else if (match(TokenType.STRING_KW))
            baseType = "string";
        if (baseType != null) {
            if (match(TokenType.LEFT_BRACKET)) {
                consume(TokenType.RIGHT_BRACKET, "Esperado ']' após tipo de array.");
                return new AST.TypeNode(baseType + "[]");
            }
            return new AST.TypeNode(baseType);
        }
        throw error(previous(), "Tipo inválido. Esperado int|float|double|char|bool|string ou array.");
    }

    private AST.Statement ifStmt() {
        consume(TokenType.LEFT_PAREN, "Esperado '(' após 'if'.");
        AST.Expr cond = expression();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após condição do if.");
        AST.Block thenBlock = block();
        AST.Block elseBlock = null;
        if (match(TokenType.ELSE))
            elseBlock = block();
        return new AST.If(cond, thenBlock, elseBlock);
    }

    private AST.Statement whileStmt() {
        consume(TokenType.LEFT_PAREN, "Esperado '(' após 'while'.");
        AST.Expr cond = expression();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após condição do while.");
        AST.Block body = block();
        return new AST.While(cond, body);
    }

    private AST.Statement doWhileStmt() {
        AST.Block body = block();
        consume(TokenType.WHILE, "Esperado 'while' após bloco do-while.");
        consume(TokenType.LEFT_PAREN, "Esperado '(' após 'while'.");
        AST.Expr cond = expression();
        consume(TokenType.RIGHT_PAREN, "Esperado ')' após condição do do-while.");
        consume(TokenType.SEMICOLON, "Esperado ';' após do-while.");
        return new AST.DoWhile(body, cond);
    }

    private AST.Statement returnStmt() {
        AST.Expr value = null;
        if (!check(TokenType.SEMICOLON))
            value = expression();
        consume(TokenType.SEMICOLON, "Esperado ';' após return.");
        return new AST.Return(value);
    }

    private AST.Statement exprStmt() {
        AST.Expr expr = expression();
        consume(TokenType.SEMICOLON, "Esperado ';' após expressão.");
        return new AST.ExprStmt(expr);
    }

    private AST.Block block() {
        consume(TokenType.LEFT_BRACE, "Esperado '{' para iniciar bloco.");
        List<AST.Statement> stmts = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            stmts.add(declarationOrStmt());
        }
        consume(TokenType.RIGHT_BRACE, "Esperado '}' para fechar bloco.");
        return new AST.Block(stmts);
    }

    // ------------------ Expressions ------------------
    private AST.Expr expression() {
        return ternary();
    }

    // cond ? expr1 : expr2
    private AST.Expr ternary() {
        AST.Expr expr = or();
        if (match(TokenType.QUESTION)) {
            AST.Expr thenExpr = expression();
            consume(TokenType.COLON, "Esperado ':' no operador ternário.");
            AST.Expr elseExpr = expression();
            return new AST.Ternary(expr, thenExpr, elseExpr);
        }
        return expr;
    }

    private AST.Expr or() {
        AST.Expr expr = and();
        while (match(TokenType.OR_OR)) {
            Token op = previous();
            AST.Expr right = and();
            expr = new AST.Binary(expr, op, right);
        }
        return expr;
    }

    private AST.Expr and() {
        AST.Expr expr = equality();
        while (match(TokenType.AND_AND)) {
            Token op = previous();
            AST.Expr right = equality();
            expr = new AST.Binary(expr, op, right);
        }
        return expr;
    }

    private AST.Expr equality() {
        AST.Expr expr = relational();
        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            Token op = previous();
            AST.Expr right = relational();
            expr = new AST.Binary(expr, op, right);
        }
        return expr;
    }

    private AST.Expr relational() {
        AST.Expr expr = addition();
        while (match(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)) {
            Token op = previous();
            AST.Expr right = addition();
            expr = new AST.Binary(expr, op, right);
        }
        return expr;
    }

    private AST.Expr addition() {
        AST.Expr expr = multiplication();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token op = previous();
            AST.Expr right = multiplication();
            expr = new AST.Binary(expr, op, right);
        }
        return expr;
    }

    private AST.Expr multiplication() {
        AST.Expr expr = unary();
        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            Token op = previous();
            AST.Expr right = unary();
            expr = new AST.Binary(expr, op, right);
        }
        return expr;
    }

    private AST.Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token op = previous();
            AST.Expr right = unary();
            return new AST.Unary(op, right);
        }
        return assignmentLike();
    }

    // Suporta atribuição simples: IDENT '=' Expr
    private AST.Expr assignmentLike() {
        AST.Expr left = primary();
        if (match(TokenType.EQUAL, TokenType.PLUS_EQUAL, TokenType.MINUS_EQUAL, TokenType.STAR_EQUAL,
                TokenType.SLASH_EQUAL, TokenType.PERCENT_EQUAL)) {
            Token op = previous();
            if (left instanceof AST.Variable) {
                AST.Expr value = expression();
                // Para operadores compostos, cria um nó de atribuição equivalente: x += y => x
                // = x + y
                if (op.type == TokenType.EQUAL) {
                    return new AST.Assign(((AST.Variable) left).name, value, op);
                } else {
                    TokenType binOp;
                    switch (op.type) {
                        case PLUS_EQUAL:
                            binOp = TokenType.PLUS;
                            break;
                        case MINUS_EQUAL:
                            binOp = TokenType.MINUS;
                            break;
                        case STAR_EQUAL:
                            binOp = TokenType.STAR;
                            break;
                        case SLASH_EQUAL:
                            binOp = TokenType.SLASH;
                            break;
                        case PERCENT_EQUAL:
                            binOp = TokenType.PERCENT;
                            break;
                        default:
                            throw error(op, "Operador de atribuição composto inválido.");
                    }
                    Token fakeOp = new Token(binOp, op.lexeme.substring(0, 1), null, op.line, op.column);
                    AST.Expr bin = new AST.Binary(left, fakeOp, value);
                    return new AST.Assign(((AST.Variable) left).name, bin, op);
                }
            }
            throw error(op, "Alvo de atribuição inválido.");
        }
        return left;
    }

    private AST.Expr primary() {
        if (match(TokenType.FALSE))
            return new AST.Literal(false);
        if (match(TokenType.TRUE))
            return new AST.Literal(true);
        if (match(TokenType.NUMBER))
            return new AST.Literal(previous().literal);
        if (match(TokenType.FLOAT))
            return new AST.Literal(previous().literal);
        if (match(TokenType.DOUBLE))
            return new AST.Literal(previous().literal);
        if (match(TokenType.CHAR))
            return new AST.Literal(previous().literal);
        if (match(TokenType.STRING)) {
            Object val = previous().literal;
            return new AST.Literal(val);
        }
        if (check(TokenType.INTERPOLATED_STRING)) {
            java.util.List<Object> parts = new java.util.ArrayList<>();
            while (check(TokenType.INTERPOLATED_STRING) || check(TokenType.LEFT_BRACE) || check(TokenType.STRING)) {
                if (match(TokenType.INTERPOLATED_STRING)) {
                    parts.add(previous().literal);
                } else if (match(TokenType.LEFT_BRACE)) {
                    // Suporta apenas expressão simples entre { e }
                    AST.Expr expr = expression();
                    consume(TokenType.RIGHT_BRACE, "Esperado '}' após expressão interpolada.");
                    parts.add(expr);
                } else if (match(TokenType.STRING)) {
                    parts.add(previous().literal);
                } else {
                    break;
                }
            }
            return new AST.InterpolatedString(parts);
        }
        if (match(TokenType.INPUT)) {
            consume(TokenType.LEFT_PAREN, "Esperado '(' após 'input'.");
            String prompt = "";
            if (!check(TokenType.RIGHT_PAREN)) {
                if (match(TokenType.STRING)) {
                    prompt = (String) previous().literal;
                } else {
                    throw error(peek(), "Esperado string como prompt do input.");
                }
            }
            consume(TokenType.RIGHT_PAREN, "Esperado ')' após input.");
            return new AST.Input(prompt);
        }
        if (match(TokenType.LEFT_BRACKET)) {
            // Inicialização literal de array: [expr1, expr2, ...]
            java.util.List<AST.Expr> elements = new java.util.ArrayList<>();
            if (!check(TokenType.RIGHT_BRACKET)) {
                do {
                    elements.add(expression());
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RIGHT_BRACKET, "Esperado ']' após elementos do array.");
            return new AST.ArrayLiteral(elements);
        }
        if (match(TokenType.IDENTIFIER)) {
            AST.Expr var = new AST.Variable(previous());
            // Suporte a acesso por índice: var[expr]
            while (match(TokenType.LEFT_BRACKET)) {
                AST.Expr index = expression();
                consume(TokenType.RIGHT_BRACKET, "Esperado ']' após índice do array.");
                var = new AST.ArrayAccess(var, index);
            }
            return var;
        }
        if (match(TokenType.LEFT_PAREN)) {
            AST.Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Esperado ')' após expressão.");
            return new AST.Grouping(expr);
        }
        throw error(peek(), "Expressão primária inválida.");
    }

    // ------------------ Utilidades ------------------
    private boolean match(TokenType... types) {
        for (TokenType t : types) {
            if (check(t)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        // Permite consumir EOF corretamente
        if (type == TokenType.EOF) {
            return peek().type == TokenType.EOF;
        }
        return !isAtEnd() && peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        String where = token.type == TokenType.EOF ? "no fim" : ("em '" + token.lexeme + "'");
        return new ParseError("[Linha " + token.line + "] Erro " + where + ": " + message);
    }
}
