import java.nio.file.*;
import java.util.*;

/**
 * Projeto AV1 – Implementação em Java (puro) separada em classes:
 *  - Lexer (scanner)
 *  - Parser (recursivo descendente)
 *  - AST (nós da árvore)
 *  - Tipos de Token
 *
 * Linguagem protótipo: Bowler
 * Regras suportadas: main { ... }, var, if/else, while, return, expressões com precedência e atribuição.
 *
 * Como executar:
 *  1) Coloque TODOS os arquivos .java na mesma pasta.
 *  2) Compile:  javac *.java
 *  3) Rode com exemplo embutido:  java Bowler
 *     ou com arquivo: java -cp bin Bowler src/meu_exemplo.min
 */
public class Bowler {
    public static void main(String[] args) throws Exception {
        String source;
        if (args.length == 0) {
            source = String.join("",
                "main {",
                "  var x: int = 10;",
                "  var y: int = 2;",
                "  var msg: string = \"hello\";",
                "  if (x > y && y != 0) {",
                "     x = x + y * 3;",
                "  } else {",
                "     x = 0;",
                "  }",
                "  while (x > 0) {",
                "     x = x - 1;",
                "  }",
                "  return x;",
                "}"
            );
            System.out.println("[Sem arquivo] Usando o exemplo embutido.");
        } else {
            source = Files.readString(Path.of(args[0]));
        }

        // 1) LÉXICO
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        // Imprimir Tabela de Tokens
        System.out.println("=== Tabela de Tokens ===");
        System.out.printf("%-15s %-18s %-6s %-6s\n", "Lexema", "Classe", "Linha", "Col");
        System.out.println("-------------------------------------------------------------");
        for (Token t : tokens) {
            System.out.printf("%-15s %-18s %-6d %-6d\n",
                t.lexeme,
                t.type,
                t.line,
                t.column);
        }

        // Imprimir Cadeia de Tokens
        System.out.println("=== Cadeia de Tokens ===");
        int count = 0;
        for (Token t : tokens) {
            System.out.printf("%-12s ", t.type);
            count++;
            if (count % 8 == 0) System.out.println();
        }
        if (count % 8 != 0) System.out.println();

        // Imprimir Expressões Regulares Utilizadas (Opção A - Conformidade)
        System.out.println("\n=== Expressões Regulares Utilizadas (Lexer) ===");
        System.out.println("IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]*");
        System.out.println("NUMBER:     [0-9]+ | [0-9]+\\.[0-9]+ | [0-9]+\\.[0-9]+[dD]");
        System.out.println("STRING:     \"[^\"]*\"");
        System.out.println("CHAR:       '[a-zA-Z0-9]'");
        System.out.println("OPERATORS:  +, -, *, /, %, ==, !=, <, >, <=, >=, =, +=, -=, *=, /=, %=");
        System.out.println("LOGICAL:    &&, ||, !");
        System.out.println("KEYWORDS:   var, int, float, double, char, bool, string, true, false,");
        System.out.println("            if, else, while, do, for, switch, case, default, break,");
        System.out.println("            continue, print, input, return, main");
        System.out.println("DELIMITERS: { } [ ] ( ) , . ; : ?");
        System.out.println("COMMENTS:   // ... (linha)  e  /* ... */ (bloco)");

        // 2) SINTÁTICO - Parser Recursivo Descendente
        Parser parser = new Parser(tokens);
        try {
            AST.Program program = parser.parse();
            System.out.println("\n✅ Parser Recursivo: Programa reconhecido sem erros.");
            System.out.println("=== Árvore (impressão simplificada) ===");
            program.prettyPrint(0);
            System.out.println("=== Árvore (detalhada) ===");
            System.out.println(program.toDetailedString());
        } catch (ParseError e) {
            System.err.println("❌ Erro sintático: " + e.getMessage());
        }
        
        // 3) ANÁLISE LL(1) - Validação com Pilha
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ANÁLISE LL(1) COM PILHA - GRAMÁTICA SIMPLIFICADA");
        System.out.println("=".repeat(60));
        System.out.println("\nNota: A gramática LL(1) simplificada foi definida para");
        System.out.println("validar a estrutura básica do programa Bowler.");
        System.out.println("O Parser Recursivo acima reconheceu o programa com sucesso.");
        
        try {
            // Criar analisador de gramática LL(1) simplificada
            LL1GrammarAnalyzer analyzer = createBowlerLL1Analyzer();
            analyzer.calculateFirst();
            analyzer.calculateFollow();
            analyzer.buildParsingTable();
            
            // Exibir FIRST, FOLLOW e Tabela
            analyzer.printFirst();
            analyzer.printFollow();
            analyzer.printParsingTable();
            
            // Executar análise com pilha usando os tokens gerados pelo Lexer (entrada real)
            System.out.println("\n" + "=".repeat(60));
            System.out.println("TESTE COM TOKENS DO LEXER");
            System.out.println("=".repeat(60));

            System.out.println("Entrada (tokens do arquivo lido):");
            System.out.println("Tokens: ");
            for (Token t : tokens) {
                System.out.print(t.type + " ");
            }
            System.out.println();

            LL1StackParser stackParser = new LL1StackParser(
                tokens,
                analyzer.getParsingTable(),
                analyzer.getNonTerminals(),
                analyzer.getTerminals()
            );

            boolean accepted = stackParser.parse("Program");
            if (accepted) {
                System.out.println("\n✅ Análise LL(1) com Pilha: ACEITA entrada lida");
            } else {
                System.out.println("\n❌ Análise LL(1) com Pilha: REJEITA entrada lida");
            }
        } catch (Exception e) {
            System.err.println("\n⚠️ Análise LL(1): " + e.getMessage());
        }
    }
    
