# Dissertação Técnica: Arquitetura, Formalismo e Implementação do Compilador Bowler (v2.1)

**Disciplina:** Construção de Compiladores  
**Projeto:** Implementação de Analisador Sintático Descendente Tabular LL(1) e Engenharia de Linguagens  
**Versão do Sistema:** 2.1 (Suporte a Arrays, Estruturas Pós-Testadas e Tipagem Estendida)  
**Data:** Dezembro de 2025

---

## 1. Introdução e Contextualização do Projeto

A construção de um compilador é, por excelência, um dos desafios mais complexos e integradores da Ciência da Computação, exigindo o domínio simultâneo de teoria da computação, estruturas de dados avançadas e engenharia de software. O projeto **Bowler** representa a materialização prática deste conhecimento, consistindo em um *front-end* de compilador completo capaz de processar uma linguagem de programação imperativa moderna.

Este projeto não deve ser encarado como uma mera ferramenta de tradução de código, mas sim como um exercício de **formalização de linguagem**. A evolução do projeto, desde a sua versão inicial (baseada em intuição e recursão simples *ad-hoc*) até a atual versão 2.1, reflete uma transição metodológica profunda. Enquanto a primeira versão resolvia problemas de forma improvisada, a versão atual fundamenta-se na teoria rigorosa dos **Autômatos de Pilha Determinísticos** e nas **Gramáticas Livres de Contexto (GLC) da classe LL(1)**.

A atualização para a versão 2.1 introduziu desafios de engenharia significativos. A inclusão de estruturas de repetição pós-testadas (`do-while`), a manipulação de estruturas de memória indexada (Arrays) e a expansão do sistema de tipos para suportar literais de precisão decimal (`double`) e caracteres (`char`), obrigou a uma refatoração completa das regras de produção gramatical para garantir a ausência de conflitos de previsão sintática.

---

## 2. Fundamentação Teórica e Arquitetura do Sistema

A arquitetura do Bowler v2.1 foi desenhada para garantir um desacoplamento total entre a definição da linguagem e o motor de processamento. Ao contrário de abordagens monolíticas, onde a gramática está embutida na lógica do código (*hardcoded*), este sistema opera com base em uma arquitetura orientada a dados, dividida em três camadas sequenciais de abstração.

### 2.1 O Paradigma LL(1)
A escolha do modelo de análise **LL(1)** (*Left-to-right scan, Leftmost derivation, 1 symbol lookahead*) define todas as restrições de engenharia do projeto. Este modelo matemático impõe que, para qualquer não-terminal na pilha de análise e qualquer token de entrada, deve existir, no máximo, uma única regra de produção aplicável.

Isto implica que a gramática da linguagem Bowler teve de ser desenhada para ser:
1.  **Livre de Ambiguidade:** Não podem existir duas árvores de derivação para a mesma frase.
2.  **Livre de Recursão à Esquerda:** Regras como `E -> E + T` são proibidas, pois levariam o parser a um ciclo infinito de expansão sem consumo de tokens.
3.  **Totalmente Fatorada:** Prefixos comuns (como em atribuições e chamadas de função, ou acesso a arrays) devem ser isolados para permitir a decisão com apenas um token de *lookahead*.

### 2.2 O Pipeline de Compilação
O fluxo de dados no compilador Bowler segue uma trajetória linear e determinística:

1.  **Entrada:** Arquivo de código-fonte (`.bw`).
2.  **Análise Léxica (`Lexer.java`):** Transforma o fluxo de caracteres em um fluxo de objetos `Token`.
3.  **Geração de Tabela (`LL1GrammarAnalyzer.java`):** Processa a definição formal da gramática, calcula os conjuntos *FIRST* e *FOLLOW*, e produz a matriz de decisão `M[A, a]`.
4.  **Execução Sintática (`LL1StackParser.java`):** Uma máquina de pilha genérica que consome os tokens seguindo as instruções da matriz.
5.  **Construção da AST (`Parser.java`):** Em paralelo, constrói-se uma Árvore Sintática Abstrata para validação semântica e futura geração de código.

---

## 3. Análise Léxica: Especificação e Autômato

O componente `Lexer.java` atua como a primeira camada de validação. Ele implementa um Autômato Finito Determinístico (DFA) manual, projetado para maximizar a eficiência na identificação de padrões e minimizar a necessidade de *backtracking*.

Na versão 2.1, a complexidade deste autômato aumentou substancialmente. A introdução de operadores compostos (`+=`, `-=`, `==`) e tipos literais expandidos (`char`, `double`) exigiu a implementação de uma lógica de *lookahead* léxico. Por exemplo, ao encontrar o caractere `+`, o scanner não emite imediatamente um token; ele verifica o próximo caractere. Se for `=`, o estado transita para `PLUS_EQUAL`; caso contrário, emite `PLUS`. Este mecanismo é crucial para desambiguar operações de atribuição composta de operações aritméticas simples.

