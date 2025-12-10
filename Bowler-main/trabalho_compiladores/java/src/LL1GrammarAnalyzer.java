import java.util.*;

/**
 * LL1GrammarAnalyzer - Calcula conjuntos FIRST e FOLLOW para uma gramática
 * LL(1)
 * 
 * Esta classe implementa o algoritmo de análise LL(1):
 * 1. Calcula FIRST para todos os não-terminais
 * 2. Calcula FOLLOW para todos os não-terminais
 * 3. Constrói a tabela de análise LL(1)
 * 4. Fornece testes de conflito LL(1)
 */
public class LL1GrammarAnalyzer {

    // Representação da gramática
    private Map<String, List<List<String>>> productions; // A → α₁ | α₂ | ...
    private Set<String> terminals;
    private Set<String> nonTerminals;
    private String startSymbol;

    // Conjuntos FIRST e FOLLOW
    private Map<String, Set<String>> first = new HashMap<>();
    private Map<String, Set<String>> follow = new HashMap<>();
    private Map<String, Set<String>> firstOfStr = new HashMap<>(); // FIRST de strings

    // Tabela de análise LL(1)
    private Map<String, Map<String, List<String>>> parsingTable = new HashMap<>();
    // Coleta de conflitos detectados durante construção da tabela
    private List<String> conflicts = new ArrayList<>();

    public LL1GrammarAnalyzer(Map<String, List<List<String>>> productions,
            Set<String> terminals,
            String startSymbol) {
        this.productions = productions;
        this.terminals = terminals;
        this.startSymbol = startSymbol;
        this.nonTerminals = productions.keySet();

        // Inicializar FIRST e FOLLOW
        for (String nt : nonTerminals) {
            first.put(nt, new HashSet<>());
            follow.put(nt, new HashSet<>());
        }
    }

    /**
     * Calcula conjuntos FIRST para todos os não-terminais
     */
    public void calculateFirst() {
        boolean changed = true;
        while (changed) {
            changed = false;

            for (String nt : nonTerminals) {
                int beforeSize = first.get(nt).size();

                // Para cada produção A → α
                for (List<String> production : productions.get(nt)) {
                    Set<String> firstOfProduction = firstOf(production);
                    first.get(nt).addAll(firstOfProduction);
                }

                if (first.get(nt).size() > beforeSize) {
                    changed = true;
                }
            }
        }
    }

    /**
     * Calcula FIRST de uma cadeia de símbolos
     */
    private Set<String> firstOf(List<String> symbols) {
        Set<String> result = new HashSet<>();

        if (symbols.isEmpty()) {
            result.add("ε");
            return result;
        }

        for (String symbol : symbols) {
            if (terminals.contains(symbol)) {
                result.add(symbol);
                break;
            } else if (nonTerminals.contains(symbol)) {
                Set<String> firstOfSymbol = first.get(symbol);
                result.addAll(firstOfSymbol);
                result.remove("ε");

                if (!firstOfSymbol.contains("ε")) {
                    break;
                }
            }
        }

        // Se todos derivam ε
        if (symbols.stream().allMatch(s -> nonTerminals.contains(s) && first.get(s).contains("ε") ||
                s.equals("ε"))) {
            result.add("ε");
        }

        return result;
    }

