import java.util.*;

/**
 * LL1StackParser - Implementação do Algoritmo de Análise Sintática LL(1) com
 * Pilha
 * 
 * Algoritmo:
 * 1. Inicializar pilha com [S, $] onde S é o símbolo inicial
 * 2. Ler primeiro token (lookahead)
 * 3. Enquanto pilha não estiver vazia:
 * a) Se topo da pilha = terminal:
 * - Se terminal = lookahead: pop e ler próximo token
 * - Caso contrário: ERRO
 * b) Se topo da pilha = não-terminal:
 * - Consultar M[não-terminal, lookahead]
 * - Se M[não-terminal, lookahead] = erro: ERRO
 * - Caso contrário: pop não-terminal e empilhar produção (reversa)
 * 4) Se pilha vazia e lookahead = EOF: SUCESSO
 */
public class LL1StackParser {

    private List<Token> tokens;
    private int current = 0;
    private Stack<String> stack = new Stack<>();

    // Tabela de análise LL(1): M[A, a] = lista de símbolos
    private Map<String, Map<String, List<String>>> parsingTable;

    // Símbolos da gramática
    private Set<String> nonTerminals;
    private Set<String> terminals;

    public LL1StackParser(List<Token> tokens,
            Map<String, Map<String, List<String>>> parsingTable,
            Set<String> nonTerminals,
            Set<String> terminals) {
        this.tokens = tokens;
        this.parsingTable = parsingTable;
        this.nonTerminals = nonTerminals;
        this.terminals = terminals;
    }

    /**
     * Realiza análise sintática LL(1)
     */
    public boolean parse(String startSymbol) throws ParseError {
        // Inicializar pilha com [startSymbol, $]
        stack.push("$");
        stack.push(startSymbol);

        Token lookahead = peek();

        System.out.println("\n=== Iniciando Análise LL(1) com Pilha ===");
        printParseStep(0);

        int step = 0;
        while (!stack.isEmpty()) {
            step++;

            String top = stack.peek();
            String lookaheadStr = tokenToTerminal(lookahead);

            System.out.println("\nPasso " + step + ":");
            System.out.println("  Pilha: " + stack);
            System.out.println("  Lookahead: " + lookaheadStr + " (" + lookahead.lexeme + ")");

            if (top.equals("$")) {
                if (lookaheadStr.equals("EOF")) {
                    System.out.println("✅ Análise SUCESSO! String aceita.");
                    return true;
                } else {
                    throw new ParseError("Tokens extras após o fim do programa.");
                }
            }

            // Se topo é terminal
            if (isTerminal(top)) {
                if (top.equals(lookaheadStr)) {
                    System.out.println("  MATCH: " + top);
                    stack.pop();
                    advance();
                    lookahead = peek();
                } else {
                    throw new ParseError("Erro de casamento: esperado '" + top +
                            "', encontrado '" + lookaheadStr + "'");
                }
            }
            // Se topo é não-terminal
            else if (nonTerminals.contains(top)) {
                List<String> production = getProduction(top, lookaheadStr);

                if (production == null) {
                    throw new ParseError("Erro LL(1): M[" + top + ", " + lookaheadStr +
                            "] não definida na tabela (linha " + lookahead.line + ")");
                }

                System.out.println("  Produção: " + top + " → " + production);
                stack.pop();

                // Empilhar produção em ordem reversa (exceto ε)
                if (!production.get(0).equals("ε")) {
                    for (int i = production.size() - 1; i >= 0; i--) {
                        stack.push(production.get(i));
                    }
                }
            } else {
                throw new ParseError("Símbolo inválido na pilha: " + top);
            }
        }

        System.out.println("\n❌ Análise FALHOU! Pilha vazia mas lookahead != $");
        return false;
    }

    /**
     * Obtém a produção da tabela M[A, a]
     */
    private List<String> getProduction(String nonTerminal, String terminal) {
        if (!parsingTable.containsKey(nonTerminal)) {
            return null;
        }

        Map<String, List<String>> row = parsingTable.get(nonTerminal);
        return row.get(terminal);
    }

    /**
     * Converte token para terminal da gramática
     */
    private String tokenToTerminal(Token token) {
        if (token.type == TokenType.EOF) {
            return "EOF";
        }

        // Mapeamento de TokenType para terminal
        switch (token.type) {
            case MAIN:
                return "main";
            case VAR:
                return "var";
            case INT:
                return "int";
            case FLOAT_KW:
                return "float";
            case DOUBLE_KW:
                return "double";
            case CHAR_KW:
                return "char";
            case BOOL:
                return "bool";
            case STRING_KW:
                return "string";
            case IF:
                return "if";
            case ELSE:
                return "else";
            case WHILE:
                return "while";
            case FOR:
                return "for";
            case SWITCH:
                return "switch";
            case CASE:
                return "case";
            case DEFAULT:
                return "default";
            case BREAK:
                return "break";
            case CONTINUE:
                return "continue";
            case PRINT:
                return "print";
            case RETURN:
                return "return";
            case INPUT:
                return "input";
            case TRUE:
                return "true";
            case FALSE:
                return "false";
            case NUMBER:
            case FLOAT:
            case DOUBLE:
            case CHAR:
                return token.type.toString();
            case STRING:
                return "STRING";
            case IDENTIFIER:
                return "IDENTIFIER";

            // Operadores e delimitadores
            case LEFT_PAREN:
                return "(";
            case RIGHT_PAREN:
                return ")";
            case LEFT_BRACE:
                return "{";
            case RIGHT_BRACE:
                return "}";
            case LEFT_BRACKET:
                return "[";
            case RIGHT_BRACKET:
                return "]";
            case SEMICOLON:
                return ";";
            case COLON:
                return ":";
            case COMMA:
                return ",";
            case DOT:
                return ".";
            case QUESTION:
                return "?";
            case PLUS:
                return "+";
            case MINUS:
                return "-";
            case STAR:
                return "*";
            case SLASH:
                return "/";
            case PERCENT:
                return "%";
            case EQUAL:
                return "=";
            case EQUAL_EQUAL:
                return "==";
            case BANG_EQUAL:
                return "!=";
            case LESS:
                return "<";
            case LESS_EQUAL:
                return "<=";
            case GREATER:
                return ">";
            case GREATER_EQUAL:
                return ">=";
            case AND_AND:
                return "&&";
            case OR_OR:
                return "||";
            case BANG:
                return "!";
            case PLUS_EQUAL:
                return "+=";
            case MINUS_EQUAL:
                return "-=";
            case STAR_EQUAL:
                return "*=";
            case SLASH_EQUAL:
                return "/=";
            case PERCENT_EQUAL:
                return "%=";

            default:
                return token.type.toString();
        }
    }

    /**
     * Verifica se um símbolo é terminal
     */
    private boolean isTerminal(String symbol) {
        return symbol.equals("$") || !nonTerminals.contains(symbol);
    }

    /**
     * Imprime passo da análise
     */
    private void printParseStep(int step) {
        System.out.println("Passo " + step + ": Pilha = " + stack +
                ", Lookahead = " + tokenToTerminal(peek()));
    }

    private Token peek() {
        if (current < tokens.size()) {
            return tokens.get(current);
        }
        return new Token(TokenType.EOF, "<EOF>", null, -1, -1);
    }

    private void advance() {
        if (current < tokens.size()) {
            current++;
        }
    }
}
