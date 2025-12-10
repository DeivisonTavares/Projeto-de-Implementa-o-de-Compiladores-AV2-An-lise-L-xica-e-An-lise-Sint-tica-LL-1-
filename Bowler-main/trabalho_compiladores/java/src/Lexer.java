import java.util.*;

public class Lexer {
    private final String src;
    private final java.util.List<Token> tokens = new ArrayList<>();
    private int start = 0; // início do lexema atual
    private int current = 0; // posição atual no código
    private int line = 1; // linha atual (1-based)
    private int col = 1; // coluna atual (1-based)
    // Estado para strings interpoladas
    private boolean inInterpolatedString = false;

    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        keywords.put("var", TokenType.VAR);
        keywords.put("int", TokenType.INT);
        keywords.put("float", TokenType.FLOAT_KW);
        keywords.put("double", TokenType.DOUBLE_KW);
        keywords.put("char", TokenType.CHAR_KW);
        keywords.put("bool", TokenType.BOOL);
        keywords.put("string", TokenType.STRING_KW);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("do", TokenType.DO);
        keywords.put("for", TokenType.FOR);
        keywords.put("switch", TokenType.SWITCH);
        keywords.put("case", TokenType.CASE);
        keywords.put("default", TokenType.DEFAULT);
        keywords.put("break", TokenType.BREAK);
        keywords.put("continue", TokenType.CONTINUE);
        keywords.put("print", TokenType.PRINT);
        keywords.put("input", TokenType.INPUT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("main", TokenType.MAIN);
    }

    public Lexer(String source) {
        this.src = source;
    }