    /**
     * Cria um analisador LL(1) configurado para a gramática Bowler SIMPLIFICADA
     * 
     * Gramática para validar a estrutura:
     * Program → main Block EOF
     * Block → { StmtList }
     * StmtList → ε | Statement StmtList
     * Statement → var IDENTIFIER : Type ; | Expr ;
     * Type → int | float
     * Expr → IDENTIFIER | NUMBER
     */
    private static LL1GrammarAnalyzer createBowlerLL1Analyzer() {
        Map<String, List<List<String>>> productions = new HashMap<>();
        
        // Program → main Block EOF
        productions.put("Program", new ArrayList<>());
        productions.get("Program").add(Arrays.asList("main", "Block", "EOF"));
        
        // Block → { StmtList }
        productions.put("Block", new ArrayList<>());
        productions.get("Block").add(Arrays.asList("{", "StmtList", "}"));
        
        // StmtList → Statement StmtList | ε
        productions.put("StmtList", new ArrayList<>());
        productions.get("StmtList").add(Arrays.asList("Statement", "StmtList"));
        productions.get("StmtList").add(Arrays.asList("ε"));
        
        // Statement → var IDENTIFIER : Type VarInit | if Condition Block | while Condition Block | print Expr ;
        // VarInit → = Expr ; | ;
        productions.put("Statement", new ArrayList<>());
        productions.get("Statement").add(Arrays.asList("var", "IDENTIFIER", ":", "Type", "VarInit"));
        // if e while: if/while Condition Block
        productions.get("Statement").add(Arrays.asList("if", "Condition", "Block"));
        productions.get("Statement").add(Arrays.asList("while", "Condition", "Block"));
        // print: print Expr ;
        productions.get("Statement").add(Arrays.asList("print", "Expr", ";"));
        // Statement pode começar com IDENTIFIER — diferenciar atribuição ou expressão
        productions.get("Statement").add(Arrays.asList("IDENTIFIER", "IdentRest"));
        // Expressões iniciadas por número: NUMBER TermP ExprP ;
        productions.get("Statement").add(Arrays.asList("NUMBER", "TermP", "ExprP", ";"));
        // Expressões iniciadas por parêntese: ( Expr ) TermP ExprP ;
        productions.get("Statement").add(Arrays.asList("(", "Expr", ")", "TermP", "ExprP", ";"));

        // VarInit → = Expr ; | ;
        productions.put("VarInit", new ArrayList<>());
        productions.get("VarInit").add(Arrays.asList("=", "Expr", ";"));
        productions.get("VarInit").add(Arrays.asList(";"));
        
        // Type → int | float | string
        productions.put("Type", new ArrayList<>());
        productions.get("Type").add(Arrays.asList("int"));
        productions.get("Type").add(Arrays.asList("float"));
        productions.get("Type").add(Arrays.asList("string"));
        
        // Expressões com precedência e comparadores (LL(1) com left-recursion removal)
        // Expr → Term ExprP
        // ExprP → CompOp Term ExprP | ε
        // Term → Factor TermP
        // TermP → * Factor TermP | / Factor TermP | ε
        // Factor → ( Expr ) | NUMBER | IDENTIFIER
        // CompOp → == | != | < | > | <= | >=
        productions.put("Expr", new ArrayList<>());
        productions.get("Expr").add(Arrays.asList("Term", "ExprP"));

        productions.put("ExprP", new ArrayList<>());
        productions.get("ExprP").add(Arrays.asList("CompOp", "Term", "ExprP"));
        productions.get("ExprP").add(Arrays.asList("+", "Term", "ExprP"));
        productions.get("ExprP").add(Arrays.asList("-", "Term", "ExprP"));
        productions.get("ExprP").add(Arrays.asList("ε"));

        productions.put("CompOp", new ArrayList<>());
        productions.get("CompOp").add(Arrays.asList("=="));
        productions.get("CompOp").add(Arrays.asList("!="));
        productions.get("CompOp").add(Arrays.asList("<"));
        productions.get("CompOp").add(Arrays.asList(">"));
        productions.get("CompOp").add(Arrays.asList("<="));
        productions.get("CompOp").add(Arrays.asList(">="));

        productions.put("Term", new ArrayList<>());
        productions.get("Term").add(Arrays.asList("Factor", "TermP"));

        productions.put("TermP", new ArrayList<>());
        productions.get("TermP").add(Arrays.asList("*", "Factor", "TermP"));
        productions.get("TermP").add(Arrays.asList("/", "Factor", "TermP"));
        productions.get("TermP").add(Arrays.asList("ε"));

        productions.put("Factor", new ArrayList<>());
        productions.get("Factor").add(Arrays.asList("(", "Expr", ")"));
        productions.get("Factor").add(Arrays.asList("NUMBER"));
        productions.get("Factor").add(Arrays.asList("STRING"));
        productions.get("Factor").add(Arrays.asList("IDENTIFIER"));

        // IdentRest -> = Expr ; | TermP ExprP ;
        productions.put("IdentRest", new ArrayList<>());
        productions.get("IdentRest").add(Arrays.asList("=", "Expr", ";"));
        productions.get("IdentRest").add(Arrays.asList("TermP", "ExprP", ";"));

        // Condition → ( LogicalExpr )
        // LogicalExpr → Expr LogicalOp LogicalExpr | Expr
        // LogicalOp → && | ||
        productions.put("Condition", new ArrayList<>());
        productions.get("Condition").add(Arrays.asList("(", "LogicalExpr", ")"));

        productions.put("LogicalExpr", new ArrayList<>());
        productions.get("LogicalExpr").add(Arrays.asList("Expr", "LogicalOpTail"));

        productions.put("LogicalOpTail", new ArrayList<>());
        productions.get("LogicalOpTail").add(Arrays.asList("LogicalOp", "Expr", "LogicalOpTail"));
        productions.get("LogicalOpTail").add(Arrays.asList("ε"));

        productions.put("LogicalOp", new ArrayList<>());
        productions.get("LogicalOp").add(Arrays.asList("&&"));
        productions.get("LogicalOp").add(Arrays.asList("||"));

        // Statement agora com else, return, do/while e for
        // Statement → var ... | if Condition Block ElseOpt | while Condition Block | do Block while Condition ;
        //           | for ForInit ForCond ForStep Block | print Expr ; | return Expr ; | ...
        productions.put("Statement", new ArrayList<>());
        productions.get("Statement").add(Arrays.asList("var", "IDENTIFIER", ":", "Type", "VarInit"));
        productions.get("Statement").add(Arrays.asList("if", "Condition", "Block", "ElseOpt"));
        productions.get("Statement").add(Arrays.asList("while", "Condition", "Block"));
        productions.get("Statement").add(Arrays.asList("do", "Block", "while", "Condition", ";"));
        productions.get("Statement").add(Arrays.asList("for", "(", "ForInit", "ForCond", "ForStep", ")", "Block"));
        productions.get("Statement").add(Arrays.asList("print", "Expr", ";"));
        productions.get("Statement").add(Arrays.asList("return", "Expr", ";"));
        productions.get("Statement").add(Arrays.asList("IDENTIFIER", "IdentRest"));
        productions.get("Statement").add(Arrays.asList("NUMBER", "TermP", "ExprP", ";"));
        productions.get("Statement").add(Arrays.asList("(", "Expr", ")", "TermP", "ExprP", ";"));

        // ElseOpt → else Block | ε
        productions.put("ElseOpt", new ArrayList<>());
        productions.get("ElseOpt").add(Arrays.asList("else", "Block"));
        productions.get("ElseOpt").add(Arrays.asList("ε"));
        
        // ForInit → VarInit | Expr ; | ε
        productions.put("ForInit", new ArrayList<>());
        productions.get("ForInit").add(Arrays.asList("var", "IDENTIFIER", ":", "Type", "VarInit"));
        productions.get("ForInit").add(Arrays.asList("IDENTIFIER", "IdentRest"));
        productions.get("ForInit").add(Arrays.asList("ε"));
        
        // ForCond → Condition ; | ε
        productions.put("ForCond", new ArrayList<>());
        productions.get("ForCond").add(Arrays.asList("Condition", ";"));
        productions.get("ForCond").add(Arrays.asList("ε"));
        
        // ForStep → Expr | ε
        productions.put("ForStep", new ArrayList<>());
        productions.get("ForStep").add(Arrays.asList("Expr"));
        productions.get("ForStep").add(Arrays.asList("ε"));
        
        // Terminais (não incluir o símbolo ε aqui)
        Set<String> terminals = new HashSet<>(Arrays.asList(
            "main", "var", "int", "float", "string", "if", "else", "while", "do", "for", "print", "return",
            "{", "}", ";", ":", "=",
            "+", "-", "*", "/", "(", ")",
            "==", "!=", "<", ">", "<=", ">=",
            "&&", "||",
            "IDENTIFIER", "NUMBER", "STRING", "EOF"
        ));
        
        return new LL1GrammarAnalyzer(productions, terminals, "Program");
    }
}