    /**
     * Calcula conjuntos FOLLOW para todos os não-terminais
     */
    public void calculateFollow() {
        // Inicialmente, EOF está em FOLLOW do símbolo inicial
        // Usar o símbolo consistente "EOF" para representar fim de arquivo
        follow.get(startSymbol).add("EOF");

        boolean changed = true;
        while (changed) {
            changed = false;

            for (String nt : nonTerminals) {
                for (List<String> production : productions.get(nt)) {
                    for (int i = 0; i < production.size(); i++) {
                        String symbol = production.get(i);

                        if (nonTerminals.contains(symbol)) {
                            int beforeSize = follow.get(symbol).size();

                            // FOLLOW(B) ∪= FIRST(β) - {ε}
                            if (i + 1 < production.size()) {
                                List<String> beta = production.subList(i + 1, production.size());
                                Set<String> firstBeta = firstOf(beta);
                                follow.get(symbol).addAll(firstBeta);
                                follow.get(symbol).remove("ε");

                                // Se ε ∈ FIRST(β), FOLLOW(B) ∪= FOLLOW(A)
                                if (firstBeta.contains("ε")) {
                                    follow.get(symbol).addAll(follow.get(nt));
                                }
                            } else {
                                // B está no final: FOLLOW(B) ∪= FOLLOW(A)
                                follow.get(symbol).addAll(follow.get(nt));
                            }

                            if (follow.get(symbol).size() > beforeSize) {
                                changed = true;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Constrói a tabela de análise LL(1)
     * M[A, a] = produção
     */
    public void buildParsingTable() {
        for (String nt : nonTerminals) {
            parsingTable.put(nt, new HashMap<>());
        }

        for (String nt : nonTerminals) {
            for (List<String> production : productions.get(nt)) {
                // Calcular FIRST(production)
                Set<String> firstProd = firstOf(production);

                // Para cada terminal em FIRST(production)
                for (String terminal : firstProd) {
                    if (!terminal.equals("ε")) {
                        List<String> existing = parsingTable.get(nt).get(terminal);
                        if (existing != null && !existing.equals(production)) {
                            // Se houver conflito entre uma produção ε e uma produção não-ε,
                            // preferimos a produção não-ε (evita mapear ε sobre um terminal)
                            boolean existingIsEps = existing.size() == 1 && existing.get(0).equals("ε");
                            boolean newIsEps = production.size() == 1 && production.get(0).equals("ε");
                            if (existingIsEps && !newIsEps) {
                                parsingTable.get(nt).put(terminal, new ArrayList<>(production));
                                // registrar como aviso resolvido
                                conflicts.add("RESOLVIDO (pref. nao-eps): M[" + nt + ", " + terminal + "] => "
                                        + production + " sobre " + existing);
                            } else if (!existingIsEps && newIsEps) {
                                // manter existing (não sobrescrever)
                                conflicts.add("IGNORADO (eps vs nao-eps): M[" + nt + ", " + terminal + "] manter "
                                        + existing + " (ignorando " + production + ")");
                            } else {
                                String msg = "CONFLITO LL(1) em M[" + nt + ", " + terminal + "] entre " + existing
                                        + " e " + production;
                                System.err.println(msg);
                                conflicts.add(msg);
                            }
                        } else {
                            parsingTable.get(nt).put(terminal, new ArrayList<>(production));
                        }
                    }
                }

                // Se ε ∈ FIRST(production)
                if (firstProd.contains("ε")) {
                    for (String followTerm : follow.get(nt)) {
                        List<String> existing = parsingTable.get(nt).get(followTerm);
                        if (existing != null && !existing.equals(production)) {
                            boolean existingIsEps = existing.size() == 1 && existing.get(0).equals("ε");
                            boolean newIsEps = production.size() == 1 && production.get(0).equals("ε");
                            if (existingIsEps && !newIsEps) {
                                parsingTable.get(nt).put(followTerm, new ArrayList<>(production));
                                conflicts.add("RESOLVIDO (pref. nao-eps): M[" + nt + ", " + followTerm + "] => "
                                        + production + " sobre " + existing);
                            } else if (!existingIsEps && newIsEps) {
                                conflicts.add("IGNORADO (eps vs nao-eps): M[" + nt + ", " + followTerm + "] manter "
                                        + existing + " (ignorando " + production + ")");
                            } else {
                                String msg = "CONFLITO LL(1) em M[" + nt + ", " + followTerm + "] entre " + existing
                                        + " e " + production;
                                System.err.println(msg);
                                conflicts.add(msg);
                            }
                        } else {
                            parsingTable.get(nt).put(followTerm, new ArrayList<>(production));
                        }
                    }
                }

                // contador de produções não utilizado (removido)
            }
        }
    }

    /**
     * Exibe os conjuntos FIRST calculados
     */
    public void printFirst() {
        System.out.println("\n=== Conjuntos FIRST ===");
        for (String nt : nonTerminals) {
            System.out.println("FIRST(" + nt + ") = " + first.get(nt));
        }
    }

    /**
     * Exibe os conjuntos FOLLOW calculados
     */
    public void printFollow() {
        System.out.println("\n=== Conjuntos FOLLOW ===");
        for (String nt : nonTerminals) {
            System.out.println("FOLLOW(" + nt + ") = " + follow.get(nt));
        }
    }

    /**
     * Exibe a tabela de análise LL(1)
     */
    public void printParsingTable() {
        System.out.println("\n=== Tabela de Análise LL(1) ===");
        System.out.println("M[A, a] = Produção");

        for (String nt : parsingTable.keySet()) {
            System.out.println("\nPara " + nt + ":");
            Map<String, List<String>> row = parsingTable.get(nt);
            for (String terminal : row.keySet()) {
                System.out.println("  M[" + nt + ", " + terminal + "] = " + row.get(terminal));
            }
        }
        // Exibir conflitos coletados (se houver)
        if (!conflicts.isEmpty()) {
            System.out.println("\n=== Conflitos detectados durante a construção da tabela ===");
            for (String c : conflicts) {
                System.out.println(c);
            }
        }
    }

    // Getters
    public Map<String, Set<String>> getFirst() {
        return first;
    }

    public Map<String, Set<String>> getFollow() {
        return follow;
    }

    public Map<String, Map<String, List<String>>> getParsingTable() {
        return parsingTable;
    }

    public Set<String> getNonTerminals() {
        return nonTerminals;
    }

    public Set<String> getTerminals() {
        return terminals;
    }

    public List<String> getConflicts() {
        return conflicts;
    }

    public static void main(String[] args) {
        // Exemplo de uso: Gramática simples
        // E → T E'
        // E' → + T E' | ε
        // T → F T'
        // T' → * F T' | ε
        // F → ( E ) | id

        Map<String, List<List<String>>> productions = new HashMap<>();

        // E → T E'
        productions.put("E", new ArrayList<>());
        productions.get("E").add(Arrays.asList("T", "E'"));

        // E' → + T E' | ε
        productions.put("E'", new ArrayList<>());
        productions.get("E'").add(Arrays.asList("+", "T", "E'"));
        productions.get("E'").add(Arrays.asList("ε"));

        // T → F T'
        productions.put("T", new ArrayList<>());
        productions.get("T").add(Arrays.asList("F", "T'"));

        // T' → * F T' | ε
        productions.put("T'", new ArrayList<>());
        productions.get("T'").add(Arrays.asList("*", "F", "T'"));
        productions.get("T'").add(Arrays.asList("ε"));

        // F → ( E ) | id
        productions.put("F", new ArrayList<>());
        productions.get("F").add(Arrays.asList("(", "E", ")"));
        productions.get("F").add(Arrays.asList("id"));

        Set<String> terminals = new HashSet<>(Arrays.asList("+", "*", "(", ")", "id"));

        LL1GrammarAnalyzer analyzer = new LL1GrammarAnalyzer(productions, terminals, "E");
        analyzer.calculateFirst();
        analyzer.calculateFollow();
        analyzer.buildParsingTable();

        analyzer.printFirst();
        analyzer.printFollow();
        analyzer.printParsingTable();
    }
}