    public java.util.List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current; // início do próximo lexema
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "<EOF>", null, line, col));
        return tokens;
    }

    private void scanToken() {
        if (inInterpolatedString) {
            interpolatedString();
            return;
        }
        char c = advance();
        switch (c) {
            case '(':
                add(TokenType.LEFT_PAREN);
                break;
            case ')':
                add(TokenType.RIGHT_PAREN);
                break;
            case '{':
                add(TokenType.LEFT_BRACE);
                break;
            case '}':
                add(TokenType.RIGHT_BRACE);
                break;
            case '[':
                add(TokenType.LEFT_BRACKET);
                break;
            case ']':
                add(TokenType.RIGHT_BRACKET);
                break;
            case ',':
                add(TokenType.COMMA);
                break;
            case '.':
                add(TokenType.DOT);
                break;
            case ';':
                add(TokenType.SEMICOLON);
                break;
            case ':':
                add(TokenType.COLON);
                break;
            case '?':
                add(TokenType.QUESTION);
                break;
            case '+':
                add(match('=') ? TokenType.PLUS_EQUAL : TokenType.PLUS);
                break;
            case '-':
                add(match('=') ? TokenType.MINUS_EQUAL : TokenType.MINUS);
                break;
            case '*':
                add(match('=') ? TokenType.STAR_EQUAL : TokenType.STAR);
                break;
            case '/':
                if (match('/')) { // comentário de linha
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                } else if (match('*')) { // comentário de bloco
                    blockComment();
                } else if (match('=')) {
                    add(TokenType.SLASH_EQUAL);
                } else {
                    add(TokenType.SLASH);
                }
                break;
            case '%':
                add(match('=') ? TokenType.PERCENT_EQUAL : TokenType.PERCENT);
                break;
            case '!':
                add(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                add(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                add(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                add(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '&':
                if (match('&'))
                    add(TokenType.AND_AND);
                else
                    error("Esperado '&' para '&&'.");
                break;
            case '|':
                if (match('|'))
                    add(TokenType.OR_OR);
                else
                    error("Esperado '|' para '||'.");
                break;
            case ' ':
            case '\r':
            case '\t':
                break; // ignorar espaços
            case '\n':
                line++;
                col = 1;
                break;
            case '"':
                inInterpolatedString = true;
                interpolatedString();
                break;
            case '\'':
                charLiteral();
                break;
            default:
                if (isDigit(c))
                    number();
                else if (isAlpha(c))
                    identifier();
                else
                    error("Caractere inesperado: '" + c + "'");
        }
    }

    // peek: olha o caractere atual sem consumir
    private char peek() {
        if (isAtEnd())
            return '\0';
        return src.charAt(current);
    }

    // peekNext: olha o próximo caractere sem consumir
    private char peekNext() {
        if (current + 1 >= src.length())
            return '\0';
        return src.charAt(current + 1);
    }

    // Suporte a comentários de bloco
    private void blockComment() {
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance(); // consome '*'
                advance(); // consome '/'
                return;
            } else if (peek() == '\n') {
                line++;
                col = 1;
                advance();
            } else {
                advance();
            }
        }
        error("Comentário de bloco não terminado.");
    }

    // Suporte a strings interpoladas (com múltiplas partes e continuidade)
    private void interpolatedString() {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd()) {
            char c = advance();
            if (c == '"') {
                if (sb.length() > 0) {
                    add(TokenType.STRING, sb.toString());
                }
                inInterpolatedString = false;
                return;
            } else if (c == '\\') {
                if (isAtEnd())
                    break;
                char next = advance();
                sb.append('\\').append(next);
            } else if (c == '{') {
                if (sb.length() > 0) {
                    add(TokenType.INTERPOLATED_STRING, sb.toString());
                    sb.setLength(0);
                }
                add(TokenType.LEFT_BRACE);
                // Após o parser consumir a expressão e o RIGHT_BRACE, o scanToken será chamado
                // novamente
                return;
            } else if (c == '}') {
                if (sb.length() > 0) {
                    add(TokenType.STRING, sb.toString());
                    sb.setLength(0);
                }
                add(TokenType.RIGHT_BRACE);
                // Continua processando a string interpolada após o }
                interpolatedString();
                return;
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            add(TokenType.STRING, sb.toString());
        }
        error("String não terminada.");
    }

    // Suporte a números (int, float, double)
    private void number() {
        boolean isFloat = false;
        boolean isDouble = false;
        while (isDigit(peek()))
            advance();
        if (peek() == '.' && isDigit(peekNext())) {
            isFloat = true;
            advance(); // consome o ponto
            while (isDigit(peek()))
                advance();
            // Se tiver 'd' ou 'D' no final, é double
            if (peek() == 'd' || peek() == 'D') {
                isDouble = true;
                advance();
            }
        }
        String text = src.substring(start, current);
        if (isDouble) {
            add(TokenType.DOUBLE, Double.parseDouble(text.substring(0, text.length() - 1)));
        } else if (isFloat) {
            add(TokenType.FLOAT, Double.parseDouble(text));
        } else {
            add(TokenType.NUMBER, Integer.parseInt(text));
        }
    }

    // Suporte a identificadores e palavras-chave
    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();
        String text = src.substring(start, current);
        TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
        add(type);
    }

    // Suporte a char literal
    private void charLiteral() {
        if (isAtEnd())
            error("Char não terminado.");
        char value = advance();
        if (peek() != '\'')
            error("Char deve ter apenas um caractere e ser fechado com aspas simples.");
        advance(); // fecha aspas
        add(TokenType.CHAR, value);
    }

    private boolean isAtEnd() {
        return current >= src.length();
    }

    private char advance() {
        char c = src.charAt(current++);
        col++;
        return c;
    }

    private boolean match(char expected) {
        if (isAtEnd() || src.charAt(current) != expected)
            return false;
        current++;
        col++;
        return true;
    }

    // add para tokens sem literal
    private void add(TokenType type) {
        add(type, null);
    }

    // add para tokens com literal
    private void add(TokenType type, Object literal) {
        String text = src.substring(start, current);
        tokens.add(new Token(type, text, literal, line, Math.max(1, col - (current - start))));
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private static boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void error(String message) {
        throw new RuntimeException("[Linha " + line + ", Col " + col + "] " + message);
    }
}