### Tabela 1: Especificação Formal dos Tokens (v2.1)

A tabela abaixo apresenta o mapeamento completo entre as classes de tokens implementadas e as Expressões Regulares (Regex) abstratas que as definem formalmente.

| Categoria | Token Class (Java) | Expressão Regular (Abstrata) | Descrição Semântica |
| :--- | :--- | :--- | :--- |
| **Estrutural** | `VAR` | `^var$` | Declaração de variável mutável. |
| **Estrutural** | `MAIN` | `^main$` | Ponto de entrada do programa. |
| **Controle** | `IF` / `ELSE` | `^if$` \| `^else$` | Desvio condicional. |
| **Controle** | `WHILE` / `DO` | `^while$` \| `^do$` | **Novo:** Repetição pré e pós-testada. |
| **Tipos** | `INT` / `DOUBLE` | `^int$` \| `^double$` | **Novo:** Tipos numéricos primitivos. |
| **Tipos** | `CHAR` / `STRING` | `^char$` \| `^string$` | **Novo:** Tipos textuais. |
| **Identidade** | `IDENTIFIER` | `[a-zA-Z_][a-zA-Z0-9_]*` | Nomes de símbolos (variáveis, funções). |
| **Literal** | `NUMBER` (Int) | `[0-9]+` | Valores inteiros constantes. |
| **Literal** | `FLOAT` / `DOUBLE` | `[0-9]+\.[0-9]+` | Valores de ponto flutuante. |
| **Literal** | `CHAR_LITERAL` | `'[^']'` | **Novo:** Caractere único ASCII. |
| **Literal** | `STRING_LITERAL` | `"[^"]*"` | Texto com suporte a interpolação `${}`. |
| **Delimitador** | `BRACKETS` | `\[` \| `\]` | **Novo:** Acesso e definição de Arrays. |
| **Operador** | `COMPOUND` | `\+=` \| `-=` \| `*=` | **Novo:** Atribuição composta. |
| **Lógico** | `LOGICAL` | `&&` \| `\|\|` \| `!` | Operadores booleanos. |

---

## 4. Engenharia de Gramática e Parsing LL(1)

A engenharia da gramática é o coração intelectual deste projeto. O módulo `LL1GrammarAnalyzer.java` não se limita a ler regras; ele executa algoritmos de teoria de grafos para calcular os conjuntos de predição. A grande inovação da versão 2.1 foi a integração de estruturas sintáticas complexas sem violar a propriedade LL(1).

A introdução de Arrays e do laço `do-while` apresentou desafios distintos. No caso dos Arrays, o acesso indexado (`vetor[0]`) gera um conflito de prefixo com o acesso a variáveis simples (`variavel`). Ambos iniciam com `IDENTIFIER`. Para resolver isto, utilizamos a técnica de **Fatoração à Esquerda**, criando uma regra intermediária `ArrayAccessOpt` que adia a decisão sintática até que o parser verifique a presença (ou ausência) do token `[`. Já o suporte ao `do-while` exigiu uma nova produção no nível dos `Statements`, cuja unicidade do token inicial `do` garantiu conjuntos FIRST disjuntos, preservando o determinismo.

### Tabela 2: Matriz de Decisão Sintática (Parsing Table - Excerto)

Esta tabela representa a "inteligência" gerada automaticamente pelo sistema. Ela dita a ação do parser com base no estado da pilha (Linhas) e no token atual (Colunas).

| Pilha \ Lookahead | `do` | `while` | `[` (L_BRACKET) | `+=` (PLUS_EQ) | `id` (IDENTIFIER) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Statement** | `do Block while...` | `while Cond Block` | *Erro* | *Erro* | `id IdentRest` |
| **IdentRest** | *Erro* | *Erro* | *Erro* | `+= Expr ;` | *Erro* |
| **ArrayAccessOpt** | *Erro* | *Erro* | `[ Expr ]` | *Erro* | *Erro* |
| **Block** | *Erro* | *Erro* | *Erro* | *Erro* | *Erro* |
| **Expr** | *Erro* | *Erro* | *Erro* | *Erro* | `Term ExprP` |

*Nota: A ausência de múltiplas regras em uma única célula comprova matematicamente que a gramática é LL(1) válida.*

### Rastreamento da Execução (Trace)

Para o código de entrada `do { x += 1; } while(x < 10);`:

1.  O parser detecta `do` e expande `Statement -> do Block while ...`.
2.  Consome `do`, entra no `Block`.
3.  Dentro do bloco, detecta `x` (`id`). Usa a regra `IDENTIFIER IdentRest`.
4.  Detecta `+=`. A regra `IdentRest` expande para `+= Expr ;`.
5.  Consome `+=`, processa a expressão `1` e o `;`.
6.  Sai do bloco, consome `while`, a condição e o `;` final.

---

## 5. Análise Semântica e Sistema de Tipos

Um programa sintaticamente correto pode, ainda assim, ser semanticamente inválido. A versão 2.1 do Bowler implementa um sistema de tipos rigoroso (*static typing*), validado durante a construção da Árvore Sintática Abstrata (AST).

A tabela de compatibilidade de tipos, ou álgebra de tipos, define quais as interações permitidas entre as primitivas da linguagem. Com a adição de `double` e `char`, esta matriz tornou-se tridimensional. O sistema implementa regras de **coerção implícita** (*widening*), permitindo, por exemplo, que um `int` seja somado a um `double` resultando em um `double` (sem perda de precisão), mas proíbe a atribuição inversa sem *casting* explícito.

### Tabela 3: Álgebra de Tipos e Regras Semânticas

A tabela abaixo dita as regras de validação aplicadas pelo compilador aos nós de expressão da AST.

| Tipo Operando A | Operador | Tipo Operando B | Tipo Resultante | Regra Semântica / Observação |
| :--- | :--- | :--- | :--- | :--- |
| `int` | `+`, `-`, `*` | `int` | `int` | Aritmética inteira pura. |
| `int` | `/` | `int` | `float` | Divisão promove para ponto flutuante. |
| `double` | `Any Arith` | `int` / `float` | `double` | **Coerção:** Promoção para maior precisão. |
| `string` | `+` | `Any` | `string` | **Polimorfismo:** Concatenação converte B para string. |
| `string` | `+=` | `string` | `string` | Concatenação destrutiva (*append*). |
| `char` | `==`, `!=` | `char` | `bool` | Comparação numérica dos códigos ASCII. |
| `int[]` | `[ index ]` | `int` | `int` | Índice de array deve ser estritamente inteiro. |
| `bool` | `&&`, `\|\|` | `bool` | `bool` | Lógica booleana estrita. |
| `void` | `=` | `Any` | *Erro* | Funções `void` não retornam valor atribuível. |

---

## 6. Validação Experimental

A confiabilidade do compilador foi atestada através de uma bateria de 17 testes de integração (`teste_*.bw`), desenhados para cobrir casos de fronteira e verificar a ausência de regressões.

### Cenários de Teste Críticos

1.  **Arrays e Recursão (`meu_exemplo2.bw`):**
    O parser processou com sucesso a inicialização recursiva de arrays (`var notas: float[] = [7.5, 8.0];`) e o acesso aninhado em expressões matemáticas, validando a robustez da regra `Factor` fatorada.

2.  **Estruturas Pós-Testadas (`teste_while.bw` vs `do_while`):**
    O sistema diferenciou corretamente a semântica do `while` (pré-testado) do `do-while` (pós-testado), impondo corretamente a necessidade do delimitador `;` apenas no segundo caso, conforme definido na gramática.

3.  **Expressões Complexas:**
    Testes de *stress* com expressões aritméticas longas e parênteses aninhados confirmaram que a precedência de operadores (Multiplicação > Adição > Relacional > Lógico) é respeitada, graças à estratificação correta da gramática em níveis (`Expr`, `Term`, `Factor`).

---

## 7. Conclusão

O desenvolvimento do compilador Bowler v2.1 cumpre integralmente os objetivos propostos, entregando um artefato de software que excede os requisitos funcionais básicos. A implementação de um **Gerador Automático de Tabelas de Parsing** e a adoção estrita do formalismo **LL(1)** conferem ao projeto uma solidez teórica distinta.

A capacidade do sistema em lidar com construções modernas como Arrays, laços `do-while` e interpolação de strings, mantendo um desempenho determinístico, comprova a eficácia da arquitetura baseada em pilha e tabelas de decisão. O código-fonte, estruturado de forma modular e coesa, constitui uma base sólida para futuras expansões, como a geração de código intermediário ou otimização de fluxo de controle.

---
**Bibliografia e Referências:**
* Aho, A. V., Lam, M. S., Sethi, R., & Ullman, J. D. (2006). *Compilers: Principles, Techniques, and Tools* (2nd Edition). Addison-Wesley.
* Documentação oficial da linguagem Java (Oracle).
* Material de apoio da disciplina de Compiladores (1º e 2º Bimestre).